package com.luigivampa92.xlogger.domain;

public enum InteractionLogEntryActionDirection {

    STRAIGHT("-->"),
    REVERSAL("<--"),
    UNDEFINED("");

    private final String value;

    InteractionLogEntryActionDirection(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
