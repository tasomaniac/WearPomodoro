package com.tasomaniac.android.pomodoro;

import com.tasomaniac.android.pomodoro.service.PomodoroNotificationService;
import com.tasomaniac.android.pomodoro.service.PomodoroWearableListenerService;
import com.tasomaniac.android.pomodoro.shared.PomodoroModule;
import com.tasomaniac.android.pomodoro.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Said Tahsin Dane on 17/03/15.
 */
@Singleton
@Component(modules = { AppModule.class, PomodoroModule.class })
public interface PomodoroComponent {

    void inject(App app);
    void inject(MainActivity activity);
    void inject(PomodoroNotificationService receiver);
    void inject(PomodoroWearableListenerService service);

    /**
     * An initializer that creates the graph from an application.
     */
    final class Initializer {
        static PomodoroComponent init(App app) {
            return DaggerPomodoroComponent.builder()
                    .appModule(new AppModule(app))
                    .build();
        }
        private Initializer() {} // No instances.
    }
}
