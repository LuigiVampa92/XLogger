package com.luigivampa92.xlogger.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.domain.InteractionLog
import com.luigivampa92.xlogger.domain.InteractionLogEntryAction
import com.luigivampa92.xlogger.domain.InteractionType.*

class InteractionLogDetailsAdapter (
    private val onShareClickListener: ((InteractionLog) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        private const val VIEW_TYPE_NFC_ENTRY = 1
        private const val VIEW_TYPE_NFC_HEADER = 2
        private const val VIEW_TYPE_BLE_GATT_ENTRY = 10
        private const val VIEW_TYPE_BLE_GATT_HEADER = 11
    }

    private var record: InteractionLog? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_NFC_ENTRY -> InteractionLogNfcEntryViewHolder(LayoutInflater.from(parent.context), parent)
            VIEW_TYPE_NFC_HEADER -> InteractionLogNfcHeaderViewHolder(LayoutInflater.from(parent.context), parent, onShareClickListener)
            VIEW_TYPE_BLE_GATT_ENTRY -> InteractionLogBleGattEntryViewHolder(LayoutInflater.from(parent.context), parent)
            VIEW_TYPE_BLE_GATT_HEADER -> InteractionLogBleGattHeaderViewHolder(LayoutInflater.from(parent.context), parent, onShareClickListener)
            else -> throw RuntimeException("Unknown view type")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (record != null) {
            when (holder.itemViewType) {
                VIEW_TYPE_NFC_ENTRY -> { (holder as? InteractionLogNfcEntryViewHolder)?.bind(record!!.entries[position - 1]) }
                VIEW_TYPE_NFC_HEADER -> { (holder as? InteractionLogNfcHeaderViewHolder)?.bind(record!!) }
                VIEW_TYPE_BLE_GATT_ENTRY -> { (holder as? InteractionLogBleGattEntryViewHolder)?.bind(record!!.entries[position - 1]) }
                VIEW_TYPE_BLE_GATT_HEADER -> { (holder as? InteractionLogBleGattHeaderViewHolder)?.bind(record!!) }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (record != null) {
            if (position == 0) {
                if (NFC_TAG_RAW == record!!.type || HCE_NORMAL == record!!.type || HCE_NFC_F == record!!.type) {
                    return VIEW_TYPE_NFC_HEADER
                } else if (BLE_GATT_INTERACTION == record!!.type) {
                    return VIEW_TYPE_BLE_GATT_HEADER
                } else {
                    throw RuntimeException("Unknown header view type")
                }
            }
            else {
                if (record!!.entries[position - 1].action.value == InteractionLogEntryAction.TRANSFER_DATA_NFC.value) {
                    return VIEW_TYPE_NFC_ENTRY
                } else if (record!!.entries[position - 1].action.value >= 200) {
                    return VIEW_TYPE_BLE_GATT_ENTRY
                } else {
                    throw RuntimeException("Unknown entry view type")
                }
            }
        } else {
            throw RuntimeException("Error. Record is not set")
        }
    }

    override fun getItemCount() = if (record != null) 1 + record!!.entries.size else 0

    fun setRecord(record: InteractionLog) {
        this.record = record
        notifyDataSetChanged()
    }
}