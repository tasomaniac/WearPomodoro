package com.vngrs.android.pomodoro;

import com.vngrs.android.pomodoro.receivers.PomodoroReceiver;
import com.vngrs.android.pomodoro.service.DataLayerListenerService;
import com.vngrs.android.pomodoro.ui.MainActivity;

/**
 * A common interface implemented by both the Standalone and VNGRS flavored components.
 *
 * Created by Said Tahsin Dane on 18/03/15.
 */
public interface PomodoroGraph {
    void inject(App app);
    void inject(MainActivity activity);
    void inject(PomodoroReceiver receiver);
    void inject(DataLayerListenerService service);
}
