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
    val startDate: String,       // ISO date string: "2026-04-04"
    val createdAt: Long = System.currentTimeMillis()
)

// ── HabitLog entity — one row per (habit, date) a user checks off ──
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
    val date: String             // ISO date string: "2026-04-04"
)

// ── Portfolio item entity ──────────────────────────────────────────
@Entity(tableName = "portfolio_items")
data class PortfolioItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val category: String,
    val isDone: Boolean = false,
    val completedAt: Long? = null
)
