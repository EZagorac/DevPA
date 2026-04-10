package com.devpa.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ── Habit entity ───────────────────────────────────────────────────
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startDate: String,
    val createdAt: Long = System.currentTimeMillis(),
    val scheduleType: String = "daily",
    val scheduleDays: String = "",
    val timesPerWeek: Int = 0,
    val breakUntil: String? = null,
    val breakStartStreak: Int = 0
)

// ── HabitLog entity ────────────────────────────────────────────────
@Entity(
    tableName = "habit_logs",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String
)

// ── Journey entity ─────────────────────────────────────────────────
@Entity(tableName = "journeys")
data class JourneyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconEmoji: String = "🎯",
    val colourHex: String = "#7FFF6E",
    val status: String = "ACTIVE",           // "ACTIVE" | "PAUSED" | "COMPLETED"
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val sortOrder: Int = 0,
    val isActiveJourney: Boolean = false     // reserved for v1, managed via DataStore
)

// ── JourneyStep entity ─────────────────────────────────────────────
@Entity(
    tableName = "journey_steps",
    foreignKeys = [ForeignKey(
        entity = JourneyEntity::class,
        parentColumns = ["id"],
        childColumns = ["journeyId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["journeyId"])]
)
data class JourneyStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val journeyId: Long,
    val label: String,
    val description: String? = null,
    val notes: String? = null,
    val category: String? = null,
    val dueDate: String? = null,             // ISO date string
    val isDone: Boolean = false,
    val progressPct: Int = 0,               // 0–100
    val completedAt: Long? = null,
    val sortOrder: Int = 0,
    val dependsOnStepId: Long? = null        // reserved for v2
)
