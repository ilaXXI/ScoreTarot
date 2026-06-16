package com.example.scoretarot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LiveLocationActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView locationText;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker marker;
    private ActivityResultLauncher<String> permissionLauncher;
    private boolean updatesRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_location);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapView = findViewById(R.id.map_live_view);
        locationText = findViewById(R.id.text_live_location);
        Button startButton = findViewById(R.id.button_start_location);
        Button stopButton = findViewById(R.id.button_stop_location);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocation(location);
                }
            }
        };

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        startUpdates();
                    } else {
                        Toast.makeText(this, R.string.permission_location_denied, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        startButton.setOnClickListener(v -> requestPermissionAndStart());
        stopButton.setOnClickListener(v -> stopUpdates());
        locationText.setText(R.string.live_location_empty);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        stopUpdates();
    }

    private void requestPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        startUpdates();
    }

    private void startUpdates() {
        updatesRunning = true;
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1000)
                .build();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopUpdates() {
        updatesRunning = false;
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void updateLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        new Thread(() -> {
            String addressText = "Adresse inconnue";
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    addressText = addresses.get(0).getAddressLine(0);
                }
            } catch (IOException ignored) {
            }
            String finalAddressText = addressText;
            runOnUiThread(() -> {
                locationText.setText(getString(R.string.live_location_format, latitude, longitude, finalAddressText));
                GeoPoint position = new GeoPoint(latitude, longitude);
                if (marker == null) {
                    marker = new Marker(mapView);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    mapView.getOverlays().add(marker);
                }
                marker.setPosition(position);
                marker.setTitle(finalAddressText);
                mapView.getController().setZoom(16.0);
                mapView.getController().setCenter(position);
                mapView.invalidate();
            });
        }).start();
    }
}