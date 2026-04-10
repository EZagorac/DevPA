package com.devpa.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        HabitEntity::class,
        HabitLogEntity::class,
        JourneyEntity::class,
        JourneyStepEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class DevPADatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun journeyDao(): JourneyDao
    abstract fun journeyStepDao(): JourneyStepDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 2 added new columns to habits table
                db.execSQL("ALTER TABLE habits ADD COLUMN scheduleType TEXT NOT NULL DEFAULT 'daily'")
                db.execSQL("ALTER TABLE habits ADD COLUMN scheduleDays TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE habits ADD COLUMN timesPerWeek INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE habits ADD COLUMN breakUntil TEXT")
                db.execSQL("ALTER TABLE habits ADD COLUMN breakStartStreak INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop portfolio_items (no longer used)
                db.execSQL("DROP TABLE IF EXISTS portfolio_items")

                // Create journeys table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS journeys (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        iconEmoji TEXT NOT NULL DEFAULT '🎯',
                        colourHex TEXT NOT NULL DEFAULT '#7FFF6E',
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        createdAt INTEGER NOT NULL,
                        completedAt INTEGER,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        isActiveJourney INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // Create journey_steps table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS journey_steps (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        journeyId INTEGER NOT NULL,
                        label TEXT NOT NULL,
                        description TEXT,
                        notes TEXT,
                        category TEXT,
                        dueDate TEXT,
                        isDone INTEGER NOT NULL DEFAULT 0,
                        progressPct INTEGER NOT NULL DEFAULT 0,
                        completedAt INTEGER,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        dependsOnStepId INTEGER,
                        FOREIGN KEY(journeyId) REFERENCES journeys(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS index_journey_steps_journeyId ON journey_steps(journeyId)")
            }
        }
    }
}
