package com.vngrs.android.pomodoro.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.shared.Constants;
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
import hugo.weaving.DebugLog;


public class MainActivity extends ActionBarActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject BaseUi baseUi;
    @Inject PomodoroMaster pomodoroMaster;
    @Inject SharedPreferences pomodoroPreferences;

    @InjectView(R.id.background_reveal) View mRevealBackground;
    @InjectView(R.id.pomodoro_progress) ProgressWheel mProgress;
    @InjectView(R.id.pomodoro_start_stop_button) ImageButton mStartStopButton;
    @InjectView(R.id.pomodoro_time) TextView mTime;
    @InjectView(R.id.pomodoro_description) TextView mDescription;

    private Handler handler = null;

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            update();
        }
    };

    private int colorPrimary;
    private int colorPrimaryDark;
    private boolean isAttached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.get(this).component().inject(this);
        setPomodoroTheme();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if ("chromium".equals(Build.BRAND) && "chromium".equals(Build.MANUFACTURER)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        handler = new Handler();
        mRevealBackground.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRevealBackground.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                isAttached = true;
            }
        });
    }

    private void setPomodoroTheme() {
        setTheme(pomodoroMaster.isOngoing() && pomodoroMaster.getActivityType() == ActivityType.POMODORO
                ? R.style.Theme_Pomodoro_Ongoing : R.style.Theme_Pomodoro_Finished);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        //When the ongoing state changes,
        //Animate and update the UI and start timer.
        if (key.equals(Constants.KEY_POMODORO_ONGOING)) {
            updateOnStateChange(/* animate */ true);

            if (pomodoroMaster.isOngoing()) {
                update();
            } else {
                handler.removeCallbacks(updateRunnable);
                updateWithoutTimer(false);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Update the UI state without animation.
        //Start timer again, if it is ongoing.
        updateOnStateChange(/* animate */ false);
        updateWithoutTimer(/* animate */ false);
        if (pomodoroMaster.isOngoing()) {
            nextTimer();
        }

        pomodoroPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(updateRunnable);
        pomodoroPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @OnClick(R.id.pomodoro_start_stop_button)
    public void start() {

        if (pomodoroMaster.isOngoing()) {
            sendBroadcast(BaseNotificationService.STOP_INTENT);
        } else {
            ActivityType activityType = pomodoroMaster.getActivityType();
            if (activityType == ActivityType.NONE) {
                activityType = ActivityType.POMODORO;
            }
            final Intent startIntent = BaseNotificationService.START_INTENT;
            startIntent.putExtra(BaseNotificationService.EXTRA_ACTIVITY_TYPE, activityType.value());
            sendBroadcast(startIntent);
        }
    }

    @DebugLog
    private void nextTimer() {
        handler.removeCallbacks(updateRunnable);
        handler.postDelayed(updateRunnable, Utils.SECOND_MILLIS);
    }

    /**
     * Updates the UI and starts a timer.
     */
    private void update() {
        updateWithoutTimer(true);
        nextTimer();
    }

    /**
     * Update the UI with an optional animation.
     * @param animate true to animate progressbar.
     */
    private void updateWithoutTimer(final boolean animate) {

        if (pomodoroMaster.isOngoing()) {
            mStartStopButton.setImageResource(R.drawable.ic_action_stop_96dp);
            final DateTime nextPomodoro = pomodoroMaster.getNextPomodoro();
            if (nextPomodoro != null) {
                final float progress = 1 -
                        (float) (nextPomodoro.getMillis() - DateTime.now().getMillis())
                                / pomodoroMaster.getActivityType().getLengthInMillis();
                setPomodoroProgress(progress, animate);
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

    private void updateOnStateChange(boolean animate) {
        int newColorPrimary = Utils.getPrimaryColor(this, pomodoroMaster);
        animate = animate && newColorPrimary != colorPrimary && isAttached;

        colorPrimary = newColorPrimary;
        colorPrimaryDark = Utils.getNotificationColorDark(this, pomodoroMaster);
        final boolean reveal = colorPrimary == getResources().getColor(R.color.ongoing_red);

        if (animate) {

            final ObjectAnimator firstAnimator = ObjectAnimator.ofFloat(mProgress, "alpha", 0)
                    .setDuration(100);
            firstAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {

                    if (mProgress != null) {
                        mProgress.setRimColor(colorPrimaryDark);
                        mProgress.setSpinSpeed(1 / ((float) pomodoroMaster.getActivityType().getLengthInMillis() / 1000));
                    }

                    final Animator revealAnimator;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        int[] startStopLocation = new int[2];
                        mStartStopButton.getLocationInWindow(startStopLocation);
                        int touchPointX = startStopLocation[0] + mStartStopButton.getWidth() / 2;
                        int touchPointY = startStopLocation[1] + mStartStopButton.getHeight() / 2;

                        final int initialRadius = reveal ? 0 : mRevealBackground.getWidth();
                        final int finalRadius = reveal ? Math.max(mRevealBackground.getWidth(), mRevealBackground.getHeight()) : 0;
                        revealAnimator =
                                ViewAnimationUtils.createCircularReveal(mRevealBackground,
                                        touchPointX, touchPointY,
                                        initialRadius, finalRadius);
                        if (!reveal) {
                            getWindow().setBackgroundDrawable(new ColorDrawable(colorPrimary));
                        }
                    } else {
                        mRevealBackground.setAlpha(0f);
                        mRevealBackground.setBackgroundColor(colorPrimary);
                        revealAnimator = ObjectAnimator.ofFloat(mRevealBackground, "alpha", 1);
                    }
                    mRevealBackground.setVisibility(View.VISIBLE);

                    final ObjectAnimator lastAnimator = ObjectAnimator.ofFloat(mProgress, "alpha", 1)
                            .setDuration(150);
                    lastAnimator.setStartDelay(150);
                    lastAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            updateOnStateChangeInstant();
                        }
                    });

                    AnimatorSet as = new AnimatorSet();
                    as.playTogether(revealAnimator, lastAnimator);
                    as.start();
                }
            });
            firstAnimator.start();
        } else {
            if (mProgress != null) {
                mProgress.setRimColor(colorPrimaryDark);
                mProgress.setSpinSpeed(1 / ((float) pomodoroMaster.getActivityType().getLengthInMillis() / 1000));
            }
            updateOnStateChangeInstant();
        }
    }

    @DebugLog
    private void updateOnStateChangeInstant() {

        mStartStopButton.post(new Runnable() {
            @Override
            public void run() {
                RecentTasksStyler.styleRecentTasksEntry(MainActivity.this, colorPrimaryDark);
            }
        });
        mRevealBackground.setVisibility(View.INVISIBLE);
        setPomodoroTheme();
        getWindow().setBackgroundDrawable(new ColorDrawable(colorPrimary));
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

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                start();
                return true;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                sendBroadcast(BaseNotificationService.STOP_INTENT);
                return true;
            default:
                return super.onKeyUp(keyCode, event);
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
