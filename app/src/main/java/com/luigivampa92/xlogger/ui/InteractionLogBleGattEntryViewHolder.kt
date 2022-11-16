package com.luigivampa92.xlogger.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.luigivampa92.xlogger.DataUtils
import com.luigivampa92.xlogger.Mappers
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.domain.InteractionLogEntry

class InteractionLogBleGattEntryViewHolder(
    inflater: LayoutInflater,
    container: ViewGroup,
) : BaseLogEntryViewHolder(inflater.inflate(R.layout.item_interaction_log_entry_ble_gatt, container, false)) {

    private val viewBackground: View
    private val textSender: TextView
    private val textOperation: TextView
    private val textReceiver: TextView
    private val textGattService: TextView
    private val textGattCharacteristic: TextView
    private val textDirection: TextView
    private val textData: TextView

    init {
        viewBackground = itemView.findViewById(R.id.view_entry_background)
        textSender = itemView.findViewById(R.id.text_entry_sender)
        textOperation = itemView.findViewById(R.id.text_entry_operation)
        textReceiver = itemView.findViewById(R.id.text_entry_receiver)
        textGattService = itemView.findViewById(R.id.text_entry_service_uuid)
        textGattCharacteristic = itemView.findViewById(R.id.text_entry_characteristic_uuid)
        textDirection = itemView.findViewById(R.id.text_entry_direction)
        textData = itemView.findViewById(R.id.text_entry_data)
    }

    fun bind(logEntry: InteractionLogEntry) {
        val direction = Mappers.mapActionToDirection(logEntry)
        viewBackground.setBackgroundColor(getBackgroundColorByDirection(direction))
        textSender.setTextOrHide(logEntry.sender)
        textOperation.setTextOrHide(Mappers.mapActionToLogEntryString(logEntry.action))
        textReceiver.setTextOrHide(logEntry.receiver)
        textDirection.text = direction.value
        textData.text = DataUtils.toHexString(logEntry.data)
        textGattService.setTextOrHide(logEntry.serviceName, "ServiceUUID: ")
        textGattCharacteristic.setTextOrHide(logEntry.characteristicName, "CharacteristicUUID: ")
    }

    private fun TextView.setTextOrHide(value: String?, prefix: String? = null) {
        if (!value.isNullOrBlank()) {
            if (!prefix.isNullOrBlank()) {
                this.text = prefix + value
            } else {
                this.text = value
            }
            this.visibility = View.VISIBLE
        } else {
            this.visibility = View.GONE
        }
    }
}