package com.luigivampa92.xlogger.domain

import android.content.Context

class InteractionLogPrintSerializerImpl(
    private val context: Context
) : InteractionLogSerializer {

    private val divider = "   --------------------   "

    override fun serialize(logObject: InteractionLog?): String {
        if (logObject == null) return ""
        val headerSerializer = InteractionLogPrintHeaderSerializerImpl(context)
        val entriesSerializer = InteractionLogEntryCollectionPrintSerializerImpl(logObject.type)
        return String.format("%s\n\n%s\n\n%s", headerSerializer.serialize(logObject), divider, entriesSerializer.serialize(logObject.entries))
    }

    override fun deserialize(serializedLogObject: String?): InteractionLog {
        throw RuntimeException("Print serializer is not intended to recover objects")
    }
}