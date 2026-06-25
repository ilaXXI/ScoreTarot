package com.example.scoretarot;

import android.provider.BaseColumns;

public final class DatabaseSchemas {
    private DatabaseSchemas() {}

    public static final class GameEntry implements BaseColumns {
        public static final String TABLE_NAME = "games";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_PLAYER_COUNT = "player_count";
        public static final String COLUMN_ROUND_COUNT = "round_count";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_ADDRESS = "address";
    }

    public static final class PlayerEntry implements BaseColumns {
        public static final String TABLE_NAME = "players";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHOTO_PATH = "photo_path";
    }

    public static final class GamePlayerEntry implements BaseColumns {
        public static final String TABLE_NAME = "game_players";
        public static final String COLUMN_GAME_ID = "game_id";
        public static final String COLUMN_PLAYER_ID = "player_id";
    }

    public static final class RoundEntry implements BaseColumns {
        public static final String TABLE_NAME = "rounds";
        public static final String COLUMN_GAME_ID = "game_id";
        public static final String COLUMN_TAKER_ID = "taker_id";
        public static final String COLUMN_PARTNER_ID = "partner_id";
        public static final String COLUMN_CONTRACT = "contract";
        public static final String COLUMN_BOUTS = "bouts";
        public static final String COLUMN_POINTS = "points";
        public static final String COLUMN_BONUSES = "bonuses";
        public static final String COLUMN_SCORE = "score";
    }

    public static final String SQL_CREATE_GAME_TABLE =
            "CREATE TABLE " + GameEntry.TABLE_NAME + " (" +
                    GameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    GameEntry.COLUMN_NAME + " TEXT, " +
                    GameEntry.COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                    GameEntry.COLUMN_PLAYER_COUNT + " INTEGER NOT NULL, " +
                    GameEntry.COLUMN_ROUND_COUNT + " INTEGER NOT NULL, " +
                    GameEntry.COLUMN_LATITUDE + " REAL, " +
                    GameEntry.COLUMN_LONGITUDE + " REAL, " +
                    GameEntry.COLUMN_ADDRESS + " TEXT)";

    public static final String SQL_CREATE_PLAYER_TABLE =
            "CREATE TABLE " + PlayerEntry.TABLE_NAME + " (" +
                    PlayerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PlayerEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    PlayerEntry.COLUMN_PHOTO_PATH + " TEXT)";

    public static final String SQL_CREATE_GAME_PLAYER_TABLE =
            "CREATE TABLE " + GamePlayerEntry.TABLE_NAME + " (" +
                    GamePlayerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    GamePlayerEntry.COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                    GamePlayerEntry.COLUMN_PLAYER_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + GamePlayerEntry.COLUMN_GAME_ID + ") REFERENCES " + GameEntry.TABLE_NAME + "(" + GameEntry._ID + "), " +
                    "FOREIGN KEY(" + GamePlayerEntry.COLUMN_PLAYER_ID + ") REFERENCES " + PlayerEntry.TABLE_NAME + "(" + PlayerEntry._ID + "))";

    public static final String SQL_CREATE_ROUND_TABLE =
            "CREATE TABLE " + RoundEntry.TABLE_NAME + " (" +
                    RoundEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RoundEntry.COLUMN_GAME_ID + " INTEGER NOT NULL, " +
                    RoundEntry.COLUMN_TAKER_ID + " INTEGER NOT NULL, " +
                    RoundEntry.COLUMN_PARTNER_ID + " INTEGER, " +
                    RoundEntry.COLUMN_CONTRACT + " TEXT NOT NULL, " +
                    RoundEntry.COLUMN_BOUTS + " INTEGER NOT NULL, " +
                    RoundEntry.COLUMN_POINTS + " REAL NOT NULL, " +
                    RoundEntry.COLUMN_BONUSES + " TEXT, " +
                    RoundEntry.COLUMN_SCORE + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + RoundEntry.COLUMN_GAME_ID + ") REFERENCES " + GameEntry.TABLE_NAME + "(" + GameEntry._ID + "))";

    public static final String SQL_DELETE_GAME_TABLE =
            "DROP TABLE IF EXISTS " + GameEntry.TABLE_NAME;

    public static final String SQL_DELETE_PLAYER_TABLE =
            "DROP TABLE IF EXISTS " + PlayerEntry.TABLE_NAME;

    public static final String SQL_DELETE_GAME_PLAYER_TABLE =
            "DROP TABLE IF EXISTS " + GamePlayerEntry.TABLE_NAME;

    public static final String SQL_DELETE_ROUND_TABLE =
            "DROP TABLE IF EXISTS " + RoundEntry.TABLE_NAME;
}
