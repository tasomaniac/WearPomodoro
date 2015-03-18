package com.vngrs.android.pomodoro.interfaces;

/**
 * Pomodor interface.
 *
 * Created by Said Tahsin Dane on 17/03/15.
 */
public interface Pomodoro {

    void getStatusAll();
    void getStatus();
    void stop();
    void doBreak();
    void start();
}