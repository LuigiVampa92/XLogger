package com.luigivampa92.xlogger.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luigivampa92.xlogger.domain.InteractionLog
import com.luigivampa92.xlogger.domain.InteractionLogEntry
import com.luigivampa92.xlogger.domain.InteractionType

@Entity(tableName = "interaction_logs")
data class InteractionLogEntity (
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    val interactionTime: Long,
    @ColumnInfo(name = "type")
    val interactionType: InteractionType,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "service_name")
    val serviceName: String,
    @ColumnInfo(name = "log_entries")
    val logEntries: List<InteractionLogEntry>
) {

    fun toInteractionLog() = InteractionLog(interactionType, packageName, serviceName, logEntries)

    companion object {

        @JvmStatic
        fun fromInteractionLog(log: InteractionLog): InteractionLogEntity {
            return InteractionLogEntity(
                log.entries.firstOrNull()?.timestamp ?: throw RuntimeException("No log entries"),
                log.type,
                log.packageName,
                log.serviceName,
                log.entries
            )
        }
    }
}