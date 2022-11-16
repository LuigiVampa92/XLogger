package com.luigivampa92.xlogger

import com.luigivampa92.xlogger.domain.InteractionLogEntry
import com.luigivampa92.xlogger.domain.InteractionLogEntryAction
import com.luigivampa92.xlogger.domain.InteractionLogEntryActionDirection

object Mappers {

    fun mapActionToDirection(logEntry: InteractionLogEntry): InteractionLogEntryActionDirection {
        return if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_DEVICE && logEntry.receiver == BroadcastConstants.PEER_CARD) {
            InteractionLogEntryActionDirection.STRAIGHT
        } else if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_CARD && logEntry.receiver == BroadcastConstants.PEER_DEVICE) {
            InteractionLogEntryActionDirection.REVERSAL
        } else if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_TERMINAL && logEntry.receiver == BroadcastConstants.PEER_DEVICE) {
            InteractionLogEntryActionDirection.REVERSAL
        } else if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_DEVICE && logEntry.receiver == BroadcastConstants.PEER_TERMINAL) {
            InteractionLogEntryActionDirection.STRAIGHT
        }

        else if (InteractionLogEntryAction.BLE_CONNECT == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            InteractionLogEntryActionDirection.STRAIGHT
        } else if (InteractionLogEntryAction.BLE_CONNECT == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            InteractionLogEntryActionDirection.REVERSAL
        } else if (InteractionLogEntryAction.BLE_DISCONNECT == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            InteractionLogEntryActionDirection.STRAIGHT
        } else if (InteractionLogEntryAction.BLE_DISCONNECT == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            InteractionLogEntryActionDirection.REVERSAL
        }

        else if (InteractionLogEntryAction.BLE_READ == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            InteractionLogEntryActionDirection.STRAIGHT
        } else if (InteractionLogEntryAction.BLE_READ == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            InteractionLogEntryActionDirection.REVERSAL
        } else if (InteractionLogEntryAction.BLE_WRITE == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            InteractionLogEntryActionDirection.STRAIGHT
        } else if (InteractionLogEntryAction.BLE_WRITE == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            InteractionLogEntryActionDirection.REVERSAL
        } else if (InteractionLogEntryAction.BLE_NOTIFY == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            InteractionLogEntryActionDirection.STRAIGHT
        } else if (InteractionLogEntryAction.BLE_NOTIFY == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            InteractionLogEntryActionDirection.REVERSAL
        }

        else {
            InteractionLogEntryActionDirection.UNDEFINED
        }
    }

    fun mapActionToLogEntryString(logEntryAction: InteractionLogEntryAction): String {
        return if (InteractionLogEntryAction.BLE_CONNECT == logEntryAction) {
            "CONNECTS TO"
        }
        else if (InteractionLogEntryAction.BLE_DISCONNECT == logEntryAction) {
            "DISCONNECTS FROM"
        }
        else if (InteractionLogEntryAction.BLE_READ == logEntryAction) {
            "READS FROM"
        }
        else if (InteractionLogEntryAction.BLE_WRITE == logEntryAction) {
            "WRITES TO"
        }
        else if (InteractionLogEntryAction.BLE_NOTIFY == logEntryAction) {
            "NOTIFIES"
        }
        else {
            ""
        }
    }
}