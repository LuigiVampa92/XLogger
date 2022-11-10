package com.luigivampa92.xlogger.data.db

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.luigivampa92.xlogger.BroadcastConstants
import com.luigivampa92.xlogger.DataUtils
import com.luigivampa92.xlogger.domain.InteractionLogEntry
import com.luigivampa92.xlogger.domain.InteractionLogEntryAction
import com.luigivampa92.xlogger.domain.InteractionType

@TypeConverters
class InteractionLogTypeConverters {

    @TypeConverter
    fun fromInteractionType(type: InteractionType): Int = type.value

    @TypeConverter
    fun toInteractionType(value: Int): InteractionType = InteractionType.fromValue(value)

    @TypeConverter
    fun fromLogEntryAction(type: InteractionLogEntryAction): Int = type.value

    @TypeConverter
    fun toLogEntryAction(value: Int): InteractionLogEntryAction = InteractionLogEntryAction.fromValue(value)

    @TypeConverter
    fun fromLogEntries(logEntries: List<InteractionLogEntry>): String {
        return logEntries.joinToString(separator = CONST_DELIMETER_ENTRIES) { serializeLogEntry(it) }
    }

    @TypeConverter
    fun toLogEntries(data: String): List<InteractionLogEntry> {
        return mutableListOf<InteractionLogEntry>().apply {
            data.split(CONST_DELIMETER_ENTRIES).forEach {
                add(deserializeLogEntry(it))
            }
        }
    }

    private fun serializeLogEntry(entry: InteractionLogEntry): String =
        listOf(
            entry.timestamp.toString(),
            entry.action.value.toString(),
            if (entry.sender != null) entry.sender.toString() else CONST_VALUE_NULL,
            if (entry.receiver != null) entry.receiver.toString() else CONST_VALUE_NULL,
            if (entry.serviceName != null) entry.serviceName.toString() else CONST_VALUE_NULL,
            if (entry.characteristicName != null) entry.characteristicName.toString() else CONST_VALUE_NULL,
            if (entry.data != null) DataUtils.toHexStringLower(entry.data) else CONST_VALUE_NULL,
            if (entry.message != null) entry.message.toString() else CONST_VALUE_NULL
        ).joinToString(separator = CONST_DELIMETER_SINGLE_ENTRY)

    private fun deserializeLogEntry(entry: String): InteractionLogEntry {
        val values: List<String> = entry.split(CONST_DELIMETER_SINGLE_ENTRY)
        return InteractionLogEntry(
            if (values[0].isNullOrBlank() || values[0] == CONST_VALUE_NULL) 0L else values[0].toLong(),
            if (values[1].isNullOrBlank() || values[1] == CONST_VALUE_NULL) InteractionLogEntryAction.UNKNOWN_EVENT else InteractionLogEntryAction.fromValue(values[1].toInt()),
            if (values[6].isNullOrBlank() || values[6] == CONST_VALUE_NULL) byteArrayOf() else hexStringToBytes(values[6]),
            if (values[7].isNullOrBlank() || values[7] == CONST_VALUE_NULL) null else values[7],
            if (values[2].isNullOrBlank() || values[2] == CONST_VALUE_NULL) BroadcastConstants.PEER_UNKNOWN else values[2],
            if (values[3].isNullOrBlank() || values[3] == CONST_VALUE_NULL) BroadcastConstants.PEER_UNKNOWN else values[3],
            if (values[4].isNullOrBlank() || values[4] == CONST_VALUE_NULL) null else values[4],
            if (values[5].isNullOrBlank() || values[5] == CONST_VALUE_NULL) null else values[5]
        )
    }

    private fun hexStringToBytes(value: String): ByteArray {
        return value.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    private companion object {
        private const val CONST_VALUE_NULL = "null"
        private const val CONST_DELIMETER_ENTRIES = "*"
        private const val CONST_DELIMETER_SINGLE_ENTRY = "^"
    }
}