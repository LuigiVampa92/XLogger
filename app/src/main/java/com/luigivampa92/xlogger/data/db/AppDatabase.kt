package com.luigivampa92.xlogger.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [(InteractionLogEntity::class)],
    version = 1,
    exportSchema = false
)
@TypeConverters(InteractionLogTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun interactionLogDao(): InteractionLogDao

    companion object {

        private var instance: AppDatabase? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room
                    .databaseBuilder(context.applicationContext, AppDatabase::class.java, "interaction_logs")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance!!
        }
    }
}