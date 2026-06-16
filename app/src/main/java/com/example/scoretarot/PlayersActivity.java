package com.example.scoretarot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayersActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_ID = "extra_game_id";

    public static Intent newIntent(Context context, long gameId) {
        Intent intent = new Intent(context, PlayersActivity.class);
        intent.putExtra(EXTRA_GAME_ID, gameId);
        return intent;
    }

    private long gameId = -1;
    private GameDatabase.GameRecord currentGame;
    private final List<GameDatabase.PlayerRecord> players = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private long selectedPlayerId = -1;
    private TextView selectedText;
    private ImageView photoPreview;
    private ActivityResultLauncher<Void> takePhotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players);

        gameId = getIntent().getLongExtra(EXTRA_GAME_ID, -1);
        selectedText = findViewById(R.id.text_selected_player);
        photoPreview = findViewById(R.id.image_player_photo);
        ListView listView = findViewById(R.id.list_players);
        EditText nameInput = findViewById(R.id.input_player_name);
        TextView gameInfo = findViewById(R.id.text_game_info);
        Button addButton = findViewById(R.id.button_add_player);
        Button photoButton = findViewById(R.id.button_take_photo);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> selectPlayer(players.get(position)));

        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
            if (bitmap == null || selectedPlayerId == -1) {
                return;
            }
            savePhoto(bitmap, selectedPlayerId);
        });

        addButton.setOnClickListener(v -> {
            if (gameId == -1) {
                Toast.makeText(this, R.string.players_empty_game, Toast.LENGTH_SHORT).show();
                return;
            }
            String playerName = nameInput.getText().toString().trim();
            if (playerName.isEmpty()) {
                return;
            }
            GameDatabase.getInstance(this).insertPlayer(gameId, playerName, null);
            nameInput.setText("");
            loadPlayers();
        });

        photoButton.setOnClickListener(v -> {
            if (selectedPlayerId == -1) {
                Toast.makeText(this, R.string.players_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            takePhotoLauncher.launch(null);
        });

        if (gameId != -1) {
            currentGame = GameDatabase.getInstance(this).getGame(gameId);
        }
        if (currentGame != null) {
            gameInfo.setText(getString(R.string.players_game_format, currentGame.id, currentGame.playerCount, currentGame.roundCount));
        } else {
            gameInfo.setText(R.string.players_empty_game);
        }
        loadPlayers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlayers();
    }

    private void loadPlayers() {
        players.clear();
        if (gameId != -1) {
            players.addAll(GameDatabase.getInstance(this).getPlayersForGame(gameId));
        }
        List<String> labels = new ArrayList<>();
        for (GameDatabase.PlayerRecord player : players) {
            labels.add(player.name + (player.photoPath == null ? "" : " (photo)"));
        }
        adapter.clear();
        adapter.addAll(labels);
        adapter.notifyDataSetChanged();
        if (players.isEmpty()) {
            selectedText.setText(R.string.players_empty);
            photoPreview.setImageDrawable(null);
            selectedPlayerId = -1;
        }
    }

    private void selectPlayer(GameDatabase.PlayerRecord player) {
        selectedPlayerId = player.id;
        selectedText.setText(getString(R.string.players_selected, player.name));
        if (player.photoPath != null) {
            photoPreview.setImageURI(android.net.Uri.fromFile(new File(player.photoPath)));
        } else {
            photoPreview.setImageDrawable(null);
        }
    }

    private void savePhoto(Bitmap bitmap, long playerId) {
        File directory = new File(getFilesDir(), "player_photos");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, String.format(Locale.getDefault(), "player_%d.jpg", playerId));
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            GameDatabase.getInstance(this).updatePlayerPhoto(playerId, file.getAbsolutePath());
            loadPlayers();
            GameDatabase.PlayerRecord updatedPlayer = findPlayer(playerId);
            if (updatedPlayer != null) {
                selectPlayer(updatedPlayer);
            }
        } catch (IOException exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private GameDatabase.PlayerRecord findPlayer(long playerId) {
        for (GameDatabase.PlayerRecord player : players) {
            if (player.id == playerId) {
                return player;
            }
        }
        return null;
    }
}