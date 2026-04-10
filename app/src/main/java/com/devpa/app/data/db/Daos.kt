package com.devpa.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ── Habit DAO ──────────────────────────────────────────────────────
@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY date ASC")
    fun getAllLogs(): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    suspend fun getAllHabitsSync(): List<HabitEntity>

    @Query("SELECT date FROM habit_logs WHERE habitId = :habitId ORDER BY date ASC")
    suspend fun getLogDatesForHabit(habitId: Long): List<String>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun isCompletedOn(habitId: Long, date: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: String)

    @Query("UPDATE habits SET name = :name WHERE id = :id")
    suspend fun updateHabitName(id: Long, name: String)

    @Query("UPDATE habits SET breakUntil = :breakUntil, breakStartStreak = :breakStartStreak WHERE id = :id")
    suspend fun updateBreak(id: Long, breakUntil: String?, breakStartStreak: Int)

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
    val scheduleType: String,
    val scheduleDays: String,
    val timesPerWeek: Int,
    val breakUntil: String?,
    val breakStartStreak: Int,
    val logCount: Int
)

// ── Journey DAO ────────────────────────────────────────────────────
@Dao
interface JourneyDao {

    @Query("SELECT * FROM journeys ORDER BY sortOrder ASC")
    fun getAllJourneys(): Flow<List<JourneyEntity>>

    @Query("SELECT * FROM journeys WHERE id = :id")
    suspend fun getJourneyById(id: Long): JourneyEntity?

    @Query("SELECT * FROM journeys WHERE status = 'ACTIVE' ORDER BY sortOrder ASC")
    fun getActiveJourneys(): Flow<List<JourneyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: JourneyEntity): Long

    @Update
    suspend fun updateJourney(journey: JourneyEntity)

    @Delete
    suspend fun deleteJourney(journey: JourneyEntity)

    @Query("UPDATE journeys SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, completedAt: Long?)

    @Query("SELECT COUNT(*) FROM journeys")
    suspend fun getJourneyCount(): Int

    @Query("SELECT * FROM journeys ORDER BY sortOrder ASC")
    suspend fun getAllJourneysSync(): List<JourneyEntity>
}

// ── Journey Step DAO ───────────────────────────────────────────────
@Dao
interface JourneyStepDao {

    @Query("SELECT * FROM journey_steps WHERE journeyId = :journeyId ORDER BY sortOrder ASC")
    fun getStepsForJourney(journeyId: Long): Flow<List<JourneyStepEntity>>

    @Query("SELECT * FROM journey_steps WHERE journeyId = :journeyId ORDER BY sortOrder ASC")
    suspend fun getStepsForJourneyOnce(journeyId: Long): List<JourneyStepEntity>

    @Query("SELECT * FROM journey_steps WHERE journeyId = :journeyId AND isDone = 0 ORDER BY sortOrder ASC LIMIT 3")
    suspend fun getNextThreeSteps(journeyId: Long): List<JourneyStepEntity>

    @Query("SELECT * FROM journey_steps WHERE journeyId = :journeyId AND isDone = 0 ORDER BY sortOrder ASC LIMIT 1")
    suspend fun getNextStep(journeyId: Long): JourneyStepEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: JourneyStepEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(steps: List<JourneyStepEntity>)

    @Update
    suspend fun updateStep(step: JourneyStepEntity)

    @Delete
    suspend fun deleteStep(step: JourneyStepEntity)

    @Query("UPDATE journey_steps SET progressPct = :pct, isDone = :isDone, completedAt = :completedAt WHERE id = :id")
    suspend fun updateProgress(id: Long, pct: Int, isDone: Boolean, completedAt: Long?)

    @Query("UPDATE journey_steps SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("UPDATE journey_steps SET category = :newName WHERE journeyId = :journeyId AND category = :oldName")
    suspend fun renameCategory(journeyId: Long, oldName: String, newName: String)

    @Query("UPDATE journey_steps SET category = null WHERE journeyId = :journeyId AND category = :name")
    suspend fun deleteCategory(journeyId: Long, name: String)

    @Query("SELECT COUNT(*) FROM journey_steps WHERE journeyId = :journeyId")
    suspend fun getTotalStepCount(journeyId: Long): Int

    @Query("SELECT COUNT(*) FROM journey_steps WHERE journeyId = :journeyId AND isDone = 1")
    suspend fun getCompletedStepCount(journeyId: Long): Int

    @Query("SELECT SUM(progressPct) FROM journey_steps WHERE journeyId = :journeyId")
    suspend fun getSumProgressPct(journeyId: Long): Int?
}
