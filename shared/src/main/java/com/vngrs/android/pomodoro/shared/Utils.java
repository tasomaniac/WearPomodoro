package com.vngrs.android.pomodoro.shared;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;

import com.vngrs.android.pomodoro.shared.model.ActivityType;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Utility class containing various operations.
 * <p/>
 * Created by Said Tahsin Dane on 31/03/15.
 */
public class Utils {

    private Utils() {
    }

    public static final int MINUTE_MILLIS = 60000;
    public static final int SECOND_MILLIS = 1000;

    /**
     * Converts the time difference to the human readable minutes.
     *
     * @param context  Context object.
     * @param diffTime Time difference.
     * @return Human readable minute representation.
     */
    @NonNull
    public static String convertDiffToPrettyMinutesLeft(@NonNull Context context,
                                                        @NonNull DateTime diffTime) {
        int minutes = diffTime.getMinuteOfHour();
        if (minutes == 0) {
            return context.getString(R.string.time_left_less_than_minute);
        } else {
            return context.getResources().getQuantityString(R.plurals.time_left_minutes, minutes, minutes);
        }
    }

    /**
     * Checks if we the 2 dates are in the same day.
     * After 6AM means that it is the next day.
     *
     * @param first  the first date.
     * @param second the second date.
     * @return true if the dates are in the same day.
     */
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
     * @param dp  original dp value.
     * @return px value.
     */
    public static int dpToPx(@NonNull Resources res, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    @NonNull
    public static String getRemainingTime(@NonNull PomodoroMaster pomodoroMaster, boolean shorten) {
        final long remaining = pomodoroMaster.getNextPomodoro().getMillis() - DateTime.now().getMillis();
        final DateTimeFormatter fmt = shorten && remaining < MINUTE_MILLIS
                ? DateTimeFormat.forPattern("ss") : DateTimeFormat.forPattern("mm:ss");
        return fmt.print(remaining);
    }

    public static int getPrimaryColor(@NonNull Context context,
                                      @NonNull PomodoroMaster pomodoroMaster) {
        return context.getResources().getColor(pomodoroMaster.isOngoing() && pomodoroMaster.getActivityType() == ActivityType.POMODORO
                ? R.color.ongoing_red : R.color.finished_green);
    }

    public static int getNotificationColorDark(@NonNull Context context,
                                           @NonNull PomodoroMaster pomodoroMaster) {
        return context.getResources().getColor(pomodoroMaster.isOngoing() && pomodoroMaster.getActivityType() == ActivityType.POMODORO
                ? R.color.ongoing_red_dark : R.color.finished_green_dark);
    }

    @NonNull
    public static String getActivityTitle(@NonNull Context context,
                                          @NonNull PomodoroMaster pomodoroMaster, boolean shorten) {
        switch (pomodoroMaster.getActivityType()) {
            case LONG_BREAK:
                return shorten
                        ? context.getString(R.string.title_short_break)
                        : context.getString(R.string.title_break_long);
            case POMODORO:
                return shorten
                        ? context.getString(R.string.title_short_pomodoro)
                        : context.getString(R.string.title_pomodoro_no, (pomodoroMaster.getPomodorosDone() + 1));
            case SHORT_BREAK:
                return shorten
                        ? context.getString(R.string.title_short_break)
                        : context.getString(R.string.title_break_short);
            default:
                return context.getString(R.string.title_none);
        }
    }

    @NonNull
    public static String getActivityTypeMessage(@NonNull Context context,
                                                @NonNull PomodoroMaster pomodoroMaster) {
        final int pomodorosDone = pomodoroMaster.getPomodorosDone();
        switch (pomodoroMaster.getActivityType()) {
            case LONG_BREAK:
                return context.getString(R.string.message_break_long);
            case POMODORO:
                return context.getString(R.string.message_pomodoro_no, (pomodorosDone + 1));
            case SHORT_BREAK:
                return context.getString(R.string.message_break_short);
            case NONE:
                return pomodorosDone == 0
                        ? context.getString(R.string.message_none_first)
                        : context.getString(R.string.message_none, pomodorosDone);
            default:
                throw new IllegalStateException("unsupported activityType " + pomodoroMaster.getActivityType());
        }
    }
}
