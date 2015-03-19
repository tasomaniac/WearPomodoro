package com.vngrs.android.pomodoro.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.BuildConfig;
import com.vngrs.android.pomodoro.Constants;
import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.data.prefs.DateTimePreference;
import com.vngrs.android.pomodoro.data.prefs.EnumPreference;
import com.vngrs.android.pomodoro.data.prefs.IntPreference;
import com.vngrs.android.pomodoro.data.prefs.LastPomodoroTimeStamp;
import com.vngrs.android.pomodoro.data.prefs.NextPomodoroTimeStamp;
import com.vngrs.android.pomodoro.model.ActivityType;
import com.vngrs.android.pomodoro.receivers.PomodoroAlarmReceiver;

import org.joda.time.DateTime;

import javax.inject.Inject;

import timber.log.Timber;

public class PomodoroService extends Service {

    public static final String ACTION_ALARM = BuildConfig.APPLICATION_ID + ".action.ALARM";
    public static final String ACTION_START = BuildConfig.APPLICATION_ID + ".action.START";
    public static final String ACTION_STOP = BuildConfig.APPLICATION_ID + ".action.STOP";

    private static final int NOTIFICATION_ID = 1;

    @Inject NotificationManager notificationManager;
    @Inject AlarmManager alarmManager;
    @Inject PowerManager powerManager;

    @Inject @NextPomodoroTimeStamp DateTimePreference nextPomodoroStorage;
    @Inject @LastPomodoroTimeStamp DateTimePreference lastPomodoroStorage;
    @Inject IntPreference pomodorosDoneStorage;
    @Inject EnumPreference<ActivityType> activityTypeStorage;

    private NotificationCompat.Builder mNotificationBuilder;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.get(this).component().inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        check();

        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_STOP:
                    stopSelf();
                    break;
                case ACTION_ALARM:
                    //TODO change the notification with a message and start button.
//                    ActivityType stoppingForType = activityTypeStorage.get();
                    PomodoroAlarmReceiver.completeWakefulIntent(intent);
                    break;
                case ACTION_START:
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
                    start(nextActivityType);
                    break;
                default:
                    break;
            }
        } else {
            syncNotification();
        }

//        mNotificationBuilder = new NotificationCompat.Builder(this);
//        mNotificationBuilder.setSmallIcon(R.drawable.ic_stat_upload)
//                .setOngoing(false)
//                .setProgress(0, 0, false)
//                .setAutoCancel(true)
//                .setOnlyAlertOnce(true)
//                .setWhen(System.currentTimeMillis())
//                .setContentTitle(title)
//                .setTicker(title)
//                .setContentText(text)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));
//
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        notificationIntent.putExtra(MainActivity.EXTRA_SELECTED_MENU_ITEM, Constants.DRAWER_MY_LISTINGS);
//        notificationIntent.putExtra(Constants.STATUS, uploadedListingStatus);
//        PendingIntent pendingIntent = PendingIntent
//                .getActivity(this, 0, notificationIntent, 0);
//        mNotificationBuilder.setContentIntent(pendingIntent);
//
//        mNotificationMgr.notify(GCMIntentService.NOTIF_ID_PHOTO_UPLOAD, mNotificationBuilder.build());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        stop();
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

    public void start(final ActivityType activityType) {
        DateTime now = DateTime.now();
        DateTime nextPomodoro = now.plus(activityType.getLengthInMillis());
        nextPomodoroStorage.set(nextPomodoro);
        activityTypeStorage.set(activityType);

        scheduleAlarms(nextPomodoro);
        syncNotification(activityType, nextPomodoro, isScreenOn());
    }

    public boolean isActive() {
        return activityTypeStorage.get() != ActivityType.NONE;
    }

    public void syncNotification() {
        syncNotification(activityTypeStorage.get(), nextPomodoroStorage.get(), isScreenOn());
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

        PendingIntent pendingAlarmIntent = createPendingIntentAlarm(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, whenMs.getMillis(), pendingAlarmIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, whenMs.getMillis(), pendingAlarmIntent);
        }
