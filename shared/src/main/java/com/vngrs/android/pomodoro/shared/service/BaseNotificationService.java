package com.vngrs.android.pomodoro.shared.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.vngrs.android.pomodoro.shared.Constants;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.Utils;
import com.vngrs.android.pomodoro.shared.model.ActivityType;
import com.vngrs.android.pomodoro.shared.receiver.BaseNotificationReceiver;

import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Service in the background to do heavy lifting of Broadcast Recevier ops.
 * <p/>
 * Created by Said Tahsin Dane on 12/4/15.
 */
public abstract class BaseNotificationService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String EXTRA_ACTIVITY_TYPE = "com.vngrs.android.pomodoro.extra.ACTIVITY_TYPE";

    public static final String ACTION_START = "com.vngrs.android.pomodoro.action.START";
    public static final String ACTION_STOP = "com.vngrs.android.pomodoro.action.STOP";
    public static final String ACTION_RESET = "com.vngrs.android.pomodoro.action.RESET";
    public static final String ACTION_DISMISS = "com.vngrs.android.pomodoro.action.DISMISS";
    public static final String ACTION_UPDATE = "com.vngrs.android.pomodoro.action.UPDATE";
    public static final String ACTION_FINISH_ALARM = "com.vngrs.android.pomodoro.action.ALARM";

    public static final Intent START_INTENT = new Intent(ACTION_START);
    public static final Intent STOP_INTENT = new Intent(ACTION_STOP);
    public static final Intent RESET_INTENT = new Intent(ACTION_RESET);
    public static final Intent UPDATE_INTENT = new Intent(ACTION_UPDATE);
    public static final Intent FINISH_ALARM_INTENT = new Intent(ACTION_FINISH_ALARM);

    private static final int REQUEST_UPDATE = 1;
    private static final int REQUEST_FINISH = 2;

    private static final int NOTIFICATION_ID_ONGOING = 101;
    private static final int NOTIFICATION_ID_NORMAL = 102;

    private boolean mConnected = false;

    @Inject PomodoroMaster pomodoroMaster;
    @Inject NotificationManagerCompat notificationManager;
    @Inject AlarmManager alarmManager;
    @Inject GoogleApiClient mGoogleApiClient;

    public BaseNotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        pomodoroMaster.check();

        if (intent == null) {
            return;
        }
        switch (intent.getAction()) {
            case ACTION_STOP:
                stop(this);
                break;
            case ACTION_FINISH_ALARM:
                finishAlarm(this);
                break;
            case ACTION_START:
                final ActivityType activityType =
                        ActivityType.fromValue(intent.getIntExtra(EXTRA_ACTIVITY_TYPE, 0));

                final DateTime nextPomodoro;
                final long nextTimeLong = intent.getLongExtra(Constants.KEY_NEXT_POMODORO, -1);
                if (nextTimeLong == -1) {
                    nextPomodoro = DateTime.now().plus(activityType.getLengthInMillis());
                } else {
                    nextPomodoro = new DateTime(nextTimeLong);
                }
                start(this, activityType, nextPomodoro);
                break;
            case ACTION_DISMISS:
                notificationManager.cancel(NOTIFICATION_ID_NORMAL);
                notificationManager.cancel(NOTIFICATION_ID_ONGOING);
                break;
            case ACTION_RESET:
                stop(this);
                pomodoroMaster.setPomodorosDone(0);
                break;
            default:
                break;
        }

        if (!intent.getAction().equals(ACTION_DISMISS)) {
            updateNotification(this, pomodoroMaster);
        }
        syncNotification(intent);

        boolean complete = BaseNotificationReceiver.completeWakefulIntent(intent);
        Timber.d("Wakelock complete: " + complete);
    }

    /**
     * Send the event to the other device
     * only if it is not coming already from the other device
     * and if it is not FINISH_ALARM action.
     *
     * @param intent coming Intent.
     */
    private void syncNotification(@NonNull Intent intent) {

        if (intent.getAction() == null
                || intent.getAction().equals(ACTION_FINISH_ALARM)
                || intent.getBooleanExtra(Constants.EXTRA_SYNC_NOTIFICATION, false)) {
            return;
        }

        if (intent.getAction().equals(ACTION_DISMISS)) {
            dismissNotification();
            return;
        }

        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(Constants.PATH_POMODORO);
        final DataMap dataMap = dataMapRequest.getDataMap();
        dataMap.clear();
        dataMap.putInt(Constants.KEY_ACTIVITY_TYPE, pomodoroMaster.getActivityType().value());
        if (pomodoroMaster.getNextPomodoro() != null) {
            dataMap.putLong(Constants.KEY_NEXT_POMODORO, pomodoroMaster.getNextPomodoro().getMillis());
        }
        dataMap.putInt(Constants.KEY_POMODOROS_DONE, pomodoroMaster.getPomodorosDone());
        dataMap.putString(Constants.SYNC_ACTION, intent.getAction());
        syncDataItem(dataMapRequest);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient.registerConnectionCallbacks(this);
        mGoogleApiClient.registerConnectionFailedListener(this);
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.unregisterConnectionFailedListener(this);
        super.onDestroy();
    }


    @Override
    public void onConnected(Bundle bundle) {
        mConnected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        mConnected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Timber.e("Failed to connect to the Google API client");
        mConnected = false;
    }

    /**
     * Removes the Data Item that was used to create a notification on the watch. By deleting the
     * data item, a {@link com.google.android.gms.wearable.WearableListenerService} on the watch
     * will be notified and the notification on the watch will be removed.
     * <p/>
     * Since connection to the Google API client is asynchronous, we spawn a thread and wait for
     * the connection to be established before attempting to use the Google API client.
     */
    private void dismissNotification() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!mConnected) {
                    mGoogleApiClient.blockingConnect(10, TimeUnit.SECONDS);
                }
                if (!mConnected) {
                    Timber.e("Failed to connect to mGoogleApiClient within 10 seconds");
                    return;
                }

                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.PATH_POMODORO);

                if (mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.deleteDataItems(mGoogleApiClient, putDataMapRequest.getUri())
                            .setResultCallback(new ResultCallback<DataApi.DeleteDataItemsResult>() {
                                @Override
                                public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {
                                    if (!deleteDataItemsResult.getStatus().isSuccess()) {
                                        Timber.d("dismissWearableNotification(): failed to delete"
                                                + " the data item");
                                    }
                                }
                            });
                }
            }
        }).start();
    }

    //General method to sync data in the Data Layer
    @DebugLog
    public void syncDataItem(final PutDataMapRequest putDataMapRequest) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!mConnected) {
                    mGoogleApiClient.blockingConnect(10, TimeUnit.SECONDS);
                }
                if (!mConnected) {
                    Timber.e("Failed to connect to mGoogleApiClient within 10 seconds");
                    return;
                }

                PutDataRequest request = putDataMapRequest.asPutDataRequest();

                if (mGoogleApiClient.isConnected()) {
                    //let's send the dataItem to the DataLayer API
                    Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                            .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                                @Override
                                public void onResult(DataApi.DataItemResult dataItemResult) {
                                    if (!dataItemResult.getStatus().isSuccess()) {
                                        Timber.e("ERROR: failed to putDataItem, status code: "
                                                + dataItemResult.getStatus().getStatusCode());
                                    }
                                }
                            });
                }
            }
        }).start();
    }

    /**
     * Abstract update Notification function to be implemented slightly differently in
     * Android Wear and Phones.
     *
     * @param context        Context object.
     * @param pomodoroMaster PomodoroMaster singletion object.
     */
    @Nullable
    public abstract Notification buildNotification(Context context, PomodoroMaster pomodoroMaster);

    private void updateNotification(Context context, PomodoroMaster pomodoroMaster) {

        final Notification notification = buildNotification(context, pomodoroMaster);
        if (notification != null) {
            notificationManager.notify(pomodoroMaster.isOngoing()
                            ? NOTIFICATION_ID_ONGOING
                            : NOTIFICATION_ID_NORMAL,
                    notification);
        }
    }

    private void start(@NonNull Context context,
                       @NonNull ActivityType activityType,
                       @NonNull DateTime nextPomodoroTime) {
        notificationManager.cancel(NOTIFICATION_ID_NORMAL);
        if (activityType != ActivityType.NONE
                && !pomodoroMaster.isOngoing()) {
            pomodoroMaster.start(activityType, nextPomodoroTime);

            setAlarm(context, REQUEST_FINISH, FINISH_ALARM_INTENT, nextPomodoroTime);
            setRepeatingAlarm(context, REQUEST_UPDATE, UPDATE_INTENT);
        }
    }

    private void stop(Context context) {
        pomodoroMaster.stop();

        notificationManager.cancel(NOTIFICATION_ID_NORMAL);
        notificationManager.cancel(NOTIFICATION_ID_ONGOING);
        cancelAlarm(context, REQUEST_FINISH, FINISH_ALARM_INTENT);
        cancelAlarm(context, REQUEST_UPDATE, UPDATE_INTENT);
    }

    private void finishAlarm(Context context) {
        notificationManager.cancel(pomodoroMaster.isOngoing()
                ? NOTIFICATION_ID_ONGOING
                : NOTIFICATION_ID_NORMAL);

        cancelAlarm(context, REQUEST_FINISH, FINISH_ALARM_INTENT);
        cancelAlarm(context, REQUEST_UPDATE, UPDATE_INTENT);

        ActivityType justStoppedActivityType = pomodoroMaster.stop();
        final ActivityType nextActivityType;
        if (justStoppedActivityType.isPomodoro()) {
            if ((pomodoroMaster.getPomodorosDone() + 1) % Constants.POMODORO_NUMBER_FOR_LONG_BREAK == 0) {
                nextActivityType = ActivityType.LONG_BREAK;
            } else {
                nextActivityType = ActivityType.SHORT_BREAK;
            }
        } else {
            nextActivityType = ActivityType.POMODORO;
        }
        pomodoroMaster.setActivityType(nextActivityType);
    }

    private void setRepeatingAlarm(Context context, int requestCode, Intent intent) {
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + Utils.MINUTE_MILLIS + 2000, Utils.MINUTE_MILLIS, pendingIntent);
    }

    private boolean isAlarmSet(Context context, int requestCode, Intent intent) {
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE) != null;
    }

    @TargetApi(19)
    private void setAlarm(Context context, int requestCode, Intent intent, DateTime time) {
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time.getMillis(), pendingIntent);
        }
    }

    private void cancelAlarm(Context context, int requestCode, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
