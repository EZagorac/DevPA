package com.devpa.app.ui.jobs;

import com.devpa.app.data.repository.ClaudeApiService;
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
public final class JobsViewModel_Factory implements Factory<JobsViewModel> {
  private final Provider<ClaudeApiService> apiProvider;

  public JobsViewModel_Factory(Provider<ClaudeApiService> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public JobsViewModel get() {
    return newInstance(apiProvider.get());
  }

  public static JobsViewModel_Factory create(Provider<ClaudeApiService> apiProvider) {
    return new JobsViewModel_Factory(apiProvider);
  }

  public static JobsViewModel newInstance(ClaudeApiService api) {
    return new JobsViewModel(api);
  }
}
