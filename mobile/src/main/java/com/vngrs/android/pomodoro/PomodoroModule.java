package com.vngrs.android.pomodoro;

import android.content.SharedPreferences;

import com.vngrs.android.pomodoro.data.DataModule;
import com.vngrs.android.pomodoro.data.prefs.LastPomodoroTimeStamp;
import com.vngrs.android.pomodoro.data.prefs.NextPomodoroTimeStamp;
import com.vngrs.android.pomodoro.data.prefs.DateTimePreference;
import com.vngrs.android.pomodoro.data.prefs.EnumPreference;
import com.vngrs.android.pomodoro.data.prefs.IntPreference;
import com.vngrs.android.pomodoro.model.ActivityType;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Said Tahsin Dane on 18/03/15.
 */
@Module(
        complete = false,
        includes = { DataModule.class }
)
public class PomodoroModule {

    @Provides @Singleton
    EnumPreference<ActivityType> provideActivityType(SharedPreferences preferences) {
        return new EnumPreference<>(preferences, ActivityType.class, "activity_type", ActivityType.NONE);
    }

    @Provides @Singleton @NextPomodoroTimeStamp
    DateTimePreference provideNextPomodoro(SharedPreferences preferences) {
        return new DateTimePreference(preferences, "next_pomodoro");
    }

    @Provides @Singleton @LastPomodoroTimeStamp
    DateTimePreference provideLastPomodoroTimeStamp(SharedPreferences preferences) {
        return new DateTimePreference(preferences, "last_pomodoro");
    }

    @Provides @Singleton
    IntPreference providePomodorosDone(SharedPreferences preferences) {
        return new IntPreference(preferences, "pomodoros_done");
    }
}
