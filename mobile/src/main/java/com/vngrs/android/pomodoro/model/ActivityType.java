package com.vngrs.android.pomodoro.model;

import com.vngrs.android.pomodoro.Constants;

public enum ActivityType {

    NONE(0, 0),
    POMODORO(1, Constants.POMODORO_LENGTH_MS),
    SHORT_BREAK(2, Constants.SHORT_BREAK_LENGTH_MS),
    LONG_BREAK(3, Constants.LONG_BREAK_LENGTH_MS);

    private int value;
    private long lengthInMillis;

    ActivityType(int value, long lengthInMillis) {
        this.value = value;
        this.lengthInMillis = lengthInMillis;
    }

    public static ActivityType fromValue(int value) {
        for (ActivityType activityType: ActivityType.values()) {
            if (activityType.value() == value) {
                return activityType;
            }
        }
        return ActivityType.NONE;
    }

    public int value() {
        return value;
    }

    public boolean isBreak() {
        return this == SHORT_BREAK || this == LONG_BREAK;
    }

    public boolean isPomodoro() {
        return this == POMODORO;
    }

    public long getLengthInMillis() {
        return lengthInMillis;
    }
}