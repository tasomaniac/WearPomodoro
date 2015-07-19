package com.tasomaniac.android.pomodoro.shared;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tasomaniac.android.pomodoro.shared.data.prefs.BooleanPreference;
import com.tasomaniac.android.pomodoro.shared.data.prefs.DateTimePreference;
import com.tasomaniac.android.pomodoro.shared.data.prefs.EnumPreference;
import com.tasomaniac.android.pomodoro.shared.data.prefs.IntPreference;
import com.tasomaniac.android.pomodoro.shared.data.prefs.LastPomodoro;
import com.tasomaniac.android.pomodoro.shared.data.prefs.NextPomodoro;
import com.tasomaniac.android.pomodoro.shared.model.ActivityType;

import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Singleton;

import hugo.weaving.DebugLog;

/**
 * Created by Said Tahsin Dane on 20/03/15.
 */
@Singleton
public class PomodoroMaster {

    private final DateTimePreference nextPomodoroStorage;
    private final DateTimePreference lastPomodoroStorage;
    private final IntPreference pomodorosDoneStorage;
    private final BooleanPreference isOngoingStorage;
    private final EnumPreference<ActivityType> activityTypeStorage;

    private final Application app;

    @Inject public PomodoroMaster(@NextPomodoro DateTimePreference nextPomodoroStorage,
                                  @LastPomodoro DateTimePreference lastPomodoroStorage,
                                  IntPreference pomodorosDoneStorage,
                                  EnumPreference<ActivityType> activityTypeStorage,
                                  BooleanPreference isOngoingStorage,
                                  Application app) {
        this.nextPomodoroStorage = nextPomodoroStorage;
        this.lastPomodoroStorage = lastPomodoroStorage;
        this.pomodorosDoneStorage = pomodorosDoneStorage;
        this.activityTypeStorage = activityTypeStorage;
        this.isOngoingStorage = isOngoingStorage;
        this.app = app;
    }

    /**
     * Starts a pomodoro session with a given finish time.
     *
     * @param nextActivityType ActivityType to start.
     * @param nextPomodoro next pomodoro time.
     */
    @DebugLog
    public void start(@NonNull final ActivityType nextActivityType,
                      @NonNull DateTime nextPomodoro) {
        nextPomodoroStorage.set(nextPomodoro);
        activityTypeStorage.set(nextActivityType);
        isOngoingStorage.set(true);
    }

    /**
     * Check and reset pomodoro count if we are in the next day.
     */
    public void check() {
        DateTime now = DateTime.now();
        DateTime last = lastPomodoroStorage.get();
        if (!Utils.isTheSamePomodoroDay(last, now)) {
            pomodorosDoneStorage.set(0);
        }
    }

    /**
     * Stop a pomodoro and set the type to NONE.
     *
     * @return Returns the Pomodoro that is just stopped.
     */
    @NonNull
    public ActivityType stop() {
        ActivityType stoppingForType = activityTypeStorage.get();
        if (stoppingForType.isBreak()) {
            pomodorosDoneStorage.set(pomodorosDoneStorage.get() + 1);
            lastPomodoroStorage.set(DateTime.now());
        }
        activityTypeStorage.set(ActivityType.NONE);
        isOngoingStorage.set(false);
        return stoppingForType;
    }

    public void setNextPomodoro(@Nullable DateTime nextPomodoro) {
        nextPomodoroStorage.set(nextPomodoro);
    }

    @Nullable public DateTime getNextPomodoro() {
        return nextPomodoroStorage.get();
    }

    public void setLastPomodoro(@Nullable DateTime lastPomodoro) {
        lastPomodoroStorage.set(lastPomodoro);
    }

    @Nullable public DateTime getLastPomodoro() {
        return lastPomodoroStorage.get();
    }

    public void setPomodorosDone(int pomodorosDone) {
        pomodorosDoneStorage.set(pomodorosDone);
    }

    public int getPomodorosDone() {
        return pomodorosDoneStorage.get();
    }

    public void setActivityType(@Nullable ActivityType activityType) {
        activityTypeStorage.set(activityType);
    }

    @NonNull public ActivityType getActivityType() {
        return activityTypeStorage.get();
    }

    /**
     * The state of the current pomodoro.
     *
     * @return true if the count down is active.
     */
    public boolean isOngoing() {
        return isOngoingStorage.get();
    }

    public void setOngoing(boolean isOngoing) {
        isOngoingStorage.set(isOngoing);
    }
}
