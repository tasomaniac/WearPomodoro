package com.vngrs.android.pomodoro.wear;

import android.app.Activity;
import android.app.PendingIntent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.InjectView;
import timber.log.Timber;

public class PomodoroNotificationActivity extends Activity {

    public static final String EXTRA_ACTION_PENDING_INTENT = "com.vngrs.android.pomodoro.extra.ACTION_PENDING_INTENT";

    public static final String EXTRA_IS_ONGOING = "com.vngrs.android.pomodoro.extra.IS_ONGOING";

    @InjectView(R.id.pomodoro_start_stop_button)
    ImageView pomodoroStartStopButton;
    @InjectView(R.id.pomodoro_time)
    TextView pomodoroTime;
    @InjectView(R.id.pomodoro_description)
    TextView pomodoroDescription;

    private boolean isOngoing;
    private PendingIntent actionPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro_notification);

        pomodoroTime = (TextView) findViewById(R.id.pomodoro_time);
        pomodoroDescription = (TextView) findViewById(R.id.pomodoro_description);
        pomodoroStartStopButton = (ImageView) findViewById(R.id.pomodoro_start_stop_button);


        if (getIntent() != null) {
            isOngoing = getIntent().getBooleanExtra(EXTRA_IS_ONGOING, false);
            actionPendingIntent = getIntent().getParcelableExtra(EXTRA_ACTION_PENDING_INTENT);
        }

        pomodoroTime.setText("23:15");
        pomodoroDescription.setText("Pomodoro #3");

        pomodoroStartStopButton.setImageResource(isOngoing
                ? R.drawable.ic_action_stop_grey
                : R.drawable.ic_action_start_grey);
        pomodoroStartStopButton.setContentDescription(isOngoing
                ? getString(R.string.cd_start_pomodoro)
                : getString(R.string.cd_stop_pomodoro));

        pomodoroStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    actionPendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    Timber.e(e, "Pending Intent cannot be executed.");
                }
            }
        });
    }
}
