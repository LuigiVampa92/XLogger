package com.luigivampa92.xlogger.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.domain.InteractionLog
import com.luigivampa92.xlogger.domain.InteractionLogPrintHeaderSerializerImpl
import com.luigivampa92.xlogger.domain.InteractionLogSerializer

class InteractionLogBleGattHeaderViewHolder (
    inflater: LayoutInflater,
    container: ViewGroup,
    private val onShareClickListener: ((InteractionLog) -> Unit)? = null
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_interaction_log_header_ble_gatt, container, false)) {

    private val textHeader: TextView
    private val buttonShare: View
    private val headerSerializer: InteractionLogSerializer

    init {
        textHeader = itemView.findViewById(R.id.text_ble_header_data)
        buttonShare = itemView.findViewById(R.id.button_share_log)
        headerSerializer = InteractionLogPrintHeaderSerializerImpl(itemView.context)
    }

    fun bind(log: InteractionLog) {
        textHeader.text = headerSerializer.serialize(log)
        buttonShare.setOnClickListener { onShareClickListener?.invoke(log) }
    }
}