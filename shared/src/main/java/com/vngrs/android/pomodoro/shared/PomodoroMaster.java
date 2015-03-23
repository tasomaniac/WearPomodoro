package com.vngrs.android.pomodoro.shared;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.vngrs.android.pomodoro.shared.data.prefs.DateTimePreference;
import com.vngrs.android.pomodoro.shared.data.prefs.EnumPreference;
import com.vngrs.android.pomodoro.shared.data.prefs.IntPreference;
import com.vngrs.android.pomodoro.shared.data.prefs.LastPomodoro;
import com.vngrs.android.pomodoro.shared.data.prefs.NextPomodoro;
import com.vngrs.android.pomodoro.shared.model.ActivityType;

import org.joda.time.DateTime;

import javax.inject.Inject;

/**
 * Created by Said Tahsin Dane on 20/03/15.
 */
public class PomodoroMaster {

    public static final String EXTRA_ACTIVITY_TYPE = "com.vngrs.android.pomodoro.extra.ACTIVITY_TYPE";

    public static final String ACTION_ALARM = "com.vngrs.android.pomodoro.action.ALARM";
    public static final String ACTION_ALARM_TICK = "com.vngrs.android.pomodoro.action.ALARM_TICK";
    public static final String ACTION_START = "com.vngrs.android.pomodoro.action.START";
    public static final String ACTION_STOP = "com.vngrs.android.pomodoro.action.STOP";

    public interface PomodoroMasterListener {
        void syncNotification(ActivityType activityType, DateTime nextPomodoro,
                              int pomodorosDone, boolean screenOn);
    }

    private final NotificationManagerCompat notificationManager;
    private final AlarmManager alarmManager;
    private final PowerManager powerManager;

    private final DateTimePreference nextPomodoroStorage;
    private final DateTimePreference lastPomodoroStorage;
    private final IntPreference pomodorosDoneStorage;
    private final EnumPreference<ActivityType> activityTypeStorage;

    private final Application app;

    private PomodoroMasterListener mListener;

    @Inject public PomodoroMaster(NotificationManagerCompat notificationManager,
                                  AlarmManager alarmManager,
                                  PowerManager powerManager,
                                  @NextPomodoro DateTimePreference nextPomodoroStorage,
                                  @LastPomodoro DateTimePreference lastPomodoroStorage,
                                  IntPreference pomodorosDoneStorage,
                                  EnumPreference<ActivityType> activityTypeStorage,
                                  Application app) {
        this.notificationManager = notificationManager;
        this.alarmManager = alarmManager;
        this.powerManager = powerManager;
        this.nextPomodoroStorage = nextPomodoroStorage;
        this.lastPomodoroStorage = lastPomodoroStorage;
        this.pomodorosDoneStorage = pomodorosDoneStorage;
        this.activityTypeStorage = activityTypeStorage;
        this.app = app;
    }

    public void setPomodoroMasterListener(PomodoroMasterListener mListener) {
        this.mListener = mListener;
    }

    public void handleStop() {

    }

    public void handleStart(final ActivityType nextActivityType) {
        DateTime now = DateTime.now();
        DateTime nextPomodoro = now.plus(nextActivityType.getLengthInMillis());
        nextPomodoroStorage.set(nextPomodoro);
        activityTypeStorage.set(nextActivityType);

        scheduleAlarms(nextPomodoro);

        if (mListener != null) {
            mListener.syncNotification(nextActivityType, nextPomodoro,
                    pomodorosDoneStorage.get(), isScreenOn());
        }
    }

    public void handleAlarm() {
        ActivityType justStoppedActivityType = stop();
        final ActivityType nextActivityType;
        if (justStoppedActivityType.isPomodoro()) {
            if ((pomodorosDoneStorage.get() + 1) % Constants.POMODORO_NUMBER_FOR_LONG_BREAK == 0) {
                nextActivityType = ActivityType.LONG_BREAK;
            } else {
                nextActivityType = ActivityType.SHORT_BREAK;
            }
        } else {
            nextActivityType = ActivityType.POMODORO;
        }
    }

    public void handleAlarmTick() {
        syncNotification();
    }
    /**
     * Check and reset pomodoro count if we are in the next day.
     */
    public void check() {
        DateTime now = DateTime.now();
        DateTime last = lastPomodoroStorage.get();
        if (!isTheSamePomodoroDay(last, now)) {
            pomodorosDoneStorage.set(0);
        }
    }

    public boolean isActive() {
        return activityTypeStorage.get() != ActivityType.NONE;
    }

    public void syncNotification() {
        if (mListener != null) {
            mListener.syncNotification(activityTypeStorage.get(), nextPomodoroStorage.get(),
                    pomodorosDoneStorage.get(), isScreenOn());
        }
    }

    @NonNull
    public ActivityType stop() {
        ActivityType stoppingForType = activityTypeStorage.get();
        if (stoppingForType.isBreak()) {
            pomodorosDoneStorage.set(pomodorosDoneStorage.get() + 1);
            lastPomodoroStorage.set(DateTime.now());
        }
        activityTypeStorage.set(ActivityType.NONE);
        unscheduleAlarms();
        return stoppingForType;
    }

