package com.vngrs.android.pomodoro.ui;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Ui Module for VNGRS product flavor.
 * It is used to show different UI components related to VNGRS Pomodoro API.
 *
 * Created by Said Tahsin Dane on 17/03/15.
 */
@Module
public final class StandaloneUiModule {
    @Provides @Singleton
    BaseUi provideAppContainer() {
        return BaseUi.DEFAULT;
    }
}
