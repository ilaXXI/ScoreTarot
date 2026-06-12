package com.example.scoretarot;
import android.content.Context;
import android.database.sqlite.*;
import android.provider.BaseColumns;

public class DatabaseSchemas {
    private DatabaseSchemas() {}

    public static class Games implements BaseColumns {
        public static final String TABLE_NAME = "game";
        public static final String COLUMN_NAME_PLAYER_COUNT = "player_count";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_ROUND_COUNT = "round_count";
    }

    private static final String SQL_CREATE_GAME_TABLE =
            "CREATE TABLE " + Games.TABLE_NAME + " (" +
            Games._ID + " INTEGER PRIMARY KEY," +
            Games.COLUMN_NAME_DATE + " INTEGER," +
            Games.COLUMN_NAME_PLAYER_COUNT + " INTEGER," +
            Games.COLUMN_NAME_ROUND_COUNT + " INTEGER)";

    private static final String SQL_DELETE_GAME_TABLE =
            "DROP TABLE IF EXISTS " + Games.TABLE_NAME;

    public class GamesDbHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "scoretarot.db";

        public GamesDbHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_GAME_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_GAME_TABLE);
            onCreate(db);
        }
    }

    GamesDbHelper dbHelper = new GamesDbHelper(getContext());
}