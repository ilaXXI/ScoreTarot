package com.example.scoretarot;

import android.content.Context;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public final class ExternalDatabaseStub {

    private ExternalDatabaseStub() {
    }

    public static String sync(Context context) {
        // Envoi des données vers une base MySQL externe (ex: WAMP/XAMPP sur PC)
        // L'adresse 10.0.2.2 correspond à l'hôte (votre PC) depuis l'émulateur
        new Thread(() -> {
            try {
                // Chargement explicite du driver si nécessaire (dépend de la version du jar)
                // Class.forName("com.mysql.jdbc.Driver");
                
                // Connexion à la base de données "scoretarot_db" sur le PC
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://10.0.2.2:3306/scoretarot_db", "root", "");

                GameDatabase localDb = GameDatabase.getInstance(context);
                List<GameDatabase.PlayerRecord> players = localDb.getAllPlayers();

                for (GameDatabase.PlayerRecord p : players) {
                    // Exemple : On insère ou met à jour les joueurs sur le serveur
                    String sql = "INSERT INTO players (remote_id, name) VALUES (?, ?) " +
                                 "ON DUPLICATE KEY UPDATE name = ?";
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setLong(1, p.id);
                    stmt.setString(2, p.name);
                    stmt.setString(3, p.name);
                    stmt.executeUpdate();
                }

                Log.d("ExternalDB", "Synchronisation des joueurs réussie");
                connection.close();

            } catch (Exception e) {
                Log.e("ExternalDB", "Erreur sync: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        return "Synchronisation lancée en arrière-plan...";
    }
}