//        PendingIntent pendingAlarmTickIntent = createPendingIntentTickAlarmBroadcast(this);
//        DateTime now = DateTime.now();
//        int oneMinuteMs = 20 * 1000;
//        int fiveSecondsMs = 20 * 1000;
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getMillis() + fiveSecondsMs, oneMinuteMs, pendingAlarmTickIntent);
        //TODO Handler ile tick leri yap.
    }

    private void unscheduleAlarms() {
        PendingIntent pendingAlarmIntent = createPendingIntentAlarm(this);
        alarmManager.cancel(pendingAlarmIntent);
//        PendingIntent pendingAlarmTickIntent = createPendingIntentTickAlarmBroadcast(this);
//        alarmManager.cancel(pendingAlarmTickIntent);
    }

    private void syncNotification(ActivityType activityType, DateTime nextPomodoro, boolean screenOn) {
        if (activityType != ActivityType.NONE) {
            startForeground(NOTIFICATION_ID,
                    createNotificationBuilderForActivityType(this, activityType, pomodorosDoneStorage.get(), nextPomodoro, screenOn));
        } else {
            Timber.d("ignore notify for activityType " + ActivityType.NONE);
        }
    }

//    private void startDisplayService() {
//        startService(new Intent(this, PomodoroNotificationService.class));
//    }
//
//    private void stopDisplayService() {
//        stopService(new Intent(this, PomodoroNotificationService.class));
//    }

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
    private static Notification createNotificationBuilderForActivityType(@NonNull Context context,
                                                                         @NonNull ActivityType activityType,
                                                                         int pomodorsDone,
                                                                         @NonNull DateTime whenMs,
                                                                         boolean isScreenOn) {
        NotificationCompat.Action stopAction = createStopAction(context);

//        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
//                .
//                .addAction(stopAction);
//                .setBackground(BitmapFactory.decodeResource(context.getResources(), backgroundResourceForActivityType(activityType)));

        final String minutesLeft = convertDiffToPrettyMinutesLeft(context, whenMs.getMillis() - System.currentTimeMillis());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
//                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(activityType != ActivityType.NONE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(stopAction)
                .setWhen(whenMs.getMillis())
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT))
                .setContentTitle(titleForActivityType(context, activityType, pomodorsDone))
                .setContentText(minutesLeft)
                .setContentInfo(minutesLeft)
                .setTicker(minutesLeft)
                .extend(new NotificationCompat.WearableExtender().addAction(stopAction));


        return builder.build();
    }

    private static NotificationCompat.Action createStartAction(@NonNull Context context) {
        PendingIntent startActionPendingIntent = createPendingIntentStart(context);

        return new NotificationCompat.Action.Builder(R.drawable.ic_action_start,
                context.getString(R.string.start), startActionPendingIntent).build();
    }

    private static PendingIntent createPendingIntentStart(@NonNull Context context) {
        Intent startActionIntent = new Intent(PomodoroService.ACTION_START);
        return PendingIntent.getService(context, 0, startActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static NotificationCompat.Action createStopAction(@NonNull Context context) {
        PendingIntent stopActionPendingIntent = createPendingIntentStop(context);

        return new NotificationCompat.Action.Builder(R.drawable.ic_action_stop,
                context.getString(R.string.stop), stopActionPendingIntent).build();
    }

    private static PendingIntent createPendingIntentStop(@NonNull Context context) {
        Intent stopActionIntent = new Intent(PomodoroService.ACTION_STOP);
        return PendingIntent.getService(context, 0, stopActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static PendingIntent createPendingIntentAlarm(@NonNull Context context) {
        Intent intent = new Intent(PomodoroService.ACTION_ALARM);
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

//    private static PendingIntent createPendingIntentTickAlarmBroadcast(Context context) {
//        Intent intent = new Intent(PomodoroAlarmTickReceiver.ACTION);
//        return PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//    }

//    private static int backgroundResourceForActivityType(ActivityType activityType) {
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

    private static String titleForActivityType(@NonNull Context context,
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

    @SuppressWarnings("deprecation")
    private boolean isScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerManager.isInteractive();
        } else {
            return powerManager.isScreenOn();
        }
    }

    private static boolean isTheSamePomodoroDay(@Nullable DateTime first,
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
