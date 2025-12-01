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

/**
 * Activity that allows users to select event filters such as tags and time range.
 * <p>
 * Filters are returned to the calling activity via an {@link Intent} result:
 * <ul>
 *     <li>{@link #EXTRA_TAGS} - selected tags as a String array</li>
 *     <li>{@link #EXTRA_START_MS} - start time in milliseconds (optional)</li>
 *     <li>{@link #EXTRA_END_MS} - end time in milliseconds (optional)</li>
 * </ul>
 * Depending on the {@code action} extra, the time filter card is shown or hidden.
 */
public class EventFilterActivity extends AppCompatActivity {

    /**
     * Intent extra key for the selected tags.
     */
    public static final String EXTRA_TAGS     = "extra_tags";

    /**
     * Intent extra key for the selected start time in milliseconds.
     */
    public static final String EXTRA_START_MS = "extra_start_ms";

    /**
     * Intent extra key for the selected end time in milliseconds.
     */
    public static final String EXTRA_END_MS   = "extra_end_ms";

    /**
     * Set of currently selected tag strings.
     */
    private final HashSet<String> selected = new HashSet<>();

    private TextView chipBadminton, chipGames, chipArts, chipLearning;
    private EditText etStart, etEnd;

    /**
     * Toggles the selection state of a tag and updates the chip UI accordingly.
     *
     * @param tag  the tag identifier (e.g., "Badminton")
     * @param chip the TextView acting as a chip representing this tag
     */
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

    /**
     * Synchronizes the visual state of all tag chips based on the current selection set.
     */
    private void syncChipUI() {
        setChipState(chipBadminton, "Badminton");
        setChipState(chipGames,     "Games");
        setChipState(chipArts,      "Arts");
        setChipState(chipLearning,  "Learning");
    }

    /**
     * Updates a single chip to reflect whether its tag is selected or not.
     *
     * @param chip the TextView chip to update
     * @param tag  the tag identifier linked to this chip
     */
    private void setChipState(android.widget.TextView chip, String tag) {
        boolean on = selected.contains(tag);
        chip.setBackgroundResource(on ? R.drawable.bg_chip_selected : R.drawable.bg_chip_inactive);
        chip.setTextColor(on ? 0xFFFFFFFF : 0xFF101114);
    }

    /**
     * Parses a date-time string into milliseconds since epoch.
     * <p>
     * Expected format: {@code yyyy-MM-dd HH:mm}. If parsing fails, a toast with
     * the expected format is shown and {@code null} is returned.
     *
     * @param s the date-time string to parse
     * @return the parsed milliseconds value, or {@code null} if parsing fails or string is empty
     */
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

    /**
     * Called when the activity is first created.
     * <p>
     * This method:
     * <ul>
     *     <li>Initializes all chip and input views.</li>
     *     <li>Restores any incoming tag filters from the launching intent.</li>
     *     <li>Shows or hides the time filter card based on the {@code action} extra.</li>
     *     <li>Sets up click listeners for chips, date/time fields, and control buttons.</li>
     * </ul>
     *
     * @param savedInstanceState previous state, or {@code null} if created fresh
     */
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

        // If we are selecting filters, time-based filters are available
        if (Objects.equals(action, "filter")) {
            timeFilterCard.setVisibility(CardView.VISIBLE);
        } else {
            // Otherwise, only tag selection is allowed
            timeFilterCard.setVisibility(CardView.GONE);
        }

        // Tag chips toggle handlers
        chipBadminton.setOnClickListener(v -> toggle("Badminton", chipBadminton));
        chipGames.setOnClickListener(v     -> toggle("Games",     chipGames));
        chipArts.setOnClickListener(v      -> toggle("Arts",      chipArts));
        chipLearning.setOnClickListener(v  -> toggle("Learning",  chipLearning));

        // Date/time pickers for start and end fields
        etStart.setOnClickListener(v -> showDateTimePicker(etStart));
        etEnd.setOnClickListener(v   -> showDateTimePicker(etEnd));

        // Back button simply closes the filter screen
        btnBack.setOnClickListener(v -> finish());

        // Clear button removes all filters and returns an empty tag list
        btnClear.setOnClickListener(v -> {
            Intent out = new Intent();
            out.putExtra(EXTRA_TAGS, new String[0]);
            setResult(RESULT_OK, out);
            finish();
        });

        // Apply button returns selected tags and optional time range to the caller
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

    /**
     * Shows a combined date and time picker dialog and writes the selected value into the target field.
     * <p>
     * The resulting text is formatted as {@code yyyy-MM-dd HH:mm}.
     *
     * @param target the {@link EditText} to receive the formatted date-time string
     */
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
