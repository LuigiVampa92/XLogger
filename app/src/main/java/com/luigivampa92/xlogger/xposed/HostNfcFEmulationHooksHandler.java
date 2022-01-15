package com.luigivampa92.xlogger.xposed;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.nfc.cardemulation.HostNfcFService;
import android.os.Bundle;

import com.luigivampa92.xlogger.BroadcastConstants;
import com.luigivampa92.xlogger.DataUtils;
import com.luigivampa92.xlogger.data.InteractionLog;
import com.luigivampa92.xlogger.data.InteractionLogEntry;
import com.luigivampa92.xlogger.data.InteractionType;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HostNfcFEmulationHooksHandler implements HooksHandler {

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context hookedAppContext;
    private Set<Class<?>> hnfServices;
    private List<InteractionLogEntry> currentLogEntries;

    public HostNfcFEmulationHooksHandler(XC_LoadPackage.LoadPackageParam lpparam, Context hookedAppContext) {
        this.lpparam = lpparam;
        this.hookedAppContext = hookedAppContext;
    }

    @Override
    public void applyHooks() {
        if (!androidVersionSupported() || !featuresSupported()) {
            return;
        }
        if (hnfServices == null) {
            hnfServices = performHostNfcFServicesSearchByPackageManager(hookedAppContext);
            if (!hnfServices.isEmpty()) {
                HookUtils.logClassList(hnfServices, lpparam.packageName);
                applyHnfHooks(lpparam, hnfServices);
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private Set<Class<?>> performHostNfcFServicesSearchByPackageManager(Context context) {
        HashSet<Class<?>> result = new HashSet<>();
        if (androidVersionSupported()) {
            Intent intent = new Intent();
            intent.setAction("android.nfc.cardemulation.action.HOST_NFCF_SERVICE");
            intent.setPackage(context.getPackageName());
            List<ResolveInfo> queryResult = context.getPackageManager().queryIntentServices(intent, PackageManager.MATCH_ALL);

            for (ResolveInfo serviceInfo : queryResult) {
                String targetClassName = serviceInfo.serviceInfo.name;
                try {
                    Class<?> targetClass = XposedHelpers.findClass(targetClassName, context.getClassLoader());
                    if (HostNfcFService.class.isAssignableFrom(targetClass) && !Modifier.isAbstract(targetClass.getModifiers())) {
                        result.add(targetClass);
                    }
                } catch (ClassCastException e) {
                    XLog.e("Error while trying to cast a hnf service class - %s", targetClassName);
                }
            }
        }

        return result;
    }

    private void applyHnfHooks(XC_LoadPackage.LoadPackageParam lpparam, Set<Class<?>> hnfServices) {
        try {
            XLog.d("Apply hnf hooks for package %s - start", lpparam.packageName);
            for (final Class<?> serviceClass : hnfServices) {
                applyHnfStopHookForService(serviceClass);
                applyHnfPacketHookForService(serviceClass);
            }
            XLog.d("Apply hnf hooks for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.e("Apply hnf hooks for package %s - error", lpparam.packageName, e);
        }
    }

    private void applyHnfPacketHookForService(Class<?> serviceClass) {
        String targetMethodName = "processNfcFPacket";
        XLog.d("Apply %s hook on %s - start", targetMethodName, serviceClass.getCanonicalName());
        if (HookUtils.hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    byte[].class,
                    Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (param.args.length > 0 && param.args[0] != null && param.args[0] instanceof byte[]) {
                                byte[] cApdu = (byte[]) param.args[0];
                                if (cApdu.length > 0) {
                                    if (currentLogEntries == null) {
                                        currentLogEntries = new ArrayList<>();
                                        XLog.i("Emulation activated - session record started");
                                    }
                                    XLog.i("HCE RX: %s", DataUtils.toHexString(cApdu));
                                    InteractionLogEntry logEntry = new InteractionLogEntry(System.currentTimeMillis(), cApdu, BroadcastConstants.PEER_TERMINAL, BroadcastConstants.PEER_DEVICE);
                                    currentLogEntries.add(logEntry);
                                } else {
                                    XLog.i("HCE ERROR: received empty command apdu");
                                }
                            }

                            Object result = param.getResultOrThrowable();
                            if (result == null || param.hasThrowable() || result instanceof Throwable) {
                                return;
                            }

                            if (result instanceof byte[]) {
                                byte[] rApdu = (byte[]) result;
                                if (rApdu.length > 0) {
                                    if (currentLogEntries == null) {
                                        currentLogEntries = new ArrayList<>();
                                        XLog.i("Emulation activated - session record started");
                                    }
                                    if (!currentLogEntries.isEmpty()) {
                                        InteractionLogEntry lastEntry = currentLogEntries.get(currentLogEntries.size() - 1);
                                        boolean alreadyRecorded = Arrays.equals(lastEntry.getData(), rApdu);
                                        if (alreadyRecorded) {
                                            return;
                                        }
                                    }
                                    XLog.i("HCE TX: %s", DataUtils.toHexString(rApdu));
                                    InteractionLogEntry logEntry = new InteractionLogEntry(System.currentTimeMillis(), rApdu, BroadcastConstants.PEER_DEVICE, BroadcastConstants.PEER_TERMINAL);
                                    currentLogEntries.add(logEntry);
                                } else {
                                    XLog.i("HCE ERROR: transmitted empty response apdu");
                                }
                            }
                        }
                    });
            XLog.d("Apply %s hook on %s - complete", targetMethodName, serviceClass.getCanonicalName());
        } else {
            XLog.d("Apply %s hook on %s - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }

        String callbackMethodName = "sendResponsePacket";
        XLog.d("Apply %s hook on parent class - start", callbackMethodName);
        try {
            Class<?> abstractParentService = serviceClass.getSuperclass();
            XposedHelpers.findAndHookMethod(
                    abstractParentService,
                    callbackMethodName,
                    byte[].class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (param != null && param.args.length > 0 && param.args[0] instanceof byte[]) {
                                byte[] rApdu = (byte[]) param.args[0];
                                if (rApdu.length > 0) {
                                    if (currentLogEntries == null) {
                                        currentLogEntries = new ArrayList<>();
                                        XLog.i("Emulation activated - session record started");
                                    }
                                    if (!currentLogEntries.isEmpty()) {
                                        InteractionLogEntry lastEntry = currentLogEntries.get(currentLogEntries.size() - 1);
                                        boolean alreadyRecorded = Arrays.equals(lastEntry.getData(), rApdu);
                                        if (alreadyRecorded) {
                                            return;
                                        }
                                    }
                                    XLog.i("HCE TX: %s", DataUtils.toHexString(rApdu));
                                    InteractionLogEntry logEntry = new InteractionLogEntry(System.currentTimeMillis(), rApdu, BroadcastConstants.PEER_DEVICE, BroadcastConstants.PEER_TERMINAL);
                                    currentLogEntries.add(logEntry);
                                } else {
                                    XLog.i("HCE ERROR: transmitted empty response apdu");
                                }
                            }
                        }
                    }
            );
            XLog.d("Apply %s hook on parent class - complete", callbackMethodName);
        } catch (Throwable e) {
            XLog.d("Apply %s hook on parent class - error", callbackMethodName);
        }
    }

    private void applyHnfStopHookForService(Class<?> serviceClass) {
        String targetMethodName = "onDeactivated";
        XLog.d("Apply %s hook on %s - start", targetMethodName, serviceClass.getCanonicalName());
        if (HookUtils.hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.i("Emulation deactivated - %s - session record stopped", serviceClass.getCanonicalName());
                            InteractionLog interactionLog = new InteractionLog(InteractionType.HCE_NFC_F, lpparam.packageName, serviceClass.getCanonicalName(), (currentLogEntries != null ? new ArrayList<>(currentLogEntries) : new ArrayList<>()));
                            Intent sendInteractionLogRecordIntent = new Intent();
                            sendInteractionLogRecordIntent.setPackage(BroadcastConstants.XLOGGER_PACKAGE);
                            sendInteractionLogRecordIntent.setComponent(new ComponentName(BroadcastConstants.XLOGGER_PACKAGE, BroadcastConstants.INTERACTION_LOG_RECEIVER));
                            sendInteractionLogRecordIntent.setAction(BroadcastConstants.ACTION_RECEIVE_INTERACTION_LOG_NFC_RAW_TAG);
                            sendInteractionLogRecordIntent.putExtra(BroadcastConstants.EXTRA_DATA, interactionLog);
                            hookedAppContext.sendBroadcast(sendInteractionLogRecordIntent);
                            currentLogEntries = null;
                        }
                    });
            XLog.d("Apply %s hook on %s - complete", targetMethodName, serviceClass.getCanonicalName());
        } else {
            XLog.d("Apply %s hook on %s - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private boolean featuresSupported() {
        boolean hasNfcFeature = hookedAppContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
        boolean hasNfcFHceFeature = hookedAppContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF);
        return hasNfcFeature && hasNfcFHceFeature;
    }

    private boolean androidVersionSupported() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N;
    }
}
