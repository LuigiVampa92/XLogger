package com.luigivampa92.xlogger.hooks.bluetooth;

import android.app.PendingIntent;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;

import com.luigivampa92.xlogger.hooks.HooksHandler;
import com.luigivampa92.xlogger.hooks.XLog;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

// This class applies hooks that track start of BLE scanning - searching of other devices
public class BluetoothLeScannerHooksHandler implements HooksHandler {

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context hookedAppContext;

    public BluetoothLeScannerHooksHandler(final XC_LoadPackage.LoadPackageParam lpparam, final Context hookedAppContext) {
        this.lpparam = lpparam;
        this.hookedAppContext = hookedAppContext;
    }

    @Override
    public void applyHooks() {

        // there are three methods to start ble scanning - one for scanning all devices and two more for scanning with filters:
        // scanner.startScan(callback);
        // scanner.startScan(filters, settings, callback);
        // scanner.startScan(filters, settings, pendingintent);

        XposedHelpers.findAndHookMethod(
                BluetoothLeScanner.class,
                "startScan",
                ScanCallback.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        XLog.d("%s - Device started BLE scanning", lpparam.packageName);
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                BluetoothLeScanner.class,
                "startScan",
                List.class,
                ScanSettings.class,
                ScanCallback.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        handleScanningStartedDataMethodParameter(param);
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                BluetoothLeScanner.class,
                "startScan",
                List.class,
                ScanSettings.class,
                PendingIntent.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        handleScanningStartedDataMethodParameter(param);
                    }
                }
        );

        // there are two methods to stop scanning that are basically the same for the purposes of this app
        // scanner.stopScan(callback);
        // scanner.stopScan(pendingintent);

        XposedHelpers.findAndHookMethod(
                BluetoothLeScanner.class,
                "stopScan",
                ScanCallback.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        handleScanningStopped();
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                BluetoothLeScanner.class,
                "stopScan",
                PendingIntent.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        handleScanningStopped();
                    }
                }
        );
    }

    private void handleScanningStartedDataMethodParameter(final XC_MethodHook.MethodHookParam param) {
        StringBuilder stringBuilder = new StringBuilder(String.format("%s - Device started BLE scanning", lpparam.packageName));
        if (param != null && param.args.length > 0 && param.args[0] != null && param.args[0] instanceof List) {
            List<?> scanFilters = (List<?>) param.args[0];
            if (scanFilters.size() > 0) {
                stringBuilder.append(" with filters [ ");
                ArrayList<String> filterStrings = new ArrayList<>();
                for (int i = 0; i < scanFilters.size(); i++) {
                    Object filterObj = scanFilters.get(i);
                    if (filterObj instanceof ScanFilter) {
                        ScanFilter filter = (ScanFilter) scanFilters.get(i);
                        filterStrings.add(ScanFilterToStringConverter.convert(filter));
                    }
                }
                stringBuilder.append(String.join(", ", filterStrings));
                stringBuilder.append(" ]");
            }
        }
        XLog.d(stringBuilder.toString());
    }

    // logging of scanning stop is for now disabled, because this information seems useless
    private void handleScanningStopped() {
//        XLog.d("%s - Device stopped BLE scanning", lpparam.packageName);
    }
}
