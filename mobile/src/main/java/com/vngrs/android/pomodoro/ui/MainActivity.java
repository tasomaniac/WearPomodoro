package com.vngrs.android.pomodoro.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.data.prefs.EnumPreference;
import com.vngrs.android.pomodoro.shared.model.ActivityType;
import com.vngrs.android.pomodoro.service.PomodoroService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {

    @Inject
    BaseUi baseUi;
    @Inject
    EnumPreference<ActivityType> activityTypeStorage;
    @InjectView(R.id.activity_type)
    TextView activityTypeTextView;
    @InjectView(R.id.start)
    Button startButton;
    @InjectView(R.id.stop)
    Button stopButton;
    boolean bound;
    PomodoroService pomodoroService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.get(this).component().inject(this);
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViews();
    }

    public void setupViews() {
        activityTypeTextView.setText(activityTypeStorage.get().toString());
        startButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.VISIBLE);
        switch (activityTypeStorage.get()) {
            case NONE:
                stopButton.setVisibility(View.GONE);
                break;
            case POMODORO:
            case SHORT_BREAK:
            case LONG_BREAK:
                startButton.setVisibility(View.GONE);
                break;
        }
    }

    @OnClick(R.id.start)
    public void start() {
        startService(new Intent(PomodoroMaster.ACTION_START, null, this, PomodoroService.class));
    }

    @OnClick(R.id.stop)
    public void stop() {
        startService(new Intent(PomodoroMaster.ACTION_STOP, null, this, PomodoroService.class));
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

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PomodoroService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PomodoroService.LocalBinder binder = (PomodoroService.LocalBinder) service;
            pomodoroService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
}
