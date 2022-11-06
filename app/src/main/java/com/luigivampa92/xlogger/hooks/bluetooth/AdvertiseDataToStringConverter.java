package com.luigivampa92.xlogger.hooks.bluetooth;

import android.bluetooth.le.AdvertiseData;
import android.os.ParcelUuid;
import android.util.SparseArray;

import com.luigivampa92.xlogger.DataUtils;

import java.util.List;

// todo : stupid solution tbh, should do something better
class AdvertiseDataToStringConverter {

   private AdvertiseDataToStringConverter() {
      throw new IllegalAccessError("No instantiation!");
   }

   public static String convert(AdvertiseData advertiseData) {
      StringBuilder stringBuilder = new StringBuilder("Device started BLE advertising ");

      List<ParcelUuid> serviceUuids = advertiseData.getServiceUuids();
      boolean shouldIncludeNormalServiceUuids = serviceUuids != null && serviceUuids.size() > 0;
      if (shouldIncludeNormalServiceUuids) {
         stringBuilder.append(" with service UUIDs [ ");
         for (ParcelUuid parcelUuid : serviceUuids) {
            String value = parcelUuid.getUuid().toString();
            stringBuilder.append(value);
            stringBuilder.append(" ");
         }
         stringBuilder.append(" ]");
      }

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
         List<ParcelUuid> serviceSolicitationUuids = advertiseData.getServiceSolicitationUuids();
         boolean shouldIncludeSolicitationServiceUuids = serviceSolicitationUuids != null && serviceSolicitationUuids.size() > 0;
         if (shouldIncludeSolicitationServiceUuids) {
            if (shouldIncludeNormalServiceUuids) {
               stringBuilder.append(" and");
            }
            stringBuilder.append(" with solicitation service UUIDs [ ");
            for (ParcelUuid parcelUuid : serviceSolicitationUuids) {
               String value = parcelUuid.getUuid().toString();
               stringBuilder.append(value);
               stringBuilder.append(" ");
            }
            stringBuilder.append(" ]");
         }
      }

      SparseArray<byte[]> manufacturerSpecificData = advertiseData.getManufacturerSpecificData();
      boolean shouldIncludeManufacturerSpecificData = manufacturerSpecificData != null && manufacturerSpecificData.size() > 0;
      if (shouldIncludeManufacturerSpecificData) {
         stringBuilder.append(" including manufacturer specific data [ ");
         for (int i = 0; i < manufacturerSpecificData.size(); i++) {
            byte[] value = manufacturerSpecificData.valueAt(i);
            if (value != null && value.length > 0) {
               String strValue = DataUtils.toHexString(value);
               stringBuilder.append(" <");
               stringBuilder.append(strValue);
               stringBuilder.append(" >");
            }
         }
         stringBuilder.append(" ]");
      }

      return stringBuilder.toString();
   }
}
