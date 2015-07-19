package com.tasomaniac.android.pomodoro.wear.service;

import com.tasomaniac.android.pomodoro.shared.service.BaseWearableListenerService;
import com.tasomaniac.android.pomodoro.wear.App;

public class PomodoroWearableListenerService extends BaseWearableListenerService {

    @Override
    public void onCreate() {
        App.get(this).component().inject(this);
        super.onCreate();
    }
}