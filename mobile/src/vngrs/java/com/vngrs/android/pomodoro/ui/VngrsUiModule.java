package com.vngrs.android.pomodoro.ui;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Said Tahsin Dane on 18/03/15.
 */
@Module
public class VngrsUiModule {
    @Provides @Singleton AppContainer provideAppContainer() {
        return new VngrsAppContainer();
    }
}
