package com.vngrs.android.pomodoro.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.Utils;
import com.vngrs.android.pomodoro.shared.model.ActivityType;
import com.vngrs.android.pomodoro.shared.service.BaseNotificationService;
import com.vngrs.android.pomodoro.util.RecentTasksStyler;

import org.joda.time.DateTime;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {

    @Inject BaseUi baseUi;
    @Inject PomodoroMaster pomodoroMaster;

    @InjectView(R.id.pomodoro_progress) ProgressWheel mProgress;
    @InjectView(R.id.pomodoro_time) TextView mTime;
    @InjectView(R.id.pomodoro_description) TextView mDescription;
    @InjectView(R.id.pomodoro_start_stop_button) ImageButton mStartStopButton;

    private Handler handler = null;

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            update();
        }
    };

    private BroadcastReceiver pomodoroReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {

            if (intent != null) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateOnStateChange();

                        switch (intent.getAction()) {
                            case BaseNotificationService.ACTION_STOP:
                            case BaseNotificationService.ACTION_RESET:
                            case BaseNotificationService.ACTION_FINISH_ALARM:
                                handler.removeCallbacks(updateRunnable);
                                updateWithoutTimer(false);
                                break;
                            case BaseNotificationService.ACTION_START:
                                update();
                                break;
                            default:
                                updateWithoutTimer(false);
                                break;
                        }
                    }
                }, 100);
            }
        }
    };

    private static final IntentFilter FILTER_START =
            new IntentFilter(BaseNotificationService.ACTION_START);
    private static final IntentFilter FILTER_STOP =
            new IntentFilter(BaseNotificationService.ACTION_STOP);
    private static final IntentFilter FILTER_RESET =
            new IntentFilter(BaseNotificationService.ACTION_RESET);
    private static final IntentFilter FILTER_UPDATE =
            new IntentFilter(BaseNotificationService.ACTION_UPDATE);
    private static final IntentFilter FILTER_FINISH_ALARM =
            new IntentFilter(BaseNotificationService.ACTION_FINISH_ALARM);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.get(this).component().inject(this);
        setPomodoroTheme();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        handler = new Handler();
    }

    private void setPomodoroTheme() {
        setTheme(pomodoroMaster.isOngoing() && pomodoroMaster.getActivityType() == ActivityType.POMODORO
                ? R.style.Theme_Pomodoro_Ongoing : R.style.Theme_Pomodoro_Finished);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateOnStateChange();
        updateWithoutTimer(false);
        update();

        registerReceiver(pomodoroReceiver, FILTER_START);
        registerReceiver(pomodoroReceiver, FILTER_STOP);
        registerReceiver(pomodoroReceiver, FILTER_RESET);
        registerReceiver(pomodoroReceiver, FILTER_UPDATE);
        registerReceiver(pomodoroReceiver, FILTER_FINISH_ALARM);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
        unregisterReceiver(pomodoroReceiver);
    }

    private void nextTimer() {
        handler.removeCallbacks(updateRunnable);
        handler.postDelayed(updateRunnable, Utils.SECOND_MILLIS);
    }

    private void update() {
        updateWithoutTimer(true);
        nextTimer();
    }

    private void updateOnStateChange() {
        final int colorPrimary = Utils.getPrimaryColor(this, pomodoroMaster);
        final int colorPrimaryDark = Utils.getNotificationColorDark(this, pomodoroMaster);

        setPomodoroTheme();
        getWindow().setBackgroundDrawable(new ColorDrawable(colorPrimary));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(colorPrimaryDark);
        }
        RecentTasksStyler.styleRecentTasksEntry(this, colorPrimaryDark);

        if (mProgress != null) {
            mProgress.setRimColor(colorPrimaryDark);
            mProgress.setSpinSpeed(1 /  ((float) pomodoroMaster.getActivityType().getLengthInMillis() / 1000));
        }
    }

    private void updateWithoutTimer(final boolean animate) {

        if (pomodoroMaster.isOngoing()) {
            mStartStopButton.setImageResource(R.drawable.ic_action_stop_96dp);
            final DateTime nextPomodoro = pomodoroMaster.getNextPomodoro();
            if (nextPomodoro != null) {
                setPomodoroProgress(1 - (float) (nextPomodoro.getMillis() - DateTime.now().getMillis()) / pomodoroMaster.getActivityType().getLengthInMillis(),
                        animate);
            } else {
                setPomodoroProgress(0f);
            }
            mTime.setText(Utils.getRemainingTime(pomodoroMaster, /* shorten */ false));
            mDescription.setText(Utils.getActivityTitle(this, pomodoroMaster, /* shorten */ false));
        } else {
            mStartStopButton.setImageResource(R.drawable.ic_action_start_96dp);
            setPomodoroProgress(0f);
            mTime.setText("00:00");
            mDescription.setText(Utils.getActivityFinishMessage(this, pomodoroMaster));
        }
    }

    private void setPomodoroProgress(float progress) {
        setPomodoroProgress(progress, true);
    }

    private void setPomodoroProgress(float progress, boolean animate) {
        if (animate) {
            mProgress.setProgress(progress);
        } else {
            mProgress.setInstantProgress(progress);
        }
    }

    @OnClick(R.id.pomodoro_start_stop_button)
    public void start() {

        if (pomodoroMaster.isOngoing()) {
            sendOrderedBroadcast(BaseNotificationService.STOP_INTENT, null);
        } else {
            ActivityType activityType = pomodoroMaster.getActivityType();
            if (activityType == ActivityType.NONE) {
                activityType = ActivityType.POMODORO;
            }
            final Intent startIntent = BaseNotificationService.START_INTENT;
            startIntent.putExtra(BaseNotificationService.EXTRA_ACTIVITY_TYPE, activityType.value());
            sendOrderedBroadcast(startIntent, null);
        }
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
