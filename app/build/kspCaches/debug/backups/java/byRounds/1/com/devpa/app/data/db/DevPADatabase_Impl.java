package com.devpa.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DevPADatabase_Impl extends DevPADatabase {
  private volatile HabitDao _habitDao;

  private volatile JourneyDao _journeyDao;

  private volatile JourneyStepDao _journeyStepDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `habits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `startDate` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `scheduleType` TEXT NOT NULL, `scheduleDays` TEXT NOT NULL, `timesPerWeek` INTEGER NOT NULL, `breakUntil` TEXT, `breakStartStreak` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `habit_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habitId` INTEGER NOT NULL, `date` TEXT NOT NULL, FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_logs_habitId_date` ON `habit_logs` (`habitId`, `date`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `journeys` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `iconEmoji` TEXT NOT NULL, `colourHex` TEXT NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `completedAt` INTEGER, `sortOrder` INTEGER NOT NULL, `isActiveJourney` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `journey_steps` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `journeyId` INTEGER NOT NULL, `label` TEXT NOT NULL, `description` TEXT, `notes` TEXT, `category` TEXT, `dueDate` TEXT, `isDone` INTEGER NOT NULL, `progressPct` INTEGER NOT NULL, `completedAt` INTEGER, `sortOrder` INTEGER NOT NULL, `dependsOnStepId` INTEGER, FOREIGN KEY(`journeyId`) REFERENCES `journeys`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_journey_steps_journeyId` ON `journey_steps` (`journeyId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '77ee7993d94ecf0f7fbebc858679bf8b')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `habits`");
        db.execSQL("DROP TABLE IF EXISTS `habit_logs`");
        db.execSQL("DROP TABLE IF EXISTS `journeys`");
        db.execSQL("DROP TABLE IF EXISTS `journey_steps`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsHabits = new HashMap<String, TableInfo.Column>(9);
        _columnsHabits.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("startDate", new TableInfo.Column("startDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("scheduleType", new TableInfo.Column("scheduleType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("scheduleDays", new TableInfo.Column("scheduleDays", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("timesPerWeek", new TableInfo.Column("timesPerWeek", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("breakUntil", new TableInfo.Column("breakUntil", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("breakStartStreak", new TableInfo.Column("breakStartStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHabits = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHabits = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHabits = new TableInfo("habits", _columnsHabits, _foreignKeysHabits, _indicesHabits);
        final TableInfo _existingHabits = TableInfo.read(db, "habits");
        if (!_infoHabits.equals(_existingHabits)) {
          return new RoomOpenHelper.ValidationResult(false, "habits(com.devpa.app.data.db.HabitEntity).\n"
                  + " Expected:\n" + _infoHabits + "\n"
                  + " Found:\n" + _existingHabits);
        }
        final HashMap<String, TableInfo.Column> _columnsHabitLogs = new HashMap<String, TableInfo.Column>(3);
        _columnsHabitLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabitLogs.put("habitId", new TableInfo.Column("habitId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabitLogs.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHabitLogs = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysHabitLogs.add(new TableInfo.ForeignKey("habits", "CASCADE", "NO ACTION", Arrays.asList("habitId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesHabitLogs = new HashSet<TableInfo.Index>(1);
        _indicesHabitLogs.add(new TableInfo.Index("index_habit_logs_habitId_date", true, Arrays.asList("habitId", "date"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoHabitLogs = new TableInfo("habit_logs", _columnsHabitLogs, _foreignKeysHabitLogs, _indicesHabitLogs);
        final TableInfo _existingHabitLogs = TableInfo.read(db, "habit_logs");
        if (!_infoHabitLogs.equals(_existingHabitLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "habit_logs(com.devpa.app.data.db.HabitLogEntity).\n"
                  + " Expected:\n" + _infoHabitLogs + "\n"
                  + " Found:\n" + _existingHabitLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsJourneys = new HashMap<String, TableInfo.Column>(10);
        _columnsJourneys.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneys.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneys.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneys.put("iconEmoji", new TableInfo.Column("iconEmoji", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneys.put("colourHex", new TableInfo.Column("colourHex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneys.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneys.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneys.put("completedAt", new TableInfo.Column("completedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneys.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneys.put("isActiveJourney", new TableInfo.Column("isActiveJourney", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysJourneys = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesJourneys = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoJourneys = new TableInfo("journeys", _columnsJourneys, _foreignKeysJourneys, _indicesJourneys);
        final TableInfo _existingJourneys = TableInfo.read(db, "journeys");
        if (!_infoJourneys.equals(_existingJourneys)) {
          return new RoomOpenHelper.ValidationResult(false, "journeys(com.devpa.app.data.db.JourneyEntity).\n"
                  + " Expected:\n" + _infoJourneys + "\n"
                  + " Found:\n" + _existingJourneys);
        }
        final HashMap<String, TableInfo.Column> _columnsJourneySteps = new HashMap<String, TableInfo.Column>(12);
        _columnsJourneySteps.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneySteps.put("journeyId", new TableInfo.Column("journeyId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneySteps.put("label", new TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneySteps.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneySteps.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneySteps.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneySteps.put("dueDate", new TableInfo.Column("dueDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneySteps.put("isDone", new TableInfo.Column("isDone", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneySteps.put("progressPct", new TableInfo.Column("progressPct", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneySteps.put("completedAt", new TableInfo.Column("completedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneySteps.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJourneySteps.put("dependsOnStepId", new TableInfo.Column("dependsOnStepId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysJourneySteps = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysJourneySteps.add(new TableInfo.ForeignKey("journeys", "CASCADE", "NO ACTION", Arrays.asList("journeyId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesJourneySteps = new HashSet<TableInfo.Index>(1);
        _indicesJourneySteps.add(new TableInfo.Index("index_journey_steps_journeyId", false, Arrays.asList("journeyId"), Arrays.asList("ASC")));
        final TableInfo _infoJourneySteps = new TableInfo("journey_steps", _columnsJourneySteps, _foreignKeysJourneySteps, _indicesJourneySteps);
        final TableInfo _existingJourneySteps = TableInfo.read(db, "journey_steps");
        if (!_infoJourneySteps.equals(_existingJourneySteps)) {
          return new RoomOpenHelper.ValidationResult(false, "journey_steps(com.devpa.app.data.db.JourneyStepEntity).\n"
                  + " Expected:\n" + _infoJourneySteps + "\n"
                  + " Found:\n" + _existingJourneySteps);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "77ee7993d94ecf0f7fbebc858679bf8b", "51fd489b97e610b251f8a30313f4ea4a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "habits","habit_logs","journeys","journey_steps");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `habits`");
      _db.execSQL("DELETE FROM `habit_logs`");
      _db.execSQL("DELETE FROM `journeys`");
      _db.execSQL("DELETE FROM `journey_steps`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(HabitDao.class, HabitDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(JourneyDao.class, JourneyDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(JourneyStepDao.class, JourneyStepDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public HabitDao habitDao() {
    if (_habitDao != null) {
      return _habitDao;
    } else {
      synchronized(this) {
        if(_habitDao == null) {
          _habitDao = new HabitDao_Impl(this);
        }
        return _habitDao;
      }
    }
  }

  @Override
  public JourneyDao journeyDao() {
    if (_journeyDao != null) {
      return _journeyDao;
    } else {
      synchronized(this) {
        if(_journeyDao == null) {
          _journeyDao = new JourneyDao_Impl(this);
        }
        return _journeyDao;
      }
    }
  }

  @Override
  public JourneyStepDao journeyStepDao() {
    if (_journeyStepDao != null) {
      return _journeyStepDao;
    } else {
      synchronized(this) {
        if(_journeyStepDao == null) {
          _journeyStepDao = new JourneyStepDao_Impl(this);
        }
        return _journeyStepDao;
      }
    }
  }
}
