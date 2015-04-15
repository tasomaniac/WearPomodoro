package com.vngrs.android.pomodoro.shared.service;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.vngrs.android.pomodoro.shared.Constants;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.model.ActivityType;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class BaseWearableListenerService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private boolean mConnected = false;

    @Inject GoogleApiClient mGoogleApiClient;
    @Inject PomodoroMaster pomodoroMaster;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient.registerConnectionCallbacks(this);
        mGoogleApiClient.registerConnectionFailedListener(this);
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.unregisterConnectionFailedListener(this);
    }

    @DebugLog
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        final List<DataEvent> events = FreezableUtils
                .freezeIterable(dataEvents);

        if (!mConnected) {
            ConnectionResult connectionResult =
                    mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess()) {
                Timber.e("Failed to connect to GoogleApiClient.");
                return;
            }
        }

        // Loop through the events.
        for (DataEvent event : events) {
            String path = event.getDataItem().getUri().getPath();

            if (event.getType() == DataEvent.TYPE_CHANGED) {
                if (Constants.PATH_POMODORO.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    DataMap dataMap = dataMapItem.getDataMap();

                    final ActivityType activityType = ActivityType.fromValue(dataMap.getInt(Constants.KEY_ACTIVITY_TYPE));
                    pomodoroMaster.setActivityType(activityType);
                    pomodoroMaster.setPomodorosDone(dataMap.getInt(Constants.KEY_POMODOROS_DONE));

                    final Intent intent = new Intent(dataMap.getString(Constants.SYNC_ACTION));
                    intent.putExtra(BaseNotificationService.EXTRA_ACTIVITY_TYPE, activityType.value());
                    intent.putExtra(Constants.EXTRA_SYNC_NOTIFICATION, true);
                    intent.putExtra(Constants.KEY_NEXT_POMODORO,
                            dataMap.getLong(Constants.KEY_NEXT_POMODORO, -1));

                    sendOrderedBroadcast(intent, null);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Timber.d(event.getDataItem().toString());
                final Intent intent = new Intent(BaseNotificationService.ACTION_DISMISS);
                intent.putExtra(Constants.EXTRA_SYNC_NOTIFICATION, true);
                sendOrderedBroadcast(intent, null);
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Timber.d("Connected to Google API Client");
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
}