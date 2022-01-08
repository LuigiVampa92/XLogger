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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HostCardEmulationHooksHandler implements HooksHandler {

    // todo com.google.android.gms.nearby.mediums.nearfieldcommunication.NfcAdvertisingChimeraService - oncreate is abstract, throws an error - bind to first apdu !!
    // todo com.google.android.gms.tapandpay.hce.service.TpHceChimeraService - onCreate called as soon as device unlocked, should bind to process first apdu !!

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context hookedAppcontext;
    private Set<Class<?>> hceServices;
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
            if (lpparam.packageName.equals("com.google.android.apps.walletnfcrel")) {
                hceServices = getHceServicesForGpay(hookedAppcontext);
            } else if (lpparam.packageName.equals("com.google.android.gms")) {
                hceServices = getHceServicesForGms(hookedAppcontext);
            } else {
                hceServices = performHostApduServicesSearchByPackageManager(hookedAppcontext);
            }
            if (!hceServices.isEmpty()) {
                logHceServiceList(lpparam, hceServices);   // todo remove
                applyHceHooks(lpparam, hceServices);
            }
        }
    }


    // todo remove ??
    private void logHceServiceList(XC_LoadPackage.LoadPackageParam lpparam, Set<Class<?>> hceServices) {
        try {
            if (hceServices != null && !hceServices.isEmpty()) {
                for (final Class<?> serviceClass : hceServices) {
                    XLog.d("Package %s - hce service found - %s", lpparam.packageName, serviceClass.getCanonicalName());
                }
            }
        }
        catch (Throwable e) {
        }
    }


    // hce services in wallet app are not related to payment cards, they are for transport and loyalty cards
    // there are two of them, they cannot found by query query, and running reflection of start of every process is way too expensive (about 1 sec!)
    private Set<Class<?>> getHceServicesForGpay(Context context) {
        List<String> walletHceServices = Arrays.asList(
                "com.google.commerce.tapandpay.android.hce.service.ValuableApduService",
                "com.google.commerce.tapandpay.android.transit.tap.service.TransitHceService"
        );

        HashSet<Class<?>> result = new HashSet<>();
        for (String targetClassName: walletHceServices) {
            try {
                Class<?> targetClass = XposedHelpers.findClass(targetClassName, context.getClassLoader());
                if (!Modifier.isAbstract(targetClass.getModifiers())) {
                    result.add(targetClass);
                }
            }
            catch (Throwable e) {
                XLog.e("Error while trying to find wallet hce service class - %s", targetClassName);
            }
        }

        return result;
    }

    // payment related hce services are located not in google pay app but in google mobile services
    // they also extend a different class, the one from chimera module - com.google.android.chimera.HostApduService
    // it is not a normal HostApduService, it is specific only to google services and not present in android framework classpath
    // it repeats a normal HostApduService in fields, methods and behaviour
    // this method provides classes that inherit it (again, to save time on reflection search)
    private Set<Class<?>> getHceServicesForGms(Context context) {
        List<String> googlePayHceServices = Arrays.asList(
                "com.google.android.gms.tapandpay.hce.service.TpHceChimeraService",
                "com.google.android.gms.nearby.mediums.nearfieldcommunication.NfcAdvertisingChimeraService"
        );

        HashSet<Class<?>> result = new HashSet<>();
        for (String targetClassName: googlePayHceServices) {
            try {
                Class<?> targetClass = XposedHelpers.findClass(targetClassName, context.getClassLoader());
                if (!Modifier.isAbstract(targetClass.getModifiers())) {
                    result.add(targetClass);
                }
            }
            catch (Throwable e) {
                XLog.e("Error while trying to find gms hce service class - %s", targetClassName);
            }
        }

        return result;
    }

    // todo common logic above

    // for all normal unprivileged app hce services can be discovered via intent query targeted to itself
    // it works in a few milliseconds, while reflection approach with scanning dex file takes from 300 to 900 ms
    @SuppressLint("QueryPermissionsNeeded")
    private Set<Class<?>> performHostApduServicesSearchByPackageManager(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.nfc.cardemulation.action.HOST_APDU_SERVICE");
        intent.setPackage(context.getPackageName());
        List<ResolveInfo> queryResult = context.getPackageManager().queryIntentServices(intent, PackageManager.MATCH_ALL);

        HashSet<Class<?>> result = new HashSet<>();
        for (ResolveInfo serviceInfo: queryResult) {
            String targetClassName = serviceInfo.serviceInfo.name;
            try {
                Class<?> targetClass = XposedHelpers.findClass(targetClassName, context.getClassLoader());
                if (HostApduService.class.isAssignableFrom(targetClass) && !Modifier.isAbstract(targetClass.getModifiers())) {
                    result.add(targetClass);
                }
            }
            catch (ClassCastException e) {
                XLog.e("Error while trying to cast a hce service class - %s", targetClassName);
            }
        }

        return result;
    }

    private void applyHceHooks(XC_LoadPackage.LoadPackageParam lpparam, Set<Class<?>> hceServices) {
        try {
            XLog.d("Apply hce hooks for package %s - start", lpparam.packageName);
            for (final Class<?> serviceClass : hceServices) {
                applyHceStopHookForService(serviceClass);
                applyHceApduHookForService(serviceClass);
            }
            XLog.d("Apply hce hooks for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.e("Apply hce hooks for package %s - error", lpparam.packageName, e);
        }
    }

    private void applyHceStopHookForService(Class<?> serviceClass) {

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

                            InteractionLog interactionLog = new InteractionLog(InteractionType.HCE_NORMAL, lpparam.packageName, serviceClass.getCanonicalName(), (currentLogEntries != null ? new ArrayList<>(currentLogEntries) : new ArrayList<>()));

                            Intent sendInteractionLogRecordIntent = new Intent();
                            sendInteractionLogRecordIntent.setPackage(BroadcastConstants.XLOGGER_PACKAGE);
                            sendInteractionLogRecordIntent.setComponent(new ComponentName(BroadcastConstants.XLOGGER_PACKAGE, BroadcastConstants.INTERACTION_LOG_RECEIVER));
                            sendInteractionLogRecordIntent.setAction(BroadcastConstants.ACTION_RECEIVE_INTERACTION_LOG_NFC_RAW_TAG);
                            sendInteractionLogRecordIntent.putExtra(BroadcastConstants.EXTRA_DATA, interactionLog);

                            hookedAppcontext.sendBroadcast(sendInteractionLogRecordIntent);
                            currentLogEntries = null;
                        }
                    });
            XLog.d("Apply %s hook on %s - complete", targetMethodName, serviceClass.getCanonicalName());
        } else {
            XLog.e("Apply %s hook on %s - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private void applyHceApduHookForService(Class<?> serviceClass) {
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

                                    if (currentLogEntries == null) {
                                        currentLogEntries = new ArrayList<>();
                                        XLog.d("Emulation activated - session record started");
                                    }

                                    InteractionLogEntry logEntry = new InteractionLogEntry(System.currentTimeMillis(), cApdu, BroadcastConstants.PEER_TERMINAL, BroadcastConstants.PEER_DEVICE);
                                    currentLogEntries.add(logEntry);

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

                                    if (currentLogEntries == null) {
                                        currentLogEntries = new ArrayList<>();
                                        XLog.d("Emulation activated - session record started");
                                    }

                                    InteractionLogEntry logEntry = new InteractionLogEntry(System.currentTimeMillis(), rApdu, BroadcastConstants.PEER_DEVICE, BroadcastConstants.PEER_TERMINAL);
                                    currentLogEntries.add(logEntry);

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
