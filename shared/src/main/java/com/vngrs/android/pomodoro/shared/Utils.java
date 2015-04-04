package com.vngrs.android.pomodoro.shared;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;

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


    /**
     * Converts dp value to px value.
     *
     * @param res Resources objects to get displayMetrics.
     * @param dp original dp value.
     * @return px value.
     */
    public static int dpToPx(Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    public static String getActivityTitle(@NonNull Context context, PomodoroMaster pomodoroMaster) {
        switch (pomodoroMaster.getActivityType()) {
            case LONG_BREAK:
                return context.getString(R.string.title_break_long);
            case POMODORO:
                return context.getString(R.string.title_pomodoro_no, (pomodoroMaster.getPomodorosDone() + 1));
            case SHORT_BREAK:
                return context.getString(R.string.title_break_short);
            default:
                throw new IllegalStateException("unsupported activityType " + pomodoroMaster.getActivityType());
        }
    }
}
