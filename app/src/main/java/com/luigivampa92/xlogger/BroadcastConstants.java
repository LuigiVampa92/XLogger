package com.luigivampa92.xlogger;

public class BroadcastConstants {
    public static final String XLOGGER_PACKAGE = BuildConfig.APPLICATION_ID;
    public static final String INTERACTION_LOG_RECEIVER = XLOGGER_PACKAGE + ".InteractionLogBroadcastReceiver";
    public static final String ACTION_RECEIVE_INTERACTION_LOG_NFC_RAW_TAG = XLOGGER_PACKAGE + ".intent.action.RECEIVE_INTERACTION_LOG_NFC_RAW_TAG";
    public static final String EXTRA_DATA = "data";

    // todo move to proper place
    public static final String PEER_DEVICE = "DEVICE";
    public static final String PEER_CARD = "CARD";
    public static final String PEER_TERMINAL = "TERMINAL";

}
