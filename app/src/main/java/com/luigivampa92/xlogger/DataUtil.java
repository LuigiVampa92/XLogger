package com.luigivampa92.xlogger;

public final class DataUtil {

    private DataUtil() {
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
}
