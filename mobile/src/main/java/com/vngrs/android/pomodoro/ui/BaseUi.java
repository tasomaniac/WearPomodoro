package com.vngrs.android.pomodoro.ui;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Base Ui class for defining the differences between product flavors.
 *
 * Created by Said Tahsin Dane on 17/03/15.
 */
public interface BaseUi {

    boolean onCreateOptionsMenu(Activity activity, Menu menu);
    boolean onOptionsItemSelected(Activity activity, MenuItem item);

    BaseUi DEFAULT = new BaseUi() {

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
