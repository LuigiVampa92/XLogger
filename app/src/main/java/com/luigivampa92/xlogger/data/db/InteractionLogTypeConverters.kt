package com.luigivampa92.xlogger.data.db

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.luigivampa92.xlogger.domain.*

@TypeConverters
class InteractionLogTypeConverters {

    private val logEntryCollectionSerializer: InteractionLogEntryCollectionSerializer =
        InteractionLogEntryCollectionCharDelimeteredSerializerImpl(InteractionLogEntryCompactSerializerImpl())

    @TypeConverter
    fun fromInteractionType(type: InteractionType): Int = type.value

    @TypeConverter
    fun toInteractionType(value: Int): InteractionType = InteractionType.fromValue(value)

    @TypeConverter
    fun fromLogEntryAction(type: InteractionLogEntryAction): Int = type.value

    @TypeConverter
    fun toLogEntryAction(value: Int): InteractionLogEntryAction = InteractionLogEntryAction.fromValue(value)

    @TypeConverter
    fun fromLogEntries(logEntries: List<InteractionLogEntry>): String = logEntryCollectionSerializer.serialize(logEntries)

    @TypeConverter
    fun toLogEntries(data: String): List<InteractionLogEntry> = logEntryCollectionSerializer.deserialize(data).toList()

}