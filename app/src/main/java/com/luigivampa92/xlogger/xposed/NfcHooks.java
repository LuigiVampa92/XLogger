package com.luigivampa92.xlogger.xposed;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NfcHooks implements IXposedHookLoadPackage {

    private Context context;
    private RawDataNfcTagsHooksHandler nfcTagsHooksHandler;
    private HostCardEmulationHooksHandler hceHooksHandler;
    private HostNfcFEmulationHooksHandler hnfHooksHandler;

    // todo check for running module !
    // todo make pcap file
    // todo seems that all hooks apply twice, and the second time there is empty data

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        obtainAppContext(lpparam);
    }

    private void obtainAppContext(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XLog.i("Init app context hook for package %s", lpparam.packageName);
            XposedHelpers.findAndHookMethod(
                    Application.class,
                    "onCreate",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (!(param.thisObject instanceof Application)) {
                                Log.e("Init app context hook for package %s - error - received object is not a context", lpparam.packageName);
                                return;
                            }
                            if (context == null) {
                                context = (Application) param.thisObject;
                                if (context.getPackageManager().checkPermission(Manifest.permission.NFC, context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                                    Log.e("Init app context hook for package %s - error - app does not use NFC", lpparam.packageName);
                                    return;
                                }
                                XLog.i("Init app context hook for package %s - complete", lpparam.packageName);
                            }

                            // apply hooks for emulation mode (normal)
                            hceHooksHandler = new HostCardEmulationHooksHandler(lpparam, context);
                            hceHooksHandler.applyHooks();

                            // apply hooks for emulation mode (nfc-f)
                            hnfHooksHandler = new HostNfcFEmulationHooksHandler(lpparam, context);
                            hnfHooksHandler.applyHooks();

                            // apply hooks for reader mode
                            nfcTagsHooksHandler = new RawDataNfcTagsHooksHandler(lpparam, context);
                            nfcTagsHooksHandler.applyHooks();
                        }
                    });
        } catch (Throwable e) {
            XLog.e("Init app context hook for package %s - error, %s", lpparam.packageName, e.getMessage());
        }
    }
}