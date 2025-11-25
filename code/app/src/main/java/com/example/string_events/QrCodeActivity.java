package com.example.string_events;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class QrCodeActivity extends AppCompatActivity {

    // Key used to receive event id from other parts of the app
    public static final String EXTRA_EVENT_ID = "extra_event_id";

    // Temporary hardcoded id for event3
    private static final String EVENT3_ID = "07d4dd53-3efe-4613-b852-0720a924be8b";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        // Back arrow on the top bar
        ImageButton btnBack = findViewById(R.id.btn_back);
        // ImageView that will display the generated QR code
        ImageView imgQr = findViewById(R.id.img_qr);

        btnBack.setOnClickListener(v -> onBackPressed());

        // Try to get event id from intent, otherwise fall back to event3
        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            eventId = EVENT3_ID;
        }

        // Custom deep link scheme that our app can handle
        String qrContent = "stringevents://event/" + eventId;

        try {
            // Generate QR bitmap using QRUtils helper
            Bitmap bitmap = QRUtils.generateQrCode(qrContent, 800, 800);
            imgQr.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }
}
