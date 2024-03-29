package com.luigivampa92.xlogger.hooks.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.luigivampa92.xlogger.BroadcastConstants;
import com.luigivampa92.xlogger.DataUtils;
import com.luigivampa92.xlogger.domain.InteractionLog;
import com.luigivampa92.xlogger.domain.InteractionLogEntry;
import com.luigivampa92.xlogger.domain.InteractionLogEntryAction;
import com.luigivampa92.xlogger.domain.InteractionType;
import com.luigivampa92.xlogger.hooks.HookUtils;
import com.luigivampa92.xlogger.hooks.HooksHandler;
import com.luigivampa92.xlogger.hooks.XLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class BluetoothHooksHandler implements HooksHandler {

    private final XC_LoadPackage.LoadPackageParam lpparam;
    private final Context hookedAppContext;
    private int verbosityLevelForLogs = XLog.SILENT;
    private List<InteractionLogEntry> currentLogEntries = new ArrayList<>();
    private final int completionTimerValueForBle = 10000;
    private ScheduledExecutorService timerService;

    public BluetoothHooksHandler(final XC_LoadPackage.LoadPackageParam lpparam, final Context hookedAppContext, int verbosityLevelForLogs) {
        this.lpparam = lpparam;
        this.hookedAppContext = hookedAppContext;
        this.verbosityLevelForLogs = verbosityLevelForLogs;
        if (timerService == null || timerService.isShutdown() || timerService.isTerminated()) {
            timerService = Executors.newSingleThreadScheduledExecutor();
            timerService.scheduleAtFixedRate(timeoutCheck(), completionTimerValueForBle, completionTimerValueForBle, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void applyHooks() {
        if (!featuresSupported()) {
            return;
        }
        applyBleHooks(lpparam);
    }

    private void applyBleHooks(final XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.v("Init ble hooks for package %s - start", lpparam.packageName);
        try {
            applyBleHooksToUnextendableGattClasses(lpparam);
            applyBleHooksToGattCallbackDirectlyInstantiatedClasses(lpparam);
            applyBleHooksToGattCallbackNonAbstractChildClasses(lpparam);
            XLog.d("Init ble hooks for package %s - complete", lpparam.packageName);
        } catch (Throwable e) {
            XLog.d("Init ble hooks for package %s - error", lpparam.packageName);
        }
    }

    private void applyBleHooksToUnextendableGattClasses(final XC_LoadPackage.LoadPackageParam lpparam) {
//            applyPrimaryGattHooks();                  // not used
//            applySecondaryGattHooks();                // not used
        applyPrimaryGattServerHooks();
//            applySecondaryGattServerHooks();          // not used
    }

    private void applyBleHooksToGattCallbackDirectlyInstantiatedClasses(final XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.v("Init ble hooks to directly instantiated callback classes for package %s - start", lpparam.packageName);
        applyPrimaryGattCallbackHooks(BluetoothGattCallback.class);
        applySecondaryGattCallbackHooks(BluetoothGattCallback.class);
        applyPrimaryGattServerCallbackHooks(BluetoothGattServerCallback.class);
        applySecondaryGattServerCallbackHooks(BluetoothGattServerCallback.class);
        XLog.v("Init ble hooks to directly instantiated callback classes for package %s - complete", lpparam.packageName);
    }

    private void applyBleHooksToGattCallbackNonAbstractChildClasses(final XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.v("Init ble hooks to non abstract children of callback classes for package %s - start", lpparam.packageName);
        Class<?>[] targetParentClasses = new Class<?>[] { BluetoothGattCallback.class, BluetoothGattServerCallback.class };
        try {
            Map<Class<?>, Set<Class<?>>> childClasses = HookUtils.findChildClassesInClassLoader(hookedAppContext, targetParentClasses);
            if (childClasses.containsKey(BluetoothGattCallback.class)) {
                Set<Class<?>> gattCallbackChildClasses = childClasses.get(BluetoothGattCallback.class);
                if (gattCallbackChildClasses != null && !gattCallbackChildClasses.isEmpty()) {
                    for (Class<?> child : gattCallbackChildClasses) {
                        applyPrimaryGattCallbackHooks(child);
                        applySecondaryGattCallbackHooks(child);
                    }
                }
            }
            if (childClasses.containsKey(BluetoothGattServerCallback.class)) {
                Set<Class<?>> gattServerCallbackChildClasses = childClasses.get(BluetoothGattServerCallback.class);
                if (gattServerCallbackChildClasses != null && !gattServerCallbackChildClasses.isEmpty()) {
                    for (Class<?> child : gattServerCallbackChildClasses) {
                        applyPrimaryGattServerCallbackHooks(child);
                        applySecondaryGattServerCallbackHooks(child);
                    }
                }
            }
            XLog.v("Init ble hooks to non abstract children of callback classes for package %s - complete", lpparam.packageName);
        } catch (IOException | ClassNotFoundException e) {
            XLog.v("Init ble hooks to non abstract children of callback classes for package %s - error", lpparam.packageName);
        }
    }

    private boolean featuresSupported() {
        return hookedAppContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private void applyPrimaryGattHooks() {}

    private void applySecondaryGattHooks() {
//            void close() {
//            void disconnect() {
//            boolean connect() {
//
//            void readPhy() {   // callback?
//            void setPreferredPhy(int txPhy, int rxPhy, int phyOptions)   // callback AND REQUEST HERE !
//            boolean readRemoteRssi()    // callback?
//            boolean requestMtu(int mtu)   // callback AND REQUEST HERE !
//
//            boolean readDescriptor(BluetoothGattDescriptor descriptor) {     // callback ?
//            boolean writeDescriptor(BluetoothGattDescriptor descriptor) {    // callback ?
//            int writeDescriptor(@NonNull BluetoothGattDescriptor descriptor, @NonNull byte[] value)     // callback ?
//
//            boolean beginReliableWrite() {
//            boolean executeReliableWrite() {
//            void abortReliableWrite() {
//            void abortReliableWrite(BluetoothDevice mDevice) { // @Deprecated
//
//            boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) // ???
//            boolean requestConnectionPriority(int connectionPriority)    // ???
    }

    private void applyPrimaryGattCallbackHooks(Class<?> bluetoothGattCallbackClass) {
        // track connections/disconnections
        if (verbosityLevelForLogs <= XLog.INFO) {
            XposedHelpers.findAndHookMethod(
                    bluetoothGattCallbackClass,
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
                                if (currentLogEntries != null) {
                                    InteractionLogEntry logEntry = new InteractionLogEntry(
                                            System.currentTimeMillis(),
                                            InteractionLogEntryAction.BLE_CONNECT,
                                            null,
                                            null,
                                            BroadcastConstants.PEER_THIS_DEVICE,
                                            gatt.getDevice().getAddress(),
                                            null,
                                            null
                                    );
                                    currentLogEntries.add(logEntry);
                                }
                            } else if (previousState == BluetoothGatt.STATE_CONNECTED && newState != BluetoothGatt.STATE_CONNECTED) {
                                XLog.i("BLE - [ THIS DEVICE ] disconnected from [ %s ] ", gatt.getDevice().getAddress());
                                if (currentLogEntries != null) {
                                    InteractionLogEntry logEntry = new InteractionLogEntry(
                                            System.currentTimeMillis(),
                                            InteractionLogEntryAction.BLE_DISCONNECT,
                                            null,
                                            null,
                                            BroadcastConstants.PEER_THIS_DEVICE,
                                            gatt.getDevice().getAddress(),
                                            null,
                                            null
                                    );
                                    currentLogEntries.add(logEntry);
                                }
                            }
                        }
                    });
        }

        // communication - THIS device requests to READ FROM a gatt characteristic on the service running on ANOTHER device
        if (verbosityLevelForLogs <= XLog.INFO) {
            XposedHelpers.findAndHookMethod(
                    bluetoothGattCallbackClass,
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
                                if (data != null && data.length > 0) {
                                    XLog.i("BLE - FROM [ THIS DEVICE ] - TO [ %s ] - [ SERV %s / CHAR %s ] - [ READ ] - %s ", gatt.getDevice().getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                                    if (currentLogEntries != null) {
                                        InteractionLogEntry logEntry = new InteractionLogEntry(
                                                System.currentTimeMillis(),
                                                InteractionLogEntryAction.BLE_READ,
                                                data,
                                                null,
                                                BroadcastConstants.PEER_THIS_DEVICE,
                                                gatt.getDevice().getAddress(),
                                                c.getService().getUuid().toString(),
                                                c.getUuid().toString()
                                        );
                                        currentLogEntries.add(logEntry);
                                    }
                                }
                            }
                        }
                    });
        }

        // communication - THIS device requests to WRITE TO a gatt characteristic on the service running on ANOTHER device
        if (verbosityLevelForLogs <= XLog.INFO) {
            XposedHelpers.findAndHookMethod(
                    bluetoothGattCallbackClass,
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
                                if (data != null && data.length > 0) {
                                    XLog.i("BLE - FROM [ THIS DEVICE ] - TO [ %s ] - [ SERV %s / CHAR %s ] - [ WRITE ] - %s ", gatt.getDevice().getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                                    if (currentLogEntries != null) {
                                        InteractionLogEntry logEntry = new InteractionLogEntry(
                                                System.currentTimeMillis(),
                                                InteractionLogEntryAction.BLE_WRITE,
                                                data,
                                                null,
                                                BroadcastConstants.PEER_THIS_DEVICE,
                                                gatt.getDevice().getAddress(),
                                                c.getService().getUuid().toString(),
                                                c.getUuid().toString()
                                        );
                                        currentLogEntries.add(logEntry);
                                    }
                                }
                            }
                        }
                    });
        }

        // communication - ANOTHER device sends a NOTIFY that gatt characteristic changed to THIS device
        if (verbosityLevelForLogs <= XLog.INFO) {
            XposedHelpers.findAndHookMethod(
                    bluetoothGattCallbackClass,
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
                                if (data != null && data.length > 0) {
                                    XLog.i("BLE - FROM [ %s ] - TO [ THIS DEVICE ] - [ SERV %s / CHAR %s ] - [ NOTIFY ] - %s ", gatt.getDevice().getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                                    if (currentLogEntries != null) {
                                        InteractionLogEntry logEntry = new InteractionLogEntry(
                                                System.currentTimeMillis(),
                                                InteractionLogEntryAction.BLE_NOTIFY,
                                                data,
                                                null,
                                                gatt.getDevice().getAddress(),
                                                BroadcastConstants.PEER_THIS_DEVICE,
                                                c.getService().getUuid().toString(),
                                                c.getUuid().toString()
                                        );
                                        currentLogEntries.add(logEntry);
                                    }
                                }
                            }
                        }
                    });
        }

