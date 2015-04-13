package com.vngrs.android.pomodoro.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.R;
import com.vngrs.android.pomodoro.shared.BaseNotificationService;
import com.vngrs.android.pomodoro.shared.Constants;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.Utils;
import com.vngrs.android.pomodoro.shared.model.ActivityType;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import timber.log.Timber;


public class MainActivity extends ActionBarActivity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final String STATE_ERROR_ALREADY_SHOWN = "error_shown";
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private boolean mResolvingError = false;
    private boolean mErrorAlreadyShown = false;

    @Inject BaseUi baseUi;
    @Inject GoogleApiClient mGoogleApiClient;
    @Inject PomodoroMaster pomodoroMaster;

    @InjectView(R.id.pomodoro_time) TextView mTime;
    @InjectView(R.id.pomodoro_description) TextView mDescription;
    @InjectView(R.id.pomodoro_start_stop_button) ImageButton mStartStopButton;

    private Handler handler = null;

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            update();
        }
    };

    private BroadcastReceiver pomodoroReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {

            if (intent != null) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switch (intent.getAction()) {
                            case BaseNotificationService.ACTION_STOP:
                            case BaseNotificationService.ACTION_RESET:
                            case BaseNotificationService.ACTION_FINISH_ALARM:
                                handler.removeCallbacks(updateRunnable);
                                updateWithoutTimer();
                                break;
                            case BaseNotificationService.ACTION_START:
                                update();
                                break;
                            default:
                                updateWithoutTimer();
                                break;
                        }
                    }
                }, 100);
            }
        }
    };

    private static final IntentFilter FILTER_START =
            new IntentFilter(BaseNotificationService.ACTION_START);
    private static final IntentFilter FILTER_STOP =
            new IntentFilter(BaseNotificationService.ACTION_STOP);
    private static final IntentFilter FILTER_RESET =
            new IntentFilter(BaseNotificationService.ACTION_RESET);
    private static final IntentFilter FILTER_UPDATE =
            new IntentFilter(BaseNotificationService.ACTION_UPDATE);
    private static final IntentFilter FILTER_FINISH_ALARM =
            new IntentFilter(BaseNotificationService.ACTION_FINISH_ALARM);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.get(this).component().inject(this);
        ButterKnife.inject(this);

        handler = new Handler();

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        mErrorAlreadyShown = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_ERROR_ALREADY_SHOWN, false);

        mGoogleApiClient.registerConnectionCallbacks(this);
        mGoogleApiClient.registerConnectionFailedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        update();

        registerReceiver(pomodoroReceiver, FILTER_START);
        registerReceiver(pomodoroReceiver, FILTER_STOP);
        registerReceiver(pomodoroReceiver, FILTER_RESET);
        registerReceiver(pomodoroReceiver, FILTER_UPDATE);
        registerReceiver(pomodoroReceiver, FILTER_FINISH_ALARM);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
        unregisterReceiver(pomodoroReceiver);
    }

    private void nextTimer() {
        handler.removeCallbacks(updateRunnable);
        handler.postDelayed(updateRunnable, Utils.SECOND_MILLIS);
    }

    private void update() {
        updateWithoutTimer();
        nextTimer();
    }

    private void updateWithoutTimer() {

        getWindow().setBackgroundDrawable(new ColorDrawable(Utils.getNotificationColor(this, pomodoroMaster)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Utils.getNotificationColorDark(this, pomodoroMaster));
        }

        if (pomodoroMaster.isOngoing()) {
            mStartStopButton.setImageResource(R.drawable.ic_action_stop_96dp);
            mTime.setText(Utils.getRemainingTime(pomodoroMaster, /* shorten */ false));
            mDescription.setText(Utils.getActivityTitle(this, pomodoroMaster, /* shorten */ false));
        } else {
            mStartStopButton.setImageResource(R.drawable.ic_action_start_96dp);
            mTime.setText("00:00");
            mDescription.setText(Utils.getActivityTypeMessage(this, pomodoroMaster));
        }
    }

    @Override
    protected void onStart() {

        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @OnClick(R.id.pomodoro_start_stop_button)
    public void start() {

        if (pomodoroMaster.isOngoing()) {
            sendOrderedBroadcast(BaseNotificationService.STOP_INTENT, null);
        } else {
            ActivityType activityType = pomodoroMaster.getActivityType();
            if (activityType == ActivityType.NONE) {
                activityType = ActivityType.POMODORO;
            }
            final Intent startIntent = BaseNotificationService.START_INTENT;
            startIntent.putExtra(BaseNotificationService.EXTRA_ACTIVITY_TYPE, activityType.value());
            sendOrderedBroadcast(startIntent, null);
        }
        update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return baseUi.onCreateOptionsMenu(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return baseUi.onOptionsItemSelected(this, item)
                || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        outState.putBoolean(STATE_ERROR_ALREADY_SHOWN, mErrorAlreadyShown);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @DebugLog
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                    this, REQUEST_RESOLVE_ERROR);
            errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mResolvingError = false;
                }
            });
            if (!mErrorAlreadyShown) {
                try {
                    errorDialog.show();
                    mErrorAlreadyShown = true;
                } catch (RuntimeException e) {
                    Timber.e("Failed to show Google Play Services Dialog.");
                }
            }
            mResolvingError = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(Constants.PATH_POMODORO) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    //TODO Update
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }
}
