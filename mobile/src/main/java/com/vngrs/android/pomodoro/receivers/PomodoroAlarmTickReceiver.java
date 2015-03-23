package com.vngrs.android.pomodoro.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.vngrs.android.pomodoro.service.PomodoroService;

import hugo.weaving.DebugLog;

public class PomodoroAlarmTickReceiver extends WakefulBroadcastReceiver {

    @DebugLog
    @Override
    public void onReceive(Context context, Intent intent) {
        final Intent service = new Intent(context, PomodoroService.class);
        service.setAction(intent.getAction());
        startWakefulService(context, service);
    }

}