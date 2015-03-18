package com.vngrs.android.pomodoro;

import com.vngrs.android.pomodoro.ui.VngrsUiModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Said Tahsin Dane on 17/03/15.
 */
@Singleton
@Component(modules = { VngrsUiModule.class, PomodoroAppModule.class })
public interface PomodoroComponent extends PomodoroGraph {

    /**
     * An initializer that creates the graph from an application.
     */
    final static class Initializer {
        static PomodoroGraph init(PomodoroApp app) {
            return Dagger_PomodoroComponent.builder()
                    .pomodoroAppModule(new PomodoroAppModule(app))
                    .build();
        }
        private Initializer() {} // No instances.
    }
}
