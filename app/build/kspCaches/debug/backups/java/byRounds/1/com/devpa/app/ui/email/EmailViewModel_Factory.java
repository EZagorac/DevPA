package com.devpa.app.ui.email;

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
public final class EmailViewModel_Factory implements Factory<EmailViewModel> {
  private final Provider<ClaudeApiService> apiProvider;

  public EmailViewModel_Factory(Provider<ClaudeApiService> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public EmailViewModel get() {
    return newInstance(apiProvider.get());
  }

  public static EmailViewModel_Factory create(Provider<ClaudeApiService> apiProvider) {
    return new EmailViewModel_Factory(apiProvider);
  }

  public static EmailViewModel newInstance(ClaudeApiService api) {
    return new EmailViewModel(api);
  }
}
