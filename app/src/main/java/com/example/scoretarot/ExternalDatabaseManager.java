package com.example.scoretarot;

import android.content.Context;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

public final class ExternalDatabaseManager {

    private ExternalDatabaseManager() {
    }

    public static String sync(Context context) {
        new Thread(() -> {
            try {
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://10.0.2.2:3306/scoretarot_db", "root", "");

                GameDatabase localDb = GameDatabase.getInstance(context);
                List<GameDatabase.PlayerRecord> players = localDb.getAllPlayers();

                for (GameDatabase.PlayerRecord p : players) {
                    String sql = "INSERT INTO players (remote_id, name) VALUES (?, ?) " +
                                 "ON DUPLICATE KEY UPDATE name = ?";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setLong(1, p.id);
                    stmt.setString(2, p.name);
                    stmt.setString(3, p.name);
                    stmt.executeUpdate();
                }

                connection.close();

            } catch (Exception e) {
                Log.e("DB_SYNC", "Erreur réseau: " + e.getMessage());
            }
        }).start();

        return context.getString(R.string.sync_success);
    }
}
