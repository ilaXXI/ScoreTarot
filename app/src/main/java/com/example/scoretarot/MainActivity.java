package com.example.scoretarot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    private GameDatabase gameDatabase;
    private TextView gamesCountText;
    private TextView playersCountText;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameDatabase = GameDatabase.getInstance(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigation(item.getItemId());
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        gamesCountText = findViewById(R.id.home_games_count);
        playersCountText = findViewById(R.id.home_players_count);

        Button newGameButton = findViewById(R.id.button_new_game);
        Button gamesButton = findViewById(R.id.button_games);
        Button playersButton = findViewById(R.id.button_players);
        Button settingsButton = findViewById(R.id.button_settings);

        newGameButton.setOnClickListener(v -> startActivity(new Intent(this, NewGameActivity.class)));
        gamesButton.setOnClickListener(v -> startActivity(new Intent(this, GamesActivity.class)));
        playersButton.setOnClickListener(v -> startActivity(new Intent(this, ManagePlayersActivity.class)));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStats();
    }

    private void refreshStats() {
        gamesCountText.setText(getString(R.string.home_games_count, gameDatabase.getGameCount()));
        playersCountText.setText(getString(R.string.home_players_count, gameDatabase.getPlayerCount()));
    }

    private void handleNavigation(int itemId) {
        if (itemId == R.id.nav_home) {
            return;
        }
        if (itemId == R.id.nav_new_game) {
            startActivity(new Intent(this, NewGameActivity.class));
            return;
        }
        if (itemId == R.id.nav_games) {
            startActivity(new Intent(this, GamesActivity.class));
            return;
        }
        if (itemId == R.id.nav_players) {
            startActivity(new Intent(this, ManagePlayersActivity.class));
            return;
        }
        if (itemId == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
    }

}