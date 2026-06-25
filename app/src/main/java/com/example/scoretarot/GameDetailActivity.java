package com.example.scoretarot;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameDetailActivity extends AppCompatActivity {

    private static final String EXTRA_GAME_ID = "game_id";
    private long gameId;
    private GameDatabase db;
    private final List<GameDatabase.RoundRecord> rounds = new ArrayList<>();
    private RoundAdapter adapter;

    public static Intent newIntent(Context context, long gameId) {
        Intent i = new Intent(context, GameDetailActivity.class);
        i.putExtra(EXTRA_GAME_ID, gameId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        db = GameDatabase.getInstance(this);
        gameId = getIntent().getLongExtra(EXTRA_GAME_ID, -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        ListView roundList = findViewById(R.id.list_rounds);
        adapter = new RoundAdapter(this, rounds);
        roundList.setAdapter(adapter);

        roundList.setOnItemClickListener((parent, view, position, id) -> showRoundDetail(rounds.get(position)));

        MaterialButton addRoundButton = findViewById(R.id.button_add_round);
        addRoundButton.setOnClickListener(v -> startActivity(ScoreEntryActivity.newIntent(this, gameId)));

        loadGameDetail();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGameDetail();
    }

    private void loadGameDetail() {
        GameDatabase.GameRecord game = db.getGame(gameId);
        if (game == null) {
            finish();
            return;
        }

        TextView nameText = findViewById(R.id.text_detail_name);
        TextView infoText = findViewById(R.id.text_detail_info);
        LinearLayout scoresContainer = findViewById(R.id.container_player_scores);

        nameText.setText(game.name != null ? game.name : getString(R.string.game_number, game.id));
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        infoText.setText(df.format(game.createdAt) + " - " + (game.address != null ? game.address : getString(R.string.unknown_location)));

        scoresContainer.removeAllViews();
        List<GameDatabase.PlayerRecord> players = db.getPlayersForGame(gameId);
        for (GameDatabase.PlayerRecord p : players) {
            View v = LayoutInflater.from(this).inflate(R.layout.item_player_score, scoresContainer, false);
            ImageView img = v.findViewById(R.id.image_score_photo);
            TextView name = v.findViewById(R.id.text_score_name);
            TextView val = v.findViewById(R.id.text_score_value);

            name.setText(p.name);
            val.setText(String.valueOf(db.getPlayerTotalScore(gameId, p.id)));
            if (p.photoPath != null) {
                img.setImageURI(Uri.fromFile(new File(p.photoPath)));
            } else {
                img.setImageDrawable(new ColorDrawable(TarotUtils.getPlaceholderColor(p.name)));
            }
            scoresContainer.addView(v);
        }

        rounds.clear();
        rounds.addAll(db.getRoundsForGame(gameId));
        adapter.notifyDataSetChanged();
    }

    private void showRoundDetail(GameDatabase.RoundRecord r) {
        GameDatabase.PlayerRecord taker = db.getPlayer(r.takerId);
        String msg = getString(R.string.round_detail_taker, (taker != null ? taker.name : "?")) + "\n" +
                getString(R.string.round_detail_contract, r.contract) + "\n" +
                getString(R.string.round_detail_bouts, r.bouts) + "\n" +
                getString(R.string.round_detail_points, r.points, TarotUtils.calculateContractThreshold(r.bouts)) + "\n" +
                getString(R.string.round_detail_bonus, (r.bonuses == null || r.bonuses.isEmpty() ? getString(R.string.none) : r.bonuses)) + "\n" +
                getString(R.string.round_detail_score, r.score);

        new AlertDialog.Builder(this)
                .setTitle(R.string.round_details_title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private class RoundAdapter extends ArrayAdapter<GameDatabase.RoundRecord> {
        public RoundAdapter(Context context, List<GameDatabase.RoundRecord> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_round, parent, false);
            }
            GameDatabase.RoundRecord r = getItem(position);
            TextView num = convertView.findViewById(R.id.text_round_number);
            TextView contract = convertView.findViewById(R.id.text_round_contract);
            TextView taker = convertView.findViewById(R.id.text_round_taker);
            TextView score = convertView.findViewById(R.id.text_round_score);

            num.setText(String.valueOf(position + 1));
            contract.setText(r.contract);
            GameDatabase.PlayerRecord takerP = db.getPlayer(r.takerId);
            taker.setText(getString(R.string.taken_by, (takerP != null ? takerP.name : "?")));
            score.setText((r.score >= 0 ? "+" : "") + r.score);
            score.setTextColor(r.score >= 0 ? 0xFF4CAF50 : 0xFFE91E63);

            return convertView;
        }
    }
}
