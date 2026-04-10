package com.devpa.app.ui.habits;

import android.app.Application;
import com.devpa.app.data.db.HabitDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class HabitsViewModel_Factory implements Factory<HabitsViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<HabitDao> habitDaoProvider;

  public HabitsViewModel_Factory(Provider<Application> applicationProvider,
      Provider<HabitDao> habitDaoProvider) {
    this.applicationProvider = applicationProvider;
    this.habitDaoProvider = habitDaoProvider;
  }

  @Override
  public HabitsViewModel get() {
    return newInstance(applicationProvider.get(), habitDaoProvider.get());
  }

  public static HabitsViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<HabitDao> habitDaoProvider) {
    return new HabitsViewModel_Factory(applicationProvider, habitDaoProvider);
  }

  public static HabitsViewModel newInstance(Application application, HabitDao habitDao) {
    return new HabitsViewModel(application, habitDao);
  }
}
