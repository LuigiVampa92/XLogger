package com.luigivampa92.xlogger.domain

class InteractionLogEntryCollectionCharDelimeteredSerializerImpl (
    private val logEntrySerializer: InteractionLogEntrySerializer
) : InteractionLogEntryCollectionSerializer {

    override fun serialize(logEntries: MutableCollection<InteractionLogEntry>?): String? {
        return logEntries?.joinToString(separator = StringSerializationUtils.CONST_DELIMETER_ENTRIES) { logEntrySerializer.serialize(it) ?: "" }
    }

    override fun deserialize(serializedLogEntries: String?): MutableCollection<InteractionLogEntry> {
        return mutableListOf<InteractionLogEntry>().apply {
            serializedLogEntries?.split(StringSerializationUtils.CONST_DELIMETER_ENTRIES)?.forEach {
                add(logEntrySerializer.deserialize(it))
            }
        }
    }
}