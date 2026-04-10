package com.devpa.app.ui.briefing;

import com.devpa.app.data.db.HabitDao;
import com.devpa.app.data.repository.BriefingRepository;
import com.devpa.app.data.repository.JourneyRepository;
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
public final class BriefingViewModel_Factory implements Factory<BriefingViewModel> {
  private final Provider<BriefingRepository> briefingRepositoryProvider;

  private final Provider<HabitDao> habitDaoProvider;

  private final Provider<JourneyRepository> journeyRepositoryProvider;

  public BriefingViewModel_Factory(Provider<BriefingRepository> briefingRepositoryProvider,
      Provider<HabitDao> habitDaoProvider, Provider<JourneyRepository> journeyRepositoryProvider) {
    this.briefingRepositoryProvider = briefingRepositoryProvider;
    this.habitDaoProvider = habitDaoProvider;
    this.journeyRepositoryProvider = journeyRepositoryProvider;
  }

  @Override
  public BriefingViewModel get() {
    return newInstance(briefingRepositoryProvider.get(), habitDaoProvider.get(), journeyRepositoryProvider.get());
  }

  public static BriefingViewModel_Factory create(
      Provider<BriefingRepository> briefingRepositoryProvider, Provider<HabitDao> habitDaoProvider,
      Provider<JourneyRepository> journeyRepositoryProvider) {
    return new BriefingViewModel_Factory(briefingRepositoryProvider, habitDaoProvider, journeyRepositoryProvider);
  }

  public static BriefingViewModel newInstance(BriefingRepository briefingRepository,
      HabitDao habitDao, JourneyRepository journeyRepository) {
    return new BriefingViewModel(briefingRepository, habitDao, journeyRepository);
  }
}
