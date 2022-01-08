package com.luigivampa92.xlogger.xposed;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NfcHooks implements IXposedHookLoadPackage {

    private Context context;
    private RawDataNfcTagsHooksHandler nfcTagsHooksHandler;
    private HostCardEmulationHooksHandler hceHooksHandler;
    private HostNfcFEmulationHooksHandler hnfHooksHandler;

    // todo нормальный билдскрипт

    // todo Для пакета com.google.android.apps.walletnfcrel с помощью интента ничего не находится, нужен рефлекшон

    // todo правильные логи, не использовать е, всё не важное в д

    // todo gms has co.g.App type of app object
    // todo check permission + !feature! nfc nfc.hce
    // todo check for running module !
    // todo make pcap file
    // todo seems that all hooks apply twice, and the second time there is empty data

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        obtainAppContext(lpparam);
    }

    private void obtainAppContext(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XLog.d("Init app context hook for package %s - start", lpparam.packageName);
            XposedHelpers.findAndHookMethod(
                    Application.class,
                    "onCreate",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (!(param.thisObject instanceof Application)) {
                                XLog.e("Init app context hook for package %s - error - received object is not a context", lpparam.packageName);
                                return;
                            }
                            if (context == null) {
                                context = (Application) param.thisObject;
                            }

                            boolean hasNfcPermission = context.getPackageManager().checkPermission(Manifest.permission.NFC, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
                            if (!hasNfcPermission) {
                                XLog.d("Init app context hook for package %s - error - app does not use NFC", lpparam.packageName);
                                return;
                            } else {
                                XLog.d("Init app context hook for package %s - complete", lpparam.packageName);
                            }

                            // todo as enumeration:

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
            XLog.d("Init app context hook for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.e("Init app context hook for package %s - error", lpparam.packageName, e);
        }
    }
}