package com.luigivampa92.xlogger.xposed;

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
import com.luigivampa92.xlogger.data.InteractionLog;
import com.luigivampa92.xlogger.data.InteractionLogEntry;
import com.luigivampa92.xlogger.data.InteractionType;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RawDataNfcTagsHooksHandler implements HooksHandler {

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context hookedAppContext;
    private List<InteractionLogEntry> currentLogEntries;

    public RawDataNfcTagsHooksHandler(XC_LoadPackage.LoadPackageParam lpparam, Context hookedAppContext) {
        this.lpparam = lpparam;
        this.hookedAppContext = hookedAppContext;
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
        XLog.d("Init nfc hooks for package %s - tag technology %s - start", lpparam.packageName, tagTechnologyClass.getSimpleName());
        try {
            XposedHelpers.findAndHookMethod(
                    tagTechnologyClass,
                    "connect",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.i("Nfc interaction - tag technology %s - session record started", tagTechnologyClass.getSimpleName());
                            currentLogEntries = new ArrayList<>();
                        }
                    });
            XposedHelpers.findAndHookMethod(
                    tagTechnologyClass,
                    "close",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.i("Nfc interaction - tag technology %s - session record stopped", tagTechnologyClass.getSimpleName());

                            InteractionLog interactionLog = new InteractionLog(InteractionType.NFC_TAG_RAW, lpparam.packageName, tagTechnologyClass.getSimpleName(), (currentLogEntries != null ? new ArrayList<>(currentLogEntries) : new ArrayList<>()));

                            Intent sendInteractionLogRecordIntent = new Intent();
                            sendInteractionLogRecordIntent.setPackage(BroadcastConstants.XLOGGER_PACKAGE);
                            sendInteractionLogRecordIntent.setComponent(new ComponentName(BroadcastConstants.XLOGGER_PACKAGE, BroadcastConstants.INTERACTION_LOG_RECEIVER));
                            sendInteractionLogRecordIntent.setAction(BroadcastConstants.ACTION_RECEIVE_INTERACTION_LOG_NFC_RAW_TAG);
                            sendInteractionLogRecordIntent.putExtra(BroadcastConstants.EXTRA_DATA, interactionLog);

                            hookedAppContext.sendBroadcast(sendInteractionLogRecordIntent);
                            currentLogEntries = null;
                        }
                    });
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
                                        InteractionLogEntry logEntry = new InteractionLogEntry(System.currentTimeMillis(), cApdu, BroadcastConstants.PEER_DEVICE, BroadcastConstants.PEER_CARD);
                                        currentLogEntries.add(logEntry);
                                    }
                                } else {
                                    XLog.i("NFC TX ERROR: empty command apdu");
                                }
                            }
                            Object result = param.getResult();
                            if (result != null && result instanceof byte[]) {
                                byte[] rApdu = (byte[]) result;
                                if (rApdu.length > 0) {
                                    XLog.i("NFC RX: %s", DataUtils.toHexString(rApdu));
                                    if (currentLogEntries != null) {
                                        InteractionLogEntry logEntry = new InteractionLogEntry(System.currentTimeMillis(), rApdu, BroadcastConstants.PEER_CARD, BroadcastConstants.PEER_DEVICE);
                                        currentLogEntries.add(logEntry);
                                    }
                                } else {
                                    XLog.i("NFC RX ERROR: empty response apdu");
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
}
