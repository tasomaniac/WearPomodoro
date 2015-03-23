package com.vngrs.android.pomodoro.wear;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.model.ActivityType;

import org.joda.time.DateTime;

import javax.inject.Inject;

import timber.log.Timber;


public class PomodoroNotificationReceiver extends BroadcastReceiver implements PomodoroMaster.PomodoroMasterListener {

    private static final int NOTIFICATION_ID = 1;
    private Context context;

    @Inject NotificationManagerCompat notificationManager;
    @Inject PomodoroMaster pomodoroMaster;

    public PomodoroNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        App.get(context).component().inject(this);
        pomodoroMaster.setPomodoroMasterListener(this);
        pomodoroMaster.check();

        if (intent != null) {
            switch (intent.getAction()) {
                case PomodoroMaster.ACTION_STOP:
                    notificationManager.cancel(NOTIFICATION_ID);
                    pomodoroMaster.stop();
                    break;
                case PomodoroMaster.ACTION_ALARM:
                    //TODO change the notification with a message and start button.
//                    ActivityType stoppingForType = activityTypeStorage.get();
                    break;
                case PomodoroMaster.ACTION_ALARM_TICK:
                    pomodoroMaster.handleAlarmTick();
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
    }

    @Override
    public void syncNotification(ActivityType activityType, DateTime nextPomodoro, int pomodorosDone, boolean screenOn) {
        if (activityType != ActivityType.NONE) {
            final Notification notification =
                    PomodoroMaster.createNotificationBuilderForActivityType(context,
                            new Intent(),
                            activityType,
                            pomodorosDone,
                            nextPomodoro,
                            screenOn)
                            .setLocalOnly(true)
                            .addAction(PomodoroMaster.createStopAction(context, R.drawable.ic_action_stop))
                            .build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            Timber.d("ignore notify for activityType " + ActivityType.NONE);
        }
    }
}