package com.luigivampa92.xlogger.hooks;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

class ConditionUtils {

    private ConditionUtils() {
        throw new IllegalAccessError("No instantiation!");
    }

    public static boolean hasNfc(Context context) {
        if (hasOneFeature(context,
                PackageManager.FEATURE_NFC,
                PackageManager.FEATURE_NFC_HOST_CARD_EMULATION,
                PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF,
                PackageManager.FEATURE_NFC_BEAM)
        ) {
            return hasOnePermissionGranted(context, Manifest.permission.NFC);
        } else {
            return false;
        }
    }

    public static boolean hasBluetooth(Context context) {
        if (hasOneFeature(context, PackageManager.FEATURE_BLUETOOTH, PackageManager.FEATURE_BLUETOOTH_LE)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                return hasOnePermissionGranted(context,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_PRIVILEGED,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                );
            } else {
                return hasOnePermissionGranted(context,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_PRIVILEGED
                );
            }
        } else {
            return false;
        }
    }

    public static boolean hasOnePermissionGranted(Context context, String... permissionValues) {
        for (String permissionValue : permissionValues) {
            if (context.getPackageManager().checkPermission(permissionValue, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasOneFeature(Context context, String... featureValues) {
        for (String featureValue : featureValues) {
            if (context.getPackageManager().hasSystemFeature(featureValue)) {
                return true;
            }
        }
        return false;
    }
}
