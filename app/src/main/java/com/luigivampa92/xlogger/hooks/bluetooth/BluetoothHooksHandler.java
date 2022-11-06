package com.luigivampa92.xlogger.hooks.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;

import com.luigivampa92.xlogger.DataUtils;
import com.luigivampa92.xlogger.domain.InteractionLogEntry;
import com.luigivampa92.xlogger.hooks.HooksHandler;
import com.luigivampa92.xlogger.hooks.XLog;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


//                                public static final int WRITE_TYPE_DEFAULT = 2;
//                                public static final int WRITE_TYPE_NO_RESPONSE = 1;
//                                public static final int WRITE_TYPE_SIGNED = 4;

public class BluetoothHooksHandler implements HooksHandler {

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context hookedAppContext;

    private List<InteractionLogEntry> currentLogEntries;

    public BluetoothHooksHandler(final XC_LoadPackage.LoadPackageParam lpparam, final Context hookedAppContext) {
        this.lpparam = lpparam;
        this.hookedAppContext = hookedAppContext;
    }

    @Override
    public void applyHooks() {
        if (!featuresSupported()) {
            return;
        }
        applyBleHooks(lpparam);
    }

    private void applyBleHooks(final XC_LoadPackage.LoadPackageParam lpparam) {

        XLog.d("Init ble hooks for package %s - start", lpparam.packageName);

        try {

            // GATT AND ITS CALLBACK

            // todo : debug info ?
            XposedHelpers.findAndHookMethod(
                    BluetoothGattCallback.class,
                    "onConnectionStateChange",
                    BluetoothGatt.class,
                    int.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            BluetoothGatt gatt = (BluetoothGatt) param.args[0];
                            int previousState = (int) param.args[1];
                            int newState = (int) param.args[2];
                            if (previousState != BluetoothGatt.STATE_CONNECTED && newState == BluetoothGatt.STATE_CONNECTED) {
                                XLog.i("BLE - [ THIS DEVICE ] connected to [ %s ] ", gatt.getDevice().getAddress());
                            } else if (previousState == BluetoothGatt.STATE_CONNECTED && newState != BluetoothGatt.STATE_CONNECTED) {
                                XLog.i("BLE - [ THIS DEVICE ] disconnected from [ %s ] ", gatt.getDevice().getAddress());
                            }
                        }
                    });


