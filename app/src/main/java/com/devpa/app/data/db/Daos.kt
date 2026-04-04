package com.devpa.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ── Habit DAO ──────────────────────────────────────────────────────
@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    // Get all log dates for a specific habit
    @Query("SELECT date FROM habit_logs WHERE habitId = :habitId ORDER BY date ASC")
    suspend fun getLogDatesForHabit(habitId: Long): List<String>

    // Check if a habit was completed on a specific date
    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun isCompletedOn(habitId: Long, date: String): Int

    // Insert a log entry (mark done)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: HabitLogEntity)

    // Delete a log entry (unmark done)
    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: String)

    // Get all habits completed today (used by widget)
    @Query("""
        SELECT h.*, COUNT(l.id) as logCount 
        FROM habits h 
        LEFT JOIN habit_logs l ON h.id = l.habitId AND l.date = :today
        GROUP BY h.id
    """)
    suspend fun getHabitsWithTodayStatus(today: String): List<HabitWithTodayStatus>
}

data class HabitWithTodayStatus(
    val id: Long,
    val name: String,
    val startDate: String,
    val createdAt: Long,
    val logCount: Int           // > 0 means done today
)

// ── Portfolio DAO ──────────────────────────────────────────────────
@Dao
interface PortfolioDao {

    @Query("SELECT * FROM portfolio_items ORDER BY category ASC, id ASC")
    fun getAllItems(): Flow<List<PortfolioItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PortfolioItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PortfolioItemEntity>)

    @Query("UPDATE portfolio_items SET isDone = :isDone, completedAt = :completedAt WHERE id = :id")
    suspend fun updateDoneStatus(id: Long, isDone: Boolean, completedAt: Long?)

    @Query("SELECT COUNT(*) FROM portfolio_items WHERE isDone = 1")
    suspend fun getCompletedCount(): Int

    @Query("SELECT COUNT(*) FROM portfolio_items")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM portfolio_items")
    fun getTotalCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM portfolio_items WHERE isDone = 1")
    fun getCompletedCountFlow(): Flow<Int>
}
