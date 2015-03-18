package com.vngrs.android.pomodoro.model;

import org.joda.time.DateTime;

/**
 * Status object.
 *
 * Created by Said Tahsin Dane on 17/03/15.
 */
public class Status {

    public String username;
    public String status;
    public DateTime begin;
    public int length;
    public boolean error;
    public String error_message;
    public int pomodoro_int;
}
