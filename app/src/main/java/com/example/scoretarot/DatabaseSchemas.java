package com.example.scoretarot;

import android.provider.BaseColumns;

public final class DatabaseSchemas {
    private DatabaseSchemas() {}

    public static final class GameEntry implements BaseColumns {
        public static final String TABLE_NAME = "games";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_PLAYER_COUNT = "player_count";
        public static final String COLUMN_ROUND_COUNT = "round_count";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_ADDRESS = "address";
    }

    public static final class PlayerEntry implements BaseColumns {
        public static final String TABLE_NAME = "players";
        public static final String COLUMN_GAME_ID = "game_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHOTO_PATH = "photo_path";
    }

    public static final String SQL_CREATE_GAME_TABLE =
            "CREATE TABLE " + GameEntry.TABLE_NAME + " (" +
                    GameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    GameEntry.COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                    GameEntry.COLUMN_PLAYER_COUNT + " INTEGER NOT NULL, " +
                    GameEntry.COLUMN_ROUND_COUNT + " INTEGER NOT NULL, " +
                    GameEntry.COLUMN_LATITUDE + " REAL, " +
                    GameEntry.COLUMN_LONGITUDE + " REAL, " +
                    GameEntry.COLUMN_ADDRESS + " TEXT)";

    public static final String SQL_CREATE_PLAYER_TABLE =
            "CREATE TABLE " + PlayerEntry.TABLE_NAME + " (" +
                    PlayerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PlayerEntry.COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                    PlayerEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    PlayerEntry.COLUMN_PHOTO_PATH + " TEXT)";

    public static final String SQL_DELETE_GAME_TABLE =
            "DROP TABLE IF EXISTS " + GameEntry.TABLE_NAME;

    public static final String SQL_DELETE_PLAYER_TABLE =
            "DROP TABLE IF EXISTS " + PlayerEntry.TABLE_NAME;
}