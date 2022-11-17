package com.luigivampa92.xlogger.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.InteractionLogSender
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.domain.InteractionLog

class InteractionLogActivity : BaseActivity() {

    companion object {

        private const val KEY_INTERACTION_LOG = "transmitted_interaction_log_details"

        @JvmStatic
        fun newIntent(context: Context, interactionLog: InteractionLog): Intent {
            val intent = Intent(context, InteractionLogActivity::class.java)
            intent.putExtra(KEY_INTERACTION_LOG, interactionLog)
            return intent
        }
    }

    private lateinit var interactionLog: InteractionLog
    private lateinit var logSender: InteractionLogSender
    private lateinit var recyclerViewLogEntries: RecyclerView
    private lateinit var recyclerViewLogEntriesAdapter: InteractionLogDetailsAdapter
    private lateinit var recyclerViewLogEntriesLayoutManager: LinearLayoutManager
    private lateinit var recyclerViewItemDecoration: DividerItemDecoration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interaction_log)

        val receivedLog = intent.getParcelableExtra<InteractionLog>(KEY_INTERACTION_LOG)
        if (receivedLog == null) {
            finish()
            return
        } else {
            interactionLog = receivedLog
        }

        logSender = InteractionLogSender(this)

        recyclerViewLogEntries = findViewById(R.id.recycler_view_log_entires)
        recyclerViewLogEntriesAdapter = InteractionLogDetailsAdapter(logSender::sendLog)
        recyclerViewLogEntriesLayoutManager = LinearLayoutManager(this)
        recyclerViewItemDecoration = DividerItemDecoration(recyclerViewLogEntries.context, DividerItemDecoration.VERTICAL)
        recyclerViewItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_line_horizontal)!!)
        recyclerViewLogEntries.adapter = recyclerViewLogEntriesAdapter
        recyclerViewLogEntries.layoutManager = recyclerViewLogEntriesLayoutManager
        recyclerViewLogEntries.addItemDecoration(recyclerViewItemDecoration)

        recyclerViewLogEntriesAdapter.setRecord(interactionLog)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}