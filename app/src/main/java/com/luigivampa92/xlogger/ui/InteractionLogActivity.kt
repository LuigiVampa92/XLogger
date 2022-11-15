package com.luigivampa92.xlogger.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.luigivampa92.xlogger.DataUtils
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

    private lateinit var logTextView: TextView
    private lateinit var interactionLog: InteractionLog

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

        logTextView = findViewById(R.id.text_log_area)

        val print = interactionLog.entries.joinToString("\n\n") { DataUtils.toHexString(it.data) }
        logTextView.setText(print)

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}