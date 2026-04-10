package com.devpa.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HabitDao_Impl implements HabitDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HabitEntity> __insertionAdapterOfHabitEntity;

  private final EntityInsertionAdapter<HabitLogEntity> __insertionAdapterOfHabitLogEntity;

  private final EntityDeletionOrUpdateAdapter<HabitEntity> __deletionAdapterOfHabitEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLog;

  private final SharedSQLiteStatement __preparedStmtOfUpdateHabitName;

  private final SharedSQLiteStatement __preparedStmtOfUpdateBreak;

  public HabitDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHabitEntity = new EntityInsertionAdapter<HabitEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `habits` (`id`,`name`,`startDate`,`createdAt`,`scheduleType`,`scheduleDays`,`timesPerWeek`,`breakUntil`,`breakStartStreak`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HabitEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getStartDate());
        statement.bindLong(4, entity.getCreatedAt());
        statement.bindString(5, entity.getScheduleType());
        statement.bindString(6, entity.getScheduleDays());
        statement.bindLong(7, entity.getTimesPerWeek());
        if (entity.getBreakUntil() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getBreakUntil());
        }
        statement.bindLong(9, entity.getBreakStartStreak());
      }
    };
    this.__insertionAdapterOfHabitLogEntity = new EntityInsertionAdapter<HabitLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `habit_logs` (`id`,`habitId`,`date`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HabitLogEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getHabitId());
        statement.bindString(3, entity.getDate());
      }
    };
    this.__deletionAdapterOfHabitEntity = new EntityDeletionOrUpdateAdapter<HabitEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `habits` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HabitEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteLog = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM habit_logs WHERE habitId = ? AND date = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateHabitName = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE habits SET name = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateBreak = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE habits SET breakUntil = ?, breakStartStreak = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertHabit(final HabitEntity habit, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfHabitEntity.insertAndReturnId(habit);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLog(final HabitLogEntity log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHabitLogEntity.insert(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteHabit(final HabitEntity habit, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfHabitEntity.handle(habit);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLog(final long habitId, final String date,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLog.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, habitId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, date);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteLog.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateHabitName(final long id, final String name,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateHabitName.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, name);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateHabitName.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateBreak(final long id, final String breakUntil, final int breakStartStreak,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateBreak.acquire();
        int _argIndex = 1;
        if (breakUntil == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, breakUntil);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, breakStartStreak);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateBreak.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<HabitEntity>> getAllHabits() {
    final String _sql = "SELECT * FROM habits ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"habits"}, new Callable<List<HabitEntity>>() {
      @Override
      @NonNull
      public List<HabitEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfScheduleDays = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleDays");
          final int _cursorIndexOfTimesPerWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "timesPerWeek");
          final int _cursorIndexOfBreakUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "breakUntil");
          final int _cursorIndexOfBreakStartStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "breakStartStreak");
          final List<HabitEntity> _result = new ArrayList<HabitEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HabitEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpStartDate;
            _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpScheduleType;
            _tmpScheduleType = _cursor.getString(_cursorIndexOfScheduleType);
            final String _tmpScheduleDays;
            _tmpScheduleDays = _cursor.getString(_cursorIndexOfScheduleDays);
            final int _tmpTimesPerWeek;
            _tmpTimesPerWeek = _cursor.getInt(_cursorIndexOfTimesPerWeek);
            final String _tmpBreakUntil;
            if (_cursor.isNull(_cursorIndexOfBreakUntil)) {
              _tmpBreakUntil = null;
            } else {
              _tmpBreakUntil = _cursor.getString(_cursorIndexOfBreakUntil);
            }
            final int _tmpBreakStartStreak;
            _tmpBreakStartStreak = _cursor.getInt(_cursorIndexOfBreakStartStreak);
            _item = new HabitEntity(_tmpId,_tmpName,_tmpStartDate,_tmpCreatedAt,_tmpScheduleType,_tmpScheduleDays,_tmpTimesPerWeek,_tmpBreakUntil,_tmpBreakStartStreak);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<HabitLogEntity>> getAllLogs() {
    final String _sql = "SELECT * FROM habit_logs ORDER BY date ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"habit_logs"}, new Callable<List<HabitLogEntity>>() {
      @Override
      @NonNull
      public List<HabitLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHabitId = CursorUtil.getColumnIndexOrThrow(_cursor, "habitId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final List<HabitLogEntity> _result = new ArrayList<HabitLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HabitLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpHabitId;
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            _item = new HabitLogEntity(_tmpId,_tmpHabitId,_tmpDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getHabitById(final long id, final Continuation<? super HabitEntity> $completion) {
    final String _sql = "SELECT * FROM habits WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<HabitEntity>() {
      @Override
      @Nullable
      public HabitEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfScheduleDays = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleDays");
          final int _cursorIndexOfTimesPerWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "timesPerWeek");
          final int _cursorIndexOfBreakUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "breakUntil");
          final int _cursorIndexOfBreakStartStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "breakStartStreak");
          final HabitEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpStartDate;
            _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpScheduleType;
            _tmpScheduleType = _cursor.getString(_cursorIndexOfScheduleType);
            final String _tmpScheduleDays;
            _tmpScheduleDays = _cursor.getString(_cursorIndexOfScheduleDays);
            final int _tmpTimesPerWeek;
            _tmpTimesPerWeek = _cursor.getInt(_cursorIndexOfTimesPerWeek);
            final String _tmpBreakUntil;
            if (_cursor.isNull(_cursorIndexOfBreakUntil)) {
              _tmpBreakUntil = null;
            } else {
              _tmpBreakUntil = _cursor.getString(_cursorIndexOfBreakUntil);
            }
            final int _tmpBreakStartStreak;
            _tmpBreakStartStreak = _cursor.getInt(_cursorIndexOfBreakStartStreak);
            _result = new HabitEntity(_tmpId,_tmpName,_tmpStartDate,_tmpCreatedAt,_tmpScheduleType,_tmpScheduleDays,_tmpTimesPerWeek,_tmpBreakUntil,_tmpBreakStartStreak);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllHabitsSync(final Continuation<? super List<HabitEntity>> $completion) {
    final String _sql = "SELECT * FROM habits ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<HabitEntity>>() {
      @Override
      @NonNull
      public List<HabitEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfScheduleDays = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleDays");
          final int _cursorIndexOfTimesPerWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "timesPerWeek");
          final int _cursorIndexOfBreakUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "breakUntil");
          final int _cursorIndexOfBreakStartStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "breakStartStreak");
          final List<HabitEntity> _result = new ArrayList<HabitEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HabitEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpStartDate;
            _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpScheduleType;
            _tmpScheduleType = _cursor.getString(_cursorIndexOfScheduleType);
            final String _tmpScheduleDays;
            _tmpScheduleDays = _cursor.getString(_cursorIndexOfScheduleDays);
            final int _tmpTimesPerWeek;
            _tmpTimesPerWeek = _cursor.getInt(_cursorIndexOfTimesPerWeek);
            final String _tmpBreakUntil;
            if (_cursor.isNull(_cursorIndexOfBreakUntil)) {
              _tmpBreakUntil = null;
            } else {
              _tmpBreakUntil = _cursor.getString(_cursorIndexOfBreakUntil);
            }
            final int _tmpBreakStartStreak;
            _tmpBreakStartStreak = _cursor.getInt(_cursorIndexOfBreakStartStreak);
            _item = new HabitEntity(_tmpId,_tmpName,_tmpStartDate,_tmpCreatedAt,_tmpScheduleType,_tmpScheduleDays,_tmpTimesPerWeek,_tmpBreakUntil,_tmpBreakStartStreak);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLogDatesForHabit(final long habitId,
      final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT date FROM habit_logs WHERE habitId = ? ORDER BY date ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, habitId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object isCompletedOn(final long habitId, final String date,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM habit_logs WHERE habitId = ? AND date = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, habitId);
    _argIndex = 2;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getHabitsWithTodayStatus(final String today,
      final Continuation<? super List<HabitWithTodayStatus>> $completion) {
    final String _sql = "\n"
            + "        SELECT h.*, COUNT(l.id) as logCount\n"
            + "        FROM habits h\n"
            + "        LEFT JOIN habit_logs l ON h.id = l.habitId AND l.date = ?\n"
            + "        GROUP BY h.id\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, today);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<HabitWithTodayStatus>>() {
      @Override
      @NonNull
      public List<HabitWithTodayStatus> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfScheduleDays = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleDays");
          final int _cursorIndexOfTimesPerWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "timesPerWeek");
          final int _cursorIndexOfBreakUntil = CursorUtil.getColumnIndexOrThrow(_cursor, "breakUntil");
          final int _cursorIndexOfBreakStartStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "breakStartStreak");
          final int _cursorIndexOfLogCount = CursorUtil.getColumnIndexOrThrow(_cursor, "logCount");
          final List<HabitWithTodayStatus> _result = new ArrayList<HabitWithTodayStatus>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HabitWithTodayStatus _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpStartDate;
            _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpScheduleType;
            _tmpScheduleType = _cursor.getString(_cursorIndexOfScheduleType);
            final String _tmpScheduleDays;
            _tmpScheduleDays = _cursor.getString(_cursorIndexOfScheduleDays);
            final int _tmpTimesPerWeek;
            _tmpTimesPerWeek = _cursor.getInt(_cursorIndexOfTimesPerWeek);
            final String _tmpBreakUntil;
            if (_cursor.isNull(_cursorIndexOfBreakUntil)) {
              _tmpBreakUntil = null;
            } else {
              _tmpBreakUntil = _cursor.getString(_cursorIndexOfBreakUntil);
            }
            final int _tmpBreakStartStreak;
            _tmpBreakStartStreak = _cursor.getInt(_cursorIndexOfBreakStartStreak);
            final int _tmpLogCount;
            _tmpLogCount = _cursor.getInt(_cursorIndexOfLogCount);
            _item = new HabitWithTodayStatus(_tmpId,_tmpName,_tmpStartDate,_tmpCreatedAt,_tmpScheduleType,_tmpScheduleDays,_tmpTimesPerWeek,_tmpBreakUntil,_tmpBreakStartStreak,_tmpLogCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
