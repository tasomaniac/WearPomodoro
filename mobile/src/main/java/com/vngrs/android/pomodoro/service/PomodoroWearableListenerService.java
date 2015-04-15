package com.vngrs.android.pomodoro.service;

import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.shared.service.BaseWearableListenerService;

public class PomodoroWearableListenerService extends BaseWearableListenerService {

    @Override
    public void onCreate() {
        App.get(this).component().inject(this);
        super.onCreate();
    }
}