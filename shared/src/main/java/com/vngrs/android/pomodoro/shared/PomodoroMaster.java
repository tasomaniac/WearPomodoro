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
import javax.inject.Singleton;

import hugo.weaving.DebugLog;

/**
 * Created by Said Tahsin Dane on 20/03/15.
 */
@Singleton
public class PomodoroMaster {

    public static final String EXTRA_ACTIVITY_TYPE = "com.vngrs.android.pomodoro.extra.ACTIVITY_TYPE";

    public static final String ACTION_START = "com.vngrs.android.pomodoro.action.START";
    public static final String ACTION_STOP = "com.vngrs.android.pomodoro.action.STOP";
//    public static final String ACTION_PAUSE = "com.vngrs.android.pomodoro.action.PAUSE";
//    public static final String ACTION_RESUME = "com.vngrs.android.pomodoro.action.RESUME";
    public static final String ACTION_RESET = "com.vngrs.android.pomodoro.action.RESET";
    public static final String ACTION_UPDATE = "com.vngrs.android.pomodoro.action.UPDATE";
    public static final String ACTION_FINISH_ALARM = "com.vngrs.android.pomodoro.action.ALARM";

    public static final Intent START_INTENT = new Intent(ACTION_START);
    public static final Intent STOP_INTENT = new Intent(ACTION_STOP);
//    public static final Intent PAUSE_INTENT = new Intent(ACTION_PAUSE);
//    public static final Intent RESUME_INTENT = new Intent(ACTION_RESUME);
    public static final Intent RESET_INTENT = new Intent(ACTION_RESET);
    public static final Intent UPDATE_INTENT = new Intent(ACTION_UPDATE);
    public static final Intent FINISH_ALARM = new Intent(ACTION_FINISH_ALARM);



    public interface PomodoroMasterListener {
        void syncNotification(ActivityType activityType, DateTime nextPomodoro,
                              int pomodorosDone, boolean screenOn, boolean isOngoing);
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

    private boolean isOngoing;

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

    @DebugLog
    public void handleStart(final ActivityType nextActivityType) {
        DateTime now = DateTime.now();
        DateTime nextPomodoro = now.plus(nextActivityType.getLengthInMillis());
        nextPomodoroStorage.set(nextPomodoro);
        activityTypeStorage.set(nextActivityType);

        scheduleAlarms(nextPomodoro);

        isOngoing = true;
        if (mListener != null) {
            mListener.syncNotification(nextActivityType, nextPomodoro,
                    pomodorosDoneStorage.get(), isScreenOn(), isOngoing);
        }
    }

    @DebugLog
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

        isOngoing = false;
        if (mListener != null) {
            mListener.syncNotification(nextActivityType, null,
                    pomodorosDoneStorage.get(), isScreenOn(), isOngoing);
        }
    }

    @DebugLog
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
                    pomodorosDoneStorage.get(), isScreenOn(), isOngoing);
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
        int oneMinuteMs = 20 * 1000;
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
    public static NotificationCompat.Builder createBaseNotification(@NonNull Context context,
                                                                    boolean isOngoing) {

        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(isOngoing ? Notification.PRIORITY_HIGH : Notification.PRIORITY_DEFAULT)
                .setOngoing(isOngoing)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(getNotificationColor(context, isOngoing));
    }

    public static int getNotificationColor(@NonNull Context context, boolean isOngoing) {
        return context.getResources().getColor(isOngoing ? R.color.ongoing_red : R.color.finished_green);
    }

    @NonNull
    public static NotificationCompat.Builder createNotificationBuilderForActivityType(@NonNull Context context,
                                                                                      @NonNull PendingIntent contentIntent,
                                                                                      @NonNull ActivityType activityType,
                                                                                      int pomodorosDone,
                                                                                      @Nullable DateTime nextPomodoro,
                                                                                      boolean isScreenOn,
                                                                                      boolean isOngoing) {
        final String message = messageForActivityType(context, activityType, pomodorosDone, nextPomodoro, isOngoing);
        final String title = titleForActivityType(context, activityType, pomodorosDone, nextPomodoro, isOngoing);

        NotificationCompat.Builder builder = createBaseNotification(context, isOngoing)
                .setWhen(nextPomodoro != null ? nextPomodoro.getMillis() : System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(message);

        return builder;
    }

    public static NotificationCompat.Action createStartAction(@NonNull Context context,
                                                              @DrawableRes int actionIcon,
                                                              @NonNull ActivityType activityType) {
        final Intent startActionIntent = new Intent(ACTION_START);
        startActionIntent.putExtra(EXTRA_ACTIVITY_TYPE, activityType.value());
        final PendingIntent startActionPendingIntent;
        if (context.getPackageManager().resolveService(startActionIntent, 0) != null) {
            startActionPendingIntent =
                    PendingIntent.getService(context, 0, startActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            startActionPendingIntent =
                    PendingIntent.getBroadcast(context, 0, startActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        return new NotificationCompat.Action.Builder(actionIcon,
                context.getString(R.string.start), startActionPendingIntent).build();
    }

    public static NotificationCompat.Action createStopAction(@NonNull Context context, @DrawableRes int actionIcon) {
        final Intent stopActionIntent = new Intent(ACTION_STOP);
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
        return PendingIntent.getBroadcast(context, 1, FINISH_ALARM, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static PendingIntent createPendingIntentTickAlarmBroadcast(Context context) {
        return PendingIntent.getBroadcast(context, 2, UPDATE_INTENT, PendingIntent.FLAG_CANCEL_CURRENT);
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
                                              int pomodorosDone,
                                              DateTime nextPomodoro,
                                              boolean isOngoing) {
        if (isOngoing) {
            return convertDiffToPrettyMinutesLeft(context, nextPomodoro.getMillis() - System.currentTimeMillis());
        } else {
            return context.getString(R.string.title_finished);
        }
    }

    public static String messageForActivityType(@NonNull Context context,
                                                @NonNull ActivityType activityType,
                                                int pomodorosDone,
                                                DateTime nextPomodoro,
                                                boolean isOngoing) {
        if (isOngoing) {
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
        } else {
            switch (activityType) {
                case LONG_BREAK:
                    return context.getString(R.string.message_break_long);
                case POMODORO:
                    return context.getString(R.string.message_pomodoro_no, (pomodorosDone + 1));
                case SHORT_BREAK:
                    return context.getString(R.string.message_break_short);
                default:
                    throw new IllegalStateException("unsupported activityType " + activityType);
            }
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
