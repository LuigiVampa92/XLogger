package com.luigivampa92.xlogger.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.domain.InteractionLog

class InteractionLogNfcHeaderViewHolder (
    inflater: LayoutInflater,
    container: ViewGroup,
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_interaction_log_header_nfc, container, false)) {

    fun bind(log: InteractionLog) {

    }
}
