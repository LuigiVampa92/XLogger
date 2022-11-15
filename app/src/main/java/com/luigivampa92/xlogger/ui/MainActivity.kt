package com.luigivampa92.xlogger.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.data.db.AppDatabase
import com.luigivampa92.xlogger.data.db.InteractionLogDao
import com.luigivampa92.xlogger.data.db.InteractionLogEntity
import com.luigivampa92.xlogger.domain.InteractionLog
import com.luigivampa92.xlogger.hooks.XposedModuleStateDetector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivity : BaseActivity(), RecyclerViewItemTouchHelper.RecyclerItemTouchHelperListener {

    // todo нормальный билдскрипт
    // todo app icon
    // todo make pcap file
    // todo ensure dark theme
    // todo no saved records


    // todo pagination !

    private lateinit var bannerModuleDisabled: View

    private lateinit var recyclerViewLogRecords: RecyclerView
    private lateinit var logRecordsAdapter: InteractionLogAdapter
    private lateinit var logRecordsLayoutManager: LinearLayoutManager

    private lateinit var appDatabase: AppDatabase
    private lateinit var interactionLogDao: InteractionLogDao
    private var currentOperation: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupModuleBanner()

        recyclerViewLogRecords = findViewById(R.id.recycler_view_log_records)
        logRecordsAdapter = InteractionLogAdapter(this::showLogDetails)
        logRecordsLayoutManager = LinearLayoutManager(this)
        recyclerViewLogRecords.adapter = logRecordsAdapter
        recyclerViewLogRecords.layoutManager = logRecordsLayoutManager
        recyclerViewLogRecords.itemAnimator = DefaultItemAnimator()
        val itemTouchHelperCallback = RecyclerViewItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewLogRecords)

        appDatabase = AppDatabase.getInstance(this.applicationContext)
        interactionLogDao = appDatabase.interactionLogDao()

        currentOperation = interactionLogDao.all
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val logRecords = it.map { it.toInteractionLog() }
                    logRecordsAdapter.setLogRecords(logRecords)
                },
                {
                    val a = "a"  // todo error state
                }
            )
    }

    override fun onDestroy() {
        if (currentOperation != null && currentOperation!!.isDisposed) {
            currentOperation?.dispose()
        }
        super.onDestroy()
    }

    private fun setupModuleBanner() {
        bannerModuleDisabled = findViewById(R.id.banner_module_disabled)
        val xposedModuleActive = XposedModuleStateDetector.isXposedModuleActive()
        if (xposedModuleActive) {
            bannerModuleDisabled.visibility = View.GONE
        } else {
            bannerModuleDisabled.visibility = View.VISIBLE
            val handler = Handler(Looper.getMainLooper())
            val timeout = resources.getInteger(R.integer.xposed_disabled_notification_timeout).toLong()
            handler.postDelayed({
                try {
                    val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                    bannerModuleDisabled.startAnimation(fadeOut)
                } catch (ignored: Throwable) {}
            }, timeout)
            handler.postDelayed({
                try {
                    bannerModuleDisabled.visibility = View.GONE
                } catch (ignored: Throwable) {}
            }, timeout + 300)
        }
    }

    private fun showLogDetails(log: InteractionLog) {
        startActivity(InteractionLogActivity.newIntent(this, log))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if (direction == ItemTouchHelper.LEFT) {
            val record = logRecordsAdapter.getItem(position)
            deleteRecord(record)
            logRecordsAdapter.removeItem(position)
            val snackbar = Snackbar.make(recyclerViewLogRecords, R.string.text_interaction_log_entry_delete, Snackbar.LENGTH_LONG)
            snackbar.setAction(R.string.text_interaction_log_entry_delete_cancel) { _ ->
                saveRecord(record)
                logRecordsAdapter.insertItem(record, position)
            }
            snackbar.show()
        }
    }

    private fun saveRecord(record: InteractionLog) {
        val start = System.currentTimeMillis()  // todo remove
        currentOperation = interactionLogDao.insert(InteractionLogEntity.fromInteractionLog(record))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val stop = System.currentTimeMillis()
                    val spent = stop - start
                    println("XLOGGER - SUCCESS - SAVE RECORD $record - $spent ms spent")  // todo remove
                },
                {
                    val stop = System.currentTimeMillis()
                    val spent = stop - start
                    println("XLOGGER - ERROR - SAVE RECORD $record - $spent ms spent")  // todo remove
                }
            )
    }

    private fun deleteRecord(record: InteractionLog) {
        val start = System.currentTimeMillis()  // todo remove
        currentOperation = interactionLogDao.delete(InteractionLogEntity.fromInteractionLog(record))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val stop = System.currentTimeMillis()
                    val spent = stop - start
                    println("XLOGGER - SUCCESS - DELETE RECORD $record - $spent ms spent")  // todo remove
                },
                {
                    val stop = System.currentTimeMillis()
                    val spent = stop - start
                    println("XLOGGER - ERROR - DELETE RECORD $record - $spent ms spent")  // todo remove
                }
            )
    }


}