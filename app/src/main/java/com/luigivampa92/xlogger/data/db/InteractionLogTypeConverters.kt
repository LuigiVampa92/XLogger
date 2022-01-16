package com.luigivampa92.xlogger.data.db

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.luigivampa92.xlogger.DataUtils
import com.luigivampa92.xlogger.domain.InteractionLogEntry
import com.luigivampa92.xlogger.domain.InteractionType

@TypeConverters
class InteractionLogTypeConverters {

    @TypeConverter
    fun fromInteractionType(type: InteractionType): Int = type.value

    @TypeConverter
    fun toInteractionType(value: Int): InteractionType = InteractionType.fromValue(value)

    @TypeConverter
    fun fromLogEntries(logEntries: List<InteractionLogEntry>): String {
        return logEntries.joinToString(separator = getDelimeterCharForEntries()) { serializeLogEntry(it) }
    }

    @TypeConverter
    fun toLogEntries(data: String): List<InteractionLogEntry> {
        return mutableListOf<InteractionLogEntry>().apply {
            data.split(getDelimeterCharForEntries()).forEach {
                add(deserializeLogEntry(it))
            }
        }
    }

    private fun serializeLogEntry(entry: InteractionLogEntry): String =
        listOf(
            entry.timestamp.toString(),
            entry.sender,
            entry.receiver,
            DataUtils.toHexStringLower(entry.data)
        ).joinToString(separator = getDelimeterCharForSingleEntry())

    private fun deserializeLogEntry(entry: String): InteractionLogEntry {
        val values = entry.split(getDelimeterCharForSingleEntry())
        return InteractionLogEntry(
            values[0].toLong(),
            hexStringToBytes(values[3]),
            values[1],
            values[2]
        )
    }

    private fun hexStringToBytes(value: String): ByteArray {
        return value.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    // todo different separators or universal scheme ! or GSON

    private fun getDelimeterCharForSingleEntry() = "^"

    private fun getDelimeterCharForEntries() = "*"

}