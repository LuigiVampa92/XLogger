package com.luigivampa92.xlogger.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.domain.InteractionLogEntry
import com.luigivampa92.xlogger.domain.InteractionLogEntryAction
import com.luigivampa92.xlogger.domain.InteractionType.*

class InteractionLogEntryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: ArrayList<InteractionLogEntry> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            NFC_TAG_RAW.value -> InteractionLogNfcEntryViewHolder(LayoutInflater.from(parent.context), parent)
            BLE_GATT_INTERACTION.value -> InteractionLogBleGattEntryViewHolder(LayoutInflater.from(parent.context), parent)
            else -> throw RuntimeException("Unknown view type")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            NFC_TAG_RAW.value -> { (holder as? InteractionLogNfcEntryViewHolder)?.bind(items[position]) }
            BLE_GATT_INTERACTION.value -> { (holder as? InteractionLogBleGattEntryViewHolder)?.bind(items[position]) }
        }
    }

    // todo not good approach, mb should introduce new constants ?
    override fun getItemViewType(position: Int): Int {
        if (items[position].action.value == InteractionLogEntryAction.TRANSFER_DATA_NFC.value) {
            return NFC_TAG_RAW.value
        } else if (items[position].action.value >= 200) {
            return BLE_GATT_INTERACTION.value
        } else {
            throw RuntimeException("Unknown view type")
        }
    }

    override fun getItemCount() = items.size

    fun setItems(records: List<InteractionLogEntry>) {
        items.clear()
        items.addAll(records)
        notifyDataSetChanged()
    }
}