package com.luigivampa92.xlogger.ui

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.nfc.tech.IsoDep
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.domain.InteractionLog
import com.luigivampa92.xlogger.domain.InteractionType
import java.text.DateFormat
import java.util.*

// todo texts for payment card, nearby, transit card etc ??

class InteractionLogViewHolder (
    inflater: LayoutInflater,
    container: ViewGroup,
    private val onItemClickListener: ((InteractionLog) -> Unit)? = null,
    private val onShareClickListener: ((InteractionLog) -> Unit)? = null,
    private val onDeleteClickListener: ((InteractionLog) -> Unit)? = null,
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_interaction_log, container, false)) {

    val viewForeground: ConstraintLayout
    private val imageAppIcon: ImageView
    private val textAppName: TextView
    private val imageIfaceBackground: ImageView
    private val imageIfaceIcon: ImageView
    private val textLogTitle: TextView
    private val textLogDate: TextView
    private val textLogTime: TextView

    init {
        viewForeground = itemView.findViewById(R.id.view_foreground)
        imageAppIcon = itemView.findViewById(R.id.img_app_icon)
        textAppName = itemView.findViewById(R.id.text_app_name)
        imageIfaceBackground = itemView.findViewById(R.id.img_type_iface_background)
        imageIfaceIcon = itemView.findViewById(R.id.img_type_iface_icon)
        textLogTitle = itemView.findViewById(R.id.text_log_title)
        textLogDate = itemView.findViewById(R.id.text_log_date)
        textLogTime = itemView.findViewById(R.id.text_log_time)
    }

    fun bind(log: InteractionLog) {
        val context = itemView.context

        setAppData(context, log)

        if (InteractionType.BLE_GATT_INTERACTION == log.type) {
            val interactionTypeBackgroundColor = ContextCompat.getColor(context, R.color.color_red)
            imageIfaceBackground.setImageTintList(ColorStateList.valueOf(interactionTypeBackgroundColor))
            imageIfaceBackground.visibility = View.VISIBLE
            val interactionTypeIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_interaction_type_bluetooth)
            imageIfaceIcon.setImageDrawable(interactionTypeIconDrawable)
            imageIfaceIcon.visibility = View.VISIBLE
        } else if (InteractionType.NFC_TAG_RAW == log.type || InteractionType.HCE_NORMAL == log.type || InteractionType.HCE_NFC_F == log.type) {
            val interactionTypeBackgroundColor = ContextCompat.getColor(context, R.color.color_red)
            imageIfaceBackground.setImageTintList(ColorStateList.valueOf(interactionTypeBackgroundColor))
            imageIfaceBackground.visibility = View.VISIBLE
            val interactionTypeIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_interaction_type_nfc)
            imageIfaceIcon.setImageDrawable(interactionTypeIconDrawable)
            imageIfaceIcon.visibility = View.VISIBLE
        } else {
            imageIfaceIcon.visibility = View.GONE
            imageIfaceBackground.visibility = View.GONE
        }

        textLogTitle.text = getTitleText(log)

        val logDate = Date(log.timestamp)
        val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT)
        val timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM)
        textLogDate.text = dateFormat.format(logDate)
        textLogTime.text = timeFormat.format(logDate)

        viewForeground.setOnClickListener {
            onItemClickListener?.invoke(log)
        }
        viewForeground.setOnLongClickListener {
            val popup = PopupMenu(itemView.context, viewForeground)
            popup.inflate(R.menu.interaction_log_options_menu)
            popup.setOnMenuItemClickListener { item ->
                when (item?.title) {
                    itemView.context.getString(R.string.text_interaction_log_menu_item_share) -> { onShareClickListener?.invoke(log) }
                    itemView.context.getString(R.string.text_interaction_log_menu_item_delete) -> { onDeleteClickListener?.invoke(log) }
                }
                true
            }
            popup.show()
            true
        }
    }

    private fun getTitleText(log: InteractionLog): String {
        return when (log.type) {
            InteractionType.BLE_GATT_INTERACTION -> itemView.context.getString(R.string.text_interaction_log_title_ble_gatt)
            InteractionType.NFC_TAG_RAW -> if (log.metadata.isNullOrBlank() || log.metadata == IsoDep::class.java.simpleName) {
                itemView.context.getString(R.string.text_interaction_log_title_read_card)
            } else {
                itemView.context.getString(R.string.text_interaction_log_title_read_card_specific, log.metadata)
            }
            InteractionType.HCE_NORMAL -> itemView.context.getString(R.string.text_interaction_log_title_hce)
            InteractionType.HCE_NFC_F -> itemView.context.getString(R.string.text_interaction_log_title_hce_f)
            else -> itemView.context.getString(R.string.text_interaction_log_title_unknown)
        }
    }

    private fun setAppData(context: Context, log: InteractionLog) {
        val packageManager = context.packageManager
        try {
            val applicationInfo = packageManager.getApplicationInfo(log.packageName, 0)
            val appName = applicationInfo.loadLabel(packageManager).toString()
            val appIcon: Drawable = applicationInfo.loadIcon(packageManager)

            // an exception for google pay
            // since its actual hce service is inside google services package, NOT the google pay package itself
            // package data are replaced with google pay for more clarity and convenience
            if (
                "com.google.android.gms" == log.packageName
                && "com.google.android.gms.tapandpay.hce.service.TpHceChimeraService" == log.metadata
            ) {
                try {
                    val gpayInfo = packageManager.getApplicationInfo("com.google.android.apps.walletnfcrel", 0)
                    val gpayIcon: Drawable = gpayInfo.loadIcon(packageManager)
                    val gpayName = gpayInfo.loadLabel(packageManager).toString()
                    imageAppIcon.setImageDrawable(gpayIcon)
                    textAppName.setText(gpayName)
                } catch (e: PackageManager.NameNotFoundException) {
                    imageAppIcon.setImageDrawable(appIcon)
                    textAppName.setText(appName)
                }
            } else {
                imageAppIcon.setImageDrawable(appIcon)
                textAppName.setText(appName)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            val defaultIcon = ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)
            val defaultName = log.packageName
            imageAppIcon.setImageDrawable(defaultIcon)
            textAppName.setText(defaultName)
        }
    }
}