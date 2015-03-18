package com.vngrs.android.pomodoro;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.vngrs.android.pomodoro.ui.AppContainer;

import javax.inject.Inject;


public class MainActivity extends ActionBarActivity {

    @Inject
    AppContainer appContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PomodoroApp app = PomodoroApp.get(this);
        app.component().inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return appContainer.onCreateOptionsMenu(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return appContainer.onOptionsItemSelected(this, item)
                || super.onOptionsItemSelected(item);
    }
}
