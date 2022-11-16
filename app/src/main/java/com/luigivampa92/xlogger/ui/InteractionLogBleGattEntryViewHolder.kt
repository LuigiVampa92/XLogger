package com.luigivampa92.xlogger.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.BroadcastConstants
import com.luigivampa92.xlogger.DataUtils
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.domain.InteractionLogEntry
import com.luigivampa92.xlogger.domain.InteractionLogEntryAction

class InteractionLogBleGattEntryViewHolder(
    inflater: LayoutInflater,
    container: ViewGroup,
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_interaction_log_entry_ble_gatt, container, false)) {

    private val viewBackground: View
    private val textSender: TextView
    private val textOperation: TextView
    private val textReceiver: TextView
    private val textGattService: TextView
    private val textGattCharacteristic: TextView
    private val textDirection: TextView
    private val textData: TextView

    private val colorNone: Int
    private val colorFirst: Int
    private val colorSecond: Int

    private val directionStraight = "-->"
    private val directionReversal = "<--"
    private val directionUndefined = ""

    init {
        viewBackground = itemView.findViewById(R.id.view_entry_background)
        textSender = itemView.findViewById(R.id.text_entry_sender)
        textOperation = itemView.findViewById(R.id.text_entry_operation)
        textReceiver = itemView.findViewById(R.id.text_entry_receiver)
        textGattService = itemView.findViewById(R.id.text_entry_service_uuid)
        textGattCharacteristic = itemView.findViewById(R.id.text_entry_characteristic_uuid)
        textDirection = itemView.findViewById(R.id.text_entry_direction)
        textData = itemView.findViewById(R.id.text_entry_data)
        colorNone = ContextCompat.getColor(itemView.context, android.R.color.transparent)
        colorFirst = ContextCompat.getColor(itemView.context, R.color.color_log_entry_first)
        colorSecond = ContextCompat.getColor(itemView.context, R.color.color_log_entry_second)
    }

    fun bind(logEntry: InteractionLogEntry) {
        viewBackground.setBackgroundColor(getBackgroundColorValue(logEntry))
        textSender.text = logEntry.sender
        textOperation.text = getOperationValue(logEntry)
        textReceiver.text = logEntry.receiver
        textDirection.text = getDirectionStringValue(logEntry)
        textData.text = DataUtils.toHexString(logEntry.data)
        if (!logEntry.serviceName.isNullOrBlank()) {
            textGattService.text = String.format("ServiceUUID: %s", logEntry.serviceName)
            textGattService.visibility = View.VISIBLE
        } else {
            textGattService.visibility = View.GONE
        }
        if (!logEntry.characteristicName.isNullOrBlank()) {
            textGattCharacteristic.text = String.format("CharacteristicUUID: %s", logEntry.characteristicName)
            textGattCharacteristic.visibility = View.VISIBLE
        } else {
            textGattCharacteristic.visibility = View.GONE
        }
    }


    // todo refactor this bullshit:
    private fun getDirectionStringValue(logEntry: InteractionLogEntry): String {
        return if (InteractionLogEntryAction.BLE_CONNECT == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            directionStraight
        } else if (InteractionLogEntryAction.BLE_CONNECT == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            directionReversal
        } else if (InteractionLogEntryAction.BLE_DISCONNECT == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            directionStraight
        } else if (InteractionLogEntryAction.BLE_DISCONNECT == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            directionReversal
        } else if (InteractionLogEntryAction.BLE_READ == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            directionStraight
        } else if (InteractionLogEntryAction.BLE_READ == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            directionReversal
        } else if (InteractionLogEntryAction.BLE_WRITE == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            directionStraight
        } else if (InteractionLogEntryAction.BLE_WRITE == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            directionReversal
        } else if (InteractionLogEntryAction.BLE_NOTIFY == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            directionStraight
        } else if (InteractionLogEntryAction.BLE_NOTIFY == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            directionReversal
        } else {
            directionUndefined
        }
    }

    private fun getBackgroundColorValue(logEntry: InteractionLogEntry): Int {
        return if (InteractionLogEntryAction.BLE_CONNECT == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            colorFirst
        } else if (InteractionLogEntryAction.BLE_CONNECT == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            colorSecond
        } else if (InteractionLogEntryAction.BLE_DISCONNECT == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            colorFirst
        } else if (InteractionLogEntryAction.BLE_DISCONNECT == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            colorSecond
        } else if (InteractionLogEntryAction.BLE_READ == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            colorFirst
        } else if (InteractionLogEntryAction.BLE_READ == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            colorSecond
        } else if (InteractionLogEntryAction.BLE_WRITE == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            colorFirst
        } else if (InteractionLogEntryAction.BLE_WRITE == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            colorSecond
        } else if (InteractionLogEntryAction.BLE_NOTIFY == logEntry.action && logEntry.sender == BroadcastConstants.PEER_THIS_DEVICE) {
            colorFirst
        } else if (InteractionLogEntryAction.BLE_NOTIFY == logEntry.action && logEntry.receiver == BroadcastConstants.PEER_THIS_DEVICE) {
            colorSecond
        } else {
            colorNone
        }
    }

    private fun getOperationValue(logEntry: InteractionLogEntry): String {
        return if (InteractionLogEntryAction.BLE_CONNECT == logEntry.action) {
            "CONNECTS TO"
        }
        else if (InteractionLogEntryAction.BLE_DISCONNECT == logEntry.action) {
            "DISCONNECTS FROM"
        }
        else if (InteractionLogEntryAction.BLE_READ == logEntry.action) {
            "READS FROM"
        }
        else if (InteractionLogEntryAction.BLE_WRITE == logEntry.action) {
            "WRITES TO"
        }
        else if (InteractionLogEntryAction.BLE_NOTIFY == logEntry.action) {
            "NOTIFIES"
        }
        else {
            "???"
        }
    }
}