package com.example.string_events;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class QrCodeActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "extra_event_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageView imgQr = findViewById(R.id.img_qr);

        btnBack.setOnClickListener(v -> onBackPressed());

        String eventId = getIntent().getStringExtra("event_id");
        if (eventId == null || eventId.isEmpty()) {
            eventId = "demo-event-id";
        }

        String qrContent = "stringevents://event/" + eventId;

        try {
            Bitmap bitmap = QRUtils.generateQrCode(qrContent, 800, 800);
            imgQr.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }
}
