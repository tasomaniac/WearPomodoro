package com.vngrs.android.pomodoro.wear.service;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.shared.service.BaseNotificationService;
import com.vngrs.android.pomodoro.shared.NotificationBuilder;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.wear.App;
import com.vngrs.android.pomodoro.wear.ui.PomodoroNotificationActivity;

/**
 * Created by tasomaniac on 12/4/15.
 */
public class PomodoroNotificationService extends BaseNotificationService {

    @Override
    public void onCreate() {
        App.get(this).component().inject(this);
        super.onCreate();
    }

    @Nullable
    @Override
    public Notification buildNotification(Context context, PomodoroMaster pomodoroMaster) {

        final Intent displayIntent = new Intent(context, PomodoroNotificationActivity.class);

        NotificationBuilder builder = new NotificationBuilder(context,
                pomodoroMaster,
                R.mipmap.ic_launcher,
                R.drawable.ic_action_start_96dp,
                R.drawable.ic_action_stop_96dp,
                R.drawable.ic_action_reset_96dp);
        return builder.buildNotificationWear(displayIntent);
    }
}
