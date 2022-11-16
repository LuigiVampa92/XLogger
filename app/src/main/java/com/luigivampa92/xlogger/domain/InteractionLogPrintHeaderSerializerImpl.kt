package com.luigivampa92.xlogger.domain

import android.content.Context
import com.luigivampa92.xlogger.R
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

// todo split with base class !

class InteractionLogPrintHeaderSerializerImpl(
    private val context: Context
) : InteractionLogSerializer {

    override fun serialize(logObject: InteractionLog?): String {
        return when (logObject?.type?.value) {
            InteractionType.BLE_GATT_INTERACTION.value -> prepareBleGattHeader(logObject)
            InteractionType.NFC_TAG_RAW.value, InteractionType.HCE_NORMAL.value, InteractionType.HCE_NFC_F.value -> prepareNfcHeader(logObject)
            else -> ""
        }
    }

    override fun deserialize(serializedLogObject: String?): InteractionLog {
        throw RuntimeException("Print serializer is not intended to recover objects")
    }

    private fun prepareBleGattHeader(log: InteractionLog): String {
        val sb = StringBuilder()
        log.packageName
        sb.appendLine("Bluetooth communication captured")
        sb.appendLine("")
        sb.appendLine("App:")
        sb.appendLine(" - name: \"${getAppName(context, log.packageName)}\"")
        sb.appendLine(" - packageName: \"${log.packageName}\"")

        sb.appendLine("")
        sb.appendLine("Bluetooth type: BLE")

        val distinctPeersValue = log.entries
            .flatMap { listOf(it.sender, it.receiver) }
            .distinct()
            .sortedDescending()
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
        sb.appendLine("Total time spent: ${formatElapsedTimeValue(context, log.duration)}")

        return sb.toString()
    }

    private fun prepareNfcHeader(log: InteractionLog): String {
        val sb = StringBuilder()
        log.packageName
        sb.appendLine("NFC communication captured")
        sb.appendLine("")
        sb.appendLine("App:")
        sb.appendLine(" - name: \"${getAppName(context, log.packageName)}\"")
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
        sb.appendLine("Total time spent: ${formatElapsedTimeValue(context, log.duration)}")
        return sb.toString()
    }

    // todo move common logic ?
    private fun getAppName(context: Context, packageName: String): String {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0).loadLabel(context.packageManager).toString()
        } catch (e: Throwable) {
            packageName
        }
    }

    // todo move common logic ?
    private fun formatDateAndTimeValue(timestamp: Long): String {
        val logDate = Date(timestamp)
        val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT)
        val timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM)
        return "${dateFormat.format(logDate)} ${timeFormat.format(logDate)}"
    }

    // todo move common logic ?
    private fun formatElapsedTimeValue(context: Context, timeMs: Long): String {
        val list = ArrayList<String>()
        val ms = timeMs % 1000
        list.add("$ms" + context.getString(R.string.text_timeunit_ms))
        val s = (timeMs / 1000) % 60
        if (s > 0) {
            list.add("$s" + context.getString(R.string.text_timeunit_sec))
        }
        val m = (timeMs / (1000 * 60) % 60)
        if (m > 0) {
            list.add("$m" + context.getString(R.string.text_timeunit_min))
        }
        val h = (timeMs / (1000 * 60 * 60) % 24)
        if (h > 0) {
            list.add("$h" + context.getString(R.string.text_timeunit_hour))
        }
        return list.reversed().joinToString(" ")
    }
}