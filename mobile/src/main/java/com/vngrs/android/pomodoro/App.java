package com.vngrs.android.pomodoro;

import android.app.Application;
import android.content.Context;

import net.danlew.android.joda.JodaTimeAndroid;

import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by Said Tahsin Dane on 17/03/15.
 */
public class App extends Application {
    private PomodoroGraph component;

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);

        buildComponentAndInject();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            // TODO Crashlytics.start(this);
            // TODO Timber.plant(new CrashlyticsTree());
        }

    }


    @DebugLog // Extracted for debugging.
    public void buildComponentAndInject() {
        component = PomodoroComponent.Initializer.init(this);
        component.inject(this);
    }

    public PomodoroGraph component() {
        return component;
    }

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }
}
