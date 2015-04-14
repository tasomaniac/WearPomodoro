package com.vngrs.android.pomodoro.receiver;

import android.content.Context;
import android.content.Intent;

import com.vngrs.android.pomodoro.service.PomodoroNotificationService;
import com.vngrs.android.pomodoro.shared.BaseNotificationReceiver;

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
