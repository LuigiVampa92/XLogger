package com.luigivampa92.xlogger.xposed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.nfc.cardemulation.HostNfcFService;
import android.os.Bundle;

import com.luigivampa92.xlogger.DataUtil;
import com.luigivampa92.xlogger.data.InteractionLogEntry;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HostNfcFEmulationHooksHandler implements HooksHandler {

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context hookedAppcontext;
    private Set<Class<? extends HostNfcFService>> hnfServices;
    private List<InteractionLogEntry> currentLogEntries;

    public HostNfcFEmulationHooksHandler(XC_LoadPackage.LoadPackageParam lpparam, Context hookedAppcontext) {
        this.lpparam = lpparam;
        this.hookedAppcontext = hookedAppcontext;
    }

    @Override
    public void applyHooks() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N && hnfServices == null) {
            hnfServices = performHostNfcFServicesSearchByPackageManager(hookedAppcontext);
            if (!hnfServices.isEmpty()) {
                logHnfServiceList(lpparam, hnfServices);
                applyHnfHooks(lpparam, hnfServices);
            }
        }
    }

    private void logHnfServiceList(XC_LoadPackage.LoadPackageParam lpparam, Set<Class<? extends HostNfcFService>> hnfServices) {
        try {
            if (hnfServices != null && !hnfServices.isEmpty()) {
                for (final Class<? extends HostNfcFService> serviceClass : hnfServices) {
                    XLog.d("Package %s - hnf service found - %s", lpparam.packageName, serviceClass.getCanonicalName());
                }
            }
        }
        catch (Throwable e) {
        }
    }


    @SuppressWarnings("unchecked")
    @SuppressLint("QueryPermissionsNeeded")
    private Set<Class<? extends HostNfcFService>> performHostNfcFServicesSearchByPackageManager(Context context) {
        HashSet<Class<? extends HostNfcFService>> result = new HashSet<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Intent intent = new Intent();
            intent.setAction("android.nfc.cardemulation.action.HOST_NFCF_SERVICE");
            intent.setPackage(context.getPackageName());
            List<ResolveInfo> queryResult = context.getPackageManager().queryIntentServices(intent, PackageManager.MATCH_ALL);

            for (ResolveInfo serviceInfo : queryResult) {
                String targetClassName = serviceInfo.serviceInfo.name;
                try {
                    Class<?> targetClass = XposedHelpers.findClass(targetClassName, context.getClassLoader());
                    if (HostNfcFService.class.isAssignableFrom(targetClass) && !Modifier.isAbstract(targetClass.getModifiers())) {
                        Class<? extends HostNfcFService> castedToHeirClass = (Class<? extends HostNfcFService>) targetClass;
                        result.add(castedToHeirClass);
                    }
                } catch (ClassCastException e) {
                    XLog.e("Error while trying to cast a hnf service class - %s", targetClassName);
                }
            }
        }

        return result;
    }

    private void applyHnfHooks(XC_LoadPackage.LoadPackageParam lpparam, Set<Class<? extends HostNfcFService>> hnfServices) {
        try {
            XLog.d("Apply hnf hooks for package %s - start", lpparam.packageName);
            for (final Class<? extends HostNfcFService> serviceClass : hnfServices) {
                applyHnfStartHookForService(serviceClass);
                applyHnfStopHookForService(serviceClass);
                applyHnfPacketHookForService(serviceClass);
            }
            XLog.d("Apply hnf hooks for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.e("Apply hnf hooks for package %s - error", lpparam.packageName, e);
        }
    }

    private void applyHnfStartHookForService(Class<? extends HostNfcFService> serviceClass) {

        String targetMethodName = "onCreate";

        XLog.d("Apply %s hook on %s - HNF - start", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        if (hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.d("Emulation activated - %s - HNF - session record started", serviceClass.getCanonicalName());
                        }
                    });
            XLog.d("Apply %s hook on %s - HNF - complete", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        } else {
            XLog.e("Apply %s hook on %s - HNF - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private void applyHnfStopHookForService(Class<? extends HostNfcFService> serviceClass) {

        String targetMethodName = "onDeactivated";

        XLog.d("Apply %s hook on %s - HNF - start", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        if (hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.d("Emulation deactivated - %s - HNF - session record stopped", serviceClass.getCanonicalName());
                        }
                    });
            XLog.d("Apply %s hook on %s - HNF - complete", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        } else {
            XLog.e("Apply %s hook on %s - HNF - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private void applyHnfPacketHookForService(Class<? extends HostNfcFService> serviceClass) {
        String targetMethodName = "processNfcFPacket";

        XLog.d("Apply %s hook on %s - HNF - start", targetMethodName, serviceClass.getCanonicalName()); // todo remove
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
                                    XLog.d("HNF RX: %s", DataUtil.toHexString(cApdu));
                                } else {
                                    XLog.e("HNF ERROR: received empty command apdu");
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
                                    XLog.d("HNF TX: %s", DataUtil.toHexString(rApdu));
                                } else {
                                    XLog.e("HNF ERROR: transmitted empty response apdu");
                                }
                            }
                        }
                    });

            XLog.d("Apply %s hook on %s - HNF - complete", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        } else {
            XLog.e("Apply %s hook on %s - HNF - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }












    // todo static !
    private boolean hasNonAbstractMethodImplementation(Class<?> cls, String methodName) {
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers()) && method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }
}
