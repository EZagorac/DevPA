package com.devpa.app.ui.portfolio;

import com.devpa.app.data.db.PortfolioDao;
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
public final class PortfolioViewModel_Factory implements Factory<PortfolioViewModel> {
  private final Provider<PortfolioDao> portfolioDaoProvider;

  public PortfolioViewModel_Factory(Provider<PortfolioDao> portfolioDaoProvider) {
    this.portfolioDaoProvider = portfolioDaoProvider;
  }

  @Override
  public PortfolioViewModel get() {
    return newInstance(portfolioDaoProvider.get());
  }

  public static PortfolioViewModel_Factory create(Provider<PortfolioDao> portfolioDaoProvider) {
    return new PortfolioViewModel_Factory(portfolioDaoProvider);
  }

  public static PortfolioViewModel newInstance(PortfolioDao portfolioDao) {
    return new PortfolioViewModel(portfolioDao);
  }
}
