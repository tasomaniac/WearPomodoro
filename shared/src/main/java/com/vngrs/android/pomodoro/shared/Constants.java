package com.vngrs.android.pomodoro.shared;

public class Constants {
    public static final int POMODORO_LENGTH_MS = BuildConfig.DEBUG ? 120 * 1000 : 25 * 60 * 1000;
    public static final int SHORT_BREAK_LENGTH_MS = BuildConfig.DEBUG ? 5 * 1000 : 5 * 60 * 1000;
    public static final int LONG_BREAK_LENGTH_MS = BuildConfig.DEBUG ? 20 * 1000 : 20 * 60 * 1000;
    public static final int POMODORO_NUMBER_FOR_LONG_BREAK = 4;

    public static final String PATH_NOTIFICATION = "/ongoingnotification";
    public static final String KEY_TITLE = "title";
    public static final String KEY_IMAGE = "image";
}