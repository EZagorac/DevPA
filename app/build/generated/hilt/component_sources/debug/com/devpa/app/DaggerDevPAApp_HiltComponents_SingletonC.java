package com.devpa.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.devpa.app.data.db.DevPADatabase;
import com.devpa.app.data.db.HabitDao;
import com.devpa.app.data.db.JourneyDao;
import com.devpa.app.data.db.JourneyStepDao;
import com.devpa.app.data.repository.BriefingRepository;
import com.devpa.app.data.repository.ClaudeApiService;
import com.devpa.app.data.repository.JourneyPrefsRepository;
import com.devpa.app.data.repository.JourneyRepository;
import com.devpa.app.data.repository.SeedDataUseCase;
import com.devpa.app.di.AppModule_ProvideClaudeApiServiceFactory;
import com.devpa.app.di.AppModule_ProvideDatabaseFactory;
import com.devpa.app.di.AppModule_ProvideHabitDaoFactory;
import com.devpa.app.di.AppModule_ProvideJourneyDaoFactory;
import com.devpa.app.di.AppModule_ProvideJourneyPrefsRepositoryFactory;
import com.devpa.app.di.AppModule_ProvideJourneyRepositoryFactory;
import com.devpa.app.di.AppModule_ProvideJourneyStepDaoFactory;
import com.devpa.app.ui.MainActivity;
import com.devpa.app.ui.briefing.BriefingFragment;
import com.devpa.app.ui.briefing.BriefingViewModel;
import com.devpa.app.ui.briefing.BriefingViewModel_HiltModules_KeyModule_ProvideFactory;
import com.devpa.app.ui.email.EmailFragment;
import com.devpa.app.ui.email.EmailViewModel;
import com.devpa.app.ui.email.EmailViewModel_HiltModules_KeyModule_ProvideFactory;
import com.devpa.app.ui.habits.HabitsFragment;
import com.devpa.app.ui.habits.HabitsViewModel;
import com.devpa.app.ui.habits.HabitsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.devpa.app.ui.jobs.JobsFragment;
import com.devpa.app.ui.jobs.JobsViewModel;
import com.devpa.app.ui.jobs.JobsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.devpa.app.ui.journey.JourneyDetailFragment;
import com.devpa.app.ui.journey.JourneyDetailViewModel;
import com.devpa.app.ui.journey.JourneyDetailViewModel_HiltModules_KeyModule_ProvideFactory;
import com.devpa.app.ui.journey.JourneyListFragment;
import com.devpa.app.ui.journey.JourneyListViewModel;
import com.devpa.app.ui.journey.JourneyListViewModel_HiltModules_KeyModule_ProvideFactory;
import com.devpa.app.ui.journey.JourneySwitcherBottomSheet;
import com.devpa.app.ui.journey.JourneySwitcherViewModel;
import com.devpa.app.ui.journey.JourneySwitcherViewModel_HiltModules_KeyModule_ProvideFactory;
import com.devpa.app.ui.portfolio.PortfolioFragment;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideApplicationFactory;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerDevPAApp_HiltComponents_SingletonC {
  private DaggerDevPAApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public DevPAApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements DevPAApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public DevPAApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements DevPAApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public DevPAApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements DevPAApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public DevPAApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements DevPAApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public DevPAApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements DevPAApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public DevPAApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements DevPAApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public DevPAApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements DevPAApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public DevPAApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends DevPAApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends DevPAApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public void injectBriefingFragment(BriefingFragment briefingFragment) {
    }

    @Override
    public void injectEmailFragment(EmailFragment emailFragment) {
    }

    @Override
    public void injectHabitsFragment(HabitsFragment habitsFragment) {
    }

    @Override
    public void injectJobsFragment(JobsFragment jobsFragment) {
    }

    @Override
    public void injectJourneyDetailFragment(JourneyDetailFragment journeyDetailFragment) {
    }

    @Override
    public void injectJourneyListFragment(JourneyListFragment journeyListFragment) {
    }

    @Override
    public void injectJourneySwitcherBottomSheet(
        JourneySwitcherBottomSheet journeySwitcherBottomSheet) {
    }

    @Override
    public void injectPortfolioFragment(PortfolioFragment portfolioFragment) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends DevPAApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends DevPAApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Set<String> getViewModelKeys() {
      return ImmutableSet.<String>of(BriefingViewModel_HiltModules_KeyModule_ProvideFactory.provide(), EmailViewModel_HiltModules_KeyModule_ProvideFactory.provide(), HabitsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), JobsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), JourneyDetailViewModel_HiltModules_KeyModule_ProvideFactory.provide(), JourneyListViewModel_HiltModules_KeyModule_ProvideFactory.provide(), JourneySwitcherViewModel_HiltModules_KeyModule_ProvideFactory.provide());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends DevPAApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<BriefingViewModel> briefingViewModelProvider;

    private Provider<EmailViewModel> emailViewModelProvider;

    private Provider<HabitsViewModel> habitsViewModelProvider;

    private Provider<JobsViewModel> jobsViewModelProvider;

    private Provider<JourneyDetailViewModel> journeyDetailViewModelProvider;

    private Provider<JourneyListViewModel> journeyListViewModelProvider;

    private Provider<JourneySwitcherViewModel> journeySwitcherViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.briefingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.emailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.habitsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.jobsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.journeyDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.journeyListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.journeySwitcherViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
    }

    @Override
    public Map<String, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(7).put("com.devpa.app.ui.briefing.BriefingViewModel", ((Provider) briefingViewModelProvider)).put("com.devpa.app.ui.email.EmailViewModel", ((Provider) emailViewModelProvider)).put("com.devpa.app.ui.habits.HabitsViewModel", ((Provider) habitsViewModelProvider)).put("com.devpa.app.ui.jobs.JobsViewModel", ((Provider) jobsViewModelProvider)).put("com.devpa.app.ui.journey.JourneyDetailViewModel", ((Provider) journeyDetailViewModelProvider)).put("com.devpa.app.ui.journey.JourneyListViewModel", ((Provider) journeyListViewModelProvider)).put("com.devpa.app.ui.journey.JourneySwitcherViewModel", ((Provider) journeySwitcherViewModelProvider)).build();
    }

    @Override
    public Map<String, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<String, Object>of();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.devpa.app.ui.briefing.BriefingViewModel 
          return (T) new BriefingViewModel(singletonCImpl.briefingRepositoryProvider.get(), singletonCImpl.habitDao(), singletonCImpl.provideJourneyRepositoryProvider.get());

          case 1: // com.devpa.app.ui.email.EmailViewModel 
          return (T) new EmailViewModel(singletonCImpl.provideClaudeApiServiceProvider.get());

          case 2: // com.devpa.app.ui.habits.HabitsViewModel 
          return (T) new HabitsViewModel(ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule), singletonCImpl.habitDao());

          case 3: // com.devpa.app.ui.jobs.JobsViewModel 
          return (T) new JobsViewModel(singletonCImpl.provideClaudeApiServiceProvider.get());

          case 4: // com.devpa.app.ui.journey.JourneyDetailViewModel 
          return (T) new JourneyDetailViewModel(ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule), singletonCImpl.provideJourneyRepositoryProvider.get());

          case 5: // com.devpa.app.ui.journey.JourneyListViewModel 
          return (T) new JourneyListViewModel(singletonCImpl.provideJourneyRepositoryProvider.get(), singletonCImpl.seedDataUseCase());

          case 6: // com.devpa.app.ui.journey.JourneySwitcherViewModel 
          return (T) new JourneySwitcherViewModel(singletonCImpl.provideJourneyRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends DevPAApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends DevPAApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends DevPAApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<DevPADatabase> provideDatabaseProvider;

    private Provider<JourneyPrefsRepository> provideJourneyPrefsRepositoryProvider;

    private Provider<JourneyRepository> provideJourneyRepositoryProvider;

    private Provider<ClaudeApiService> provideClaudeApiServiceProvider;

    private Provider<BriefingRepository> briefingRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private JourneyDao journeyDao() {
      return AppModule_ProvideJourneyDaoFactory.provideJourneyDao(provideDatabaseProvider.get());
    }

    private JourneyStepDao journeyStepDao() {
      return AppModule_ProvideJourneyStepDaoFactory.provideJourneyStepDao(provideDatabaseProvider.get());
    }

    private SeedDataUseCase seedDataUseCase() {
      return new SeedDataUseCase(provideJourneyRepositoryProvider.get());
    }

    private HabitDao habitDao() {
      return AppModule_ProvideHabitDaoFactory.provideHabitDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<DevPADatabase>(singletonCImpl, 1));
      this.provideJourneyPrefsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<JourneyPrefsRepository>(singletonCImpl, 2));
      this.provideJourneyRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<JourneyRepository>(singletonCImpl, 0));
      this.provideClaudeApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<ClaudeApiService>(singletonCImpl, 4));
      this.briefingRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<BriefingRepository>(singletonCImpl, 3));
    }

    @Override
    public void injectDevPAApp(DevPAApp devPAApp) {
      injectDevPAApp2(devPAApp);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private DevPAApp injectDevPAApp2(DevPAApp instance) {
      DevPAApp_MembersInjector.injectSeedDataUseCase(instance, seedDataUseCase());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.devpa.app.data.repository.JourneyRepository 
          return (T) AppModule_ProvideJourneyRepositoryFactory.provideJourneyRepository(singletonCImpl.journeyDao(), singletonCImpl.journeyStepDao(), singletonCImpl.provideJourneyPrefsRepositoryProvider.get());

          case 1: // com.devpa.app.data.db.DevPADatabase 
          return (T) AppModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.devpa.app.data.repository.JourneyPrefsRepository 
          return (T) AppModule_ProvideJourneyPrefsRepositoryFactory.provideJourneyPrefsRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.devpa.app.data.repository.BriefingRepository 
          return (T) new BriefingRepository(singletonCImpl.provideClaudeApiServiceProvider.get());

          case 4: // com.devpa.app.data.repository.ClaudeApiService 
          return (T) AppModule_ProvideClaudeApiServiceFactory.provideClaudeApiService();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
