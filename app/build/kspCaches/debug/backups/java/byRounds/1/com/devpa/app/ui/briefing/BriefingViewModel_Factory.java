package com.devpa.app.ui.briefing;

import com.devpa.app.data.db.HabitDao;
import com.devpa.app.data.db.PortfolioDao;
import com.devpa.app.data.repository.BriefingRepository;
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

  private final Provider<PortfolioDao> portfolioDaoProvider;

  public BriefingViewModel_Factory(Provider<BriefingRepository> briefingRepositoryProvider,
      Provider<HabitDao> habitDaoProvider, Provider<PortfolioDao> portfolioDaoProvider) {
    this.briefingRepositoryProvider = briefingRepositoryProvider;
    this.habitDaoProvider = habitDaoProvider;
    this.portfolioDaoProvider = portfolioDaoProvider;
  }

  @Override
  public BriefingViewModel get() {
    return newInstance(briefingRepositoryProvider.get(), habitDaoProvider.get(), portfolioDaoProvider.get());
  }

  public static BriefingViewModel_Factory create(
      Provider<BriefingRepository> briefingRepositoryProvider, Provider<HabitDao> habitDaoProvider,
      Provider<PortfolioDao> portfolioDaoProvider) {
    return new BriefingViewModel_Factory(briefingRepositoryProvider, habitDaoProvider, portfolioDaoProvider);
  }

  public static BriefingViewModel newInstance(BriefingRepository briefingRepository,
      HabitDao habitDao, PortfolioDao portfolioDao) {
    return new BriefingViewModel(briefingRepository, habitDao, portfolioDao);
  }
}
