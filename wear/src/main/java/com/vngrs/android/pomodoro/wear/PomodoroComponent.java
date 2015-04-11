package com.vngrs.android.pomodoro.wear;

import com.vngrs.android.pomodoro.shared.PomodoroModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Dagger 2 Component containing parts of the application.
 *
 * Created by Said Tahsin Dane on 17/03/15.
 */
@Singleton
@Component(modules = { AppModule.class, PomodoroModule.class })
public interface PomodoroComponent {

    void inject(App app);
    void inject(PomodoroNotificationService receiver);
    void inject(OngoingNotificationListenerService service);
    void inject(PomodoroNotificationActivity activity);

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
