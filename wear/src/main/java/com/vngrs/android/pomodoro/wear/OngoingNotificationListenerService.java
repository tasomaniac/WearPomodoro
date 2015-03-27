package com.vngrs.android.pomodoro.wear;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.vngrs.android.pomodoro.shared.Constants;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import timber.log.Timber;

public class OngoingNotificationListenerService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean mConnected = false;
    private final static long TIMEOUT_S = 10; // how long to wait for GoogleApi Client connection

    @Inject NotificationManagerCompat notificationManager;
    @Inject PomodoroMaster pomodoroMaster;

    @Override
    public void onCreate() {
        super.onCreate();
        App.get(this).component().inject(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        if (!mConnected) {
            ConnectionResult connectionResult = mGoogleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Timber.e("Service failed to connect to GoogleApiClient.");
                return;
            }
        }

        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();
            if (event.getType() == DataEvent.TYPE_DELETED) {
                if (uri.getPath().startsWith(Constants.PATH_NOTIFICATION)) {
                    pomodoroMaster.stop();
                }
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                if (uri.getPath().startsWith(Constants.PATH_NOTIFICATION)) {
                    // Get the data out of the event
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    final DataMap dataMap = dataMapItem.getDataMap();
                    final String title = dataMap.getString(Constants.KEY_TITLE);
                    Asset asset = dataMap.getAsset(Constants.KEY_IMAGE);


                } else {
                    Timber.d("Unrecognized path: " + uri.getPath());
                }
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