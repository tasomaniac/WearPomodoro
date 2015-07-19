package com.tasomaniac.android.pomodoro.service;

import com.tasomaniac.android.pomodoro.App;
import com.tasomaniac.android.pomodoro.shared.service.BaseWearableListenerService;

public class PomodoroWearableListenerService extends BaseWearableListenerService {

    @Override
    public void onCreate() {
        App.get(this).component().inject(this);
        super.onCreate();
    }
}