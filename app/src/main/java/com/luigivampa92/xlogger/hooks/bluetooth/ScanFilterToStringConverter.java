package com.luigivampa92.xlogger.hooks.bluetooth;

import android.bluetooth.le.ScanFilter;
import android.os.ParcelUuid;

import com.luigivampa92.xlogger.DataUtils;

import java.util.ArrayList;

// todo : stupid solution tbh, should do something better
class ScanFilterToStringConverter {

    private ScanFilterToStringConverter() {
        throw new IllegalAccessError("No instantiation!");
    }

    public static String convert(ScanFilter scanFilter) {
        ArrayList<String> scanFilterBlocks = new ArrayList<>();

        String addr = scanFilter.getDeviceAddress();
        boolean shouldIncludeAddr = addr != null && !addr.isEmpty();
        String name = scanFilter.getDeviceName();
        boolean shouldIncludeName = name != null && !name.isEmpty();
        boolean shouldIncludeDeviceInfoBlock = shouldIncludeAddr || shouldIncludeName;
        if (shouldIncludeDeviceInfoBlock) {
            StringBuilder stringBuilder = new StringBuilder();
            if (shouldIncludeAddr) {
                stringBuilder.append(String.format("address is \"%s\"", addr));
            }
            if (shouldIncludeName) {
                if (shouldIncludeAddr) {
                    stringBuilder.append(" and ");
                }
                stringBuilder.append(String.format("name is \"%s\"", name));
            }
            scanFilterBlocks.add(stringBuilder.toString());
        }

        int manufacturerId = scanFilter.getManufacturerId();
        boolean shouldIncludeManufacturerId = manufacturerId != -1;
        byte[] manufacturerData = scanFilter.getManufacturerData();
        boolean shouldIncludeManufacturerData = manufacturerData != null && manufacturerData.length > 0;
        byte[] manufacturerDataMask = scanFilter.getManufacturerDataMask();
        boolean shouldIncludeManufacturerDataMask = manufacturerDataMask != null && manufacturerDataMask.length > 0;
        boolean shouldIncludeManufacturerBlock = shouldIncludeManufacturerId || shouldIncludeManufacturerData || shouldIncludeManufacturerDataMask;
        if (shouldIncludeManufacturerBlock) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("manufacturer ");
            if (shouldIncludeManufacturerId) {
                stringBuilder.append(String.format("id is %s", String.valueOf(manufacturerId)));
            }
            if (shouldIncludeManufacturerData || shouldIncludeManufacturerDataMask) {
                stringBuilder.append(", ");
            }
            if (shouldIncludeManufacturerData) {
                stringBuilder.append(String.format("data is %s", DataUtils.toHexStringLower(manufacturerData).replaceAll(" ", "")));
            }
            if (shouldIncludeManufacturerDataMask) {
                stringBuilder.append(", ");
            }
            if (shouldIncludeManufacturerDataMask) {
                stringBuilder.append(String.format("data mask is %s", DataUtils.toHexStringLower(manufacturerDataMask).replaceAll(" ", "")));
            }
            scanFilterBlocks.add(stringBuilder.toString());
        }

        ParcelUuid serviceUuid = scanFilter.getServiceUuid();
        boolean shouldIncludeServiceUuid = serviceUuid != null && serviceUuid.getUuid() != null;
        ParcelUuid serviceUuidMask = scanFilter.getServiceUuidMask();
        boolean shouldIncludeServiceUuidMask = serviceUuidMask != null && serviceUuidMask.getUuid() != null;
        boolean shouldIncludeServiceUuidBlock = shouldIncludeServiceUuid || shouldIncludeServiceUuidMask;
        if (shouldIncludeServiceUuidBlock) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("service ");
            if (shouldIncludeServiceUuid) {
                stringBuilder.append(serviceUuid.getUuid().toString());
            }
            if (shouldIncludeServiceUuidMask) {
                if (shouldIncludeServiceUuid) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append("with mask ");
                stringBuilder.append(serviceUuidMask.getUuid().toString());
            }
            scanFilterBlocks.add(stringBuilder.toString());
        }

        ParcelUuid serviceDataUuid = scanFilter.getServiceDataUuid();
        boolean shouldIncludeServiceDataUuid = serviceDataUuid != null && serviceDataUuid.getUuid() != null;
        byte[] serviceData = scanFilter.getServiceData();
        boolean shouldIncludeServiceData = serviceData != null && serviceData.length > 0;
        byte[] serviceDataMask = scanFilter.getServiceDataMask();
        boolean shouldIncludeServiceDataMask = serviceDataMask != null && serviceDataMask.length > 0;
        boolean shouldIncludeServiceDataBlock = shouldIncludeServiceDataUuid || shouldIncludeServiceData || shouldIncludeServiceDataMask;
        if (shouldIncludeServiceDataBlock) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("service data ");
            if (shouldIncludeServiceDataUuid) {
                stringBuilder.append("uuid ");
                stringBuilder.append(serviceDataUuid.getUuid().toString());
            }
            if (shouldIncludeServiceData) {
                if (shouldIncludeServiceDataUuid) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append("value ");
                stringBuilder.append(DataUtils.toHexStringLower(serviceData).replaceAll(" ", ""));
            }
            if (shouldIncludeServiceDataMask) {
                if (shouldIncludeServiceDataUuid || shouldIncludeServiceData) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append("with mask ");
                stringBuilder.append(DataUtils.toHexStringLower(serviceDataMask).replaceAll(" ", ""));
            }
            scanFilterBlocks.add(stringBuilder.toString());
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ParcelUuid serviceSolicitationUuid = scanFilter.getServiceSolicitationUuid();
            boolean shouldIncludeServiceSolicitationUuid = serviceSolicitationUuid != null && serviceSolicitationUuid.getUuid() != null;
            ParcelUuid serviceSolicitationUuidMask = scanFilter.getServiceSolicitationUuidMask();
            boolean shouldIncludeServiceSolicitationUuidMask = serviceSolicitationUuidMask != null && serviceSolicitationUuidMask.getUuid() != null;
            boolean shouldIncludeServiceSolicitationBlock = shouldIncludeServiceSolicitationUuid || shouldIncludeServiceSolicitationUuidMask;
            if (shouldIncludeServiceSolicitationBlock) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("solicitation service ");
                if (shouldIncludeServiceSolicitationUuid) {
                    stringBuilder.append("uuid ");
                    stringBuilder.append(serviceSolicitationUuid);
                }
                if (shouldIncludeServiceSolicitationUuidMask) {
                    if (shouldIncludeServiceSolicitationUuid) {
                        stringBuilder.append(" ");
                    }
                    stringBuilder.append("with mask ");
                    stringBuilder.append(serviceSolicitationUuidMask.getUuid().toString());
                }
                scanFilterBlocks.add(stringBuilder.toString());
            }
        }

        return String.join(", ", scanFilterBlocks);
    }
}
