package com.devpa.app.di;

import com.devpa.app.data.db.DevPADatabase;
import com.devpa.app.data.db.PortfolioDao;
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
public final class AppModule_ProvidePortfolioDaoFactory implements Factory<PortfolioDao> {
  private final Provider<DevPADatabase> dbProvider;

  public AppModule_ProvidePortfolioDaoFactory(Provider<DevPADatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PortfolioDao get() {
    return providePortfolioDao(dbProvider.get());
  }

  public static AppModule_ProvidePortfolioDaoFactory create(Provider<DevPADatabase> dbProvider) {
    return new AppModule_ProvidePortfolioDaoFactory(dbProvider);
  }

  public static PortfolioDao providePortfolioDao(DevPADatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePortfolioDao(db));
  }
}