            XposedHelpers.findAndHookMethod(
                    BluetoothGattCallback.class,
                    "onCharacteristicRead",
                    BluetoothGatt.class,
                    BluetoothGattCharacteristic.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (param.args.length >= 2
                                    && param.args[0] !=null && param.args[0] instanceof BluetoothGatt
                                    && param.args[1] !=null && param.args[1] instanceof BluetoothGattCharacteristic
                            ) {
                                BluetoothGatt gatt = (BluetoothGatt) param.args[0];
                                BluetoothGattCharacteristic c = (BluetoothGattCharacteristic) param.args[1];
                                byte[] data = c.getValue();
                                XLog.i("BLE - FROM [ THIS DEVICE ] - TO [ %s ] - [ SERV %s / CHAR %s ] - [ READ ] - %s ", gatt.getDevice().getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                            }
                        }
                    });

            // ДЛЯ ECDHE - Я записываю в характеристику ДРУГОМУ УСТРОЙСТВУ
            XposedHelpers.findAndHookMethod(
                    BluetoothGattCallback.class,
                    "onCharacteristicWrite",
                    BluetoothGatt.class,
                    BluetoothGattCharacteristic.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (param.args.length >= 2
                                    && param.args[0] !=null && param.args[0] instanceof BluetoothGatt
                                    && param.args[1] !=null && param.args[1] instanceof BluetoothGattCharacteristic
                            ) {
                                BluetoothGatt gatt = (BluetoothGatt) param.args[0];
                                BluetoothGattCharacteristic c = (BluetoothGattCharacteristic) param.args[1];
                                byte[] data = c.getValue();
                                XLog.i("BLE - FROM [ THIS DEVICE ] - TO [ %s ] - [ SERV %s / CHAR %s ] - [ WRITE ] - %s ", gatt.getDevice().getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                            }
                        }
                    });

            // ДЛЯ ECDHE - ДРУГОЕ УСТРОЙСТВО отвечает мне в ответ на записанную харатеристику
            XposedHelpers.findAndHookMethod(
                    BluetoothGattCallback.class,
                    "onCharacteristicChanged",
                    BluetoothGatt.class,
                    BluetoothGattCharacteristic.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (param.args.length == 2
                                    && param.args[0] !=null && param.args[0] instanceof BluetoothGatt
                                    && param.args[1] !=null && param.args[1] instanceof BluetoothGattCharacteristic
                            ) {
                                BluetoothGatt gatt = (BluetoothGatt) param.args[0];
                                BluetoothGattCharacteristic c = (BluetoothGattCharacteristic) param.args[1];
                                byte[] data = c.getValue();
                                XLog.i("BLE - FROM [ %s ] - TO [ THIS DEVICE ] - [ SERV %s / CHAR %s ] - [ NOTIFY ] - %s ", gatt.getDevice().getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                            }
                        }
                    });








            // GATT SERVER AND ITS CALLBACK !

            XposedHelpers.findAndHookMethod(
                    BluetoothGattServerCallback.class,
                    "onConnectionStateChange",
                    BluetoothDevice.class,
                    int.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            BluetoothDevice device = (BluetoothDevice) param.args[0];
                            int previousState = (int) param.args[1];
                            int newState = (int) param.args[2];
                            if (previousState != BluetoothGatt.STATE_CONNECTED && newState == BluetoothGatt.STATE_CONNECTED) {
                                XLog.i("BLE - [ %s ] connected to [ THIS DEVICE ] ", device.getAddress());
                            } else if (previousState == BluetoothGatt.STATE_CONNECTED && newState != BluetoothGatt.STATE_CONNECTED) {
                                XLog.i("BLE - [ %s ] disconnected from [ THIS DEVICE ] ", device.getAddress());
                            }
                        }
                    });

            // ДЛЯ ECDHE - Я отправляю ответ ДРУГОМУ УСТРОЙСТВУ на запись в характеристику
            XposedHelpers.findAndHookMethod(
                    BluetoothGattServer.class,
                    "notifyCharacteristicChanged",
                    BluetoothDevice.class,
                    BluetoothGattCharacteristic.class,
                    "boolean",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (param.args.length > 2
                                    && param.args[0] instanceof BluetoothDevice
                                    && param.args[1] instanceof BluetoothGattCharacteristic
                            ) {
                                BluetoothDevice d = (BluetoothDevice) param.args[0];
                                BluetoothGattCharacteristic c = (BluetoothGattCharacteristic) param.args[1];
                                byte[] data = c.getValue();
                                XLog.i("BLE - FROM [ THIS DEVICE ] - TO [ %s ] - [ SERV %s / CHAR %s ] - [ NOTIFY ] - %s ", d.getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                            }
                        }
                    });

            XposedHelpers.findAndHookMethod(
                    BluetoothGattServerCallback.class,
                    "onCharacteristicReadRequest",
                    BluetoothDevice.class,
                    int.class,
                    int.class,
                    BluetoothGattCharacteristic.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (param.args.length == 4
                                    && param.args[0] != null && param.args[0] instanceof BluetoothDevice
                                    && param.args[3] != null && param.args[2] instanceof BluetoothGattCharacteristic
                            ) {
                                BluetoothDevice d = (BluetoothDevice) param.args[0];
                                BluetoothGattCharacteristic c = (BluetoothGattCharacteristic) param.args[3];
                                byte[] data = c.getValue();
                                XLog.i("BLE - FROM [ %s ] - TO [ THIS DEVICE ] - [ SERV %s / CHAR %s ] - [ READ ] - %s ", d.getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                            }
                        }
                    });

            // ДЛЯ ECDHE - ДРУГОЕ УСТРОЙСТВО инициирует запись МНЕ в характеристику
            XposedHelpers.findAndHookMethod(
                    BluetoothGattServerCallback.class,
                    "onCharacteristicWriteRequest",
                    BluetoothDevice.class,
                    int.class,
                    BluetoothGattCharacteristic.class,
                    "boolean",
                    "boolean",
                    int.class,
                    byte[].class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (param.args.length == 7
                                    && param.args[0] != null && param.args[0] instanceof BluetoothDevice
                                    && param.args[2] != null && param.args[2] instanceof BluetoothGattCharacteristic
                                    && param.args[6] != null && param.args[6] instanceof byte[]
                            ) {
                                BluetoothDevice d = (BluetoothDevice) param.args[0];
                                BluetoothGattCharacteristic c = (BluetoothGattCharacteristic) param.args[2];
                                byte[] data = (byte[]) param.args[6];
                                XLog.i("BLE - FROM [ %s ] - TO [ THIS DEVICE ] - [ SERV %s / CHAR %s ] - [ WRITE ] - %s ", d.getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                            }
                        }
                    });






            // todo : debug info
            XposedHelpers.findAndHookMethod(
                    BluetoothGattCallback.class,
                    "onServicesDiscovered",
                    BluetoothGatt.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (param != null && param.args.length > 0 && param.args[0] != null && param.args[0] instanceof BluetoothGatt) {
                                BluetoothGatt gatt = (BluetoothGatt) param.args[0];
                                List<BluetoothGattService> services = gatt.getServices();
                                if (services != null && services.size() > 0) {
                                    XLog.d("BLE - services discovered on [ %s ]:", gatt.getDevice().getAddress());
                                    for (BluetoothGattService service : services) {
                                        XLog.d(".");
                                        XLog.d("  service [ %s ]", service.getUuid().toString());
                                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                                        for (BluetoothGattCharacteristic characteristic : characteristics) {
                                            XLog.d("    characterisctic [ %s ]", characteristic.getUuid().toString());
                                        }
                                        XLog.d(".");
                                    }
                                }
                            }
                        }
                    }
            );

            // todo : debug info
            XposedHelpers.findAndHookMethod(
                    BluetoothGattServerCallback.class,
                    "onServiceAdded",
                    int.class,
                    BluetoothGattService.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (param != null && param.args.length == 2 && param.args[1] != null && param.args[1] instanceof BluetoothGattService) {
                                XLog.d(".");
                                BluetoothGattService service = (BluetoothGattService) param.args[1];
                                XLog.d("BLE - service started on [ THIS DEVICE ]:");
                                XLog.d("  service [ %s ]", service.getUuid().toString());
                                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                                for (BluetoothGattCharacteristic characteristic : characteristics) {
                                    XLog.d("    characterisctic [ %s ]", characteristic.getUuid().toString());
                                }
                                XLog.d(".");
                            }
                        }
                    });


            XLog.d("Init ble hooks for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.d("Init ble hooks for package %s - error", lpparam.packageName);
        }

    }

    private boolean featuresSupported() {
        return hookedAppContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

}