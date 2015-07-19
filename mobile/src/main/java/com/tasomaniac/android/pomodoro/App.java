package com.tasomaniac.android.pomodoro;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.danlew.android.joda.JodaTimeAndroid;

import hugo.weaving.DebugLog;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Created by Said Tahsin Dane on 17/03/15.
 */
public class App extends Application {
    private PomodoroComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);

        buildComponentAndInject();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Fabric.with(this, new Crashlytics());
            Timber.plant(new CrashReportingTree());
        }
    }


    @DebugLog // Extracted for debugging.
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


    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.Tree {
        @Override protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            Crashlytics.log(priority, tag, message);
            if (t != null && priority >= Log.WARN) {
                Crashlytics.logException(t);
            }
        }
    }
}