//            void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value)
//            void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }

    private void applySecondaryGattCallbackHooks(Class<?> bluetoothGattCallbackClass) {
        // peripherial gatt service received
        if (verbosityLevelForLogs <= XLog.DEBUG) {
            XposedHelpers.findAndHookMethod(
                    bluetoothGattCallbackClass,
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

//            void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) !
//            void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) !
//            void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) !
//            void onMtuChanged(BluetoothGatt gatt, int mtu, int status) !
//
//            void onServiceChanged(@NonNull BluetoothGatt gatt)                // gatt services on peripherial should be rediscovered ?
//            void onReliableWriteCompleted(BluetoothGatt gatt, int status)     // confirm reliable write ?
    }

    private void applyPrimaryGattServerHooks() {

        // communication - THIS device answers to ANOTHER device in a form of a notify
//            boolean notifyCharacteristicChanged(BluetoothDevice device, BluetoothGattCharacteristic characteristic, boolean confirm) // deprecated, calls the method below
//            int notifyCharacteristicChanged(@NonNull BluetoothDevice device, @NonNull BluetoothGattCharacteristic characteristic, boolean confirm, @NonNull byte[] value)
        if (verbosityLevelForLogs <= XLog.INFO) {
            XposedHelpers.findAndHookMethod(
                    BluetoothGattServer.class,
                    "notifyCharacteristicChanged",
                    BluetoothDevice.class,
                    BluetoothGattCharacteristic.class,
                    "boolean",
//                    byte[].class,
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
                                if (data != null && data.length > 0) {
                                    XLog.i("BLE - FROM [ THIS DEVICE ] - TO [ %s ] - [ SERV %s / CHAR %s ] - [ NOTIFY ] - %s ", d.getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                                    if (currentLogEntries != null) {
                                        InteractionLogEntry logEntry = new InteractionLogEntry(
                                                System.currentTimeMillis(),
                                                InteractionLogEntryAction.BLE_NOTIFY,
                                                data,
                                                null,
                                                BroadcastConstants.PEER_THIS_DEVICE,
                                                d.getAddress(),
                                                c.getService().getUuid().toString(),
                                                c.getUuid().toString()
                                        );
                                        currentLogEntries.add(logEntry);
                                    }
                                }
                            }
                        }
                    });
        }

//            boolean sendResponse(BluetoothDevice device, int requestId, int status, int offset, byte[] value)
    }

    private void applySecondaryGattServerHooks() {
//            BluetoothManager.openGattServer(Context context, BluetoothGattServerCallback callback)
//            XLog.i("BLE - INFO - [ THIS DEVICE ] started a new gatt server);
//            BluetoothGattServer.close()
//            XLog.i("BLE - INFO - [ THIS DEVICE ] stopped running gatt server);
//            boolean connect(BluetoothDevice device, boolean autoConnect)
//            XLog.i("BLE - INFO - gatt server on [ THIS DEVICE ] requested to connect to [ %s ]", d.getAddress()); // skip autoconnect for now
//            void cancelConnection(BluetoothDevice device)
//            XLog.i("BLE - INFO - gatt server on [ THIS DEVICE ] requested to disconnect from [ %s ]", d.getAddress());
//            public void readPhy(BluetoothDevice device)
//            XLog.i("BLE - INFO - gatt server on [ THIS DEVICE ] requested to read phy from [ %s ]", d.getAddress());
//            void setPreferredPhy(BluetoothDevice device, int txPhy, int rxPhy, int phyOptions)
//            XLog.i("BLE - INFO - gatt server on [ THIS DEVICE ] requested to update new phy on [ %s ]: txPhy - ; rxPhy - ; phyOptions - ;", d.getAddress());
//            boolean addService(BluetoothGattService service)
//            XLog.i("BLE - INFO - gatt server on [ THIS DEVICE ] requested to add new gatt service [ %s ]", );
//            boolean removeService(BluetoothGattService service)
//            XLog.i("BLE - INFO - gatt server on [ THIS DEVICE ] requested to remove running gatt service [ %s ]", );
//            public void clearServices()
//            XLog.i("BLE - INFO - gatt server on [ THIS DEVICE ] requested to remove all running gatt services");
    }

    private void applyPrimaryGattServerCallbackHooks(Class<?> bluetoothGattServerCallbackClass) {
        // track connections/disconnections
        if (verbosityLevelForLogs <= XLog.INFO) {
            XposedHelpers.findAndHookMethod(
                    bluetoothGattServerCallbackClass,
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
                                if (currentLogEntries != null) {
                                    InteractionLogEntry logEntry = new InteractionLogEntry(
                                            System.currentTimeMillis(),
                                            InteractionLogEntryAction.BLE_CONNECT,
                                            null,
                                            null,
                                            device.getAddress(),
                                            BroadcastConstants.PEER_THIS_DEVICE,
                                            null,
                                            null
                                    );
                                    currentLogEntries.add(logEntry);
                                }
                            } else if (previousState == BluetoothGatt.STATE_CONNECTED && newState != BluetoothGatt.STATE_CONNECTED) {
                                XLog.i("BLE - [ %s ] disconnected from [ THIS DEVICE ] ", device.getAddress());
                                if (currentLogEntries != null) {
                                    InteractionLogEntry logEntry = new InteractionLogEntry(
                                            System.currentTimeMillis(),
                                            InteractionLogEntryAction.BLE_DISCONNECT,
                                            null,
                                            null,
                                            device.getAddress(),
                                            BroadcastConstants.PEER_THIS_DEVICE,
                                            null,
                                            null
                                    );
                                    currentLogEntries.add(logEntry);
                                }
                            }
                        }
                    });
        }

        // communication - ANOTHER device requests to READ FROM a gatt characteristic on the service running on THIS device
        if (verbosityLevelForLogs <= XLog.INFO) {
            XposedHelpers.findAndHookMethod(
                    bluetoothGattServerCallbackClass,
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
                                if (data != null && data.length > 0) {
                                    XLog.i("BLE - FROM [ %s ] - TO [ THIS DEVICE ] - [ SERV %s / CHAR %s ] - [ READ ] - %s ", d.getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                                    if (currentLogEntries != null) {
                                        InteractionLogEntry logEntry = new InteractionLogEntry(
                                                System.currentTimeMillis(),
                                                InteractionLogEntryAction.BLE_READ,
                                                data,
                                                null,
                                                d.getAddress(),
                                                BroadcastConstants.PEER_THIS_DEVICE,
                                                c.getService().getUuid().toString(),
                                                c.getUuid().toString()
                                        );
                                        currentLogEntries.add(logEntry);
                                    }
                                }
                            }
                        }
                    });
        }

        // communication - ANOTHER device requests to WRITE TO a gatt characteristic on the service running on THIS device
        if (verbosityLevelForLogs <= XLog.INFO) {
            XposedHelpers.findAndHookMethod(
                    bluetoothGattServerCallbackClass,
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
                                if (data != null && data.length > 0) {
                                    XLog.i("BLE - FROM [ %s ] - TO [ THIS DEVICE ] - [ SERV %s / CHAR %s ] - [ WRITE ] - %s ", d.getAddress(), c.getService().getUuid().toString(), c.getUuid().toString(), DataUtils.toHexString(data));
                                    if (currentLogEntries != null) {
                                        InteractionLogEntry logEntry = new InteractionLogEntry(
                                                System.currentTimeMillis(),
                                                InteractionLogEntryAction.BLE_WRITE,
                                                data,
                                                null,
                                                d.getAddress(),
                                                BroadcastConstants.PEER_THIS_DEVICE,
                                                c.getService().getUuid().toString(),
                                                c.getUuid().toString()
                                        );
                                        currentLogEntries.add(logEntry);
                                    }
                                }
                            }
                        }
                    });
        }

