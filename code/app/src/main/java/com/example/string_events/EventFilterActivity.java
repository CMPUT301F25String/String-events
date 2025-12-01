package com.example.string_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class EventFilterActivity extends AppCompatActivity {
    public static final String EXTRA_TAGS     = "extra_tags";
    public static final String EXTRA_START_MS = "extra_start_ms";
    public static final String EXTRA_END_MS   = "extra_end_ms";

    private final HashSet<String> selected = new HashSet<>();
    private TextView chipBadminton, chipGames, chipArts, chipLearning;
    private EditText etStart, etEnd;

    private void toggle(String tag, android.widget.TextView chip) {
        if (selected.contains(tag)) {
            selected.remove(tag);
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(0xFF101114);
        } else {
            selected.add(tag);
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(0xFFFFFFFF);
        }
    }

    private void syncChipUI() {
        setChipState(chipBadminton, "Badminton");
        setChipState(chipGames,     "Games");
        setChipState(chipArts,      "Arts");
        setChipState(chipLearning,  "Learning");
    }

    private void setChipState(android.widget.TextView chip, String tag) {
        boolean on = selected.contains(tag);
        chip.setBackgroundResource(on ? R.drawable.bg_chip_selected : R.drawable.bg_chip_inactive);
        chip.setTextColor(on ? 0xFFFFFFFF : 0xFF101114);
    }

    private @Nullable Long parseToMillis(String s) {
        if (s.isEmpty()) return null;
        try {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
            fmt.setLenient(false);
            return Objects.requireNonNull(fmt.parse(s)).getTime();
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Time format: yyyy-MM-dd HH:mm", android.widget.Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_filter);

        chipBadminton = findViewById(R.id.chip_badminton_f);
        chipGames     = findViewById(R.id.chip_games_f);
        chipArts      = findViewById(R.id.chip_arts_f);
        chipLearning  = findViewById(R.id.chip_learning_f);

        etStart = findViewById(R.id.et_start_time);
        etEnd   = findViewById(R.id.et_end_time);

        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnClear = findViewById(R.id.btn_clear);
        MaterialButton btnApply = findViewById(R.id.btn_apply);
        CardView timeFilterCard = findViewById(R.id.time_filter_card);

        Intent intent = getIntent();
        String[] incoming = intent.getStringArrayExtra(EXTRA_TAGS);
        if (incoming != null) java.util.Collections.addAll(selected, incoming);
        syncChipUI();
        String action = intent.getStringExtra("action");

        // if we're selecting filters, we should be able to select time filters
        if (Objects.equals(action, "filter")) {
            timeFilterCard.setVisibility(CardView.VISIBLE);

        } else {
            // otherwise, we can only select tags
            timeFilterCard.setVisibility(CardView.GONE);
        }

        chipBadminton.setOnClickListener(v -> toggle("Badminton", chipBadminton));
        chipGames.setOnClickListener(v     -> toggle("Games",     chipGames));
        chipArts.setOnClickListener(v      -> toggle("Arts",      chipArts));
        chipLearning.setOnClickListener(v  -> toggle("Learning",  chipLearning));

        etStart.setOnClickListener(v -> showDateTimePicker(etStart));
        etEnd.setOnClickListener(v   -> showDateTimePicker(etEnd));

        btnBack.setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> {
            Intent out = new Intent();
            out.putExtra(EXTRA_TAGS, new String[0]);
            setResult(RESULT_OK, out);
            finish();
        });

        btnApply.setOnClickListener(v -> {
            Intent out = new Intent();
            out.putExtra(EXTRA_TAGS, selected.toArray(new String[0]));

            Long msStart = parseToMillis(etStart.getText().toString().trim());
            Long msEnd   = parseToMillis(etEnd.getText().toString().trim());
            if (msStart != null) out.putExtra(EXTRA_START_MS, msStart);
            if (msEnd   != null) out.putExtra(EXTRA_END_MS,   msEnd);

            setResult(RESULT_OK, out);
            finish();
        });
    }

    private void showDateTimePicker(EditText target) {
        final java.util.Calendar cal = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, y, m, d) -> {
            cal.set(java.util.Calendar.YEAR, y);
            cal.set(java.util.Calendar.MONTH, m);
            cal.set(java.util.Calendar.DAY_OF_MONTH, d);

            new android.app.TimePickerDialog(this, (v, h, min) -> {
                cal.set(java.util.Calendar.HOUR_OF_DAY, h);
                cal.set(java.util.Calendar.MINUTE, min);
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
                target.setText(fmt.format(cal.getTime()));
            }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), true).show();

        }, cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH),
                cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }
}
