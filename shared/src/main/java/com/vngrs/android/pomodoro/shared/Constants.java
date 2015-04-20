package com.vngrs.android.pomodoro.shared;

public class Constants {
    public static final int POMODORO_LENGTH_MS = BuildConfig.DEBUG ? 60 * 1000 : 25 * 60 * 1000;
    public static final int SHORT_BREAK_LENGTH_MS = BuildConfig.DEBUG ? 5 * 1000 : 5 * 60 * 1000;
    public static final int LONG_BREAK_LENGTH_MS = BuildConfig.DEBUG ? 20 * 1000 : 20 * 60 * 1000;
    public static final int POMODORO_NUMBER_FOR_LONG_BREAK = 4;

    public static final String PATH_POMODORO = "/pomodoro";

    public static final String EXTRA_SYNC_NOTIFICATION = "sync_notification";
    public static final String KEY_ACTIVITY_TYPE = "activity_type";
    public static final String KEY_NEXT_POMODORO = "next_pomodoro";
    public static final String KEY_LAST_POMODORO = "last_pomodoro";
    public static final String KEY_POMODOROS_DONE = "pomodoros_done";
    public static final String KEY_POMODORO_ONGOING = "is_ongoing";

    public static final String SYNC_ACTION = "sync_action";
}