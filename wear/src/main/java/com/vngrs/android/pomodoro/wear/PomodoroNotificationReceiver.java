package com.vngrs.android.pomodoro.wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.model.ActivityType;

import org.joda.time.DateTime;

import javax.inject.Inject;

import timber.log.Timber;


public class PomodoroNotificationReceiver extends BroadcastReceiver implements PomodoroMaster.PomodoroMasterListener {

    private static final int NOTIFICATION_ID = 1;
    private Context context;

    @Inject
    NotificationManagerCompat notificationManager;
    @Inject
    PomodoroMaster pomodoroMaster;

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
                    pomodoroMaster.handleAlarm();
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
    public void syncNotification(ActivityType activityType,
                                 DateTime nextPomodoro,
                                 int pomodorosDone,
                                 boolean screenOn,
                                 boolean isOngoing) {
        if (activityType != ActivityType.NONE) {
            final NotificationCompat.Action action;
            if (isOngoing) {
                action = PomodoroMaster.createStopAction(context, R.drawable.ic_action_stop);
            } else {
                action = PomodoroMaster.createStartAction(context, R.drawable.ic_action_start, activityType);
            }
            NotificationCompat.Builder builder = PomodoroMaster.createNotificationBuilderForActivityType(context,
                    action.actionIntent,
                    activityType,
                    pomodorosDone,
                    nextPomodoro,
                    screenOn,
                    isOngoing)
                    .setLocalOnly(true)
                    .setContentText(null)
                    .addAction(action)
                    .extend(new NotificationCompat.WearableExtender()
                            .setHintHideIcon(true)
//                            .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_MEDIUM)
//                            .setContentAction(0)
                            .setContentIcon(isOngoing
                                    ? R.drawable.ic_action_stop_grey
                                    : R.drawable.ic_action_start_grey));
//            final Intent displayIntent = new Intent(context, PomodoroNotificationActivity.class);
//            displayIntent.putExtra(PomodoroNotificationActivity.EXTRA_ACTION_PENDING_INTENT, action.actionIntent);
//            displayIntent.putExtra(PomodoroNotificationActivity.EXTRA_IS_ONGOING, isOngoing);
//            final PendingIntent displayPendingIntent = PendingIntent.getActivity(context,
//                    0, displayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
////                    .setDefaults(Notification.DEFAULT_ALL)
////                    .setCategory(NotificationCompat.CATEGORY_ALARM)
////                    .setPriority(isOngoing ? Notification.PRIORITY_HIGH : Notification.PRIORITY_DEFAULT)
//                    .setOngoing(isOngoing)
////                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
////                    .setLocalOnly(true)
//                    .addAction(action)
//                    .extend(new NotificationCompat.WearableExtender()
//                            .setHintHideIcon(true)
//                            .setDisplayIntent(displayPendingIntent)
//                            .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_MEDIUM));
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } else {
            Timber.d("ignore notify for activityType " + ActivityType.NONE);
        }
    }
}