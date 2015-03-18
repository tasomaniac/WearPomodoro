package com.vngrs.android.pomodoro.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.vngrs.android.pomodoro.R;

import javax.inject.Singleton;

/**
 * Created by Said Tahsin Dane on 17/03/15.
 */
@Singleton
public class VngrsAppContainer implements AppContainer {

    @Override
    public boolean onCreateOptionsMenu(Activity activity, Menu menu) {
        activity.getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(Activity activity, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                activity.startActivity(new Intent(activity, SettingsActivity.class));
            default:
                return false;
        }
    }
}
