package com.luigivampa92.xlogger.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.domain.InteractionLog

class InteractionLogAdapter (
    private val onItemClickListener: ((InteractionLog) -> Unit)? = null,
) : RecyclerView.Adapter<InteractionLogViewHolder>() {

    private val items: ArrayList<InteractionLog> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        InteractionLogViewHolder(LayoutInflater.from(parent.context), parent, onItemClickListener)

    override fun onBindViewHolder(holder: InteractionLogViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun setLogRecords(records: List<InteractionLog>) {
        items.clear()
        items.addAll(records)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): InteractionLog = items[position]

    fun insertItem(item: InteractionLog, position: Int) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}