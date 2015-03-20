package com.vngrs.android.pomodoro.shared.model;

import com.vngrs.android.pomodoro.shared.model.ActivityType;

/**
 * Pomodor interface.
 *
 * Created by Said Tahsin Dane on 17/03/15.
 */
public interface Pomodoro {

//    ArrayList<Status> getStatusAll();
//    Status getStatus();
    ActivityType stop();
//    void takeBreak();
    void start(ActivityType type);
}