package com.luigivampa92.xlogger.hooks;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NfcHooks implements IXposedHookLoadPackage {

    private Context context;
    private List<HooksHandler> hooksHandlers;

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

                            hooksHandlers = new ArrayList<>();
                            hooksHandlers.add(new HostCardEmulationHooksHandler(lpparam, context));
                            hooksHandlers.add(new HostNfcFEmulationHooksHandler(lpparam, context));
                            hooksHandlers.add(new RawDataNfcTagsHooksHandler(lpparam, context));

                            for (HooksHandler handler : hooksHandlers) {
                                handler.applyHooks();
                            }
                        }
                    });
            XLog.d("Init app context hook for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.e("Init app context hook for package %s - error", lpparam.packageName, e);
        }
    }
}