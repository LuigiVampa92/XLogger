package com.luigivampa92.xlogger.xposed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.nfc.cardemulation.HostApduService;
import android.nfc.cardemulation.HostNfcFService;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.nfc.tech.TagTechnology;
import android.os.Bundle;
import android.util.Log;

import com.luigivampa92.xlogger.DataUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NfcHooks implements IXposedHookLoadPackage {

    private Context context;
    private Set<Class<? extends HostApduService>> hceServices;
    private Set<Class<? extends HostNfcFService>> hnfServices;

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
                            if (hceServices == null) {
                                hceServices = performHostApduServicesSearchByPackageManager(context);
                                if (!hceServices.isEmpty()) {
                                    logHceServiceList(lpparam, hceServices);   // todo remove
                                    applyHceHooks(lpparam, hceServices);
                                }
                            }

                            // apply hooks for emulation mode (nfc-f)
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N && hnfServices == null) {
                                hnfServices = performHostNfcFServicesSearchByPackageManager(context);
                                if (!hnfServices.isEmpty()) {
                                    logHnfServiceList(lpparam, hnfServices);
                                    applyHnfHooks(lpparam, hnfServices);
                                }
                            }

                            // apply hooks for reader mode
                            applyNfcHooks(lpparam);
                        }
                    });
        } catch (Throwable e) {
            XLog.e("Init app context hook for package %s - error, %s", lpparam.packageName, e.getMessage());
        }
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
//        XLog.i("Init nfc hooks for package %s - tag technology %s - start", lpparam.packageName, tagTechnologyClass.getSimpleName()); // todo remove
        try {

            XposedHelpers.findAndHookMethod(
                    tagTechnologyClass,
                    "connect",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.i("Nfc interaction - tag technology %s - session record started", tagTechnologyClass.getSimpleName());
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
                                    XLog.i("NFC TX: %s", DataUtil.toHexString(cApdu));
                                } else {
                                    XLog.e("NFC TX ERROR: empty command apdu");
                                }
                            }
                            Object result = param.getResult();
                            if (result != null && result instanceof byte[]) {
                                byte[] rApdu = (byte[]) result;
                                if (rApdu.length > 0) {
                                    XLog.i("NFC RX: %s", DataUtil.toHexString(rApdu));
                                } else {
                                    XLog.e("NFC RX ERROR: empty response apdu");
                                }
                            }
                        }
                    });

//            XLog.i("Init nfc hooks for package %s - tag technology %s - complete", lpparam.packageName, tagTechnologyClass.getSimpleName()); // todo remove
        } catch (Throwable e) {
            XLog.e("Init nfc hooks for package %s - tag technology %s - error", lpparam.packageName, tagTechnologyClass.getSimpleName(), e);
        }
    }








    // below - hce

    // todo remove ??
    private void logHceServiceList(XC_LoadPackage.LoadPackageParam lpparam, Set<Class<? extends HostApduService>> hceServices) {
        try {
            if (hceServices != null && !hceServices.isEmpty()) {
                XLog.i("*****");
                for (final Class<? extends HostApduService> serviceClass : hceServices) {
                    XLog.i("Package %s - hce service found - %s", lpparam.packageName, serviceClass.getCanonicalName());
                }
                XLog.i("*****");
            }
        }
        catch (Throwable e) {
        }
    }

    private void logHnfServiceList(XC_LoadPackage.LoadPackageParam lpparam, Set<Class<? extends HostNfcFService>> hnfServices) {
        try {
            if (hnfServices != null && !hnfServices.isEmpty()) {
                XLog.i("*****");
                for (final Class<? extends HostNfcFService> serviceClass : hnfServices) {
                    XLog.i("Package %s - hnf service found - %s", lpparam.packageName, serviceClass.getCanonicalName());
                }
                XLog.i("*****");
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
            for (final Class<? extends HostApduService> serviceClass : hceServices) {
                applyHceStartHookForService(serviceClass);
                applyHceStopHookForService(serviceClass);
                applyHceApduHookForService(serviceClass);
            }
            XLog.i("Apply hce hooks for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.e("Apply hce hooks for package %s - error", lpparam.packageName, e);
        }
    }

    private void applyHceStartHookForService(Class<? extends HostApduService> serviceClass) {

        String targetMethodName = "onCreate";

//        XLog.i("Apply %s hook on %s - start", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        if (hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.i("Emulation activated - %s - session record started", serviceClass.getCanonicalName());
                        }
                    });
