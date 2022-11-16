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

class InteractionLogBleGattHeaderViewHolder (
    inflater: LayoutInflater,
    container: ViewGroup,
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_interaction_log_header_ble_gatt, container, false)) {

    private val textHeader: TextView
    private val buttonShare: View

    init {
        textHeader = itemView.findViewById(R.id.text_ble_header_data)
        buttonShare = itemView.findViewById(R.id.button_share_log)
    }

    fun bind(log: InteractionLog) {
        if (log.type != InteractionType.BLE_GATT_INTERACTION) {
            return
        }

        val sb = StringBuilder()
        log.packageName
        sb.appendLine("Bluetooth communication captured")
        sb.appendLine("")
        sb.appendLine("App:")
        sb.appendLine(" - name: \"${getAppName(log.packageName)}\"")
        sb.appendLine(" - packageName: \"${log.packageName}\"")

        sb.appendLine("")
        sb.appendLine("Bluetooth type: BLE")

        val distinctPeersValue = log.entries
            .flatMap { listOf(it.sender, it.receiver) }
            .distinct()
            .joinToString("\n") { " - $it" }
        sb.appendLine("Involved devices:")
        sb.appendLine(distinctPeersValue)

        val distinctServicesValue = log.entries
            .map { Pair<String,String>(it.serviceName ?: "", it.characteristicName ?: "") }
            .distinct()
            .groupBy({ it.first }, { it.second })
            .entries
            .filterNot { it.key == "" }
            .joinToString("\n") { " - ${it.key} with characteristics [${it.value.joinToString(",")}]" }
        sb.appendLine("Involved GATT services:")
        sb.appendLine(distinctServicesValue)

        sb.appendLine("")
        sb.appendLine("Total transferred commands: ${log.entries.size}")
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
        val ms = timeMs % 1000
        list.add("$ms" + itemView.context.getString(R.string.text_timeunit_ms))
        val s = (timeMs / 1000) % 60
        if (s > 0) {
            list.add("$s" + itemView.context.getString(R.string.text_timeunit_sec))
        }
        val m = (timeMs / (1000 * 60) % 60)
        if (m > 0) {
            list.add("$m" + itemView.context.getString(R.string.text_timeunit_min))
        }
        val h = (timeMs / (1000 * 60 * 60) % 24)
        if (h > 0) {
            list.add("$h" + itemView.context.getString(R.string.text_timeunit_hour))
        }
        return list.reversed().joinToString(" ")
    }
}