package com.vngrs.android.pomodoro.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.shared.NotificationBuilder;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.model.ActivityType;
import com.vngrs.android.pomodoro.shared.service.BaseNotificationService;
import com.vngrs.android.pomodoro.ui.MainActivity;
import com.vngrs.android.pomodoro.util.Analytics;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * @see BaseNotificationService
 *
 * Created by Said Tahsin Dane on 12/4/15.
 */
public class PomodoroNotificationService extends BaseNotificationService {

    @Inject Lazy<Analytics> analytics;

    @Override
    public void onCreate() {
        App.get(this).component().inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);

        if (intent != null) {
            handleAnalytics(intent);
        }
    }

    private void handleAnalytics(Intent intent) {
        String action = null, label = "";
        switch (intent.getAction()) {
            case ACTION_STOP:
                action = "Stop";
                label = pomodoroMaster.getActivityType().toString();
                break;
            case ACTION_FINISH_ALARM:
                action = "Pomodoro Finish";
                label = pomodoroMaster.getActivityType().toString();
                break;
            case ACTION_START:
                final ActivityType activityType =
                        ActivityType.fromValue(intent.getIntExtra(EXTRA_ACTIVITY_TYPE, 0));
                action = "Start";
                label = activityType.toString();
                break;
            case ACTION_DISMISS:
                action = "Dismiss";
                break;
            case ACTION_RESET:
                action = "Reset";
                break;
            default:
                break;
        }

        if (action != null) {
            /* [ANALYTICS:EVENT]
             * TRIGGER:   Taking one of the Pomodoro Actions.
             * CATEGORY:  'Pomodoro'
             * ACTION:    'Feedback'
             * LABEL:     Activity Type if the action is Start
             * [/ANALYTICS]
             */
            analytics.get().sendEvent("Pomodoro", action, label);
        }
    }

    @Nullable
    @Override
    public Notification buildNotification(Context context, PomodoroMaster pomodoroMaster) {
        if (pomodoroMaster.getActivityType() != ActivityType.NONE) {
            NotificationBuilder builder = new NotificationBuilder(context,
                    pomodoroMaster,
                    R.drawable.ic_stat_pomodoro,
                    R.drawable.ic_action_start_phone,
                    R.drawable.ic_action_stop_phone,
                    R.drawable.ic_action_reset_96dp);
            final Intent contentIntent = new Intent(context, MainActivity.class);
            return builder
                    .buildNotificationPhone(PendingIntent.getActivity(context, 0, contentIntent, 0));
        } else {
            return null;
        }
    }
}
