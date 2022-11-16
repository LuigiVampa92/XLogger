package com.luigivampa92.xlogger.ui

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.domain.InteractionLogEntryActionDirection

abstract class BaseLogEntryViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val colorNone: Int
    private  val colorFirst: Int
    private  val colorSecond: Int

    init {
        colorNone = ContextCompat.getColor(itemView.context, android.R.color.transparent)
        colorFirst = ContextCompat.getColor(itemView.context, R.color.color_log_entry_first)
        colorSecond = ContextCompat.getColor(itemView.context, R.color.color_log_entry_second)
    }

    protected fun getBackgroundColorByDirection(direction: InteractionLogEntryActionDirection) =
        when (direction) {
            InteractionLogEntryActionDirection.STRAIGHT -> colorFirst
            InteractionLogEntryActionDirection.REVERSAL -> colorSecond
            else -> colorNone
        }
}