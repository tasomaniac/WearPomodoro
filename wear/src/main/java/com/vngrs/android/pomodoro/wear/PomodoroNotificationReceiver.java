package com.vngrs.android.pomodoro.wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vngrs.android.pomodoro.shared.PomodoroMaster;

import javax.inject.Inject;


public class PomodoroNotificationReceiver extends BroadcastReceiver {
    public static final String CONTENT_KEY = "contentText";

    private static final int NOTIFICATION_ID = 1;

    @Inject PomodoroMaster pomodoroMaster;

    public PomodoroNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        App.get(context).component().inject(this);

    }
}