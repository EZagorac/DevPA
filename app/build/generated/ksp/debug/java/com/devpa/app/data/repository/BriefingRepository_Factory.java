package com.devpa.app.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class BriefingRepository_Factory implements Factory<BriefingRepository> {
  private final Provider<ClaudeApiService> apiProvider;

  public BriefingRepository_Factory(Provider<ClaudeApiService> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public BriefingRepository get() {
    return newInstance(apiProvider.get());
  }

  public static BriefingRepository_Factory create(Provider<ClaudeApiService> apiProvider) {
    return new BriefingRepository_Factory(apiProvider);
  }

  public static BriefingRepository newInstance(ClaudeApiService api) {
    return new BriefingRepository(api);
  }
}
