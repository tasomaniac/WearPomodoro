package com.vngrs.android.pomodoro.receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import com.vngrs.android.pomodoro.shared.BaseNotificationReceiver;
import com.vngrs.android.pomodoro.shared.NotificationBuilder;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.model.ActivityType;
import com.vngrs.android.pomodoro.ui.MainActivity;

import timber.log.Timber;

/**
 * Created by taso on 02/04/15.
 */
public class PomodoroReceiver extends BaseNotificationReceiver {

    @Override
    public void updateNotification(Context context, PomodoroMaster pomodoroMaster) {
        if (pomodoroMaster.getActivityType() != ActivityType.NONE) {

            NotificationBuilder builder = new NotificationBuilder(context, pomodoroMaster);

            final Intent contentIntent = new Intent(context, MainActivity.class);

            Notification notification = builder.buildNotificationPhone(context,
                    PendingIntent.getActivity(context, 0, contentIntent, 0));
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            Timber.d("ignore notify for activityType " + ActivityType.NONE);
        }
    }
}
