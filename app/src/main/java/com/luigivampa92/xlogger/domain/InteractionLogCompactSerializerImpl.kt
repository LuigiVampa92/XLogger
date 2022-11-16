package com.luigivampa92.xlogger.domain

class InteractionLogCompactSerializerImpl : InteractionLogSerializer {

    private val entrySerializer: InteractionLogEntrySerializer
    private val entryCollectionSerializer: InteractionLogEntryCollectionSerializer

    init {
        entrySerializer = InteractionLogEntryCompactSerializerImpl()
        entryCollectionSerializer = InteractionLogEntryCollectionCharDelimeteredSerializerImpl(entrySerializer)
    }

    override fun serialize(logObject: InteractionLog?): String? {
        return serializeLog(logObject ?: return null)
    }

    override fun deserialize(serializedLogObject: String?): InteractionLog? {
        return deserializeLog(serializedLogObject ?: return null)
    }

    private fun serializeLog(logObject: InteractionLog): String =
        listOf(
            if (logObject.type != null) logObject.type.value.toString() else StringSerializationUtils.CONST_VALUE_NULL,
            if (logObject.packageName != null) logObject.packageName.toString() else StringSerializationUtils.CONST_VALUE_NULL,
            if (logObject.metadata != null) logObject.metadata.toString() else StringSerializationUtils.CONST_VALUE_NULL,
            entryCollectionSerializer.serialize(logObject.entries)
        ).joinToString(separator = StringSerializationUtils.CONST_DELIMETER_LOG_OBJECT_ELEMENT)

    private fun deserializeLog(serializedObject: String): InteractionLog {
        val values: List<String> = serializedObject.split(StringSerializationUtils.CONST_DELIMETER_LOG_OBJECT_ELEMENT)
        return InteractionLog(
            if (values[0].isNullOrBlank() || values[0] == StringSerializationUtils.CONST_VALUE_NULL) InteractionType.GENERAL_LOG else InteractionType.fromValue(values[0].toInt()),
            if (values[1].isNullOrBlank() || values[1] == StringSerializationUtils.CONST_VALUE_NULL) null else values[1],
            if (values[2].isNullOrBlank() || values[2] == StringSerializationUtils.CONST_VALUE_NULL) null else values[2],
            if (values[3].isNullOrBlank() || values[3] == StringSerializationUtils.CONST_VALUE_NULL) null else entryCollectionSerializer.deserialize(values[3]).toList()
        )
    }
}