    private void scheduleAlarms(DateTime whenMs) {

        PendingIntent pendingAlarmIntent = createPendingIntentAlarm(app);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, whenMs.getMillis(), pendingAlarmIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, whenMs.getMillis(), pendingAlarmIntent);
        }
        PendingIntent pendingAlarmTickIntent = createPendingIntentTickAlarmBroadcast(app);
        DateTime now = DateTime.now();
        int oneMinuteMs = 40 * 1000;
        int fiveSecondsMs = 5 * 1000;
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getMillis() + fiveSecondsMs, oneMinuteMs, pendingAlarmTickIntent);
    }

    private void unscheduleAlarms() {
        PendingIntent pendingAlarmIntent = createPendingIntentAlarm(app);
        alarmManager.cancel(pendingAlarmIntent);
        PendingIntent pendingAlarmTickIntent = createPendingIntentTickAlarmBroadcast(app);
        alarmManager.cancel(pendingAlarmTickIntent);
    }

//    private void startDisplayService() {
//        startService(new Intent(this, PomodoroNotificationService.class));
//    }
//
//    private void stopDisplayService() {
//        stopService(new Intent(this, PomodoroNotificationService.class));
//    }

    @SuppressWarnings("deprecation")
    private boolean isScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerManager.isInteractive();
        } else {
            return powerManager.isScreenOn();
        }
    }

    public static String convertDiffToPrettyMinutesLeft(Context context, long diffMs) {
        diffMs = Math.max(0, diffMs);
        int secondsTotal = (int) diffMs / 1000;
        int minutes = secondsTotal / 60;
        if (minutes == 0) {
            return context.getString(R.string.time_left_less_than_minute);
        } else {
            return context.getResources().getQuantityString(R.plurals.time_left_minutes, minutes, minutes);
        }
    }

    @NonNull
    public static NotificationCompat.Builder createNotificationBuilderForActivityType(@NonNull Context context,
                                                                        @NonNull Intent contentIntent,
                                                                        @NonNull ActivityType activityType,
                                                                        int pomodorsDone,
                                                                        @NonNull DateTime whenMs,
                                                                        boolean isScreenOn) {
//        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
//                .
//                .addAction(stopAction);
//                .setBackground(BitmapFactory.decodeResource(context.getResources(), backgroundResourceForActivityType(activityType)));

        final String minutesLeft =
                convertDiffToPrettyMinutesLeft(context, whenMs.getMillis() - System.currentTimeMillis());
        final PendingIntent contentPendingIntent =
                PendingIntent.getActivity(context, 0, contentIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(activityType != ActivityType.NONE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(whenMs.getMillis())
                .setContentIntent(contentPendingIntent)
                .setContentTitle(titleForActivityType(context, activityType, pomodorsDone))
                .setContentText(minutesLeft)
//                .setContentInfo(minutesLeft)
                .setTicker(minutesLeft);

        return builder;
    }

    public static NotificationCompat.Action createStartAction(@NonNull Context context, @DrawableRes int actionIcon) {
        PendingIntent startActionPendingIntent = createPendingIntentStart(context);

        return new NotificationCompat.Action.Builder(actionIcon,
                context.getString(R.string.start), startActionPendingIntent).build();
    }

    public static PendingIntent createPendingIntentStart(@NonNull Context context) {
        Intent startActionIntent = new Intent(ACTION_START);
        return PendingIntent.getService(context, 0, startActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static NotificationCompat.Action createStopAction(@NonNull Context context, @DrawableRes int actionIcon) {
        Intent stopActionIntent = new Intent(ACTION_STOP);
        final PendingIntent stopActionPendingIntent;
        if (context.getPackageManager().resolveService(stopActionIntent, 0) != null) {
            stopActionPendingIntent =
                    PendingIntent.getService(context, 0, stopActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            stopActionPendingIntent =
                    PendingIntent.getBroadcast(context, 0, stopActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        return new NotificationCompat.Action.Builder(actionIcon,
                context.getString(R.string.stop), stopActionPendingIntent).build();
    }

    public static PendingIntent createPendingIntentAlarm(@NonNull Context context) {
        Intent intent = new Intent(ACTION_ALARM);
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static PendingIntent createPendingIntentTickAlarmBroadcast(Context context) {
        Intent intent = new Intent(ACTION_ALARM_TICK);
        return PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

//    public static int backgroundResourceForActivityType(ActivityType activityType) {
//        switch (activityType) {
//            case LONG_BREAK:
//                return R.drawable.bg_long_break;
//            case POMODORO:
//                return R.drawable.bg_pomodoro;
//            case SHORT_BREAK:
//                return R.drawable.bg_short_break;
//        }
//        throw new IllegalStateException("unsupported activityType " + activityType);
//    }

    public static String titleForActivityType(@NonNull Context context,
                                              @NonNull ActivityType activityType,
                                              int pomodorosDone) {
        switch (activityType) {
            case LONG_BREAK:
                return context.getString(R.string.title_break_long);
            case POMODORO:
                return context.getString(R.string.title_pomodoro_no, (pomodorosDone + 1));
            case SHORT_BREAK:
                return context.getString(R.string.title_break_short);
            default:
                throw new IllegalStateException("unsupported activityType " + activityType);
        }
    }

    public static boolean isTheSamePomodoroDay(@Nullable DateTime first,
                                               @Nullable DateTime second) {
        if (first != null && second != null) {
            boolean sameDay = first.getYear() == second.getYear()
                    && first.getDayOfYear() == second.getDayOfYear();
            boolean isBothAfter6am = first.getHourOfDay() > 6 && second.getHourOfDay() > 6;
            return sameDay && isBothAfter6am;
        } else {
            return false;
        }
    }
}
