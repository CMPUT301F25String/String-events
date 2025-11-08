package com.example.string_events;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Lightweight activity that hosts an arbitrary layout resource.
 * <p>
 * Useful for UI previews/tests by passing a layout resource ID via intent.
 */
public class UiHostActivity extends AppCompatActivity {

    /**
     * Intent extra key for the layout resource ID to display.
     */
    public static final String EXTRA_LAYOUT_RES_ID = "layoutResId";

    /**
     * Reads the target layout resource ID from the intent and sets it as content.
     * Finishes immediately if no valid layout was provided.
     *
     * @param savedInstanceState previous state, or {@code null}
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        @LayoutRes int layout = getIntent().getIntExtra(EXTRA_LAYOUT_RES_ID, 0);
        if (layout == 0) {
            finish();
            return;
        }
        setContentView(layout);
    }
}
