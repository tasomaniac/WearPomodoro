package com.vngrs.android.pomodoro;

import com.vngrs.android.pomodoro.shared.PomodoroModule;
import com.vngrs.android.pomodoro.ui.StandaloneUiModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Said Tahsin Dane on 17/03/15.
 */
@Singleton
@Component(modules = { StandaloneUiModule.class, AppModule.class, PomodoroModule.class })
public interface PomodoroComponent extends PomodoroGraph {

    /**
     * An initializer that creates the graph from an application.
     */
    final static class Initializer {
        static PomodoroGraph init(App app) {
            return Dagger_PomodoroComponent.builder()
                    .appModule(new AppModule(app))
                    .build();
        }
        private Initializer() {} // No instances.
    }
}
