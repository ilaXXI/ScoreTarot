package com.example.scoretarot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GamesActivity extends AppCompatActivity {

    private final List<GameDatabase.GameRecord> games = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);

        TextView emptyText = findViewById(R.id.text_empty_games);
        ListView listView = findViewById(R.id.list_games);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> startActivity(GameMapActivity.newIntent(this, games.get(position).id)));
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            startActivity(PlayersActivity.newIntent(this, games.get(position).id));
            return true;
        });

        findViewById(R.id.button_refresh_games).setOnClickListener(v -> loadGames(emptyText));
        loadGames(emptyText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGames(findViewById(R.id.text_empty_games));
    }

    private void loadGames(TextView emptyText) {
        games.clear();
        games.addAll(GameDatabase.getInstance(this).getAllGames());

        List<String> labels = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        for (GameDatabase.GameRecord game : games) {
            String dateText = format.format(game.createdAt);
            String addressText = game.address == null ? "Adresse inconnue" : game.address;
            labels.add("#" + game.id + " • " + dateText + "\n" + game.playerCount + " joueurs, " + game.roundCount + " manches\n" + addressText);
        }
        adapter.clear();
        adapter.addAll(labels);
        adapter.notifyDataSetChanged();
        emptyText.setVisibility(labels.isEmpty() ? View.VISIBLE : View.GONE);
    }
}