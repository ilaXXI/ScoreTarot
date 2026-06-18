package com.example.scoretarot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

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
    private final List<GameDatabase.PlayerRecord> playersInGame = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private long selectedPlayerId = -1;
    private TextView selectedText;
    private ImageView photoPreview;
    private Button startGameButton;
    private ActivityResultLauncher<Void> takePhotoLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players);

        gameId = getIntent().getLongExtra(EXTRA_GAME_ID, -1);
        if (gameId != -1) {
            currentGame = GameDatabase.getInstance(this).getGame(gameId);
        }

        selectedText = findViewById(R.id.text_selected_player);
        photoPreview = findViewById(R.id.image_player_photo);
        ListView listView = findViewById(R.id.list_players);
        TextInputEditText nameInput = findViewById(R.id.input_player_name);
        TextView gameInfo = findViewById(R.id.text_game_info);
        Button addButton = findViewById(R.id.button_add_player);
        Button photoButton = findViewById(R.id.button_take_photo);
        Button pickExistingButton = findViewById(R.id.button_pick_existing);
        startGameButton = findViewById(R.id.button_start_game);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> selectPlayer(playersInGame.get(position)));

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        takePhotoLauncher.launch(null);
                    } else {
                        Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
            if (bitmap == null || selectedPlayerId == -1) {
                return;
            }
            savePhoto(bitmap, selectedPlayerId);
        });

        addButton.setOnClickListener(v -> {
            if (currentGame == null) return;
            if (playersInGame.size() >= currentGame.playerCount) {
                Toast.makeText(this, R.string.players_full, Toast.LENGTH_SHORT).show();
                return;
            }
            String playerName = nameInput.getText().toString().trim();
            if (playerName.isEmpty()) return;

            long playerId = GameDatabase.getInstance(this).insertPlayer(playerName, null);
            GameDatabase.getInstance(this).addPlayerToGame(gameId, playerId);
            nameInput.setText("");
            loadPlayers();
        });

        pickExistingButton.setOnClickListener(v -> showExistingPlayersDialog());

        photoButton.setOnClickListener(v -> {
            if (selectedPlayerId == -1) {
                Toast.makeText(this, R.string.players_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                takePhotoLauncher.launch(null);
            }
        });

        startGameButton.setOnClickListener(v -> {
            startActivity(GameDetailActivity.newIntent(this, gameId));
            finish();
        });

        if (currentGame != null) {
            gameInfo.setText(getString(R.string.players_game_format, currentGame.id, currentGame.playerCount, currentGame.roundCount));
        } else {
            gameInfo.setText(R.string.players_empty_game);
        }
        loadPlayers();
    }

    private void showExistingPlayersDialog() {
        if (currentGame == null || playersInGame.size() >= currentGame.playerCount) {
            Toast.makeText(this, R.string.players_full, Toast.LENGTH_SHORT).show();
            return;
        }

        List<GameDatabase.PlayerRecord> allPlayers = GameDatabase.getInstance(this).getAllPlayers();
        List<GameDatabase.PlayerRecord> availablePlayers = new ArrayList<>();
        for (GameDatabase.PlayerRecord p : allPlayers) {
            boolean alreadyIn = false;
            for (GameDatabase.PlayerRecord inGame : playersInGame) {
                if (inGame.id == p.id) {
                    alreadyIn = true;
                    break;
                }
            }
            if (!alreadyIn) availablePlayers.add(p);
        }

        if (availablePlayers.isEmpty()) {
            Toast.makeText(this, "Aucun autre joueur disponible dans la base.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[availablePlayers.size()];
        for (int i = 0; i < availablePlayers.size(); i++) {
            names[i] = availablePlayers.get(i).name;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.players_pick_existing)
                .setItems(names, (dialog, which) -> {
                    GameDatabase.PlayerRecord selected = availablePlayers.get(which);
                    GameDatabase.getInstance(this).addPlayerToGame(gameId, selected.id);
                    loadPlayers();
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlayers();
    }

    private void loadPlayers() {
        playersInGame.clear();
        if (gameId != -1) {
            playersInGame.addAll(GameDatabase.getInstance(this).getPlayersForGame(gameId));
        }
        List<String> labels = new ArrayList<>();
        for (GameDatabase.PlayerRecord player : playersInGame) {
            labels.add(player.name + (player.photoPath == null ? "" : " (📸)"));
        }
        adapter.clear();
        adapter.addAll(labels);
        adapter.notifyDataSetChanged();

        if (playersInGame.isEmpty()) {
            selectedText.setText(R.string.players_empty);
            photoPreview.setImageDrawable(null);
            selectedPlayerId = -1;
        }

        if (currentGame != null && playersInGame.size() >= currentGame.playerCount) {
            startGameButton.setVisibility(android.view.View.VISIBLE);
        } else {
            startGameButton.setVisibility(android.view.View.GONE);
        }
    }

    private void selectPlayer(GameDatabase.PlayerRecord player) {
        selectedPlayerId = player.id;
        selectedText.setText(player.name);
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
        for (GameDatabase.PlayerRecord player : playersInGame) {
            if (player.id == playerId) {
                return player;
            }
        }
        return null;
    }
}
