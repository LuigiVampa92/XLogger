package com.luigivampa92.xlogger.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var recyclerViewLogEntries: RecyclerView
    private lateinit var recyclerViewLogEntriesAdapter: InteractionLogEntryAdapter
    private lateinit var recyclerViewLogEntriesLayoutManager: LinearLayoutManager

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

        recyclerViewLogEntries = findViewById(R.id.recycler_view_log_entires)
        recyclerViewLogEntriesAdapter = InteractionLogEntryAdapter()
        recyclerViewLogEntriesLayoutManager = LinearLayoutManager(this)
        recyclerViewLogEntries.adapter = recyclerViewLogEntriesAdapter
        recyclerViewLogEntries.layoutManager = recyclerViewLogEntriesLayoutManager

        recyclerViewLogEntriesAdapter.setItems(interactionLog.entries)

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}