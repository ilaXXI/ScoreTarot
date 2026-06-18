package com.example.scoretarot;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManagePlayersActivity extends AppCompatActivity {

    private final List<GameDatabase.PlayerRecord> allPlayers = new ArrayList<>();
    private PlayerAdapter adapter;
    private GameDatabase db;
    private long playerPendingPhoto = -1;

    private final ActivityResultLauncher<Void> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(), bitmap -> {
        if (bitmap != null && playerPendingPhoto != -1) {
            savePhoto(bitmap, playerPendingPhoto);
        }
    });

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
        if (granted) takePhotoLauncher.launch(null);
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_players);

        db = GameDatabase.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ListView listView = findViewById(R.id.list_all_players);
        TextInputEditText nameInput = findViewById(R.id.input_manage_name);
        findViewById(R.id.button_manage_add).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (!name.isEmpty()) {
                long id = db.insertPlayer(name, null);
                nameInput.setText("");
                loadPlayers();
                promptForPhoto(id);
            }
        });

        adapter = new PlayerAdapter(this, allPlayers);
        listView.setAdapter(adapter);

        loadPlayers();
    }

    private void promptForPhoto(long playerId) {
        new AlertDialog.Builder(this)
                .setTitle("Photo")
                .setMessage("Prendre une photo pour ce joueur ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    playerPendingPhoto = playerId;
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                    } else {
                        takePhotoLauncher.launch(null);
                    }
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void loadPlayers() {
        allPlayers.clear();
        allPlayers.addAll(db.getAllPlayers());
        adapter.notifyDataSetChanged();
    }

    private void savePhoto(Bitmap bitmap, long playerId) {
        File directory = new File(getFilesDir(), "player_photos");
        if (!directory.exists()) directory.mkdirs();
        File file = new File(directory, String.format(Locale.getDefault(), "player_%d.jpg", playerId));
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            db.updatePlayerPhoto(playerId, file.getAbsolutePath());
            loadPlayers();
        } catch (IOException e) {
            Log.e("ManagePlayers", "Error saving photo", e);
        }
    }

    private class PlayerAdapter extends ArrayAdapter<GameDatabase.PlayerRecord> {
        public PlayerAdapter(Context context, List<GameDatabase.PlayerRecord> players) {
            super(context, 0, players);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_player_manage, parent, false);
            }
            GameDatabase.PlayerRecord player = getItem(position);
            TextView text = convertView.findViewById(R.id.text_manage_name);
            ImageView img = convertView.findViewById(R.id.image_manage_photo);

            if (player != null) {
                text.setText(player.name);
                if (player.photoPath != null) {
                    img.setImageURI(Uri.fromFile(new File(player.photoPath)));
                } else {
                    img.setImageDrawable(new ColorDrawable(TarotUtils.getPlaceholderColor(player.name)));
                }
                convertView.setOnClickListener(v -> showPlayerOptions(player));
            }
            return convertView;
        }
    }

    private void showPlayerOptions(GameDatabase.PlayerRecord player) {
        String[] options = {"Photo", "Modifier", "Supprimer"};
        new AlertDialog.Builder(this)
                .setTitle(player.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        playerPendingPhoto = player.id;
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        } else {
                            takePhotoLauncher.launch(null);
                        }
                    } else if (which == 1) {
                        showEditDialog(player);
                    } else {
                        db.deletePlayer(player.id);
                        loadPlayers();
                    }
                })
                .show();
    }

    private void showEditDialog(GameDatabase.PlayerRecord player) {
        TextInputEditText input = new TextInputEditText(this);
        input.setText(player.name);
        new AlertDialog.Builder(this)
                .setTitle("Modifier")
                .setView(input)
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        db.updatePlayer(player.id, name, player.photoPath);
                        loadPlayers();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}
