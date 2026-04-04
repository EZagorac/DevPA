package com.devpa.app.data.db

import android.content.Context
import androidx.room.Room

// Singleton accessor used by WidgetRefreshWorker which runs outside Hilt's scope
object DatabaseProvider {
    @Volatile private var instance: DevPADatabase? = null

    fun getInstance(context: Context): DevPADatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                DevPADatabase::class.java,
                "dev_pa.db"
            )
            .fallbackToDestructiveMigration()
            .build()
            .also { instance = it }
        }
    }
}

// Extension to make the worker cleaner
fun DevPADatabase.Companion.getInstance(context: Context) =
    DatabaseProvider.getInstance(context)
