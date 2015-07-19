package com.tasomaniac.android.pomodoro.receiver;

import android.content.Context;
import android.content.Intent;

import com.tasomaniac.android.pomodoro.service.PomodoroNotificationService;
import com.tasomaniac.android.pomodoro.shared.receiver.BaseNotificationReceiver;

/**
 * Created by Said Tahsin Dane on 02/04/15.
 */
public class PomodoroNotificationReceiver extends BaseNotificationReceiver {

    public PomodoroNotificationReceiver() {
    }

    @Override
    public Intent getNotificationServiceIntent(Context context) {
        return new Intent(context, PomodoroNotificationService.class);
    }
}
