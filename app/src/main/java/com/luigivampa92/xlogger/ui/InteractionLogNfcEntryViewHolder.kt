package com.luigivampa92.xlogger.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.DataUtils
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.domain.InteractionLogEntry

class InteractionLogNfcEntryViewHolder(
    inflater: LayoutInflater,
    container: ViewGroup,
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_interaction_log_entry_nfc, container, false)) {

    private val textData: TextView

    init {
        textData = itemView.findViewById(R.id.text_entry_data)
    }

    fun bind(interactionLogEntry: InteractionLogEntry) {
        textData.text = DataUtils.toHexString(interactionLogEntry.data)
    }
}