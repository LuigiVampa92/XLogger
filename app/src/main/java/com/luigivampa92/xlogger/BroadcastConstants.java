package com.luigivampa92.xlogger;

public final class BroadcastConstants {

    private BroadcastConstants() {
        throw new IllegalAccessError("No instantiation!");
    }

    public static final String XLOGGER_PACKAGE = BuildConfig.APPLICATION_ID;
    public static final String INTERACTION_LOG_RECEIVER = XLOGGER_PACKAGE + ".InteractionLogBroadcastReceiver";
    public static final String ACTION_RECEIVE_INTERACTION_LOG = XLOGGER_PACKAGE + ".intent.action.RECEIVE_INTERACTION_LOG";
    public static final String EXTRA_DATA = "data";

    // todo move to proper place
    // todo dedicated enum class ?
    public static final String PEER_DEVICE = "DEVICE";
    public static final String PEER_CARD = "CARD";
    public static final String PEER_TERMINAL = "TERMINAL";

    public static final String PEER_UNKNOWN = "UNKNOWN";
    public static final String PEER_THIS_DEVICE = "THIS DEVICE";

}
