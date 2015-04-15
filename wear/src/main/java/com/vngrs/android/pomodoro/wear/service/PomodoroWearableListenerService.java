package com.vngrs.android.pomodoro.wear.service;

import com.vngrs.android.pomodoro.shared.service.BaseWearableListenerService;
import com.vngrs.android.pomodoro.wear.App;

public class PomodoroWearableListenerService extends BaseWearableListenerService {

    @Override
    public void onCreate() {
        App.get(this).component().inject(this);
        super.onCreate();
    }
}