package com.vngrs.android.pomodoro.wear;

import com.vngrs.android.pomodoro.shared.PomodoroModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Said Tahsin Dane on 17/03/15.
 */
@Singleton
@Component(modules = { AppModule.class, PomodoroModule.class })
public interface PomodoroComponent {

    void inject(App app);
    void inject(PomodoroNotificationReceiver receiver);
    void inject(OngoingNotificationListenerService service);

    /**
     * An initializer that creates the graph from an application.
     */
    final static class Initializer {
        static PomodoroComponent init(App app) {
            return Dagger_PomodoroComponent.builder()
                    .appModule(new AppModule(app))
                    .build();
        }
        private Initializer() {} // No instances.
    }
}
