package com.luigivampa92.xlogger.data

import android.content.Context
import android.content.SharedPreferences

class DebugLastLogStorage(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences("DEBUG", Context.MODE_PRIVATE)
    }

    fun saveLastLog(logText: String?) {
        sharedPreferences.edit()
            .putString("LAST_LOG", logText)
            .apply()
    }

    fun getLastLog(): String? {
        return sharedPreferences.getString("LAST_LOG", "")
    }
}