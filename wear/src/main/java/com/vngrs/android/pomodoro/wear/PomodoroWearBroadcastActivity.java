package com.vngrs.android.pomodoro.wear;

import android.app.Activity;
import android.os.Bundle;


/**
 * Example shell activity which simply broadcasts to our receiver and exits.
 */
public class PomodoroWearBroadcastActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendBroadcast(PomodoroNotificationService.UPDATE_INTENT);
        finish();
    }
}
