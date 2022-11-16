package com.luigivampa92.xlogger.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.luigivampa92.xlogger.DataUtils
import com.luigivampa92.xlogger.Mappers
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.domain.InteractionLogEntry

class InteractionLogNfcEntryViewHolder(
    inflater: LayoutInflater,
    container: ViewGroup,
) : BaseLogEntryViewHolder(inflater.inflate(R.layout.item_interaction_log_entry_nfc, container, false)) {

    private val viewBackground: View
    private val textPeer: TextView
    private val textDirection: TextView
    private val textData: TextView

    init {
        viewBackground = itemView.findViewById(R.id.view_entry_background)
        textPeer = itemView.findViewById(R.id.text_entry_peer)
        textDirection = itemView.findViewById(R.id.text_entry_direction)
        textData = itemView.findViewById(R.id.text_entry_data)
    }

    fun bind(logEntry: InteractionLogEntry) {
        val direction = Mappers.mapActionToDirection(logEntry)
        viewBackground.setBackgroundColor(getBackgroundColorByDirection(direction))
        textPeer.text = logEntry.sender
        textDirection.text = direction.value
        textData.text = DataUtils.toHexString(logEntry.data)
    }
}