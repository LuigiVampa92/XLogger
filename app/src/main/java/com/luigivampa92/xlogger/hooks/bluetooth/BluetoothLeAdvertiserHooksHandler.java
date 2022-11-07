package com.luigivampa92.xlogger.hooks.bluetooth;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.PeriodicAdvertisingParameters;
import android.content.Context;
import android.os.Handler;

import com.luigivampa92.xlogger.hooks.HooksHandler;
import com.luigivampa92.xlogger.hooks.XLog;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

// This class applies hooks that track start of BLE advertisement - announcement to other devices of address and capabilities
public class BluetoothLeAdvertiserHooksHandler implements HooksHandler {

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context hookedAppContext;
    private int verbosityLevelForLogs = XLog.SILENT;

    public BluetoothLeAdvertiserHooksHandler(final XC_LoadPackage.LoadPackageParam lpparam, final Context hookedAppContext, int verbosityLevelForLogs) {
        this.lpparam = lpparam;
        this.hookedAppContext = hookedAppContext;
        this.verbosityLevelForLogs = verbosityLevelForLogs;
    }

    @Override
    public void applyHooks() {

        // there are two methods that start advertising:
        // bluetoothLeAdvertiser.startAdvertising(settings, data, callback);
        // bluetoothLeAdvertiser.startAdvertising(settings, data, scanresponse, callback);
        // but the first method just falls into the second one with null in 3rd param
        if (verbosityLevelForLogs <= XLog.DEBUG) {
            XposedHelpers.findAndHookMethod(
                    BluetoothLeAdvertiser.class,
                    "startAdvertising",
                    AdvertiseSettings.class,
                    AdvertiseData.class,
                    AdvertiseData.class,
                    AdvertiseCallback.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            handleAdvertiseStartedDataMethodParameter(param);
                        }
                    }
            );
            if (verbosityLevelForLogs <= XLog.VERBOSE) {
                XposedHelpers.findAndHookMethod(
                        BluetoothLeAdvertiser.class,
                        "stopAdvertising",
                        AdvertiseCallback.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                handleAdvertiseStop();
                            }
                        }
                );
            }
        }

        // on android 9+ new ble api methods were added, that now operate with advertising sets
        // old start and stop advertising methods call these new advertising set methods under the hood
        // so you can see some messages may occur twice on new versions of android
        //
        // Following methods are hooked
        // bluetoothLeAdvertiser.startAdvertisingSet(params, advertisedata, scanresponse, periodicParams, periodicData, callback);
        // bluetoothLeAdvertiser.startAdvertisingSet(params, advertisedata, scanresponse, periodicParams, periodicData, callback, handler);
        // bluetoothLeAdvertiser.startAdvertisingSet(params, advertisedata, scanresponse, periodicParams, periodicData, duration, maxExAdvEvents, callback);
        // bluetoothLeAdvertiser.startAdvertisingSet(params, advertisedata, scanresponse, periodicParams, periodicData, duration, maxExAdvEvents, callback, handler);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            if (verbosityLevelForLogs <= XLog.DEBUG) {
                XposedHelpers.findAndHookMethod(
                        BluetoothLeAdvertiser.class,
                        "startAdvertisingSet",
                        AdvertisingSetParameters.class,
                        AdvertiseData.class,
                        AdvertiseData.class,
                        PeriodicAdvertisingParameters.class,
                        AdvertiseData.class,
                        AdvertisingSetCallback.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                handleAdvertiseStartedDataMethodParameter(param);
                            }
                        }
                );

                XposedHelpers.findAndHookMethod(
                        BluetoothLeAdvertiser.class,
                        "startAdvertisingSet",
                        AdvertisingSetParameters.class,
                        AdvertiseData.class,
                        AdvertiseData.class,
                        PeriodicAdvertisingParameters.class,
                        AdvertiseData.class,
                        AdvertisingSetCallback.class,
                        Handler.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                handleAdvertiseStartedDataMethodParameter(param);
                            }
                        }
                );

                XposedHelpers.findAndHookMethod(
                        BluetoothLeAdvertiser.class,
                        "startAdvertisingSet",
                        AdvertisingSetParameters.class,
                        AdvertiseData.class,
                        AdvertiseData.class,
                        PeriodicAdvertisingParameters.class,
                        AdvertiseData.class,
                        int.class,
                        int.class,
                        AdvertisingSetCallback.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                handleAdvertiseStartedDataMethodParameter(param);
                            }
                        }
                );

                XposedHelpers.findAndHookMethod(
                        BluetoothLeAdvertiser.class,
                        "startAdvertisingSet",
                        AdvertisingSetParameters.class,
                        AdvertiseData.class,
                        AdvertiseData.class,
                        PeriodicAdvertisingParameters.class,
                        AdvertiseData.class,
                        int.class,
                        int.class,
                        AdvertisingSetCallback.class,
                        Handler.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                handleAdvertiseStartedDataMethodParameter(param);
                            }
                        }
                );

                if (verbosityLevelForLogs <= XLog.VERBOSE) {
                    XposedHelpers.findAndHookMethod(
                            BluetoothLeAdvertiser.class,
                            "stopAdvertisingSet",
                            AdvertisingSetCallback.class,
                            new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    super.afterHookedMethod(param);
                                    handleAdvertiseStop();
                                }
                            }
                    );
                }
            }
        }
    }

    private void handleAdvertiseStartedDataMethodParameter(XC_MethodHook.MethodHookParam param) {
        if (param != null && param.args.length > 2 && param.args[1] != null && param.args[1] instanceof AdvertiseData) {
            AdvertiseData advertiseData = (AdvertiseData) param.args[1];
            XLog.d("%s - %s", lpparam.packageName, AdvertiseDataToStringConverter.convert(advertiseData));
        }
    }

    private void handleAdvertiseStop() {
        XLog.v("%s - Device stopped BLE advertising", lpparam.packageName);
    }
}
