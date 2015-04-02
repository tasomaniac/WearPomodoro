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
    private static final int ID_PAUSE = 4;
    private static final int ID_RESUME = 5;
    private static final int ID_RESET = 6;

    private final Context context;
    private final PomodoroMaster pomodoroMaster;

    public NotificationBuilder(Context context, PomodoroMaster pomodoroMaster) {
        this.context = context;
        this.pomodoroMaster = pomodoroMaster;
    }

    @NonNull
    public NotificationCompat.Builder buildBaseNotification(@NonNull Context context) {

        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(pomodoroMaster.isOngoing()
                        ? Notification.PRIORITY_HIGH : Notification.PRIORITY_DEFAULT)
                .setOngoing(pomodoroMaster.isOngoing())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(getNotificationColor(context));
    }

    public int getNotificationColor(@NonNull Context context) {
        return context.getResources().getColor(pomodoroMaster.isOngoing()
                ? R.color.ongoing_red : R.color.finished_green);
    }

    public Notification buildNotificationWear() {
            final NotificationCompat.Action action;
        if (pomodoroMaster.isOngoing()) {
            action = buildStopAction(context, R.drawable.ic_action_stop_white);
        } else {
            action = buildStartAction(context, R.drawable.ic_action_start_white, pomodoroMaster.getActivityType());
        }

        final String message = messageForActivityType(context);
        final String title = titleForActivityType(context);

        NotificationCompat.Builder builder = buildBaseNotification(context)
                .setContentTitle(title)
                .setContentText(message)
                .setLocalOnly(true)
                .addAction(action);

        Bitmap background = Bitmap.createBitmap(600, 600, Bitmap.Config.RGB_565);
        background.eraseColor(getNotificationColor(context));

        final NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setBackground(background)
                .setHintHideIcon(true);
        if (pomodoroMaster.isOngoing()) {
//                final Intent displayIntent = new Intent(context, PomodoroNotificationActivity.class);
//                displayIntent.putExtra(PomodoroNotificationActivity.EXTRA_ACTION_PENDING_INTENT, action.actionIntent);
//                displayIntent.putExtra(PomodoroNotificationActivity.EXTRA_IS_ONGOING, isOngoing);
//                final PendingIntent displayPendingIntent = PendingIntent.getActivity(context,
//                        0, displayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                extender
////                        .addAction(action)
////                        .addAction(action)
////                        .setContentIcon(R.drawable.ic_action_stop_grey)
////                        .setContentAction(0)
//                        .setDisplayIntent(displayPendingIntent)
//                        .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_MEDIUM);
        } else {
            extender
//                        .addAction(action)
//                        .addAction(action)
                    .setContentIcon(R.drawable.ic_action_start_grey);
//                        .setContentAction(0);
        }
        builder.extend(extender);

//        Intent activityIntent = new Intent(context, MatchTimerNotificationActivity.class);
//        PendingIntent activityPendingIntent = PendingIntent.getActivity(context, ID_ACTIVITY, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
//        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
//        extender.setBackground(BitmapFactory.decodeResource(context.getResources(), R.drawable.bkg_football));
//        extender.setDisplayIntent(activityPendingIntent);
//        boolean ongoing = true;
//        if (pomodoroMaster.isPaused()) {
//            buildPausedActions(extender);
//        } else if (pomodoroMaster.isRunning()) {
//            buildRunningActions(extender);
//        } else {
//            buildStoppedActions(extender);
//            ongoing = false;
//        }
//        builder.setContentTitle(buildNotificationTitle(pomodoroMaster))
//                .setSmallIcon(R.drawable.ic_football)
//                .setStyle(new NotificationCompat.BigTextStyle())
//                .setOngoing(ongoing);
//        builder.setPriority(Notification.PRIORITY_MAX);
//        builder.extend(extender);

        return builder.build();
    }


    @NonNull
    public Notification buildNotificationPhone(@NonNull Context context,
                                                             @NonNull PendingIntent contentIntent) {

        final NotificationCompat.Action action;
        if (pomodoroMaster.isOngoing()) {
            action = buildStopAction(context, R.drawable.ic_action_stop_phone);
        } else {
            action = buildStartAction(context, R.drawable.ic_action_start_phone, pomodoroMaster.getActivityType());
        }

        final String message = messageForActivityType(context);
        final String title = titleForActivityType(context);

        final DateTime nextPomodoro = pomodoroMaster.getNextPomodoro();
        NotificationCompat.Builder builder = buildBaseNotification(context)
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

//    public static int backgroundResourceForActivityType(ActivityType activityType) {
//        switch (activityType) {
//            case LONG_BREAK:
//                return R.drawable.bg_long_break;
//            case POMODORO:
//                return R.drawable.bg_pomodoro;
//            case SHORT_BREAK:
//                return R.drawable.bg_short_break;
//        }
//        throw new IllegalStateException("unsupported activityType " + activityType);
//    }

    public String titleForActivityType(@NonNull Context context) {
        if (pomodoroMaster.isOngoing()) {
            return Utils.convertDiffToPrettyMinutesLeft(context, pomodoroMaster.getNextPomodoro().getMillis() - System.currentTimeMillis());
        } else {
            return context.getString(R.string.title_finished);
        }
    }

    public String messageForActivityType(@NonNull Context context) {
        if (pomodoroMaster.isOngoing()) {
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
        } else {
            switch (pomodoroMaster.getActivityType()) {
                case LONG_BREAK:
                    return context.getString(R.string.message_break_long);
                case POMODORO:
                    return context.getString(R.string.message_pomodoro_no, (pomodoroMaster.getPomodorosDone() + 1));
                case SHORT_BREAK:
                    return context.getString(R.string.message_break_short);
                default:
                    throw new IllegalStateException("unsupported activityType " + pomodoroMaster.getActivityType());
            }
        }
    }

}