package com.tasomaniac.android.pomodoro.data;

import android.net.Uri;

import com.tasomaniac.android.pomodoro.BuildConfig;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = PomodoroProvider.AUTHORITY,
        database = PomodoroDatabase.class,
        packageName = "com.tasomaniac.android.pomodoro.provider")
public final class PomodoroProvider {

    private PomodoroProvider() {
    }

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    @TableEndpoint(table = PomodoroDatabase.POMODOROS)
    public static class Pomodoros {

        @ContentUri(
                path = PomodoroDatabase.POMODOROS,
                type = "vnd.android.cursor.dir/pomodoro",
                defaultSort = PomodoroDatabase.PomodoroColumns.DATETIME + " DESC")
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PomodoroDatabase.POMODOROS);

        @InexactContentUri(
                name = "POMODORO_ID",
                path = PomodoroDatabase.POMODOROS + "/#",
                type = "vnd.android.cursor.item/pomodoro",
                whereColumn = PomodoroDatabase.PomodoroColumns.ID,
                pathSegment = 1)
        public static Uri withId(long id) {
            return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
        }
    }
}