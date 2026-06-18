package com.example.scoretarot;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView infoText = findViewById(R.id.text_settings_info);
        infoText.setText(R.string.settings_about);

        Button syncButton = findViewById(R.id.button_test_sync);
        syncButton.setOnClickListener(v -> {
            String msg = ExternalDatabaseStub.sync(this);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });

        Button clearButton = findViewById(R.id.button_clear_data);
        clearButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Attention")
                    .setMessage("Voulez-vous vraiment supprimer toutes les parties et tous les joueurs ? Cette action est irréversible.")
                    .setPositiveButton("Tout supprimer", (dialog, which) -> {
                        GameDatabase.getInstance(this).clearDatabase();
                        Toast.makeText(this, "Données effacées", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }
}
