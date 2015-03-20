package com.vngrs.android.pomodoro.wear;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.Utils;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hugo.weaving.DebugLog;

public class PomodoroNotificationActivity extends Activity {

    private Handler handler = null;

    @InjectView(R.id.pomodoro_time) TextView mTime;
    @InjectView(R.id.pomodoro_description) TextView mDescription;
//    @InjectView(R.id.pomodoro_start_stop_button) ImageButton mStartStopButton;

    @Inject PomodoroMaster pomodoroMaster;

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            update();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_pomodoro_notification);
        App.get(this).component().inject(this);
        ButterKnife.inject(this);

        handler = new Handler();

        updateWithoutTimer();
    }

//    @OnClick(R.id.content)
//    public void onNotificationClicked() {
//
//        if (pomodoroMaster.isOngoing()) {
//            sendBroadcast(BaseNotificationReceiver.STOP_INTENT);
//        } else {
//            sendBroadcast(BaseNotificationReceiver.START_INTENT);
//        }
//    }

    @DebugLog
    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    @DebugLog
    @Override
    protected void onPause() {
        handler.removeCallbacks(updateRunnable);
        super.onPause();
    }

    private void nextTimer() {
        handler.removeCallbacks(updateRunnable);
        handler.postDelayed(updateRunnable, Utils.SECOND_MILLIS);
    }

    private void update() {
        updateWithoutTimer();
        nextTimer();
    }

    private void updateWithoutTimer() {

        if (!pomodoroMaster.isOngoing()) {
            return;
        }

        if (mTime != null) {
            mTime.setText(Utils.getRemainingTime(pomodoroMaster, /* shorten */ true));
        }

        if (mDescription != null) {
            mDescription.setText(Utils.getActivityTitle(this, pomodoroMaster, /* shorten */ false));
        }

//        if (pomodoroStartStopButton != null) {
//            pomodoroStartStopButton.setImageResource(pomodoroMaster.isOngoing()
//                    ? R.drawable.ic_action_stop_grey
//                    : R.drawable.ic_action_start_grey);
//            pomodoroStartStopButton.setContentDescription(pomodoroMaster.isOngoing()
//                    ? getString(R.string.cd_start_pomodoro)
//                    : getString(R.string.cd_stop_pomodoro));
//        }
    }

}
