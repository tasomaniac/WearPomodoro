package com.vngrs.android.pomodoro.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.receivers.PomodoroAlarmReceiver;
import com.vngrs.android.pomodoro.receivers.PomodoroAlarmTickReceiver;
import com.vngrs.android.pomodoro.shared.Constants;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.model.ActivityType;
import com.vngrs.android.pomodoro.ui.MainActivity;

import org.joda.time.DateTime;

import javax.inject.Inject;

import timber.log.Timber;

public class PomodoroService extends Service implements PomodoroMaster.PomodoroMasterListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int NOTIFICATION_ID = 1;

    @Inject PomodoroMaster pomodoroMaster;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.get(this).component().inject(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        pomodoroMaster.setPomodoroMasterListener(this);
        pomodoroMaster.check();

        if (intent != null) {
            switch (intent.getAction()) {
                case PomodoroMaster.ACTION_STOP:
                    stopSelf();
                    break;
                case PomodoroMaster.ACTION_ALARM:
                    pomodoroMaster.handleAlarm();
                    PomodoroAlarmReceiver.completeWakefulIntent(intent);
                    break;
                case PomodoroMaster.ACTION_ALARM_TICK:
                    pomodoroMaster.handleAlarmTick();
                    PomodoroAlarmTickReceiver.completeWakefulIntent(intent);
                    break;
                case PomodoroMaster.ACTION_START:
                    final ActivityType activityType =
                        ActivityType.fromValue(intent.getIntExtra(PomodoroMaster.EXTRA_ACTIVITY_TYPE, 0));
                    if (activityType != ActivityType.NONE) {
                        pomodoroMaster.handleStart(activityType);
                    }
                    break;
                default:
                    break;
            }
        } else {
            pomodoroMaster.syncNotification();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        pomodoroMaster.stop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void syncNotification(ActivityType activityType,
                                 DateTime nextPomodoro,
                                 int pomodorosDone,
                                 boolean screenOn,
                                 boolean isOngoing) {
        if (activityType != ActivityType.NONE) {
            final NotificationCompat.Action action;
            if (isOngoing) {
                action = PomodoroMaster.createStopAction(this, R.drawable.ic_action_stop);
            } else {
                action = PomodoroMaster.createStartAction(this, R.drawable.ic_action_start, activityType);
            }
            final Intent contentIntent = new Intent(this, MainActivity.class);
            final Notification notification =
                    PomodoroMaster.createNotificationBuilderForActivityType(this,
                            PendingIntent.getActivity(this, 0, contentIntent, 0),
                            activityType,
                            pomodorosDone,
                            nextPomodoro,
                            screenOn,
                            isOngoing)
                            .addAction(action)
                            .build();
            startForeground(NOTIFICATION_ID, notification);
        } else {
            Timber.d("ignore notify for activityType " + ActivityType.NONE);
        }
    }


    @Override
    public void onConnected(Bundle bundle) {

        if (mGoogleApiClient.isConnected()) {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.PATH_NOTIFICATION);

            // Add data to the request
            putDataMapRequest.getDataMap().putString(Constants.KEY_TITLE, "hello world!");

//                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//                Asset asset = createAssetFromBitmap(icon);
//                putDataMapRequest.getDataMap().putAsset(Constants.KEY_IMAGE, asset);

            PutDataRequest request = putDataMapRequest.asPutDataRequest();

            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Timber.d("putDataItem status: " + dataItemResult.getStatus().toString());
                        }
                    });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Timber.e("Failed to connect to Google Api Client with error code "
                + connectionResult.getErrorCode());
    }
}
