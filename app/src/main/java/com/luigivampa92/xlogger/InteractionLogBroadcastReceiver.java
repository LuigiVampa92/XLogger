package com.luigivampa92.xlogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.luigivampa92.xlogger.data.InteractionLog;
import com.luigivampa92.xlogger.data.InteractionLogEntry;
import com.luigivampa92.xlogger.xposed.XLog;

import java.text.SimpleDateFormat;
import java.util.Date;

public class InteractionLogBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case BroadcastConstants.ACTION_RECEIVE_INTERACTION_LOG_NFC_RAW_TAG:
                handleLogRawNfcTag(context, intent);
                break;
        }
    }

    // todo get calling package !

    private void handleLogRawNfcTag(Context context, Intent intent) {
        InteractionLog interactionLog = intent.getParcelableExtra(BroadcastConstants.EXTRA_DATA);
        if (interactionLog == null || interactionLog.getEntries() == null || interactionLog.getEntries().isEmpty()) {
            return;
        }


        XLog.i("*******");
        XLog.i("*******");
        XLog.i("*******");
        XLog.i("INTERACTION LOG RECEIVED");
        XLog.i("TYPE - %s", interactionLog.getType().name());
        XLog.i("TIME - %s", new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss").format(new Date(interactionLog.getTimestamp())));
        XLog.i("DURATION - %d ms", interactionLog.getDuration());
        for (InteractionLogEntry logEntry : interactionLog.getEntries()) {
            XLog.i("[%s] %s : %s", new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(logEntry.getTimestamp())), logEntry.getSender(), DataUtil.toHexString(logEntry.getData()));
        }
        XLog.i("*******");
        XLog.i("*******");

        StringBuilder sb = new StringBuilder();
        sb.append("*******\n");
        sb.append("INTERACTION LOG RECEIVED\n");
        sb.append(String.format("TYPE - %s\n", interactionLog.getType().name()));
        sb.append(String.format("TIME - %s\n", new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss").format(new Date(interactionLog.getTimestamp()))));
        sb.append(String.format("DURATION - %d ms\n", interactionLog.getDuration()));
        sb.append("*******\n");
        for (InteractionLogEntry logEntry : interactionLog.getEntries()) {
            sb.append(String.format("[%s] %s : %s\n", new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(logEntry.getTimestamp())), logEntry.getSender(), DataUtil.toHexString(logEntry.getData())));
        }
        sb.append("*******\n");



        DebugLastLogStorage storage = new DebugLastLogStorage(context);
        storage.saveLastLog(sb.toString());

        
    }
}
