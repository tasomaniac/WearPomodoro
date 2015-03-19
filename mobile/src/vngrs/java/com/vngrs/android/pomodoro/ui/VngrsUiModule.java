package com.vngrs.android.pomodoro.ui;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * VNGRS UI Module that provides WNGRS UI.
 *
 * Created by Said Tahsin Dane on 18/03/15.
 */
@Module
public class VngrsUiModule {
    @Provides @Singleton
    BaseUi provideAppContainer() {
        return new VngrsUi();
    }
}
