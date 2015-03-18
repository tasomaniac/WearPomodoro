package com.vngrs.android.pomodoro;

/**
 * A common interface implemented by both the Standalone and VNGRS flavored components.
 *
 * Created by Said Tahsin Dane on 18/03/15.
 */
public interface PomodoroGraph {
    void inject(PomodoroApp app);
    void inject(MainActivity activity);
}
