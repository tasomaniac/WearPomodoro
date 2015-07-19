package com.tasomaniac.android.pomodoro.shared;

import android.content.SharedPreferences;

import com.tasomaniac.android.pomodoro.shared.data.DataModule;
import com.tasomaniac.android.pomodoro.shared.data.prefs.BooleanPreference;
import com.tasomaniac.android.pomodoro.shared.data.prefs.DateTimePreference;
import com.tasomaniac.android.pomodoro.shared.data.prefs.EnumPreference;
import com.tasomaniac.android.pomodoro.shared.data.prefs.IntPreference;
import com.tasomaniac.android.pomodoro.shared.data.prefs.LastPomodoro;
import com.tasomaniac.android.pomodoro.shared.data.prefs.NextPomodoro;
import com.tasomaniac.android.pomodoro.shared.model.ActivityType;

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
        return new EnumPreference<>(preferences, ActivityType.class, Constants.KEY_ACTIVITY_TYPE, ActivityType.NONE);
    }

    @Provides @Singleton @NextPomodoro
    DateTimePreference provideNextPomodoro(SharedPreferences preferences) {
        return new DateTimePreference(preferences, Constants.KEY_NEXT_POMODORO);
    }

    @Provides @Singleton @LastPomodoro
    DateTimePreference provideLastPomodoroTimeStamp(SharedPreferences preferences) {
        return new DateTimePreference(preferences, Constants.KEY_LAST_POMODORO);
    }

    @Provides @Singleton
    IntPreference providePomodorosDone(SharedPreferences preferences) {
        return new IntPreference(preferences, Constants.KEY_POMODOROS_DONE);
    }

    @Provides @Singleton
    BooleanPreference providePomodoroOngoing(SharedPreferences preferences) {
        return new BooleanPreference(preferences, Constants.KEY_POMODORO_ONGOING);
    }
}
