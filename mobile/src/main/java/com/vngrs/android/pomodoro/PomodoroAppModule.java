package com.vngrs.android.pomodoro;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Said Tahsin Dane on 17/03/15.
 */
@Module
final class PomodoroAppModule {
    private final PomodoroApp app;

    PomodoroAppModule(PomodoroApp app) {
        this.app = app;
    }

    @Provides @Singleton Application application() {
        return app;
    }
}
