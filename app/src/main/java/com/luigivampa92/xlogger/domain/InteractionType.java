package com.luigivampa92.xlogger.domain;

public enum InteractionType {

    NFC_TAG_RAW(1),
    HCE_NORMAL(2),
    HCE_NFC_F(3);

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
