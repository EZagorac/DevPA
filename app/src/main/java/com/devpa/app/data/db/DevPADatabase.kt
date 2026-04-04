package com.devpa.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        PortfolioItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DevPADatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun portfolioDao(): PortfolioDao
}
