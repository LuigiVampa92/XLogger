package com.luigivampa92.xlogger.hooks;

import android.app.Application;
import android.content.Context;

import com.luigivampa92.xlogger.BuildConfig;
import com.luigivampa92.xlogger.hooks.bluetooth.BluetoothHooksHandler;
import com.luigivampa92.xlogger.hooks.bluetooth.BluetoothLeAdvertiserHooksHandler;
import com.luigivampa92.xlogger.hooks.bluetooth.BluetoothLeScannerHooksHandler;
import com.luigivampa92.xlogger.hooks.nfc.HostCardEmulationHooksHandler;
import com.luigivampa92.xlogger.hooks.nfc.HostNfcFEmulationHooksHandler;
import com.luigivampa92.xlogger.hooks.nfc.RawDataNfcTagsHooksHandler;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HooksApplicator implements IXposedHookLoadPackage {

    private Context context;
    private List<HooksHandler> hooksHandlers;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        obtainAppContext(lpparam);
    }

    private void obtainAppContext(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XLog.v("Init app context hook for package %s - start", lpparam.packageName);
            XposedHelpers.findAndHookMethod(
                    Application.class,
                    "onCreate",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (!(param.thisObject instanceof Application)) {
                                XLog.d("Init app context hook for package %s - error - received object is not a context", lpparam.packageName);
                                return;
                            }
                            if (context == null) {
                                context = (Application) param.thisObject;
                            }
                            XLog.d("Init app context hook for package %s - complete", lpparam.packageName);
                            applyHooksForAppContext(lpparam, context);
                        }
                    });
        } catch (Throwable e) {
            XLog.d("Init app context hook for package %s - error", lpparam.packageName);
        }
    }

    private void applyHooksForAppContext(final XC_LoadPackage.LoadPackageParam lpparam, Context hookedAppContext) {
        if (isManagerApp(lpparam)) {
            setModuleStatus(lpparam);
            return;
        }

        hooksHandlers = new ArrayList<>();

        if (!ConditionUtils.hasNfc(hookedAppContext)) {
            XLog.d("NFC hooks for package %s will NOT be applied - app does not use NFC", lpparam.packageName);
        } else {
            hooksHandlers.add(new HostCardEmulationHooksHandler(lpparam, hookedAppContext));
            hooksHandlers.add(new HostNfcFEmulationHooksHandler(lpparam, hookedAppContext));
            hooksHandlers.add(new RawDataNfcTagsHooksHandler(lpparam, hookedAppContext));
        }

        if (!ConditionUtils.hasBluetooth(hookedAppContext)) {
            XLog.d("Bluetooth hooks for package %s will NOT be applied - app does not use bluetooth", lpparam.packageName);
        } else {
            hooksHandlers.add(new BluetoothHooksHandler(lpparam, hookedAppContext, XLog.DEBUG));
            hooksHandlers.add(new BluetoothLeScannerHooksHandler(lpparam, hookedAppContext, XLog.DEBUG));
            hooksHandlers.add(new BluetoothLeAdvertiserHooksHandler(lpparam, hookedAppContext, XLog.DEBUG));
        }

        for (HooksHandler handler : hooksHandlers) {
            handler.applyHooks();
        }
    }

    private boolean isManagerApp(final XC_LoadPackage.LoadPackageParam lpparam) {
        return BuildConfig.APPLICATION_ID.equals(lpparam.packageName);
    }

    private void setModuleStatus(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            System.setProperty(XposedModuleStateDetector.KEY_XLOGGER_XPOSED_MODULE_ACTIVATED, "true");
            XLog.d("Manager app detected - set xposed module status");
        } catch (Throwable e) {
            XLog.d("Manager app detected - error - failed to set xposed module status");
        }
    }
}