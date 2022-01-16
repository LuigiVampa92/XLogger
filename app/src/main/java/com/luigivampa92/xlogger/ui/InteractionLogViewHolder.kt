package com.luigivampa92.xlogger.ui

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.luigivampa92.xlogger.R
import com.luigivampa92.xlogger.domain.InteractionLog
import com.luigivampa92.xlogger.domain.InteractionType
import java.text.DateFormat
import java.util.*

class InteractionLogViewHolder (
    inflater: LayoutInflater,
    container: ViewGroup,
    private val onItemClickListener: ((InteractionLog) -> Unit)? = null,
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_interaction_log, container, false)) {

    // todo fix markup

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

        val interactionTypeBackgroundColor = ContextCompat.getColor(context, R.color.color_red)
        imageIfaceBackground.setImageTintList(ColorStateList.valueOf(interactionTypeBackgroundColor))
        imageIfaceBackground.visibility = View.VISIBLE

        val interactionTypeIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_interaction_type_nfc)
        imageIfaceIcon.setImageDrawable(interactionTypeIconDrawable)
        imageIfaceIcon.visibility = View.VISIBLE

        textLogTitle.text = getTitleText(log)

        val logDate = Date(log.timestamp)
        val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT)
        val timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM)
        textLogDate.text = dateFormat.format(logDate)
        textLogTime.text = timeFormat.format(logDate)

        viewForeground.setOnClickListener {
            onItemClickListener?.invoke(log)
        }
    }

    // todo texts for payment card, nearby, transit card etc
    // todo isodep is not text
    // todo nfc f emulation - separated

    private fun getTitleText(log: InteractionLog): String {
        return when (log.type) {
            InteractionType.NFC_TAG_RAW -> "Read ${log.serviceName} NFC card"
            else -> "Emulate NFC card"
        }
    }

    // todo common logic

    private fun setAppData(context: Context, log: InteractionLog) {
        val packageManager = context.packageManager

        try {
            val applicationInfo = packageManager.getApplicationInfo(log.packageName, 0)
            val appName = applicationInfo.loadLabel(packageManager).toString()
            val appIcon: Drawable = applicationInfo.loadIcon(packageManager)

            if (
                "com.google.android.gms" == log.packageName
                && "com.google.android.gms.tapandpay.hce.service.TpHceChimeraService" == log.serviceName
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