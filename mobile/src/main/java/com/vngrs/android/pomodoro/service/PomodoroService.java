package com.vngrs.android.pomodoro.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.vngrs.android.pomodoro.App;
import com.vngrs.android.pomodoro.receivers.PomodoroAlarmReceiver;
import com.vngrs.android.pomodoro.shared.PomodoroMaster;
import com.vngrs.android.pomodoro.shared.model.ActivityType;
import com.vngrs.android.pomodoro.ui.MainActivity;

import org.joda.time.DateTime;

import javax.inject.Inject;

import timber.log.Timber;

public class PomodoroService extends Service implements PomodoroMaster.PomodoroMasterListener {

    private static final int NOTIFICATION_ID = 1;

    @Inject PomodoroMaster pomodoroMaster;
    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public PomodoroService getService() {
            return PomodoroService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.get(this).component().inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pomodoroMaster.setPomodoroMasterListener(this);
        pomodoroMaster.check();

        if (intent != null) {
            switch (intent.getAction()) {
                case PomodoroMaster.ACTION_STOP:
                    stopSelf();
                    break;
                case PomodoroMaster.ACTION_ALARM:
                    //TODO change the notification with a message and start button.
//                    ActivityType stoppingForType = activityTypeStorage.get();
                    PomodoroAlarmReceiver.completeWakefulIntent(intent);
                    break;
                case PomodoroMaster.ACTION_START:
                    pomodoroMaster.handleStart();
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
    }

    @Override
    public void syncNotification(ActivityType activityType,
                                 DateTime nextPomodoro,
                                 int pomodorosDone,
                                 boolean screenOn) {
        if (activityType != ActivityType.NONE) {
            final Notification notification =
                    PomodoroMaster.createNotificationBuilderForActivityType(this,
                            new Intent(this, MainActivity.class),
                            activityType,
                            pomodorosDone,
                            nextPomodoro,
                            screenOn);
            startForeground(NOTIFICATION_ID, notification);
        } else {
            Timber.d("ignore notify for activityType " + ActivityType.NONE);
        }
    }
}
