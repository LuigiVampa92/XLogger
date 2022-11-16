package com.luigivampa92.xlogger.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.domain.InteractionLog
import com.luigivampa92.xlogger.domain.InteractionType
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

// todo onclick

class InteractionLogNfcHeaderViewHolder (
    inflater: LayoutInflater,
    container: ViewGroup,
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_interaction_log_header_nfc, container, false)) {

    private val textHeader: TextView
    private val buttonShare: View

    init {
        textHeader = itemView.findViewById(R.id.text_nfc_header_data)
        buttonShare = itemView.findViewById(R.id.button_share_log)
    }

    fun bind(log: InteractionLog) {
        val sb = StringBuilder()
        log.packageName
        sb.appendLine("NFC communication captured")
        sb.appendLine("")
        sb.appendLine("App:")
        sb.appendLine(" - name: \"${getAppName(log.packageName)}\"")
        sb.appendLine(" - packageName: \"${log.packageName}\"")

        sb.appendLine("")
        sb.appendLine("Involved devices:")
        if (log.type == InteractionType.NFC_TAG_RAW) {
            sb.appendLine(" - this device [ DEVICE ] as NFC reader")
            sb.appendLine(" - another device [ CARD ] as NFC tag")
            sb.appendLine("NFC tag technology: ${log.metadata}")
        }
        if (log.type == InteractionType.HCE_NORMAL || log.type == InteractionType.HCE_NFC_F) {
            sb.appendLine(" - this device [ DEVICE ] emulating an NFC tag")
            sb.appendLine(" - another device [ TERMINAL ] as NFC reader")
            sb.appendLine("Emulation service: ${log.metadata}")
        }
        sb.appendLine("")
        sb.appendLine("Total transferred APDUs: ${log.entries.size}")
        sb.appendLine("Date and time: ${formatDateAndTimeValue(log.timestamp)}")
        sb.appendLine("Total time spent: ${formatElapsedTimeValue(log.duration)}")
        textHeader.text = sb.toString()
    }

    // todo move common logic
    private fun getAppName(packageName: String): String {
        return try {
            itemView.context.packageManager.getApplicationInfo(packageName, 0).loadLabel(itemView.context.packageManager).toString()
        } catch (e: Throwable) {
            packageName
        }
    }

    // todo move common logic
    private fun formatDateAndTimeValue(timestamp: Long): String {
        val logDate = Date(timestamp)
        val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT)
        val timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM)
        return "${dateFormat.format(logDate)} ${timeFormat.format(logDate)}"
    }

    // todo move common logic
    private fun formatElapsedTimeValue(timeMs: Long): String {
        val list = ArrayList<String>()
        if (timeMs < 1000) {
            val ms = timeMs % 1000
            list.add("$ms" + itemView.context.getString(R.string.text_timeunit_ms))
        }
        if (timeMs < (1000 * 60)) {
            val s = (timeMs / 1000) % 60
            if (s > 0) {
                list.add("$s" + itemView.context.getString(R.string.text_timeunit_sec))
            }
        }
        if (timeMs < (1000 * 60 * 60)) {
            val m = (timeMs / (1000 * 60) % 60)
            if (m > 0) {
                list.add("$m" + itemView.context.getString(R.string.text_timeunit_min))
            }
        }
        if (timeMs < (1000 * 60 * 60 * 24)) {
            val h = (timeMs / (1000 * 60 * 60) % 24)
            if (h > 0) {
                list.add("$h" + itemView.context.getString(R.string.text_timeunit_hour))
            }
        }
        return list.reversed().joinToString(" ")
    }
}
