package com.vngrs.android.pomodoro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.service.PomodoroService;
import com.vngrs.android.pomodoro.shared.BaseNotificationReceiver;
import com.vngrs.android.pomodoro.shared.data.prefs.EnumPreference;
import com.vngrs.android.pomodoro.shared.model.ActivityType;

import javax.inject.Inject;


public class MainActivity extends ActionBarActivity {

    @Inject BaseUi baseUi;
    @Inject EnumPreference<ActivityType> activityTypeStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.get(this).component().inject(this);

        final Intent intent = new Intent(this, PomodoroService.class);
        startService(intent);

        sendBroadcast(
                BaseNotificationReceiver.START_INTENT
                    .putExtra(BaseNotificationReceiver.EXTRA_ACTIVITY_TYPE,
                            ActivityType.POMODORO.value())
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return baseUi.onCreateOptionsMenu(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return baseUi.onOptionsItemSelected(this, item)
                || super.onOptionsItemSelected(item);
    }
}
