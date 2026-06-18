package com.example.scoretarot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ScoreEntryActivity extends AppCompatActivity {

    private static final String EXTRA_GAME_ID = "game_id";
    private long gameId;
    private GameDatabase.GameRecord game;
    private List<GameDatabase.PlayerRecord> players;

    public static Intent newIntent(Context context, long gameId) {
        Intent i = new Intent(context, ScoreEntryActivity.class);
        i.putExtra(EXTRA_GAME_ID, gameId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_entry);

        gameId = getIntent().getLongExtra(EXTRA_GAME_ID, -1);
        game = GameDatabase.getInstance(this).getGame(gameId);
        players = GameDatabase.getInstance(this).getPlayersForGame(gameId);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        Spinner takerSpinner = findViewById(R.id.spinner_taker);
        Spinner partnerSpinner = findViewById(R.id.spinner_partner);
        TextView partnerLabel = findViewById(R.id.label_partner);
        Spinner contractSpinner = findViewById(R.id.spinner_contract);
        Spinner boutsSpinner = findViewById(R.id.spinner_bouts);
        
        CheckBox cbPetitWon = findViewById(R.id.cb_petit_bout_won);
        CheckBox cbPetitLost = findViewById(R.id.cb_petit_bout_lost);
        CheckBox cbPoigneeS = findViewById(R.id.cb_poignee_simple);
        CheckBox cbPoigneeD = findViewById(R.id.cb_poignee_double);
        CheckBox cbPoigneeT = findViewById(R.id.cb_poignee_triple);
        CheckBox cbChelemWon = findViewById(R.id.cb_chelem_won);
        CheckBox cbChelemAnn = findViewById(R.id.cb_chelem_announced);

        TextInputEditText pointsInput = findViewById(R.id.input_points);
        Button saveButton = findViewById(R.id.button_save_round);

        List<String> playerNames = new ArrayList<>();
        for (GameDatabase.PlayerRecord p : players) playerNames.add(p.name);
        ArrayAdapter<String> playerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, playerNames);
        playerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        takerSpinner.setAdapter(playerAdapter);

        if (game.playerCount == 5) {
            partnerLabel.setVisibility(View.VISIBLE);
            partnerSpinner.setVisibility(View.VISIBLE);
            partnerSpinner.setAdapter(playerAdapter);
        }

        String[] contracts = {"Petite", "Garde", "Garde Sans", "Garde Contre"};
        ArrayAdapter<String> contractAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contracts);
        contractAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contractSpinner.setAdapter(contractAdapter);

        String[] bouts = {"0 Bout", "1 Bout", "2 Bouts", "3 Bouts"};
        ArrayAdapter<String> boutsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bouts);
        boutsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        boutsSpinner.setAdapter(boutsAdapter);

        saveButton.setOnClickListener(v -> {
            String pStr = pointsInput.getText().toString();
            if (pStr.isEmpty()) return;
            double points = Double.parseDouble(pStr);
            int boutCount = boutsSpinner.getSelectedItemPosition();
            String contract = contracts[contractSpinner.getSelectedItemPosition()];
            long takerId = players.get(takerSpinner.getSelectedItemPosition()).id;
            Long partnerId = (game.playerCount == 5) ? players.get(partnerSpinner.getSelectedItemPosition()).id : null;

            List<String> selectedBonuses = new ArrayList<>();
            if (cbPetitWon.isChecked()) selectedBonuses.add("Petit au bout (Gagné)");
            if (cbPetitLost.isChecked()) selectedBonuses.add("Petit au bout (Perdu)");
            if (cbPoigneeS.isChecked()) selectedBonuses.add("Poignée Simple");
            if (cbPoigneeD.isChecked()) selectedBonuses.add("Double Poignée");
            if (cbPoigneeT.isChecked()) selectedBonuses.add("Triple Poignée");
            if (cbChelemWon.isChecked()) selectedBonuses.add("Chelem réalisé");
            if (cbChelemAnn.isChecked()) selectedBonuses.add("Chelem annoncé");

            int score = calculateTarotScore(contract, boutCount, points, selectedBonuses);
            String bonusesStr = TextUtils.join(",", selectedBonuses);
            
            GameDatabase.getInstance(this).insertRound(gameId, takerId, partnerId, contract, boutCount, points, bonusesStr, score);
            
            Toast.makeText(this, "Manche enregistrée ! Score: " + score, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private int calculateTarotScore(String contract, int bouts, double points, List<String> bonuses) {
        int threshold = TarotUtils.calculateContractThreshold(bouts);
        double diff = points - threshold;
        int score = (int) (25 + Math.abs(diff));
        int coef = TarotUtils.getContractCoefficient(contract);

        int finalScore = score * coef;

        for (String b : bonuses) {
            if (b.equals("Petit au bout (Gagné)")) finalScore += 10 * coef;
            else if (b.equals("Petit au bout (Perdu)")) finalScore -= 10 * coef;
        }

        if (diff < 0) finalScore = -finalScore;

        for (String b : bonuses) {
            if (b.equals("Poignée Simple")) finalScore += (finalScore >= 0 ? 20 : -20);
            else if (b.equals("Double Poignée")) finalScore += (finalScore >= 0 ? 30 : -30);
            else if (b.equals("Triple Poignée")) finalScore += (finalScore >= 0 ? 40 : -40);
            else if (b.equals("Chelem réalisé")) finalScore += (finalScore >= 0 ? 200 : -200);
            else if (b.equals("Chelem annoncé")) finalScore += (finalScore >= 0 ? 400 : -400);
        }

        return finalScore;
    }
}
