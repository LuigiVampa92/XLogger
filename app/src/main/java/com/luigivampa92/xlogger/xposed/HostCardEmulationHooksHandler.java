package com.luigivampa92.xlogger.xposed;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import com.luigivampa92.xlogger.BroadcastConstants;
import com.luigivampa92.xlogger.DataUtil;
import com.luigivampa92.xlogger.data.InteractionLog;
import com.luigivampa92.xlogger.data.InteractionLogEntry;
import com.luigivampa92.xlogger.data.InteractionType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HostCardEmulationHooksHandler implements HooksHandler {

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context hookedAppcontext;
    private Set<Class<? extends HostApduService>> hceServices;
    private List<InteractionLogEntry> currentLogEntries;

    public HostCardEmulationHooksHandler(XC_LoadPackage.LoadPackageParam lpparam, Context hookedAppcontext) {
        this.lpparam = lpparam;
        this.hookedAppcontext = hookedAppcontext;
    }

    @Override
    public void applyHooks() {
        if (!featuresSupported()) {
            return;
        }
        if (hceServices == null) {
            hceServices = performHostApduServicesSearchByPackageManager(hookedAppcontext);
            if (!hceServices.isEmpty()) {
                logHceServiceList(lpparam, hceServices);   // todo remove
                applyHceHooks(lpparam, hceServices);
            }
        }
    }


    // todo remove ??
    private void logHceServiceList(XC_LoadPackage.LoadPackageParam lpparam, Set<Class<? extends HostApduService>> hceServices) {
        try {
            if (hceServices != null && !hceServices.isEmpty()) {
                for (final Class<? extends HostApduService> serviceClass : hceServices) {
                    XLog.d("Package %s - hce service found - %s", lpparam.packageName, serviceClass.getCanonicalName());
                }
            }
        }
        catch (Throwable e) {
        }
    }








    @SuppressWarnings("unchecked")
    @SuppressLint("QueryPermissionsNeeded")
    private Set<Class<? extends HostApduService>> performHostApduServicesSearchByPackageManager(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.nfc.cardemulation.action.HOST_APDU_SERVICE");
        intent.setPackage(context.getPackageName());
        List<ResolveInfo> queryResult = context.getPackageManager().queryIntentServices(intent, PackageManager.MATCH_ALL);

        HashSet<Class<? extends HostApduService>> result = new HashSet<>();
        for (ResolveInfo serviceInfo: queryResult) {
            String targetClassName = serviceInfo.serviceInfo.name;
            try {
                Class<?> targetClass = XposedHelpers.findClass(targetClassName, context.getClassLoader());
                if (HostApduService.class.isAssignableFrom(targetClass) && !Modifier.isAbstract(targetClass.getModifiers())) {
                    Class<? extends HostApduService> castedToHeirClass = (Class<? extends HostApduService>) targetClass;
                    result.add(castedToHeirClass);
                }
            }
            catch (ClassCastException e) {
                XLog.e("Error while trying to cast a hce service class - %s", targetClassName);
            }
        }

        return result;
    }

    private void applyHceHooks(XC_LoadPackage.LoadPackageParam lpparam, Set<Class<? extends HostApduService>> hceServices) {
        try {
            XLog.d("Apply hce hooks for package %s - start", lpparam.packageName);
            for (final Class<? extends HostApduService> serviceClass : hceServices) {
                applyHceStartHookForService(serviceClass);
                applyHceStopHookForService(serviceClass);
                applyHceApduHookForService(serviceClass);
            }
            XLog.d("Apply hce hooks for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.e("Apply hce hooks for package %s - error", lpparam.packageName, e);
        }
    }

    private void applyHceStartHookForService(Class<? extends HostApduService> serviceClass) {

        String targetMethodName = "onCreate";

        XLog.d("Apply %s hook on %s - start", targetMethodName, serviceClass.getCanonicalName());
        if (hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.d("Emulation activated - %s - session record started", serviceClass.getCanonicalName());

                            currentLogEntries = new ArrayList<>();
                        }
                    });
            XLog.d("Apply %s hook on %s - complete", targetMethodName, serviceClass.getCanonicalName());
        } else {
            XLog.e("Apply %s hook on %s - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private void applyHceStopHookForService(Class<? extends HostApduService> serviceClass) {

        String targetMethodName = "onDeactivated";

        XLog.d("Apply %s hook on %s - start", targetMethodName, serviceClass.getCanonicalName());
        if (hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.d("Emulation deactivated - %s - session record stopped", serviceClass.getCanonicalName());

                            InteractionLog interactionLog = new InteractionLog(InteractionType.HCE_NORMAL, (currentLogEntries != null ? new ArrayList<>(currentLogEntries) : new ArrayList<>()));

                            Intent sendInteractionLogRecordIntent = new Intent();
                            sendInteractionLogRecordIntent.setPackage(BroadcastConstants.XLOGGER_PACKAGE);
                            sendInteractionLogRecordIntent.setComponent(new ComponentName(BroadcastConstants.XLOGGER_PACKAGE, BroadcastConstants.INTERACTION_LOG_RECEIVER));
                            sendInteractionLogRecordIntent.setAction(BroadcastConstants.ACTION_RECEIVE_INTERACTION_LOG_NFC_RAW_TAG);
                            sendInteractionLogRecordIntent.putExtra(BroadcastConstants.EXTRA_DATA, interactionLog);

                            hookedAppcontext.sendBroadcast(sendInteractionLogRecordIntent);

//                            currentTagType = null;     // todo !!!
                            currentLogEntries = null;

                        }
                    });
            XLog.d("Apply %s hook on %s - complete", targetMethodName, serviceClass.getCanonicalName());
        } else {
            XLog.e("Apply %s hook on %s - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private void applyHceApduHookForService(Class<? extends HostApduService> serviceClass) {
        String targetMethodName = "processCommandApdu";

        XLog.d("Apply %s hook on %s - start", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        if (hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    byte[].class,
                    Bundle.class,
                    new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (param.args.length > 0 && param.args[0] != null && param.args[0] instanceof byte[]) {
                                byte[] cApdu = (byte[]) param.args[0];
                                if (cApdu.length > 0) {
                                    XLog.d("HCE RX: %s", DataUtil.toHexString(cApdu));

                                    if (currentLogEntries != null) {
                                        InteractionLogEntry logEntry = new InteractionLogEntry(System.currentTimeMillis(), cApdu, BroadcastConstants.PEER_TERMINAL, BroadcastConstants.PEER_DEVICE);
                                        currentLogEntries.add(logEntry);
                                    }

                                } else {
                                    XLog.e("HCE ERROR: received empty command apdu");
                                }
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Object result = param.getResult();
                            if (result != null && result instanceof byte[]) {
                                byte[] rApdu = (byte[]) result;
                                if (rApdu.length > 0) {
                                    XLog.d("HCE TX: %s", DataUtil.toHexString(rApdu));

                                    if (currentLogEntries != null) {
                                        InteractionLogEntry logEntry = new InteractionLogEntry(System.currentTimeMillis(), rApdu, BroadcastConstants.PEER_DEVICE, BroadcastConstants.PEER_TERMINAL);
                                        currentLogEntries.add(logEntry);
                                    }

                                } else {
                                    XLog.e("HCE ERROR: transmitted empty response apdu");
                                }
                            }
                        }
                    });

            XLog.d("Apply %s hook on %s - complete", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        } else {
            XLog.e("Apply %s hook on %s - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private boolean hasNonAbstractMethodImplementation(Class<?> cls, String methodName) {
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers()) && method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    private boolean featuresSupported() {
        boolean hasNfcFeature = hookedAppcontext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
        boolean hasNfcHceFeature = hookedAppcontext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION);
        return hasNfcFeature && hasNfcHceFeature;
    }
}
