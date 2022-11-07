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
    private int verbosityLevelForLogs = XLog.SILENT;

    public BluetoothHooksHandler(final XC_LoadPackage.LoadPackageParam lpparam, final Context hookedAppContext, int verbosityLevelForLogs) {
        this.lpparam = lpparam;
        this.hookedAppContext = hookedAppContext;
        this.verbosityLevelForLogs = verbosityLevelForLogs;
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

            // todo debug info messages:
//            BluetoothGattCallback
//
//            void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) !
//            void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) !
//            void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) !
//            void onMtuChanged(BluetoothGatt gatt, int mtu, int status) !
//
//            void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) // todo already?
//            void onServiceChanged(@NonNull BluetoothGatt gatt)  // todo !
//
//            void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value)
//            void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//
//            void onReliableWriteCompleted(BluetoothGatt gatt, int status) {

            // todo BluetoothGatt - mb, not very practical
//            BluetoothGatt
//
//            void close() {
//            void disconnect() {
//            boolean connect() {
//
//            void readPhy() {   // todo : callback
//            void setPreferredPhy(int txPhy, int rxPhy, int phyOptions)   // todo : callback AND REQUEST HERE
//            boolean readRemoteRssi()    // todo : callback
//            boolean requestMtu(int mtu)   // todo : callback AND REQUEST HERE
//
//            boolean readDescriptor(BluetoothGattDescriptor descriptor) {     // todo : callback
//            boolean writeDescriptor(BluetoothGattDescriptor descriptor) {    // todo : callback
//            int writeDescriptor(@NonNull BluetoothGattDescriptor descriptor, @NonNull byte[] value)     // todo : callback
//
//            boolean beginReliableWrite() {       // todo : какое то апи для транзакций ? в любом случае нужны и гатт и колбэк
//            boolean executeReliableWrite() {
//            void abortReliableWrite() {
//            void abortReliableWrite(BluetoothDevice mDevice) { // @Deprecated
//
//            boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) // todo - что это? это нужно?


//            boolean requestConnectionPriority(int connectionPriority)    // todo : not working ? already in callback ?

//            int getConnectionState(BluetoothDevice device)   // todo : not needed


            ///////////////////////////////////////////////////////////////////////

//            BluetoothGattServer

//            void close()
//            boolean connect(BluetoothDevice device, boolean autoConnect)
//            void cancelConnection(BluetoothDevice device)

//            void setPreferredPhy(BluetoothDevice device, int txPhy, int rxPhy, int phyOptions)
//            public void readPhy(BluetoothDevice device)

//            boolean sendResponse(BluetoothDevice device, int requestId, int status, int offset, byte[] value)
//            boolean notifyCharacteristicChanged(BluetoothDevice device, BluetoothGattCharacteristic characteristic, boolean confirm)
//            int notifyCharacteristicChanged(@NonNull BluetoothDevice device, @NonNull BluetoothGattCharacteristic characteristic, boolean confirm, @NonNull byte[] value)

//            boolean addService(BluetoothGattService service)
//            boolean removeService(BluetoothGattService service)
//            public void clearServices()


//            BluetoothGattServerCallback
//            void onConnectionStateChange(BluetoothDevice device, int status, int newState)
//            void onServiceAdded(int status, BluetoothGattService service) {
//            void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
//            void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
//            void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
//            void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
//            void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
//            void onNotificationSent(BluetoothDevice device, int status) {
//            void onMtuChanged(BluetoothDevice device, int mtu)
//            void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status)
//            void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status)


            ///////////////////////////////////////////////////////////////////////


            // GATT AND ITS CALLBACK

            // todo : debug info ?
            if (verbosityLevelForLogs <= XLog.INFO) {
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
            }

            if (verbosityLevelForLogs <= XLog.INFO) {
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
                                        && param.args[0] != null && param.args[0] instanceof BluetoothGatt
                                        && param.args[1] != null && param.args[1] instanceof BluetoothGattCharacteristic
                                ) {
                                    BluetoothGatt gatt = (BluetoothGatt) param.args[0];
                                    BluetoothGattCharacteristic c = (BluetoothGattCharacteristic) param.args[1];
                                    byte[] data = c.getValue();
                                    XLog.i("BLE - FROM [ THIS DEVICE ] - TO [ %s ] - [ SERV %s / CHAR %s ] - [ READ ] - %s ", gatt.getDevice().getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                                }
                            }
                        });
            }

            // ДЛЯ ECDHE - Я записываю в характеристику ДРУГОМУ УСТРОЙСТВУ
            if (verbosityLevelForLogs <= XLog.INFO) {
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
                                        && param.args[0] != null && param.args[0] instanceof BluetoothGatt
                                        && param.args[1] != null && param.args[1] instanceof BluetoothGattCharacteristic
                                ) {
                                    BluetoothGatt gatt = (BluetoothGatt) param.args[0];
                                    BluetoothGattCharacteristic c = (BluetoothGattCharacteristic) param.args[1];
                                    byte[] data = c.getValue();
                                    XLog.i("BLE - FROM [ THIS DEVICE ] - TO [ %s ] - [ SERV %s / CHAR %s ] - [ WRITE ] - %s ", gatt.getDevice().getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                                }
                            }
                        });
            }

            // ДЛЯ ECDHE - ДРУГОЕ УСТРОЙСТВО отвечает мне в ответ на записанную харатеристику
            if (verbosityLevelForLogs <= XLog.INFO) {
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
                                        && param.args[0] != null && param.args[0] instanceof BluetoothGatt
                                        && param.args[1] != null && param.args[1] instanceof BluetoothGattCharacteristic
                                ) {
                                    BluetoothGatt gatt = (BluetoothGatt) param.args[0];
                                    BluetoothGattCharacteristic c = (BluetoothGattCharacteristic) param.args[1];
                                    byte[] data = c.getValue();
                                    XLog.i("BLE - FROM [ %s ] - TO [ THIS DEVICE ] - [ SERV %s / CHAR %s ] - [ NOTIFY ] - %s ", gatt.getDevice().getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                                }
                            }
                        });
            }







            // GATT SERVER AND ITS CALLBACK !
            if (verbosityLevelForLogs <= XLog.INFO) {
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
            }

            // ДЛЯ ECDHE - Я отправляю ответ ДРУГОМУ УСТРОЙСТВУ на запись в характеристику
            if (verbosityLevelForLogs <= XLog.INFO) {
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
            }

            if (verbosityLevelForLogs <= XLog.INFO) {
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
            }

            // ДЛЯ ECDHE - ДРУГОЕ УСТРОЙСТВО инициирует запись МНЕ в характеристику
            if (verbosityLevelForLogs <= XLog.INFO) {
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
            }





            // todo : debug info
            if (verbosityLevelForLogs <= XLog.DEBUG) {
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
            }

            // todo : debug info
            if (verbosityLevelForLogs <= XLog.DEBUG) {
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
            }



            XLog.d("Init ble hooks for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.d("Init ble hooks for package %s - error", lpparam.packageName);
        }

    }

    private boolean featuresSupported() {
        return hookedAppContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
}