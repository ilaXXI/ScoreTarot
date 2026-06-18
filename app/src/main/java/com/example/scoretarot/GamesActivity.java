package com.example.scoretarot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GamesActivity extends AppCompatActivity {

    private final List<GameDatabase.GameRecord> games = new ArrayList<>();
    private GameAdapter adapter;
    private GameDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);

        db = GameDatabase.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        ListView listView = findViewById(R.id.list_games);
        adapter = new GameAdapter(this, games);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            startActivity(GameDetailActivity.newIntent(this, games.get(position).id));
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showGameOptions(games.get(position));
            return true;
        });

        findViewById(R.id.button_open_map).setOnClickListener(v -> startActivity(new Intent(this, GlobalMapActivity.class)));
        findViewById(R.id.button_refresh_games).setOnClickListener(v -> loadGames());
        loadGames();
    }

    private void showGameOptions(GameDatabase.GameRecord game) {
        String[] options = {"Supprimer la partie"};
        new AlertDialog.Builder(this)
                .setTitle(game.name != null ? game.name : "Partie #" + game.id)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        db.deleteGame(game.id);
                        loadGames();
                        Toast.makeText(this, "Partie supprimée", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGames();
    }

    private void loadGames() {
        games.clear();
        games.addAll(db.getAllGames());
        adapter.notifyDataSetChanged();
        findViewById(R.id.text_empty_games).setVisibility(games.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private class GameAdapter extends ArrayAdapter<GameDatabase.GameRecord> {
        public GameAdapter(Context context, List<GameDatabase.GameRecord> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_game, parent, false);
            }
            GameDatabase.GameRecord game = getItem(position);
            TextView dateText = convertView.findViewById(R.id.text_game_date);
            TextView detailsText = convertView.findViewById(R.id.text_game_details);
            TextView scoresText = convertView.findViewById(R.id.text_game_scores);

            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
            dateText.setText(game.name != null ? game.name : "Partie du " + df.format(game.createdAt));
            detailsText.setText(game.playerCount + " joueurs - " + (game.address != null ? game.address : "Lieu inconnu"));

            List<GameDatabase.PlayerRecord> players = db.getPlayersForGame(game.id);
            StringBuilder sb = new StringBuilder();
            for (GameDatabase.PlayerRecord p : players) {
                int s = db.getPlayerTotalScore(game.id, p.id);
                sb.append(p.name).append(": ").append(s).append(" | ");
            }
            scoresText.setText(sb.toString());

            return convertView;
        }
    }
}
