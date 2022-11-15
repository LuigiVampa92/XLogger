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

class InteractionLogNfcEntryViewHolder(
    inflater: LayoutInflater,
    container: ViewGroup,
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_interaction_log_entry_nfc, container, false)) {

    private val viewBackground: View
    private val textPeer: TextView
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
        textPeer = itemView.findViewById(R.id.text_entry_peer)
        textDirection = itemView.findViewById(R.id.text_entry_direction)
        textData = itemView.findViewById(R.id.text_entry_data)
        colorNone = ContextCompat.getColor(itemView.context, android.R.color.transparent)
        colorFirst = ContextCompat.getColor(itemView.context, R.color.color_log_entry_first)
        colorSecond = ContextCompat.getColor(itemView.context, R.color.color_log_entry_second)
    }

    fun bind(logEntry: InteractionLogEntry) {
        viewBackground.setBackgroundColor(getBackgroundColorValue(logEntry))
        textPeer.text = logEntry.sender
        textDirection.text = getDirectionStringValue(logEntry)
        textData.text = DataUtils.toHexString(logEntry.data)
    }

    private fun getDirectionStringValue(logEntry: InteractionLogEntry): String {
        return if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_DEVICE && logEntry.receiver == BroadcastConstants.PEER_CARD) {
            directionStraight
        } else if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_CARD && logEntry.receiver == BroadcastConstants.PEER_DEVICE) {
            directionReversal
        } else if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_TERMINAL && logEntry.receiver == BroadcastConstants.PEER_DEVICE) {
            directionReversal
        } else if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_DEVICE && logEntry.receiver == BroadcastConstants.PEER_TERMINAL) {
            directionStraight
        } else {
            directionUndefined
        }
    }

    private fun getBackgroundColorValue(logEntry: InteractionLogEntry): Int {
        return if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_DEVICE && logEntry.receiver == BroadcastConstants.PEER_CARD) {
            colorFirst
        } else if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_CARD && logEntry.receiver == BroadcastConstants.PEER_DEVICE) {
            colorSecond
        } else if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_TERMINAL && logEntry.receiver == BroadcastConstants.PEER_DEVICE) {
            colorSecond
        } else if (InteractionLogEntryAction.TRANSFER_DATA_NFC == logEntry.action && logEntry.sender == BroadcastConstants.PEER_DEVICE && logEntry.receiver == BroadcastConstants.PEER_TERMINAL) {
            colorFirst
        } else {
            colorNone
        }
    }
}