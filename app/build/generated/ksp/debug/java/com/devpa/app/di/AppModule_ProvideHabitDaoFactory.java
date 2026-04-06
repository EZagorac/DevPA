package com.devpa.app.di;

import com.devpa.app.data.db.DevPADatabase;
import com.devpa.app.data.db.HabitDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppModule_ProvideHabitDaoFactory implements Factory<HabitDao> {
  private final Provider<DevPADatabase> dbProvider;

  public AppModule_ProvideHabitDaoFactory(Provider<DevPADatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public HabitDao get() {
    return provideHabitDao(dbProvider.get());
  }

  public static AppModule_ProvideHabitDaoFactory create(Provider<DevPADatabase> dbProvider) {
    return new AppModule_ProvideHabitDaoFactory(dbProvider);
  }

  public static HabitDao provideHabitDao(DevPADatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideHabitDao(db));
  }
}
