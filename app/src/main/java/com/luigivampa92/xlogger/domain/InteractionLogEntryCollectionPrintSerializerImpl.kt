package com.luigivampa92.xlogger.domain

class InteractionLogEntryCollectionPrintSerializerImpl (
    private val interactionType: InteractionType
) : InteractionLogEntryCollectionSerializer {

    private val logEntrySerializer: InteractionLogEntrySerializer = InteractionLogEntryPrintSerializerImpl(interactionType)

    override fun serialize(logEntries: MutableCollection<InteractionLogEntry>?): String? {
        return logEntries?.joinToString(separator = "\n") { logEntrySerializer.serialize(it) ?: "" }
    }

    override fun deserialize(serializedLogEntries: String?): MutableCollection<InteractionLogEntry> {
        throw RuntimeException("Print serializer is not intended to recover objects")
    }
}