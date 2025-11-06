// LotteryInformationActivity.java
package com.example.string_events;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class LotteryInformationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lottery_information_screen);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

    }
}
