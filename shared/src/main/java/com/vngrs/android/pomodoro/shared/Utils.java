package com.vngrs.android.pomodoro.shared;

import android.content.Context;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

/**
 * Utility class containing various operations.
 *
 * Created by Said Tahsin Dane on 31/03/15.
 */
public class Utils {

    private Utils() {
    }

    public static String convertDiffToPrettyMinutesLeft(Context context, long diffMs) {
        diffMs = Math.max(0, diffMs);
        int secondsTotal = (int) diffMs / 1000;
        int minutes = secondsTotal / 60;
        if (minutes == 0) {
            return context.getString(R.string.time_left_less_than_minute);
        } else {
            return context.getResources().getQuantityString(R.plurals.time_left_minutes, minutes, minutes);
        }
    }

    public static boolean isTheSamePomodoroDay(@Nullable DateTime first,
                                               @Nullable DateTime second) {
        if (first != null && second != null) {
            boolean sameDay = first.getYear() == second.getYear()
                    && first.getDayOfYear() == second.getDayOfYear();
            boolean isBothAfter6am = first.getHourOfDay() > 6 && second.getHourOfDay() > 6;
            return sameDay && isBothAfter6am;
        } else {
            return false;
        }
    }
}
