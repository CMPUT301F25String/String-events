// LotteryInformationActivity.java
package com.example.string_events;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Static information screen explaining the lottery process.
 * Provides a back button to return to the previous screen.
 */
public class LotteryInformationActivity extends AppCompatActivity {

    /**
     * Inflates the layout and wires the back button to finish this activity.
     *
     * @param savedInstanceState previously saved instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lottery_information_screen);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
