package com.example.scoretarot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NewGameActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText playerCountInput;
    private TextView locationText;
    private Double currentLatitude;
    private Double currentLongitude;
    private String currentAddress;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ActivityResultLauncher<String> locationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        nameInput = findViewById(R.id.input_game_name);
        playerCountInput = findViewById(R.id.input_player_count);
        locationText = findViewById(R.id.text_location);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        Button useLocationButton = findViewById(R.id.button_use_location);
        Button createButton = findViewById(R.id.button_create_game);
        Button cancelButton = findViewById(R.id.button_cancel);

        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        loadCurrentLocation();
                    } else {
                        Toast.makeText(this, R.string.permission_location_denied, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        useLocationButton.setOnClickListener(v -> requestCurrentLocation());
        createButton.setOnClickListener(v -> createGame());
        cancelButton.setOnClickListener(v -> finish());
        refreshLocationText();
    }

    private void requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        loadCurrentLocation();
    }

    private void loadCurrentLocation() {
        locationText.setText(R.string.location_loading);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location == null) {
                    Toast.makeText(this, R.string.location_loading, Toast.LENGTH_SHORT).show();
                    return;
                }
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                resolveAddress(location);
            });
        }
    }

    private void resolveAddress(Location location) {
        new Thread(() -> {
            String addressText = null;
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    addressText = address.getAddressLine(0);
                }
            } catch (IOException ignored) {
            }
            String finalAddressText = addressText == null ? "Adresse inconnue" : addressText;
            currentAddress = finalAddressText;
            runOnUiThread(this::refreshLocationText);
        }).start();
    }

    private void refreshLocationText() {
        if (currentLatitude == null || currentLongitude == null) {
            locationText.setText(R.string.new_game_no_location);
            return;
        }
        locationText.setText(getString(R.string.new_game_location_format, currentLatitude, currentLongitude, currentAddress == null ? "Adresse inconnue" : currentAddress));
    }

    private void createGame() {
        int playerCount;
        String gameName = nameInput.getText().toString().trim();
        if (gameName.isEmpty()) gameName = null;
        
        try {
            playerCount = Integer.parseInt(playerCountInput.getText().toString().trim());
        } catch (NumberFormatException exception) {
            Toast.makeText(this, R.string.new_game_invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        long gameId = GameDatabase.getInstance(this).insertGame(gameName, playerCount, 0, currentLatitude, currentLongitude, currentAddress);
        if (gameId == -1) {
            Toast.makeText(this, R.string.new_game_invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(PlayersActivity.newIntent(this, gameId));
        finish();
    }
}
