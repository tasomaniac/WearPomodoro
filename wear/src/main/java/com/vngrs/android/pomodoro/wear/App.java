package com.vngrs.android.pomodoro.wear;

import android.app.Application;
import android.content.Context;

import com.vngrs.android.pomodoro.BuildConfig;

import net.danlew.android.joda.JodaTimeAndroid;

import timber.log.Timber;

/**
 * Created by taso on 20/03/15.
 */
public class App extends Application {

    private PomodoroComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            // TODO Crashlytics.start(this);
            // TODO Timber.plant(new CrashlyticsTree());
        }

        JodaTimeAndroid.init(this);

        buildComponentAndInject();
    }

    public void buildComponentAndInject() {
        component = PomodoroComponent.Initializer.init(this);
        component.inject(this);
    }

    public PomodoroComponent component() {
        return component;
    }

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }
}
