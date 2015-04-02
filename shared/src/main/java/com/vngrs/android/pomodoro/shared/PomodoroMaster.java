package com.vngrs.android.pomodoro.shared;

import android.app.AlarmManager;
import android.app.Application;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;

import com.vngrs.android.pomodoro.shared.data.prefs.DateTimePreference;
import com.vngrs.android.pomodoro.shared.data.prefs.EnumPreference;
import com.vngrs.android.pomodoro.shared.data.prefs.IntPreference;
import com.vngrs.android.pomodoro.shared.data.prefs.LastPomodoro;
import com.vngrs.android.pomodoro.shared.data.prefs.NextPomodoro;
import com.vngrs.android.pomodoro.shared.model.ActivityType;

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
    private final EnumPreference<ActivityType> activityTypeStorage;

    private final Application app;

    private boolean isOngoing;

    @Inject public PomodoroMaster(NotificationManagerCompat notificationManager,
                                  AlarmManager alarmManager,
                                  @NextPomodoro DateTimePreference nextPomodoroStorage,
                                  @LastPomodoro DateTimePreference lastPomodoroStorage,
                                  IntPreference pomodorosDoneStorage,
                                  EnumPreference<ActivityType> activityTypeStorage,
                                  Application app) {
        this.nextPomodoroStorage = nextPomodoroStorage;
        this.lastPomodoroStorage = lastPomodoroStorage;
        this.pomodorosDoneStorage = pomodorosDoneStorage;
        this.activityTypeStorage = activityTypeStorage;
        this.app = app;
    }

    @DebugLog
    public void start(final ActivityType nextActivityType) {
        DateTime now = DateTime.now();
        DateTime nextPomodoro = now.plus(nextActivityType.getLengthInMillis());
        nextPomodoroStorage.set(nextPomodoro);
        activityTypeStorage.set(nextActivityType);

        isOngoing = true;
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

    public boolean isActive() {
        return activityTypeStorage.get() != ActivityType.NONE;
    }


    @NonNull
    public ActivityType stop() {
        ActivityType stoppingForType = activityTypeStorage.get();
        if (stoppingForType.isBreak()) {
            pomodorosDoneStorage.set(pomodorosDoneStorage.get() + 1);
            lastPomodoroStorage.set(DateTime.now());
        }
        activityTypeStorage.set(ActivityType.NONE);

        isOngoing = false;
        return stoppingForType;
    }

    public void setNextPomodoro(DateTime nextPomodoro) {
        nextPomodoroStorage.set(nextPomodoro);
    }

    public DateTime getNextPomodoro() {
        return nextPomodoroStorage.get();
    }

    public DateTime getLastPomodoro() {
        return lastPomodoroStorage.get();
    }

    public int getPomodorosDone() {
        return pomodorosDoneStorage.get();
    }

    public void setActivityType(ActivityType activityType) {
        activityTypeStorage.set(activityType);
    }

    public ActivityType getActivityType() {
        return activityTypeStorage.get();
    }

    public boolean isOngoing() {
        return isOngoing;
    }
}
