package com.luigivampa92.xlogger.domain

import com.luigivampa92.xlogger.BroadcastConstants
import com.luigivampa92.xlogger.DataUtils
import com.luigivampa92.xlogger.Mappers

class InteractionLogEntryPrintSerializerImpl(
    private val interactionType: InteractionType
) : InteractionLogEntrySerializer {

    override fun serialize(logEntryObject: InteractionLogEntry?): String? {
        return logEntryObject?.let { serializePrintableLogEntry(it, interactionType) }
    }

    override fun deserialize(serializedLogEntryObject: String?): InteractionLogEntry {
        throw RuntimeException("Print serializer is not intended to recover objects")
    }

    private fun serializePrintableLogEntry(logEntry: InteractionLogEntry, type: InteractionType): String {
        return if (type.value == InteractionType.BLE_GATT_INTERACTION.value) {
            serializeBleGattEntry(logEntry)
        } else if (type.value == InteractionType.HCE_NORMAL.value || type.value == InteractionType.HCE_NFC_F.value) {
            serializeHceEntry(logEntry)
        } else if (type.value == InteractionType.NFC_TAG_RAW.value) {
            serializeNfcTagRawEntry(logEntry)
        } else {
            ""
        }
    }

    private fun serializeNfcTagRawEntry(logEntry: InteractionLogEntry): String {
        val direction = Mappers.mapActionToDirection(logEntry)
        val peerValue = if (direction == InteractionLogEntryActionDirection.STRAIGHT) BroadcastConstants.PEER_DEVICE else BroadcastConstants.PEER_CARD
        val dataValue = DataUtils.toHexString(logEntry.data)
        return String.format("%-10s %s %s", peerValue, direction.value, dataValue)
    }

    private fun serializeHceEntry(logEntry: InteractionLogEntry): String {
        val direction = Mappers.mapActionToDirection(logEntry)
        val peerValue = if (direction == InteractionLogEntryActionDirection.STRAIGHT) BroadcastConstants.PEER_DEVICE else BroadcastConstants.PEER_TERMINAL
        val dataValue = DataUtils.toHexString(logEntry.data)
        return String.format("%-10s %s %s", peerValue, direction.value, dataValue)
    }

    private fun serializeBleGattEntry(logEntry: InteractionLogEntry): String {
        val direction = Mappers.mapActionToDirection(logEntry)
        val operation = Mappers.mapActionToLogEntryString(logEntry.action)
        if (logEntry.action == InteractionLogEntryAction.BLE_CONNECT || logEntry.action == InteractionLogEntryAction.BLE_DISCONNECT) {
            return String.format("[ %s ] %s [ %s ]", logEntry.sender, operation, logEntry.receiver)
        } else {
            return String.format(
                "[ %s ] %s [ %s ] - (ServiceUUID %s CharacteristicUUID %s) %s %s",
                logEntry.sender,
                operation,
                logEntry.receiver,
                logEntry.serviceName,
                logEntry.characteristicName,
                direction.value,
                DataUtils.toHexString(logEntry.data)
            )
        }
    }
}