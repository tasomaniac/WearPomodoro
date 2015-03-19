package com.vngrs.android.pomodoro.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.vngrs.android.pomodoro.BuildConfig;

import hugo.weaving.DebugLog;

public class PomodoroAlarmTickReceiver extends WakefulBroadcastReceiver {

    public static final String ACTION = BuildConfig.APPLICATION_ID + ".action.ALARM_TICK";

    @DebugLog
    @Override
    public void onReceive(Context context, Intent intent) {
//        App.get(context).component().inject(this);
//        pomodoroMaster.syncNotification();
    }

}