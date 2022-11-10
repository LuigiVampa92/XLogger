package com.luigivampa92.xlogger.domain;

public enum InteractionType {

    GENERAL_LOG(1),

    NFC_TAG_RAW(11),
    HCE_NORMAL(12),
    HCE_NFC_F(13),

    BLE_GATT_INTERACTION(21);

    private final int value;

    InteractionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static InteractionType fromValue(int value) {
        for (InteractionType type : InteractionType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
