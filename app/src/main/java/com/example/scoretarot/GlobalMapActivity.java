package com.example.scoretarot;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class GlobalMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_map);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        MapView mapView = findViewById(R.id.global_map_view);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        List<GameDatabase.GameRecord> games = GameDatabase.getInstance(this).getAllGames();
        GeoPoint firstPoint = null;

        for (GameDatabase.GameRecord game : games) {
            if (game.latitude != null && game.longitude != null) {
                GeoPoint point = new GeoPoint(game.latitude, game.longitude);
                if (firstPoint == null) firstPoint = point;

                Marker marker = new Marker(mapView);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(game.name != null ? game.name : "Partie #" + game.id);
                marker.setSnippet(game.address);
                mapView.getOverlays().add(marker);
            }
        }

        if (firstPoint != null) {
            mapView.getController().setZoom(10.0);
            mapView.getController().setCenter(firstPoint);
        }
    }
}
