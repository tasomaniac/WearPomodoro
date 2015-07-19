package com.tasomaniac.android.pomodoro.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.DefaultValue;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Table;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

@Database(version = PomodoroDatabase.VERSION,
        packageName = "com.tasomaniac.android.pomodoro.provider")
public final class PomodoroDatabase {

    private PomodoroDatabase() {
    }

    public static final int VERSION = 1;

    @Table(PomodoroColumns.class)
    public static final String POMODOROS = "pomodoros";

    public interface PomodoroColumns {

        @DataType(INTEGER) @PrimaryKey @AutoIncrement String ID = "_id";

        @DataType(TEXT) @NotNull String ACTION = "action";

        @DataType(TEXT) String ACTIVITY_TYPE = "activity_type";

        @DataType(INTEGER) @DefaultValue("CURRENT_TIMESTAMP") String DATETIME = "datetime";
    }
}