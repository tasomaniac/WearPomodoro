package com.vngrs.android.pomodoro.shared.data.prefs;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class EnumPreference<E extends Enum<E>> {
    private final SharedPreferences preferences;
    private final Class<E>          clazz;
    private final String            key;
    private final E                 defaultValue;

 
    public EnumPreference(@NonNull SharedPreferences preferences,
                          @NonNull Class<E> clazz,
                          @NonNull String key,
                          @NonNull E defaultValue) {
        this.preferences = preferences;
        this.clazz = clazz;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @NonNull
    public E get() {
        final String string = preferences.getString(key, null);
        return string != null ? E.valueOf(clazz, string) : defaultValue;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isSet() {
        return preferences.contains(key);
    }
 
    public void set(@Nullable E value) {
        preferences.edit().putString(key, value != null ? value.name() : null).apply();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void delete() {
        preferences.edit().remove(key).apply();
    }
}