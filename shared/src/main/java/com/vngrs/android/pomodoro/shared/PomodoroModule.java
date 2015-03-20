package com.vngrs.android.pomodoro.shared;

import android.content.SharedPreferences;

import com.vngrs.android.pomodoro.shared.data.DataModule;
import com.vngrs.android.pomodoro.shared.data.prefs.DateTimePreference;
import com.vngrs.android.pomodoro.shared.data.prefs.EnumPreference;
import com.vngrs.android.pomodoro.shared.data.prefs.IntPreference;
import com.vngrs.android.pomodoro.shared.data.prefs.LastPomodoro;
import com.vngrs.android.pomodoro.shared.data.prefs.NextPomodoro;
import com.vngrs.android.pomodoro.shared.model.ActivityType;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Said Tahsin Dane on 18/03/15.
 */
@Module(includes = DataModule.class)
public class PomodoroModule {

    @Provides @Singleton
    EnumPreference<ActivityType> provideActivityType(SharedPreferences preferences) {
        return new EnumPreference<>(preferences, ActivityType.class, "activity_type", ActivityType.NONE);
    }

    @Provides @Singleton @NextPomodoro
    DateTimePreference provideNextPomodoro(SharedPreferences preferences) {
        return new DateTimePreference(preferences, "next_pomodoro");
    }

    @Provides @Singleton @LastPomodoro
    DateTimePreference provideLastPomodoroTimeStamp(SharedPreferences preferences) {
        return new DateTimePreference(preferences, "last_pomodoro");
    }

    @Provides @Singleton
    IntPreference providePomodorosDone(SharedPreferences preferences) {
        return new IntPreference(preferences, "pomodoros_done");
    }
}
