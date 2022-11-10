package com.luigivampa92.xlogger.domain;

public enum InteractionLogEntryAction {

    GENERAL_EVENT(1),
    UNKNOWN_EVENT(2),

    TRANSFER_DATA_NFC(101),

    BLE_READ(201),
    BLE_WRITE(202),
    BLE_NOTIFY(203),
    BLE_CONNECT(211),
    BLE_DISCONNECT(212),
    BLE_SCANNING(213),
    BLE_ADVERTISING(214);

    private final int value;

    InteractionLogEntryAction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static InteractionLogEntryAction fromValue(int value) {
        for (InteractionLogEntryAction type : InteractionLogEntryAction.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
