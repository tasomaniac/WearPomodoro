package com.vngrs.android.pomodoro.wear;

import android.content.Context;
import android.content.Intent;

import com.vngrs.android.pomodoro.shared.BaseNotificationReceiver;

public class PomodoroNotificationReceiver extends BaseNotificationReceiver {

    public PomodoroNotificationReceiver() {
    }

    @Override
    public Intent getNotificationServiceIntent(Context context) {
        return new Intent(context, PomodoroNotificationService.class);
    }
}
