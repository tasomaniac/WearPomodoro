package com.vngrs.android.pomodoro.wear;

import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.vngrs.android.pomodoro.shared.Constants;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import timber.log.Timber;

public class OngoingNotificationListenerService extends WearableListenerService {
    private static final int NOTIFICATION_ID = 100;

    private GoogleApiClient mGoogleApiClient;

    @Inject NotificationManagerCompat notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        App.get(this).component().inject(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Timber.e("Service failed to connect to GoogleApiClient.");
                return;
            }
        }

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (Constants.PATH_NOTIFICATION.equals(path)) {
                    // Get the data out of the event
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    final String title = dataMapItem.getDataMap().getString(Constants.KEY_TITLE);
                    Asset asset = dataMapItem.getDataMap().getAsset(Constants.KEY_IMAGE);


                } else {
                    Timber.d("Unrecognized path: " + path);
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(Constants.PATH_DISMISS)) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }
}