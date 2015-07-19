package com.tasomaniac.android.pomodoro.shared.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import hugo.weaving.DebugLog;

public abstract class BaseNotificationReceiver extends WakefulBroadcastReceiver {

    public abstract Intent getNotificationServiceIntent(Context context);

    @DebugLog
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = getNotificationServiceIntent(context);
        serviceIntent.setAction(intent.getAction());
        serviceIntent.setData(intent.getData());
        serviceIntent.putExtras(intent);
        startWakefulService(context, serviceIntent);
    }
}
