package com.vngrs.android.pomodoro;

import com.vngrs.android.pomodoro.ui.StandaloneUiModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Said Tahsin Dane on 17/03/15.
 */
@Singleton
@Component(modules = { StandaloneUiModule.class, PomodoroAppModule.class })
public interface PomodoroComponent extends PomodoroGraph {

    /**
     * An initializer that creates the graph from an application.
     */
    final static class Initializer {
        static PomodoroComponent init(PomodoroApp app) {
            return Dagger_PomodoroComponent.builder()
                    .pomodoroAppModule(new PomodoroAppModule(app))
                    .build();
        }
        private Initializer() {} // No instances.
    }
}
