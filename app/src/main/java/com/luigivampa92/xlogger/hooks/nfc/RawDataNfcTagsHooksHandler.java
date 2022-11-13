package com.luigivampa92.xlogger.hooks.nfc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.nfc.tech.TagTechnology;

import com.luigivampa92.xlogger.BroadcastConstants;
import com.luigivampa92.xlogger.DataUtils;
import com.luigivampa92.xlogger.domain.InteractionLog;
import com.luigivampa92.xlogger.domain.InteractionLogEntry;
import com.luigivampa92.xlogger.domain.InteractionLogEntryAction;
import com.luigivampa92.xlogger.domain.InteractionType;
import com.luigivampa92.xlogger.hooks.HooksHandler;
import com.luigivampa92.xlogger.hooks.XLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RawDataNfcTagsHooksHandler implements HooksHandler {

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context hookedAppContext;
    private List<InteractionLogEntry> currentLogEntries;
    private String currentTagTechValue;
    private final int completionTimerValueForNfc = 1500;
    private ScheduledExecutorService timerService;

    public RawDataNfcTagsHooksHandler(XC_LoadPackage.LoadPackageParam lpparam, Context hookedAppContext) {
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
        applyNfcHooks(lpparam);
    }

    private void applyNfcHooks(XC_LoadPackage.LoadPackageParam lpparam) {
        applyNfcHooksForTagTech(lpparam, IsoDep.class);
        applyNfcHooksForTagTech(lpparam, MifareUltralight.class);
        applyNfcHooksForTagTech(lpparam, NfcA.class);
        applyNfcHooksForTagTech(lpparam, NfcB.class);
        applyNfcHooksForTagTech(lpparam, NfcF.class);
        applyNfcHooksForTagTech(lpparam, NfcV.class);
    }

    private void applyNfcHooksForTagTech(XC_LoadPackage.LoadPackageParam lpparam, Class<? extends TagTechnology> tagTechnologyClass) {
        XLog.v("Init nfc hooks for package %s - tag technology %s - start", lpparam.packageName, tagTechnologyClass.getSimpleName());
        try {
            XposedHelpers.findAndHookMethod(
                    tagTechnologyClass,
                    "connect",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.d("Nfc interaction - tag technology %s - session record started", tagTechnologyClass.getSimpleName());
                            currentLogEntries = new ArrayList<>();
                        }
                    });
//            hookExplicitDeactivation(tagTechnologyClass);    // disabled for now, seems very unreliable compared to timeouts
            XposedHelpers.findAndHookMethod(
                    tagTechnologyClass,
                    "transceive",
                    byte[].class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (param.args.length > 0 && param.args[0] != null && param.args[0] instanceof byte[]) {
                                byte[] cApdu = (byte[]) param.args[0];
                                if (cApdu.length > 0) {
                                    XLog.i("NFC TX: %s", DataUtils.toHexString(cApdu));
                                    if (currentLogEntries != null) {
                                        InteractionLogEntry logEntry = new InteractionLogEntry(
                                                System.currentTimeMillis(),
                                                InteractionLogEntryAction.TRANSFER_DATA_NFC,
                                                cApdu,
                                                null,
                                                BroadcastConstants.PEER_DEVICE,
                                                BroadcastConstants.PEER_CARD,
                                                null,
                                                null
                                        );
                                        currentTagTechValue = tagTechnologyClass.getSimpleName();
                                        currentLogEntries.add(logEntry);
                                    }
                                } else {
                                    XLog.d("NFC TX ERROR: empty command apdu");
                                }
                            }
                            Object result = param.getResult();
                            if (result != null && result instanceof byte[]) {
                                byte[] rApdu = (byte[]) result;
                                if (rApdu.length > 0) {
                                    XLog.i("NFC RX: %s", DataUtils.toHexString(rApdu));
                                    if (currentLogEntries != null) {
                                        InteractionLogEntry logEntry = new InteractionLogEntry(
                                                System.currentTimeMillis(),
                                                InteractionLogEntryAction.TRANSFER_DATA_NFC,
                                                rApdu,
                                                null,
                                                BroadcastConstants.PEER_CARD,
                                                BroadcastConstants.PEER_DEVICE,
                                                null,
                                                null
                                        );
                                        currentTagTechValue = tagTechnologyClass.getSimpleName();
                                        currentLogEntries.add(logEntry);
                                    }
                                } else {
                                    XLog.d("NFC RX ERROR: empty response apdu");
                                }
                            }
                        }
                    });
            XLog.d("Init nfc hooks for package %s - tag technology %s - complete", lpparam.packageName, tagTechnologyClass.getSimpleName());
        } catch (Throwable e) {
            XLog.d("Init nfc hooks for package %s - tag technology %s - error", lpparam.packageName, tagTechnologyClass.getSimpleName());
        }
    }

    private boolean featuresSupported() {
        return hookedAppContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
    }

    private void hookExplicitDeactivation(Class<? extends TagTechnology> tagTechnologyClass) {
        XposedHelpers.findAndHookMethod(
                tagTechnologyClass,
                "close",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        XLog.d("Nfc interaction - tag technology %s - session record stopped explicitly", tagTechnologyClass.getSimpleName());
                        transmitInteractionLog(tagTechnologyClass.getSimpleName());
                    }
                });
    }

    private synchronized void transmitInteractionLog(final String tagTechnologyClassName) {
        if (currentLogEntries != null && !currentLogEntries.isEmpty()) {
            InteractionLog interactionLog = new InteractionLog(InteractionType.NFC_TAG_RAW, lpparam.packageName, tagTechnologyClassName, (currentLogEntries != null ? new ArrayList<>(currentLogEntries) : new ArrayList<>()));
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
                        String tagTechnologyClassName = (currentTagTechValue != null && !currentTagTechValue.isEmpty()) ? currentTagTechValue : "";
                        XLog.d("Nfc interaction - tag technology %s - session record stopped by timeout", tagTechnologyClassName);
                        transmitInteractionLog(tagTechnologyClassName);
                    }
                }
            }
        };
    }

    private void clearCurrentData() {
        currentTagTechValue = null;
        if (currentLogEntries != null) {
            currentLogEntries.clear();
        }
    }
}
