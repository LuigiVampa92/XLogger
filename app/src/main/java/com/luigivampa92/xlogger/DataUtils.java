package com.luigivampa92.xlogger;

public final class DataUtils {

    private DataUtils() {
        throw new IllegalAccessError("No instantiation!");
    }

    public static String toHexString(byte[] array) {
        StringBuilder stringBuilder = new StringBuilder();
        if (array != null && array.length > 0) {
            for (int i = 0; i < array.length; ++i) {
                stringBuilder.append(String.format("%02X ", array[i]));
            }
        }
        return stringBuilder.toString();
    }

    public static String toHexStringLower(byte[] array) {
        StringBuilder stringBuilder = new StringBuilder();
        if (array != null && array.length > 0) {
            for (int i = 0; i < array.length; ++i) {
                stringBuilder.append(String.format("%02x", array[i]));
            }
        }
        return stringBuilder.toString();
    }
}
