package com.vngrs.android.pomodoro.wear;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.os.PowerManager;
import android.support.v4.app.NotificationManagerCompat;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * A module for Android-specific dependencies which require a Context to create.
 *
 * Created by Said Tahsin Dane on 17/03/15.
 */
@Module
final class AppModule {
    private final App app;

    AppModule(App app) {
        this.app = app;
    }

    @Provides @Singleton Application application() {
        return app;
    }

    @Provides @Singleton NotificationManagerCompat provideNotificationManager() {
        return NotificationManagerCompat.from(app);
    }

    @Provides @Singleton AlarmManager provideAlarmManager() {
        return (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
    }

    @Provides @Singleton PowerManager providePowerManager() {
        return (PowerManager) app.getSystemService(Context.POWER_SERVICE);
    }
}
