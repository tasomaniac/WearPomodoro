package com.vngrs.android.pomodoro.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.shared.Constants;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;

import javax.inject.Inject;

import timber.log.Timber;

public class PomodoroService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
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
