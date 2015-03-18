package com.vngrs.android.pomodoro.ui;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Said Tahsin Dane on 17/03/15.
 */
public interface AppContainer {

    boolean onCreateOptionsMenu(Activity activity, Menu menu);
    boolean onOptionsItemSelected(Activity activity, MenuItem item);

    AppContainer DEFAULT = new AppContainer() {

        @Override
        public boolean onCreateOptionsMenu(Activity activity, Menu menu) {
            return false;
        }

        @Override
        public boolean onOptionsItemSelected(Activity activity, MenuItem item) {
            return false;
        }
    };
}
