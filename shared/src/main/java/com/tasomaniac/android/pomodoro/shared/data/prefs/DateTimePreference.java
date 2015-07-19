package com.tasomaniac.android.pomodoro.shared.data.prefs;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

public class DateTimePreference {
    private final SharedPreferences preferences;
    private final String key;
    private final DateTime defaultValue;

    public DateTimePreference(@NonNull SharedPreferences preferences, @NonNull String key) {
        this(preferences, key, null);
    }

    public DateTimePreference(@NonNull SharedPreferences preferences, @NonNull String key, @Nullable DateTime defaultValue) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Nullable
    public DateTime get() {
        final long millis = preferences.getLong(key, defaultValue != null ? defaultValue.getMillis() : -1);
        return millis == -1 ? null : new DateTime(millis);
    }

    public boolean isSet() {
        return preferences.contains(key);
    }

    public void set(@Nullable DateTime value) {
        preferences.edit().putLong(key, value != null ? value.getMillis() : -1).apply();
    }

    public void delete() {
        preferences.edit().remove(key).apply();
    }
}