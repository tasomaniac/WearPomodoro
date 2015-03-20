package com.vngrs.android.pomodoro.wear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


/**
 * Example shell activity which simply broadcasts to our receiver and exits.
 */
public class PomodoroWearBroadcastActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent();
        i.setAction("com.vngrs.android.pomodoro.SHOW_NOTIFICATION");
        i.putExtra(PomodoroNotificationReceiver.CONTENT_KEY, getString(R.string.title));
        sendBroadcast(i);
        finish();
    }
}
