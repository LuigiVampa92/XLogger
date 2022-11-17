package com.luigivampa92.xlogger

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.luigivampa92.xlogger.domain.InteractionLog
import com.luigivampa92.xlogger.domain.InteractionLogPrintSerializerImpl
import com.luigivampa92.xlogger.domain.InteractionLogSerializer
import com.luigivampa92.xlogger.domain.InteractionType
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class InteractionLogSender (private val context: Context) {

    private val serializer: InteractionLogSerializer = InteractionLogPrintSerializerImpl(context)

    fun sendLog(log: InteractionLog) {
        try {
            val logText = serializer.serialize(log)
            val logFileName = getFileNameForLog(log)
            val logFile = ensureAndGetFile(logFileName)
            saveLogTextToFile(logText, logFile)
            val logFileShareIntent = getLogFileShareIntent(logFile)
            val chooserIntent = Intent.createChooser(logFileShareIntent, context.getString(R.string.text_share_log_intent_chooser_message))
            context.startActivity(chooserIntent)
        } catch (e: Throwable) {
        }
    }

    private fun getLogFileShareIntent(file: File): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "*/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.text_share_log_intent_subject))
        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.text_share_log_intent_message))
        val fileURI = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
        intent.putExtra(Intent.EXTRA_STREAM, fileURI)
        return intent
    }

    private fun saveLogTextToFile(logText: String, file: File) {
        FileWriter(file).use { it.write(logText) }
    }

    private fun ensureAndGetFile(fileName: String): File {
        val transferredFile = File(ensureAndGetShareDir(context), fileName)
        if (transferredFile.exists() && transferredFile.isDirectory) {
            transferredFile.deleteRecursively()
            transferredFile.createNewFile()
        } else if (transferredFile.exists() && !transferredFile.isDirectory) {
            transferredFile.delete()
            transferredFile.createNewFile()
        }
        return transferredFile
    }

    private fun ensureAndGetShareDir(context: Context): File {
        val dir = File(context.filesDir, FILE_SHARE_DIR)
        if (!dir.exists()) {
            dir.mkdir()
        }
        if (!dir.isDirectory) {
            dir.delete()
            dir.mkdir()
        }
        return dir
    }

    private fun getFileNameForLog(log: InteractionLog): String {
        return FILE_NAME_PREFIX + "_" + getTypeSuffix(log.type) + getTimeSuffix(log.timestamp) + FILE_NAME_EXTENSION
    }

    private fun getTimeSuffix(timestamp: Long): String {
        return SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(Date(timestamp))
    }

    private fun getTypeSuffix(type: InteractionType): String =
        when (type) {
            InteractionType.BLE_GATT_INTERACTION -> "ble_"
            InteractionType.NFC_TAG_RAW, InteractionType.HCE_NORMAL, InteractionType.HCE_NFC_F -> "nfc_"
            else -> ""
        }

    private companion object {
        private const val FILE_SHARE_DIR = "share"
        private const val FILE_NAME_EXTENSION = ".txt"
        private const val FILE_NAME_PREFIX = "xlog"
    }
}