//            XLog.i("Apply %s hook on %s - complete", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        } else {
            XLog.e("Apply %s hook on %s - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private void applyHceStopHookForService(Class<? extends HostApduService> serviceClass) {

        String targetMethodName = "onDeactivated";

//        XLog.i("Apply %s hook on %s - start", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        if (hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.i("Emulation deactivated - %s - session record stopped", serviceClass.getCanonicalName());
                        }
                    });
//            XLog.i("Apply %s hook on %s - complete", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        } else {
            XLog.e("Apply %s hook on %s - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private void applyHceApduHookForService(Class<? extends HostApduService> serviceClass) {
        String targetMethodName = "processCommandApdu";

//        XLog.i("Apply %s hook on %s - start", targetMethodName, serviceClass.getCanonicalName()); // todo remove
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
                                    XLog.i("HCE RX: %s", DataUtil.toHexString(cApdu));
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
                                    XLog.i("HCE TX: %s", DataUtil.toHexString(rApdu));
                                } else {
                                    XLog.e("HCE ERROR: transmitted empty response apdu");
                                }
                            }
                        }
                    });

//            XLog.i("Apply %s hook on %s - complete", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        } else {
            XLog.e("Apply %s hook on %s - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
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
            for (final Class<? extends HostNfcFService> serviceClass : hnfServices) {
                applyHnfStartHookForService(serviceClass);
                applyHnfStopHookForService(serviceClass);
                applyHnfPacketHookForService(serviceClass);
            }
            XLog.i("Apply hnf hooks for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.e("Apply hnf hooks for package %s - error", lpparam.packageName, e);
        }
    }

    private void applyHnfStartHookForService(Class<? extends HostNfcFService> serviceClass) {

        String targetMethodName = "onCreate";

//        XLog.i("Apply %s hook on %s - HNF - start", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        if (hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.i("Emulation activated - %s - HNF - session record started", serviceClass.getCanonicalName());
                        }
                    });
//            XLog.i("Apply %s hook on %s - HNF - complete", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        } else {
            XLog.e("Apply %s hook on %s - HNF - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private void applyHnfStopHookForService(Class<? extends HostNfcFService> serviceClass) {

        String targetMethodName = "onDeactivated";

//        XLog.i("Apply %s hook on %s - HNF - start", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        if (hasNonAbstractMethodImplementation(serviceClass, targetMethodName)) {
            XposedHelpers.findAndHookMethod(
                    serviceClass,
                    targetMethodName,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XLog.i("Emulation deactivated - %s - HNF - session record stopped", serviceClass.getCanonicalName());
                        }
                    });
//            XLog.i("Apply %s hook on %s - HNF - complete", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        } else {
            XLog.e("Apply %s hook on %s - HNF - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
        }
    }

    private void applyHnfPacketHookForService(Class<? extends HostNfcFService> serviceClass) {
        String targetMethodName = "processNfcFPacket";

//        XLog.i("Apply %s hook on %s - HNF - start", targetMethodName, serviceClass.getCanonicalName()); // todo remove
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
                                    XLog.i("HNF RX: %s", DataUtil.toHexString(cApdu));
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
                                    XLog.i("HNF TX: %s", DataUtil.toHexString(rApdu));
                                } else {
                                    XLog.e("HNF ERROR: transmitted empty response apdu");
                                }
                            }
                        }
                    });

//            XLog.i("Apply %s hook on %s - HNF - complete", targetMethodName, serviceClass.getCanonicalName()); // todo remove
        } else {
            XLog.e("Apply %s hook on %s - HNF - error - method is abstract", targetMethodName, serviceClass.getCanonicalName());
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
}