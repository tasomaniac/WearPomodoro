package com.vngrs.android.pomodoro.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.receivers.PomodoroAlarmReceiver;
import com.vngrs.android.pomodoro.receivers.PomodoroAlarmTickReceiver;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.model.ActivityType;
import com.vngrs.android.pomodoro.ui.MainActivity;

import org.joda.time.DateTime;

import javax.inject.Inject;

import timber.log.Timber;

public class PomodoroService extends Service implements PomodoroMaster.PomodoroMasterListener {

    private static final int NOTIFICATION_ID = 1;

    @Inject PomodoroMaster pomodoroMaster;

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
        pomodoroMaster.setPomodoroMasterListener(this);
        pomodoroMaster.check();

        if (intent != null) {
            switch (intent.getAction()) {
                case PomodoroMaster.ACTION_STOP:
                    stopSelf();
                    break;
                case PomodoroMaster.ACTION_ALARM:
                    pomodoroMaster.handleAlarm();
                    PomodoroAlarmReceiver.completeWakefulIntent(intent);
                    break;
                case PomodoroMaster.ACTION_ALARM_TICK:
                    pomodoroMaster.handleAlarmTick();
                    PomodoroAlarmTickReceiver.completeWakefulIntent(intent);
                    break;
                case PomodoroMaster.ACTION_START:
                    final ActivityType activityType =
                        ActivityType.fromValue(intent.getIntExtra(PomodoroMaster.EXTRA_ACTIVITY_TYPE, 0));
                    if (activityType != ActivityType.NONE) {
                        pomodoroMaster.handleStart(activityType);
                    }
                    break;
                default:
                    break;
            }
        } else {
            pomodoroMaster.syncNotification();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        pomodoroMaster.stop();
    }

    @Override
    public void syncNotification(ActivityType activityType,
                                 DateTime nextPomodoro,
                                 int pomodorosDone,
                                 boolean screenOn,
                                 boolean isOngoing) {
        if (activityType != ActivityType.NONE) {
            final NotificationCompat.Action action;
            if (isOngoing) {
                action = PomodoroMaster.createStopAction(this, R.drawable.ic_action_stop);
            } else {
                action = PomodoroMaster.createStartAction(this, R.drawable.ic_action_start, activityType);
            }
            final Notification notification =
                    PomodoroMaster.createNotificationBuilderForActivityType(this,
                            new Intent(this, MainActivity.class),
                            activityType,
                            pomodorosDone,
                            nextPomodoro,
                            screenOn,
                            isOngoing)
                            .addAction(action)
                            .build();
            startForeground(NOTIFICATION_ID, notification);
        } else {
            Timber.d("ignore notify for activityType " + ActivityType.NONE);
        }
    }
}
