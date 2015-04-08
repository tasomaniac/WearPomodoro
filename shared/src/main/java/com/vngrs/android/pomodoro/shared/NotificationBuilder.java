package com.vngrs.android.pomodoro.shared;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.vngrs.android.pomodoro.shared.model.ActivityType;

import org.joda.time.DateTime;

public class NotificationBuilder {
    private static final int ID_ACTIVITY = 1;
    private static final int ID_START = 2;
    private static final int ID_STOP = 3;
    private static final int ID_RESET = 6;

    private final Context context;
    private final PomodoroMaster pomodoroMaster;

    public NotificationBuilder(Context context, PomodoroMaster pomodoroMaster) {
        this.context = context;
        this.pomodoroMaster = pomodoroMaster;
    }

    @NonNull
    private NotificationCompat.Builder buildBaseNotification() {

        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(pomodoroMaster.isOngoing() ? 0 : Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(pomodoroMaster.isOngoing())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .setStyle(new NotificationCompat.BigTextStyle())
                .setColor(getNotificationColor());
    }

    private int getNotificationColor() {
        return context.getResources().getColor(pomodoroMaster.isOngoing() && pomodoroMaster.getActivityType() == ActivityType.POMODORO
                ? R.color.ongoing_red : R.color.finished_green);
    }

    private Bitmap getBackground() {
        Bitmap background = Bitmap.createBitmap(600, 600, Bitmap.Config.RGB_565);
        background.eraseColor(getNotificationColor());
        return background;
    }

    public Notification buildNotificationWear(@NonNull Intent displayIntent) {
            final NotificationCompat.Action action;
        if (pomodoroMaster.isOngoing()) {
            action = buildStopAction(context, R.drawable.ic_action_stop_wear);
        } else {
            ActivityType activityType = pomodoroMaster.getActivityType();
            if (activityType == ActivityType.NONE) {
                activityType = ActivityType.POMODORO;
            }
            action = buildStartAction(context, R.drawable.ic_action_start_wear, activityType);
        }

        final String message = messageForActivityType(context);
        final String title = titleForActivityType(context);

        final NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setBackground(getBackground())
                .setHintHideIcon(true);
        if (pomodoroMaster.isOngoing()) {
                final PendingIntent displayPendingIntent = PendingIntent.getActivity(context,
                        ID_ACTIVITY, displayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                extender.setDisplayIntent(displayPendingIntent);
//                        .setCustomContentHeight(Utils.dpToPx(context.getResources(), 64));
//                        .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_SMALL);
        } else {
            extender.setContentIcon(R.drawable.ic_action_start_grey);
        }

        NotificationCompat.Builder builder = buildBaseNotification()
                .setContentTitle(title)
                .setContentText(message)
                .setLocalOnly(true)
                .addAction(action)
                .addAction(buildResetAction(context, R.drawable.ic_action_reset_wear))
                .extend(extender);

        return builder.build();
    }


    @NonNull
    public Notification buildNotificationPhone(@NonNull PendingIntent contentIntent) {

        final NotificationCompat.Action action;
        if (pomodoroMaster.isOngoing()) {
            action = buildStopAction(context, R.drawable.ic_action_stop_phone);
        } else {
            ActivityType activityType = pomodoroMaster.getActivityType();
            if (activityType == ActivityType.NONE) {
                activityType = ActivityType.POMODORO;
            }
            action = buildStartAction(context, R.drawable.ic_action_start_phone, activityType);
        }

        final String message = messageForActivityType(context);
        final String title = titleForActivityType(context);

        final DateTime nextPomodoro = pomodoroMaster.getNextPomodoro();
        NotificationCompat.Builder builder = buildBaseNotification()
                .setWhen(nextPomodoro != null ? nextPomodoro.getMillis() : System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(message)
                .addAction(action);

        return builder.build();
    }

    public NotificationCompat.Action buildStartAction(@NonNull Context context,
                                                             @DrawableRes int actionIcon,
                                                             @NonNull ActivityType activityType) {
        final Intent startActionIntent = BaseNotificationReceiver.START_INTENT;
        startActionIntent.putExtra(BaseNotificationReceiver.EXTRA_ACTIVITY_TYPE, activityType.value());
        final PendingIntent startActionPendingIntent =
                    PendingIntent.getBroadcast(context, ID_START, startActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Action.Builder(actionIcon,
                context.getString(R.string.start), startActionPendingIntent).build();
    }

    public static NotificationCompat.Action buildStopAction(@NonNull Context context, @DrawableRes int actionIcon) {
        final Intent stopActionIntent = BaseNotificationReceiver.STOP_INTENT;
        final PendingIntent stopActionPendingIntent =
                    PendingIntent.getBroadcast(context, ID_STOP, stopActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Action.Builder(actionIcon,
                context.getString(R.string.stop), stopActionPendingIntent).build();
    }

    public static NotificationCompat.Action buildResetAction(@NonNull Context context, @DrawableRes int actionIcon) {
        final Intent resetActionIntent = BaseNotificationReceiver.RESET_INTENT;
        final PendingIntent resetActionPendingIntent =
                PendingIntent.getBroadcast(context, ID_RESET, resetActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Action.Builder(actionIcon,
                context.getString(R.string.reset), resetActionPendingIntent).build();
    }

    public String titleForActivityType(@NonNull Context context) {
        if (pomodoroMaster.isOngoing()) {
            final String minutesLeft = Utils.convertDiffToPrettyMinutesLeft(context,
                    pomodoroMaster.getNextPomodoro().minus(DateTime.now().getMillis()));
            return minutesLeft + " | " + Utils.getActivityTitle(context, pomodoroMaster, /* shorten */ true);
        } else {
            if (pomodoroMaster.getActivityType() == ActivityType.NONE) {
                return context.getString(R.string.title_none);
            } else {
                return context.getString(R.string.title_finished);
            }
        }
    }

    public String messageForActivityType(@NonNull Context context) {
        if (pomodoroMaster.isOngoing()) {
            return null;
        } else {
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

}