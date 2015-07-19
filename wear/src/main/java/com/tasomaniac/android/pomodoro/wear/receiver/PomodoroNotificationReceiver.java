package com.tasomaniac.android.pomodoro.wear.receiver;

import android.content.Context;
import android.content.Intent;

import com.tasomaniac.android.pomodoro.shared.receiver.BaseNotificationReceiver;
import com.tasomaniac.android.pomodoro.wear.service.PomodoroNotificationService;

public class PomodoroNotificationReceiver extends BaseNotificationReceiver {

    public PomodoroNotificationReceiver() {
    }

    @Override
    public Intent getNotificationServiceIntent(Context context) {
        return new Intent(context, PomodoroNotificationService.class);
    }
}
