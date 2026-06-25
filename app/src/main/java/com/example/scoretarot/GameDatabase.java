package com.example.scoretarot;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.example.scoretarot.DatabaseSchemas.GameEntry;
import com.example.scoretarot.DatabaseSchemas.PlayerEntry;
import com.example.scoretarot.DatabaseSchemas.GamePlayerEntry;
import com.example.scoretarot.DatabaseSchemas.RoundEntry;

import java.util.ArrayList;
import java.util.List;

public class GameDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "scoretarot.db";
	private static final int DATABASE_VERSION = 6;
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
		db.execSQL(DatabaseSchemas.SQL_CREATE_GAME_PLAYER_TABLE);
		db.execSQL(DatabaseSchemas.SQL_CREATE_ROUND_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DatabaseSchemas.SQL_DELETE_ROUND_TABLE);
		db.execSQL(DatabaseSchemas.SQL_DELETE_GAME_PLAYER_TABLE);
		db.execSQL(DatabaseSchemas.SQL_DELETE_PLAYER_TABLE);
		db.execSQL(DatabaseSchemas.SQL_DELETE_GAME_TABLE);
		onCreate(db);
	}

	public long insertGame(String name, int playerCount, int roundCount, Double latitude, Double longitude, String address) {
		ContentValues values = new ContentValues();
		values.put(GameEntry.COLUMN_NAME, name);
		values.put(GameEntry.COLUMN_CREATED_AT, System.currentTimeMillis());
		values.put(GameEntry.COLUMN_PLAYER_COUNT, playerCount);
		values.put(GameEntry.COLUMN_ROUND_COUNT, roundCount);
		if (latitude == null) values.putNull(GameEntry.COLUMN_LATITUDE);
		else values.put(GameEntry.COLUMN_LATITUDE, latitude);
		if (longitude == null) values.putNull(GameEntry.COLUMN_LONGITUDE);
		else values.put(GameEntry.COLUMN_LONGITUDE, longitude);
		if (TextUtils.isEmpty(address)) values.putNull(GameEntry.COLUMN_ADDRESS);
		else values.put(GameEntry.COLUMN_ADDRESS, address);
		return getWritableDatabase().insert(GameEntry.TABLE_NAME, null, values);
	}

	public void deleteGame(long gameId) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(GameEntry.TABLE_NAME, GameEntry._ID + " = ?", new String[]{String.valueOf(gameId)});
		db.delete(GamePlayerEntry.TABLE_NAME, GamePlayerEntry.COLUMN_GAME_ID + " = ?", new String[]{String.valueOf(gameId)});
		db.delete(RoundEntry.TABLE_NAME, RoundEntry.COLUMN_GAME_ID + " = ?", new String[]{String.valueOf(gameId)});
	}

	public long insertPlayer(String name, String photoPath) {
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.COLUMN_NAME, name);
		if (TextUtils.isEmpty(photoPath)) values.putNull(PlayerEntry.COLUMN_PHOTO_PATH);
		else values.put(PlayerEntry.COLUMN_PHOTO_PATH, photoPath);
		return getWritableDatabase().insert(PlayerEntry.TABLE_NAME, null, values);
	}

	public void addPlayerToGame(long gameId, long playerId) {
		ContentValues values = new ContentValues();
		values.put(GamePlayerEntry.COLUMN_GAME_ID, gameId);
		values.put(GamePlayerEntry.COLUMN_PLAYER_ID, playerId);
		getWritableDatabase().insert(GamePlayerEntry.TABLE_NAME, null, values);
	}

	public void updatePlayer(long id, String name, String photoPath) {
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.COLUMN_NAME, name);
		values.put(PlayerEntry.COLUMN_PHOTO_PATH, photoPath);
		getWritableDatabase().update(PlayerEntry.TABLE_NAME, values, PlayerEntry._ID + " = ?", new String[]{String.valueOf(id)});
	}

	public void deletePlayer(long id) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(PlayerEntry.TABLE_NAME, PlayerEntry._ID + " = ?", new String[]{String.valueOf(id)});
		db.delete(GamePlayerEntry.TABLE_NAME, GamePlayerEntry.COLUMN_PLAYER_ID + " = ?", new String[]{String.valueOf(id)});
	}

	public void updatePlayerPhoto(long playerId, String photoPath) {
		ContentValues values = new ContentValues();
		values.put(PlayerEntry.COLUMN_PHOTO_PATH, photoPath);
		getWritableDatabase().update(PlayerEntry.TABLE_NAME, values, PlayerEntry._ID + " = ?", new String[]{String.valueOf(playerId)});
	}

	public long insertRound(long gameId, long takerId, Long partnerId, String contract, int bouts, double points, String bonuses, int score) {
		ContentValues values = new ContentValues();
		values.put(RoundEntry.COLUMN_GAME_ID, gameId);
		values.put(RoundEntry.COLUMN_TAKER_ID, takerId);
		if (partnerId != null) values.put(RoundEntry.COLUMN_PARTNER_ID, partnerId);
		values.put(RoundEntry.COLUMN_CONTRACT, contract);
		values.put(RoundEntry.COLUMN_BOUTS, bouts);
		values.put(RoundEntry.COLUMN_POINTS, points);
		values.put(RoundEntry.COLUMN_BONUSES, bonuses);
		values.put(RoundEntry.COLUMN_SCORE, score);
		return getWritableDatabase().insert(RoundEntry.TABLE_NAME, null, values);
	}

	public List<RoundRecord> getRoundsForGame(long gameId) {
		List<RoundRecord> rounds = new ArrayList<>();
		Cursor c = getReadableDatabase().query(RoundEntry.TABLE_NAME, null, RoundEntry.COLUMN_GAME_ID + " = ?", new String[]{String.valueOf(gameId)}, null, null, RoundEntry._ID + " ASC");
		while (c.moveToNext()) {
			rounds.add(new RoundRecord(
					c.getLong(c.getColumnIndexOrThrow(RoundEntry._ID)),
					c.getLong(c.getColumnIndexOrThrow(RoundEntry.COLUMN_GAME_ID)),
					c.getLong(c.getColumnIndexOrThrow(RoundEntry.COLUMN_TAKER_ID)),
					c.isNull(c.getColumnIndexOrThrow(RoundEntry.COLUMN_PARTNER_ID)) ? null : c.getLong(c.getColumnIndexOrThrow(RoundEntry.COLUMN_PARTNER_ID)),
					c.getString(c.getColumnIndexOrThrow(RoundEntry.COLUMN_CONTRACT)),
					c.getInt(c.getColumnIndexOrThrow(RoundEntry.COLUMN_BOUTS)),
					c.getDouble(c.getColumnIndexOrThrow(RoundEntry.COLUMN_POINTS)),
					c.getString(c.getColumnIndexOrThrow(RoundEntry.COLUMN_BONUSES)),
					c.getInt(c.getColumnIndexOrThrow(RoundEntry.COLUMN_SCORE))
			));
		}
		c.close();
		return rounds;
	}

    public RoundRecord getRound(long roundId) {
        Cursor c = getReadableDatabase().query(RoundEntry.TABLE_NAME, null, RoundEntry._ID + " = ?", new String[]{String.valueOf(roundId)}, null, null, null);
        try {
            if (c.moveToFirst()) {
                return new RoundRecord(
                        c.getLong(c.getColumnIndexOrThrow(RoundEntry._ID)),
                        c.getLong(c.getColumnIndexOrThrow(RoundEntry.COLUMN_GAME_ID)),
                        c.getLong(c.getColumnIndexOrThrow(RoundEntry.COLUMN_TAKER_ID)),
                        c.isNull(c.getColumnIndexOrThrow(RoundEntry.COLUMN_PARTNER_ID)) ? null : c.getLong(c.getColumnIndexOrThrow(RoundEntry.COLUMN_PARTNER_ID)),
                        c.getString(c.getColumnIndexOrThrow(RoundEntry.COLUMN_CONTRACT)),
                        c.getInt(c.getColumnIndexOrThrow(RoundEntry.COLUMN_BOUTS)),
                        c.getDouble(c.getColumnIndexOrThrow(RoundEntry.COLUMN_POINTS)),
                        c.getString(c.getColumnIndexOrThrow(RoundEntry.COLUMN_BONUSES)),
                        c.getInt(c.getColumnIndexOrThrow(RoundEntry.COLUMN_SCORE))
                );
            }
            return null;
        } finally { c.close(); }
    }

	public int getPlayerTotalScore(long gameId, long playerId) {
		int total = 0;
		GameRecord game = getGame(gameId);
		if (game == null) return 0;
		List<RoundRecord> rounds = getRoundsForGame(gameId);
		for (RoundRecord r : rounds) {
			if (game.playerCount == 3) {
				if (r.takerId == playerId) total += r.score * 2;
				else total -= r.score;
			} else if (game.playerCount == 4) {
				if (r.takerId == playerId) total += r.score * 3;
				else total -= r.score;
			} else if (game.playerCount == 5) {
				if (r.partnerId == null) {
					if (r.takerId == playerId) total += r.score * 4;
					else total -= r.score;
				} else {
					if (r.takerId == playerId) total += r.score * 2;
					else if (r.partnerId == playerId) total += r.score;
					else total -= r.score;
				}
			}
		}
		return total;
	}

	public List<GameRecord> getAllGames() {
		List<GameRecord> games = new ArrayList<>();
		Cursor cursor = getReadableDatabase().query(GameEntry.TABLE_NAME, null, null, null, null, null, GameEntry.COLUMN_CREATED_AT + " DESC");
		while (cursor.moveToNext()) games.add(readGame(cursor));
		cursor.close();
		return games;
	}

	public GameRecord getGame(long gameId) {
		Cursor cursor = getReadableDatabase().query(GameEntry.TABLE_NAME, null, GameEntry._ID + " = ?", new String[]{String.valueOf(gameId)}, null, null, null);
		try {
			if (cursor.moveToFirst()) return readGame(cursor);
			return null;
		} finally { cursor.close(); }
	}

    public PlayerRecord getPlayer(long playerId) {
        Cursor cursor = getReadableDatabase().query(PlayerEntry.TABLE_NAME, null, PlayerEntry._ID + " = ?", new String[]{String.valueOf(playerId)}, null, null, null);
        try {
            if (cursor.moveToFirst()) return readPlayer(cursor);
            return null;
        } finally { cursor.close(); }
    }

	public List<PlayerRecord> getPlayersForGame(long gameId) {
		List<PlayerRecord> players = new ArrayList<>();
		String query = "SELECT p.* FROM " + PlayerEntry.TABLE_NAME + " p JOIN " + GamePlayerEntry.TABLE_NAME + " gp ON p." + PlayerEntry._ID + " = gp." + GamePlayerEntry.COLUMN_PLAYER_ID + " WHERE gp." + GamePlayerEntry.COLUMN_GAME_ID + " = ? ORDER BY p." + PlayerEntry._ID + " ASC";
		Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(gameId)});
		while (cursor.moveToNext()) players.add(readPlayer(cursor));
		cursor.close();
		return players;
	}

	public List<PlayerRecord> getAllPlayers() {
		List<PlayerRecord> players = new ArrayList<>();
		Cursor cursor = getReadableDatabase().query(PlayerEntry.TABLE_NAME, null, null, null, null, null, PlayerEntry.COLUMN_NAME + " ASC");
		while (cursor.moveToNext()) players.add(readPlayer(cursor));
		cursor.close();
		return players;
	}

	public int getGameCount() { return countRows(GameEntry.TABLE_NAME); }
	public int getPlayerCount() { return countRows(PlayerEntry.TABLE_NAME); }

	public void clearDatabase() {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DELETE FROM " + RoundEntry.TABLE_NAME);
		db.execSQL("DELETE FROM " + GamePlayerEntry.TABLE_NAME);
		db.execSQL("DELETE FROM " + PlayerEntry.TABLE_NAME);
		db.execSQL("DELETE FROM " + GameEntry.TABLE_NAME);
	}

	private int countRows(String tableName) {
		Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + tableName, null);
		try {
			if (cursor.moveToFirst()) return cursor.getInt(0);
			return 0;
		} finally { cursor.close(); }
	}

	private GameRecord readGame(Cursor cursor) {
		return new GameRecord(
				cursor.getLong(cursor.getColumnIndexOrThrow(GameEntry._ID)),
				cursor.getString(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_NAME)),
				cursor.getLong(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_CREATED_AT)),
				cursor.getInt(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_PLAYER_COUNT)),
				cursor.getInt(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_ROUND_COUNT)),
				cursor.isNull(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_LATITUDE)) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_LATITUDE)),
				cursor.isNull(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_LONGITUDE)) ? null : cursor.getDouble(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_LONGITUDE)),
				cursor.getString(cursor.getColumnIndexOrThrow(GameEntry.COLUMN_ADDRESS))
		);
	}

	private PlayerRecord readPlayer(Cursor cursor) {
		return new PlayerRecord(
				cursor.getLong(cursor.getColumnIndexOrThrow(PlayerEntry._ID)),
				cursor.getString(cursor.getColumnIndexOrThrow(PlayerEntry.COLUMN_NAME)),
				cursor.getString(cursor.getColumnIndexOrThrow(PlayerEntry.COLUMN_PHOTO_PATH))
		);
	}

	public static class GameRecord {
		public final long id;
		public final String name;
		public final long createdAt;
		public final int playerCount;
		public final int roundCount;
		public final Double latitude;
		public final Double longitude;
		public final String address;
		public GameRecord(long id, String name, long createdAt, int playerCount, int roundCount, Double latitude, Double longitude, String address) {
			this.id = id; this.name = name; this.createdAt = createdAt; this.playerCount = playerCount; this.roundCount = roundCount;
			this.latitude = latitude; this.longitude = longitude; this.address = address;
		}
	}

	public static class PlayerRecord {
		public final long id;
		public final String name;
		public final String photoPath;
		public PlayerRecord(long id, String name, String photoPath) {
			this.id = id; this.name = name; this.photoPath = photoPath;
		}
	}

	public static class RoundRecord {
		public final long id;
		public final long gameId;
		public final long takerId;
		public final Long partnerId;
		public final String contract;
		public final int bouts;
		public final double points;
		public final String bonuses;
		public final int score;
		public RoundRecord(long id, long gameId, long takerId, Long partnerId, String contract, int bouts, double points, String bonuses, int score) {
			this.id = id; this.gameId = gameId; this.takerId = takerId; this.partnerId = partnerId;
			this.contract = contract; this.bouts = bouts; this.points = points; this.score = score; this.bonuses = bonuses;
		}
	}
}