//            void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor)
//            void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value)
    }

    private void applySecondaryGattServerCallbackHooks(Class<?> bluetoothGattServerCallbackClass) {
        // announce gatt service
        if (verbosityLevelForLogs <= XLog.DEBUG) {
            XposedHelpers.findAndHookMethod(
                    bluetoothGattServerCallbackClass,
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

//            void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute)
//            void onNotificationSent(BluetoothDevice device, int status)
//            void onMtuChanged(BluetoothDevice device, int mtu)
//            void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status)
//            void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status)
    }

    private synchronized void transmitInteractionLog() {
        InteractionLog interactionLog = new InteractionLog(InteractionType.BLE_GATT_INTERACTION, lpparam.packageName, null, (currentLogEntries != null ? new ArrayList<>(currentLogEntries) : new ArrayList<>()));
        Intent sendInteractionLogRecordIntent = new Intent();
        sendInteractionLogRecordIntent.setPackage(BroadcastConstants.XLOGGER_PACKAGE);
        sendInteractionLogRecordIntent.setComponent(new ComponentName(BroadcastConstants.XLOGGER_PACKAGE, BroadcastConstants.INTERACTION_LOG_RECEIVER));
        sendInteractionLogRecordIntent.setAction(BroadcastConstants.ACTION_RECEIVE_INTERACTION_LOG);
        sendInteractionLogRecordIntent.putExtra(BroadcastConstants.EXTRA_DATA, interactionLog);
        hookedAppContext.sendBroadcast(sendInteractionLogRecordIntent);
        if (currentLogEntries != null) {
            currentLogEntries.clear();
        }
    }

    private Runnable timeoutCheck() {
        return new Runnable() {
            @Override
            public void run() {
                if (currentLogEntries != null && !currentLogEntries.isEmpty()) {
                    InteractionLogEntry lastEntry = currentLogEntries.get(currentLogEntries.size() - 1);
                    boolean timeout = (System.currentTimeMillis() - lastEntry.getTimestamp()) >= completionTimerValueForBle;
                    if (timeout) {
                        XLog.d("Ble interaction - session record stopped by timeout");
                        transmitInteractionLog();
                    }
                }
            }
        };
    }
}