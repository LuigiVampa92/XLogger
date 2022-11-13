package com.luigivampa92.xlogger.hooks.nfc;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import com.luigivampa92.xlogger.BroadcastConstants;
import com.luigivampa92.xlogger.DataUtils;
import com.luigivampa92.xlogger.domain.InteractionLog;
import com.luigivampa92.xlogger.domain.InteractionLogEntry;
import com.luigivampa92.xlogger.domain.InteractionLogEntryAction;
import com.luigivampa92.xlogger.domain.InteractionType;
import com.luigivampa92.xlogger.hooks.HookUtils;
import com.luigivampa92.xlogger.hooks.HooksHandler;
import com.luigivampa92.xlogger.hooks.XLog;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HostCardEmulationHooksHandler implements HooksHandler {

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context hookedAppContext;
    private Set<Class<?>> hceServices;
    private List<InteractionLogEntry> currentLogEntries;
    private String currentServiceValue;
    private final int completionTimerValueForNfc = 1500;
    private ScheduledExecutorService timerService;

    public HostCardEmulationHooksHandler(XC_LoadPackage.LoadPackageParam lpparam, Context hookedAppContext) {
        this.lpparam = lpparam;
        this.hookedAppContext = hookedAppContext;
        if (timerService == null || timerService.isShutdown() || timerService.isTerminated()) {
            timerService = Executors.newSingleThreadScheduledExecutor();
            timerService.scheduleAtFixedRate(timeoutCheck(), completionTimerValueForNfc, completionTimerValueForNfc, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void applyHooks() {
        if (!featuresSupported()) {
            return;
        }
        if (hceServices == null) {
            if (lpparam.packageName.equals("com.google.android.apps.walletnfcrel")) {
                hceServices = getHceServicesForGpay(hookedAppContext);
            } else if (lpparam.packageName.equals("com.google.android.gms")) {
                hceServices = getHceServicesForGms(hookedAppContext);
            } else {
                hceServices = performHostApduServicesSearchByPackageManager(hookedAppContext);
            }
            if (!hceServices.isEmpty()) {
                HookUtils.logClassList(hceServices, lpparam.packageName);
                applyHceHooks(lpparam, hceServices);
            }
        }
    }

    // hce services in wallet app are not related to payment cards, they are for transport and loyalty cards
    // there are two of them, they cannot found by query query, and running reflection of start of every process is way too expensive (about 1 sec!)
    private Set<Class<?>> getHceServicesForGpay(Context context) {
        List<String> walletHceServices = Arrays.asList(
                "com.google.commerce.tapandpay.android.hce.service.ValuableApduService",
                "com.google.commerce.tapandpay.android.transit.tap.service.TransitHceService"
        );
        return getClassesFromList(context, walletHceServices);
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
        return getClassesFromList(context, googlePayHceServices);
    }

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
                XLog.d("Error while trying to find a hce service class - %s", targetClassName);
            }
        }

        return result;
    }

    private Set<Class<?>> getClassesFromList(Context context, List<String> classes) {
        HashSet<Class<?>> result = new HashSet<>();
        for (String targetClassName: classes) {
            try {
                Class<?> targetClass = XposedHelpers.findClass(targetClassName, context.getClassLoader());
                if (!Modifier.isAbstract(targetClass.getModifiers())) {
                    result.add(targetClass);
                }
            }
            catch (Throwable e) {
                XLog.d("Error while trying to find class - %s", targetClassName);
            }
        }
        return result;
    }

    private void applyHceHooks(XC_LoadPackage.LoadPackageParam lpparam, Set<Class<?>> hceServices) {
        try {
            XLog.v("Apply hce hooks for package %s - start", lpparam.packageName);
            for (final Class<?> serviceClass : hceServices) {
//                applyHceStopHookForService(serviceClass);  // disabled for now, seems very unreliable compared to timeouts
                applyHceApduHookForService(serviceClass);
            }
            XLog.d("Apply hce hooks for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.d("Apply hce hooks for package %s - error", lpparam.packageName, e);
        }
    }

    // This one is a bit tricky:
    // First - two cases must be handled here in a single hook - the start of an emulation session and the command itself
    // The first idea is to get the oncreate method of the hce service and consider it to be a start of an emulation
    // However, the hce service can be instantiated before the actual emulation starts. Gpay is an example of it - its hce service starts as soon as device unlocked
    // So oncreate is not reliable point of the actual emulation start. Also it can simply be not implemented, because its not mandatory to define it explicitly
    // Also xpf has a limitation - it cannot hook abstract methods, and that's the case with nearby api hce service - it does not have oncreate anywhere
    // That's why processCommandApdu has to be hooked for both these situations
    // Second - there are two approaches of how apdu received by hce service can be handled - sync and async
    // In most cases it is handled synchronously, which means that result is returned straight in processCommandApdu method
    // However, there is also a second possible way - to receive the apdu, start its handling and return null in processCommandApdu
    // After the command has been handled and the result is ready the sendResponseApdu callback is triggered on a hce service instance to deliver the result back
    // This approach is rare but cannot be ignored, that's how gpay works under the hood
    // So processCommandApdu hook must also cover this two different cases depending on the returned value of the processCommandApdu
    // If the approach is async then sendResponseApdu is the point where result apdu can be obtained
    // That's why the sendResponseApdu callback must also be hooked to make sure result value is not missed
    // And both result handling point must check if the result haven't been recorded by another callback
    private void applyHceApduHookForService(Class<?> serviceClass) {
        String targetMethodName = "processCommandApdu";
        XLog.v("Apply %s hook on %s - start", targetMethodName, serviceClass.getCanonicalName());
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
                                        XLog.d("Emulation activated - session record started");
                                    }
                                    XLog.i("HCE RX: %s", DataUtils.toHexString(cApdu));
                                    InteractionLogEntry logEntry = new InteractionLogEntry(
                                            System.currentTimeMillis(),
                                            InteractionLogEntryAction.TRANSFER_DATA_NFC,
                                            cApdu,
                                            null,
                                            BroadcastConstants.PEER_TERMINAL,
                                            BroadcastConstants.PEER_DEVICE,
                                            null,
                                            null
                                    );
                                    currentServiceValue = serviceClass.getCanonicalName();
                                    currentLogEntries.add(logEntry);
                                } else {
                                    XLog.d("HCE ERROR: received empty command apdu");
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
                                        XLog.d("Emulation activated - session record started");
                                    }
                                    if (!currentLogEntries.isEmpty()) {
                                        InteractionLogEntry lastEntry = currentLogEntries.get(currentLogEntries.size() - 1);
                                        boolean alreadyRecorded = Arrays.equals(lastEntry.getData(), rApdu);
                                        if (alreadyRecorded) {
                                            return;
                                        }
                                    }
                                    XLog.i("HCE TX: %s", DataUtils.toHexString(rApdu));
                                    InteractionLogEntry logEntry = new InteractionLogEntry(
                                            System.currentTimeMillis(),
                                            InteractionLogEntryAction.TRANSFER_DATA_NFC,
                                            rApdu,
                                            null,
                                            BroadcastConstants.PEER_DEVICE,
                                            BroadcastConstants.PEER_TERMINAL,
                                            null,
                                            null
                                    );
                                    currentServiceValue = serviceClass.getCanonicalName();
                                    currentLogEntries.add(logEntry);
                                } else {
                                    XLog.d("HCE ERROR: transmitted empty response apdu");
                                }
                            }
                        }
                    });
            XLog.d("Apply %s hook on %s - complete", targetMethodName, serviceClass.getCanonicalName());
        } else {
            XLog.d("Apply %s hook on %s - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }

        String callbackMethodName = "sendResponseApdu";
        XLog.v("Apply %s hook on parent class - start", callbackMethodName);
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
                                        XLog.d("Emulation activated - session record started");
                                    }
                                    if (!currentLogEntries.isEmpty()) {
                                        InteractionLogEntry lastEntry = currentLogEntries.get(currentLogEntries.size() - 1);
                                        boolean alreadyRecorded = Arrays.equals(lastEntry.getData(), rApdu);
                                        if (alreadyRecorded) {
                                            return;
                                        }
                                    }
                                    XLog.i("HCE TX: %s", DataUtils.toHexString(rApdu));
                                    InteractionLogEntry logEntry = new InteractionLogEntry(
                                            System.currentTimeMillis(),
                                            InteractionLogEntryAction.TRANSFER_DATA_NFC,
                                            rApdu,
                                            null,
                                            BroadcastConstants.PEER_DEVICE,
                                            BroadcastConstants.PEER_TERMINAL,
                                            null,
                                            null
                                    );
                                    currentServiceValue = serviceClass.getCanonicalName();
                                    currentLogEntries.add(logEntry);
                                } else {
                                    XLog.d("HCE ERROR: transmitted empty response apdu");
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

    private void applyHceStopHookForService(Class<?> serviceClass) {
        String targetMethodName = "onDeactivated";
        XLog.v("Apply %s hook on %s - start", targetMethodName, serviceClass.getCanonicalName());
        if (HookUtils.hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.d("Emulation deactivated explicitly - %s - session record stopped", serviceClass.getCanonicalName());
                            transmitInteractionLog(serviceClass.getCanonicalName());
                        }
                    });
            XLog.d("Apply %s hook on %s - complete", targetMethodName, serviceClass.getCanonicalName());
        } else {
            XLog.d("Apply %s hook on %s - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private boolean featuresSupported() {
        boolean hasNfcFeature = hookedAppContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
        boolean hasNfcHceFeature = hookedAppContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION);
        return hasNfcFeature && hasNfcHceFeature;
    }

    private synchronized void transmitInteractionLog(final String serviceClassName) {
        if (currentLogEntries != null && !currentLogEntries.isEmpty()) {
            InteractionLog interactionLog = new InteractionLog(InteractionType.HCE_NORMAL, lpparam.packageName, serviceClassName, (currentLogEntries != null ? new ArrayList<>(currentLogEntries) : new ArrayList<>()));
            Intent sendInteractionLogRecordIntent = new Intent();
            sendInteractionLogRecordIntent.setPackage(BroadcastConstants.XLOGGER_PACKAGE);
            sendInteractionLogRecordIntent.setComponent(new ComponentName(BroadcastConstants.XLOGGER_PACKAGE, BroadcastConstants.INTERACTION_LOG_RECEIVER));
            sendInteractionLogRecordIntent.setAction(BroadcastConstants.ACTION_RECEIVE_INTERACTION_LOG);
            sendInteractionLogRecordIntent.putExtra(BroadcastConstants.EXTRA_DATA, interactionLog);
            hookedAppContext.sendBroadcast(sendInteractionLogRecordIntent);
            clearCurrentData();
        }
    }

    private Runnable timeoutCheck() {
        return new Runnable() {
            @Override
            public void run() {
                if (currentLogEntries != null && !currentLogEntries.isEmpty()) {
                    InteractionLogEntry lastEntry = currentLogEntries.get(currentLogEntries.size() - 1);
                    boolean timeout = (System.currentTimeMillis() - lastEntry.getTimestamp()) >= completionTimerValueForNfc;
                    if (timeout) {
                        String serviceClassName = (currentServiceValue != null && !currentServiceValue.isEmpty()) ? currentServiceValue : "";
                        XLog.d("Emulation deactivated by timeout - %s - session record stopped", serviceClassName);
                        transmitInteractionLog(serviceClassName);
                    }
                }
            }
        };
    }

    private void clearCurrentData() {
        currentServiceValue = null;
        if (currentLogEntries != null) {
            currentLogEntries.clear();
        }
    }
}
