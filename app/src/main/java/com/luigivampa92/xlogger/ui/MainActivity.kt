package com.luigivampa92.xlogger.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.DataUtils
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.data.db.AppDatabase
import com.luigivampa92.xlogger.domain.InteractionLog
import com.luigivampa92.xlogger.hooks.XposedModuleStateDetector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity(), RecyclerViewItemTouchHelper.RecyclerItemTouchHelperListener {

    // todo нормальный билдскрипт
    // todo app icon
    // todo check for uses-feature ?
    // todo check for running module ! - notify if not working
    // todo mifare class mb ??
    // todo make pcap file
    // todo ensure dark theme
    // todo no saved records


    // todo pagination !

    private lateinit var bannerModuleDisabled: View

    private lateinit var recyclerViewLogRecords: RecyclerView
    private lateinit var logRecordsAdapter: InteractionLogAdapter
    private lateinit var logRecordsLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bannerModuleDisabled = findViewById(R.id.banner_module_disabled)
        bannerModuleDisabled.visibility = if (XposedModuleStateDetector.isXposedModuleActive()) View.GONE else View.VISIBLE

        recyclerViewLogRecords = findViewById(R.id.recycler_view_log_records)
        logRecordsAdapter = InteractionLogAdapter(this::showLogDetails)
        logRecordsLayoutManager = LinearLayoutManager(this)
        recyclerViewLogRecords.adapter = logRecordsAdapter
        recyclerViewLogRecords.layoutManager = logRecordsLayoutManager
        recyclerViewLogRecords.itemAnimator = DefaultItemAnimator()
        val itemTouchHelperCallback = RecyclerViewItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewLogRecords)

        val db = AppDatabase.getInstance(this.applicationContext)
        val d = db.interactionLogDao().all
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
//                    it.forEach {
//                        XLog.d(it.toString())
//                    }

//                    val text = mutableListOf<String>().apply {
//                        it.forEach {
//                            this.add(entryToText(it.toInteractionLog()))
//                        }
//                    }.joinToString(separator = "\n")
//                    textView.text = text

                    val logRecords = it.map { it.toInteractionLog() }
                    logRecordsAdapter.setLogRecords(logRecords)

                },
                {
                    val a = "a"
                }
            )
    }

    private fun showLogDetails(log: InteractionLog) {

    }

    private fun entryToShortText(log: InteractionLog): String {
        return "${log.timestamp.toString()} - ${log.packageName} - ${log.metadata} - ${log.type.name}\n"
    }

    private fun entryToText(log: InteractionLog): String {
        val sb = StringBuilder()
        sb.append("*******\n")
        sb.append("INTERACTION LOG RECEIVED\n")
        sb.append(String.format("APP - %s\n", log.getPackageName()))
        sb.append(String.format("TYPE - %s\n", log.getType().name))
        sb.append(String.format("SERVICE - %s\n", log.getMetadata()))
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

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        val record = logRecordsAdapter.getItem(position)
//        deleteRecord(record.value)
        logRecordsAdapter.removeItem(position)
//        showNoErrors()
    }
}