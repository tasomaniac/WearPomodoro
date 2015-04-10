package com.vngrs.android.pomodoro.receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.shared.BaseNotificationReceiver;
import com.vngrs.android.pomodoro.shared.NotificationBuilder;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.model.ActivityType;
import com.vngrs.android.pomodoro.ui.MainActivity;

/**
 * Created by Said Tahsin Dane on 02/04/15.
 */
public class PomodoroReceiver extends BaseNotificationReceiver {

    public PomodoroReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        App.get(context).component().inject(this);
        super.onReceive(context, intent);
    }

    @Nullable
    @Override
    public Notification buildNotification(Context context, PomodoroMaster pomodoroMaster) {
        if (pomodoroMaster.getActivityType() != ActivityType.NONE) {
            NotificationBuilder builder = new NotificationBuilder(context,
                    pomodoroMaster,
                    R.drawable.ic_stat_pomodoro,
                    R.drawable.ic_action_start_phone,
                    R.drawable.ic_action_stop_phone,
                    R.drawable.ic_action_reset_96dp);
            final Intent contentIntent = new Intent(context, MainActivity.class);
            return builder
                    .buildNotificationPhone(PendingIntent.getActivity(context, 0, contentIntent, 0));
        } else {
            return null;
        }
    }
}
