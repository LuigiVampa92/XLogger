package com.luigivampa92.xlogger.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.luigivampa92.xlogger.DataUtils
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.data.db.AppDatabase
import com.luigivampa92.xlogger.domain.InteractionLog
import com.luigivampa92.xlogger.hooks.XLog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity() {

    // todo нормальный билдскрипт
    // todo check for running module !
    // todo make pcap file

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById<View>(R.id.text_debug) as TextView

        val db = AppDatabase.getInstance(this.applicationContext)
        val d = db.interactionLogDao().all
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    it.forEach {
                        XLog.d(it.toString())
                    }

                    val text = mutableListOf<String>().apply {
                        it.forEach {
                            this.add(entryToText(it.toInteractionLog()))
                        }
                    }.joinToString(separator = "\n")
                    textView.text = text
                },
                {
                    textView.text = "LOGS: ERROR"
                }
            )
    }

    private fun entryToShortText(log: InteractionLog): String {
        return "${log.timestamp.toString()} - ${log.packageName} - ${log.serviceName} - ${log.type.name}\n"
    }

    private fun entryToText(log: InteractionLog): String {
        val sb = StringBuilder()
        sb.append("*******\n")
        sb.append("INTERACTION LOG RECEIVED\n")
        sb.append(String.format("APP - %s\n", log.getPackageName()))
        sb.append(String.format("TYPE - %s\n", log.getType().name))
        sb.append(String.format("SERVICE - %s\n", log.getServiceName()))
        sb.append(
            String.format(
                "TIME - %s\n",
                SimpleDateFormat("dd.MM.yyyy, HH:mm:ss").format(Date(log.getTimestamp()))
            )
        )
        sb.append(String.format("DURATION - %d ms\n", log.getDuration()))
        sb.append("*******\n")
        for (logEntry in log.getEntries()) {
            sb.append(
                String.format(
                    "[%s] %s : %s\n",
                    SimpleDateFormat("HH:mm:ss.SSS").format(Date(logEntry.timestamp)),
                    logEntry.sender,
                    DataUtils.toHexString(logEntry.data)
                )
            )
        }
        sb.append("*******\n")
        return sb.toString()
    }
}