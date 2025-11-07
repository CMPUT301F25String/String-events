package com.example.string_events;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class UiHostActivity extends AppCompatActivity {

    public static final String EXTRA_LAYOUT_RES_ID = "layoutResId";

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

