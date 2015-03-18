package com.vngrs.android.pomodoro;

import com.vngrs.android.pomodoro.model.Status;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Internal VNGRS Pomodoro API definition.
 * More info can be found at https://github.com/bdaylik/pomodoro
 *
 * Created by Said Tahsin Dane on 17/03/15.
 */
public interface PomodoroApi {

    @GET("/api.php?c=status_all")
    void getStatusAll(Callback<ArrayList<Status>> callback);
    @GET("/api.php?c=status")
    void getStatus(Callback<Status> callback);
    @GET("/api.php?c=stop")
    void stop(Callback<Status> callback);
    @GET("/api.php?c=break")
    void doBreak(Callback<Status> callback);
    @GET("/api.php?c=start")
    void start(Callback<Status> callback);
}
