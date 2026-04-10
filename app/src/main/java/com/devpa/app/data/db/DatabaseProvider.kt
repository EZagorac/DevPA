package com.devpa.app.data.db

import android.content.Context
import androidx.room.Room

// Singleton accessor used by widget providers which run outside Hilt's scope
object DatabaseProvider {
    @Volatile private var instance: DevPADatabase? = null

    fun getInstance(context: Context): DevPADatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                DevPADatabase::class.java,
                "dev_pa.db"
            )
            .addMigrations(DevPADatabase.MIGRATION_1_2, DevPADatabase.MIGRATION_2_3)
            .build()
            .also { instance = it }
        }
    }
}
