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
import com.vngrs.android.pomodoro.shared.service.BaseNotificationService;

import org.joda.time.DateTime;

public class NotificationBuilder {
    private static final int ID_ACTIVITY = 1;
    private static final int ID_START = 2;
    private static final int ID_STOP = 3;
    private static final int ID_RESET = 6;
    private static final int ID_DISMISS = 7;

    private final Context context;
    private final PomodoroMaster pomodoroMaster;
    private int notificationIcon;
    private int actionStartIcon;
    private int actionStopIcon;
    private int actionResetIcon;

    public NotificationBuilder(@NonNull Context context,
                               @NonNull PomodoroMaster pomodoroMaster,
                               @DrawableRes int notificationIcon,
                               @DrawableRes int actionStartIcon,
                               @DrawableRes int actionStopIcon,
                               @DrawableRes int actionResetIcon) {
        this.context = context;
        this.pomodoroMaster = pomodoroMaster;
        this.notificationIcon = notificationIcon;
        this.actionStartIcon = actionStartIcon;
        this.actionStopIcon = actionStopIcon;
        this.actionResetIcon = actionResetIcon;
    }

    @NonNull
    private NotificationCompat.Builder buildBaseNotification() {

        final NotificationCompat.Action action = getNotificationAction();

        final String title = titleForActivityType(context);
        final String message = messageForActivityType(context);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(notificationIcon)
                .setDefaults(pomodoroMaster.isOngoing() ? 0 : Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(pomodoroMaster.isOngoing())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLocalOnly(true)
//                .setStyle(new NotificationCompat.BigTextStyle())
                .setColor(Utils.getPrimaryColor(context, pomodoroMaster))
                .setContentTitle(title)
                .setContentText(message)
                .addAction(action);
        if (!pomodoroMaster.isOngoing()) {
            builder.setDeleteIntent(PendingIntent.getBroadcast(context, ID_DISMISS,
                    new Intent(BaseNotificationService.ACTION_DISMISS), PendingIntent.FLAG_UPDATE_CURRENT));
        }
        return builder;
    }

    private Bitmap getBackground() {
        Bitmap background = Bitmap.createBitmap(600, 600, Bitmap.Config.RGB_565);
        background.eraseColor(Utils.getPrimaryColor(context, pomodoroMaster));
        return background;
    }

    private NotificationCompat.Action getNotificationAction() {
        final NotificationCompat.Action action;
        if (pomodoroMaster.isOngoing()) {
            action = buildStopAction(context);
        } else {
            ActivityType activityType = pomodoroMaster.getActivityType();
            if (activityType == ActivityType.NONE) {
                activityType = ActivityType.POMODORO;
            }
            action = buildStartAction(context, activityType);
        }
        return action;
    }

    @NonNull
    public Notification buildNotificationWear(@NonNull Intent displayIntent) {

        final NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setBackground(getBackground())
                .setHintHideIcon(true);
        if (pomodoroMaster.isOngoing()) {
            final PendingIntent displayPendingIntent = PendingIntent.getActivity(context,
                    ID_ACTIVITY, displayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            extender.setDisplayIntent(displayPendingIntent);
        } else {
            extender.setContentIcon(R.drawable.ic_action_start_grey);
        }

        NotificationCompat.Builder builder = buildBaseNotification()
                .setContentText(null)
                .addAction(buildResetAction(context))
                .extend(extender);

        return builder.build();
    }

    @NonNull
    public Notification buildNotificationPhone(@NonNull PendingIntent contentIntent) {

        final DateTime nextPomodoro = pomodoroMaster.getNextPomodoro();
        NotificationCompat.Builder builder = buildBaseNotification()
                .setWhen(nextPomodoro != null ? nextPomodoro.getMillis() : System.currentTimeMillis())
                .setContentIntent(contentIntent);

        return builder.build();
    }

    public NotificationCompat.Action buildStartAction(@NonNull Context context,
                                                      @NonNull ActivityType activityType) {
        final Intent startActionIntent = BaseNotificationService.START_INTENT;
        startActionIntent.putExtra(BaseNotificationService.EXTRA_ACTIVITY_TYPE, activityType.value());
        final PendingIntent startActionPendingIntent =
                PendingIntent.getBroadcast(context, ID_START, startActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Action.Builder(actionStartIcon,
                context.getString(R.string.start), startActionPendingIntent).build();
    }

    public NotificationCompat.Action buildStopAction(@NonNull Context context) {
        final Intent stopActionIntent = BaseNotificationService.STOP_INTENT;
        final PendingIntent stopActionPendingIntent =
                PendingIntent.getBroadcast(context, ID_STOP, stopActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Action.Builder(actionStopIcon,
                context.getString(R.string.stop), stopActionPendingIntent).build();
    }

    public NotificationCompat.Action buildResetAction(@NonNull Context context) {
        final Intent resetActionIntent = BaseNotificationService.RESET_INTENT;
        final PendingIntent resetActionPendingIntent =
                PendingIntent.getBroadcast(context, ID_RESET, resetActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Action.Builder(actionResetIcon,
                context.getString(R.string.reset), resetActionPendingIntent).build();
    }

    public String titleForActivityType(@NonNull Context context) {
        if (pomodoroMaster.isOngoing()) {
            if (pomodoroMaster.getNextPomodoro() == null) {
                return null;
            }
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
            return Utils.getActivityTitle(context, pomodoroMaster, /* shorten */ false);
        } else {
            return Utils.getActivityFinishMessage(context, pomodoroMaster);
        }
    }

}