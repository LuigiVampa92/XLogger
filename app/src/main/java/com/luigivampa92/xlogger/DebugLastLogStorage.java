package com.luigivampa92.xlogger;

import android.content.Context;
import android.content.SharedPreferences;

public class DebugLastLogStorage {

    private SharedPreferences sharedPreferences;

    public DebugLastLogStorage(Context context) {
        sharedPreferences = context.getSharedPreferences("DEBUG", Context.MODE_PRIVATE);
    }

    public void saveLastLog(String logText) {
        sharedPreferences.edit()
                .putString("LAST_LOG", logText)
                .apply();
    }

    public String getLastLog() {
        return sharedPreferences.getString("LAST_LOG", "");
    }
}
