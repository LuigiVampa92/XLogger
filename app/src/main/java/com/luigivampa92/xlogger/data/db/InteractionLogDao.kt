package com.luigivampa92.xlogger.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface InteractionLogDao {

    @get:Query("SELECT * FROM interaction_logs ORDER BY timestamp DESC")
    val all: Single<List<InteractionLogEntity>>

    @Insert
    fun insert(entity: InteractionLogEntity): Completable

    @Delete
    fun delete(entity: InteractionLogEntity): Completable

}