package com.vngrs.android.pomodoro.wear;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.Utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class PomodoroNotificationActivity extends Activity {

    public static final int MINUTE_MILLIS = 60000;
    public static final int SECOND_MILLIS = 1000;
    private Handler handler = null;

    private ImageView pomodoroStartStopButton;
    private TextView pomodoroTime;
    private TextView pomodoroDescription;

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

        handler = new Handler();

        pomodoroTime = (TextView) findViewById(R.id.pomodoro_time);
        pomodoroDescription = (TextView) findViewById(R.id.pomodoro_description);
        pomodoroStartStopButton = (ImageView) findViewById(R.id.pomodoro_start_stop_button);

//        findViewById(R.id.content).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (pomodoroMaster.isOngoing()) {
//                    sendBroadcast(BaseNotificationReceiver.STOP_INTENT);
//                } else {
//                    sendBroadcast(BaseNotificationReceiver.START_INTENT);
//                }
//            }
//        });

        updateWithoutTimer();

    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(updateRunnable);
        super.onPause();
    }

    private void nextTimer() {
        handler.removeCallbacks(updateRunnable);
        handler.postDelayed(updateRunnable, SECOND_MILLIS);
    }

    private void update() {
        updateWithoutTimer();
        nextTimer();
    }

    @DebugLog
    private void updateWithoutTimer() {

        if (!pomodoroMaster.isOngoing()) {
            return;
        }

        if (pomodoroTime != null) {
            final long remaining = pomodoroMaster.getNextPomodoro().getMillis() - DateTime.now().getMillis();
            final DateTimeFormatter fmt = remaining >= MINUTE_MILLIS
                    ? DateTimeFormat.forPattern("mm:ss") : DateTimeFormat.forPattern("ss");
            pomodoroTime.setText(fmt.print(remaining));
        }
        if (pomodoroDescription != null) {
            pomodoroDescription.setText(Utils.getActivityTitle(this, pomodoroMaster));
        }

        if (pomodoroStartStopButton != null) {
            pomodoroStartStopButton.setImageResource(pomodoroMaster.isOngoing()
                    ? R.drawable.ic_action_stop_grey
                    : R.drawable.ic_action_start_grey);
            pomodoroStartStopButton.setContentDescription(pomodoroMaster.isOngoing()
                    ? getString(R.string.cd_start_pomodoro)
                    : getString(R.string.cd_stop_pomodoro));
        }
    }
}
