package com.example.scoretarot;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.example.scoretarot.DatabaseSchemas.GameEntry;
import com.example.scoretarot.DatabaseSchemas.PlayerEntry;

import java.util.ArrayList;
import java.util.List;

public class GameDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "scoretarot.db";
	private static final int DATABASE_VERSION = 2;
	private static GameDatabase instance;

	public static synchronized GameDatabase getInstance(Context context) {
		if (instance == null) {
			instance = new GameDatabase(context.getApplicationContext());
		}
		return instance;
	}

	private GameDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DatabaseSchemas.SQL_CREATE_GAME_TABLE);
		db.execSQL(DatabaseSchemas.SQL_CREATE_PLAYER_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DatabaseSchemas.SQL_DELETE_PLAYER_TABLE);
		db.execSQL(DatabaseSchemas.SQL_DELETE_GAME_TABLE);
		onCreate(db);
	}

	public long insertGame(int playerCount, int roundCount, Double latitude, Double longitude, String address) {
		ContentValues values = new ContentValues();
		values.put(GameEntry.COLUMN_CREATED_AT, System.currentTimeMillis());
		values.put(GameEntry.COLUMN_PLAYER_COUNT, playerCount);
		values.put(GameEntry.COLUMN_ROUND_COUNT, roundCount);
		if (latitude == null) {
			values.putNull(GameEntry.COLUMN_LATITUDE);
		} else {
			values.put(GameEntry.COLUMN_LATITUDE, latitude);
		}
		if (longitude == null) {
			values.putNull(GameEntry.COLUMN_LONGITUDE);
		} else {
			values.put(GameEntry.COLUMN_LONGITUDE, longitude);
		}
		if (TextUtils.isEmpty(address)) {
			values.putNull(GameEntry.COLUMN_ADDRESS);
		} else {
			values.put(GameEntry.COLUMN_ADDRESS, address);
		}
		return getWritableDatabase().insert(GameEntry.TABLE_NAME, null, values);
	}

	public long insertPlayer(long gameId, String name, String photoPath) {
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.COLUMN_GAME_ID, gameId);
		values.put(PlayerEntry.COLUMN_NAME, name);
		if (TextUtils.isEmpty(photoPath)) {
			values.putNull(PlayerEntry.COLUMN_PHOTO_PATH);
		} else {
			values.put(PlayerEntry.COLUMN_PHOTO_PATH, photoPath);
		}
		return getWritableDatabase().insert(PlayerEntry.TABLE_NAME, null, values);
	}

	public void updatePlayerPhoto(long playerId, String photoPath) {
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.COLUMN_PHOTO_PATH, photoPath);
		getWritableDatabase().update(
				PlayerEntry.TABLE_NAME,
				values,
				PlayerEntry._ID + " = ?",
				new String[]{String.valueOf(playerId)}
		);
	}

	public List<GameRecord> getAllGames() {
		List<GameRecord> games = new ArrayList<>();
		Cursor cursor = getReadableDatabase().query(
				GameEntry.TABLE_NAME,
				null,
				null,
				null,
				null,
				null,
				GameEntry.COLUMN_CREATED_AT + " DESC"
		);
		try {
			while (cursor.moveToNext()) {
				games.add(readGame(cursor));
			}
		} finally {
			cursor.close();
		}
		return games;
	}

	public GameRecord getLatestGame() {
		List<GameRecord> games = getAllGames();
		if (games.isEmpty()) {
			return null;
		}
		return games.get(0);
	}

	public GameRecord getGame(long gameId) {
		Cursor cursor = getReadableDatabase().query(
				GameEntry.TABLE_NAME,
				null,
				GameEntry._ID + " = ?",
				new String[]{String.valueOf(gameId)},
				null,
				null,
				null
		);
		try {
			if (cursor.moveToFirst()) {
				return readGame(cursor);
			}
			return null;
		} finally {
			cursor.close();
		}
	}

	public List<PlayerRecord> getPlayersForGame(long gameId) {
		List<PlayerRecord> players = new ArrayList<>();
		Cursor cursor = getReadableDatabase().query(
				PlayerEntry.TABLE_NAME,
				null,
				PlayerEntry.COLUMN_GAME_ID + " = ?",
				new String[]{String.valueOf(gameId)},
				null,
				null,
				PlayerEntry._ID + " ASC"
		);
		try {
			while (cursor.moveToNext()) {
				players.add(readPlayer(cursor));
			}
		} finally {
			cursor.close();
		}
		return players;
	}

	public int getGameCount() {
		return countRows(GameEntry.TABLE_NAME);
	}

	public int getPlayerCount() {
		return countRows(PlayerEntry.TABLE_NAME);
	}

	private int countRows(String tableName) {
		Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + tableName, null);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0);
			}
			return 0;
		} finally {
			cursor.close();
		}
	}

	private GameRecord readGame(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(GameEntry._ID));
		long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_CREATED_AT));
		int playerCount = cursor.getInt(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_PLAYER_COUNT));
		int roundCount = cursor.getInt(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_ROUND_COUNT));
		Double latitude = cursor.isNull(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_LATITUDE)) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_LATITUDE));
		Double longitude = cursor.isNull(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_LONGITUDE)) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_LONGITUDE));
		String address = cursor.getString(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_ADDRESS));
		return new GameRecord(id, createdAt, playerCount, roundCount, latitude, longitude, address);
	}

	private PlayerRecord readPlayer(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(PlayerEntry._ID));
		long gameId = cursor.getLong(cursor.getColumnIndexOrThrow(PlayerEntry.COLUMN_GAME_ID));
		String name = cursor.getString(cursor.getColumnIndexOrThrow(PlayerEntry.COLUMN_NAME));
		String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(PlayerEntry.COLUMN_PHOTO_PATH));
		return new PlayerRecord(id, gameId, name, photoPath);
	}

	public static class GameRecord {
		public final long id;
		public final long createdAt;
		public final int playerCount;
		public final int roundCount;
		public final Double latitude;
		public final Double longitude;
		public final String address;

		public GameRecord(long id, long createdAt, int playerCount, int roundCount, Double latitude, Double longitude, String address) {
			this.id = id;
			this.createdAt = createdAt;
			this.playerCount = playerCount;
			this.roundCount = roundCount;
			this.latitude = latitude;
			this.longitude = longitude;
			this.address = address;
		}
	}

	public static class PlayerRecord {
		public final long id;
		public final long gameId;
		public final String name;
		public final String photoPath;

		public PlayerRecord(long id, long gameId, String name, String photoPath) {
			this.id = id;
			this.gameId = gameId;
			this.name = name;
			this.photoPath = photoPath;
		}
	}

}
