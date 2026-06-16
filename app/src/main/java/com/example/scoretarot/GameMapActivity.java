package com.example.scoretarot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class GameMapActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_ID = "extra_game_id";

    public static Intent newIntent(Context context, long gameId) {
        Intent intent = new Intent(context, GameMapActivity.class);
        intent.putExtra(EXTRA_GAME_ID, gameId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_map);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        long gameId = getIntent().getLongExtra(EXTRA_GAME_ID, -1);
        TextView details = findViewById(R.id.text_game_details);
        MapView mapView = findViewById(R.id.map_view);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        GameDatabase.GameRecord game = GameDatabase.getInstance(this).getGame(gameId);
        if (game == null || game.latitude == null || game.longitude == null) {
            details.setText(R.string.map_empty);
            return;
        }

        details.setText(getString(
                R.string.new_game_location_format,
                game.latitude,
                game.longitude,
                game.address == null ? "Adresse inconnue" : game.address
        ));

        GeoPoint position = new GeoPoint(game.latitude, game.longitude);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(position);

        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(game.address == null ? "Partie" : game.address);
        mapView.getOverlays().add(marker);
    }
}