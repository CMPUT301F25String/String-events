package com.example.string_events;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QrScanActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    private DecoratedBarcodeView barcodeView;
    private boolean handledResult = false;
    private boolean torchOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnFlash = findViewById(R.id.btnFlash);
        ImageButton btnHome = findViewById(R.id.btnHome);
        ImageButton btnNotification = findViewById(R.id.btnNotification);
        ImageButton btnProfile = findViewById(R.id.btnProfile);

        MaterialCardView qrFrame = findViewById(R.id.qr_frame);

        // Create scanner view programmatically inside the card frame
        barcodeView = new DecoratedBarcodeView(this);
        MaterialCardView.LayoutParams params = new MaterialCardView.LayoutParams(
                MaterialCardView.LayoutParams.MATCH_PARENT,
                MaterialCardView.LayoutParams.MATCH_PARENT
        );
        barcodeView.setLayoutParams(params);
        qrFrame.removeAllViews();
        qrFrame.addView(barcodeView);

        btnBack.setOnClickListener(v -> finish());

        btnFlash.setOnClickListener(v -> {
            if (torchOn) {
                barcodeView.setTorchOff();
                torchOn = false;
            } else {
                barcodeView.setTorchOn();
                torchOn = true;
            }
        });

        btnHome.setOnClickListener(v -> {
            Intent i = new Intent(QrScanActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        btnNotification.setOnClickListener(v -> {
            Intent i = new Intent(QrScanActivity.this, NotificationScreen.class);
            startActivity(i);
        });

        btnProfile.setOnClickListener(v -> {
            Intent i = new Intent(QrScanActivity.this, ProfileScreen.class);
            startActivity(i);
        });

        // Camera permission check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );
        } else {
            startScanning();
        }
    }

    private void startScanning() {
        handledResult = false;
        barcodeView.decodeContinuous(callback);
        barcodeView.resume();
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (handledResult) {
                return;
            }
            handledResult = true;

            String contents = result.getText();
            if (contents == null || contents.isEmpty()) {
                Toast.makeText(QrScanActivity.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String eventId = null;

            try {
                Uri uri = Uri.parse(contents);
                // ex. "stringevents://event/<id>"
                // scheme = "stringevents", host = "event", lastPathSegment = <id>
                if ("stringevents".equals(uri.getScheme())
                        && "event".equals(uri.getHost())) {

                    String last = uri.getLastPathSegment();
                    if (last != null && !last.isEmpty()) {
                        eventId = last;
                    }
                }
            } catch (Exception ignored) {
            }

            // fallback if contents itself is just the event id
            if (eventId == null || eventId.isEmpty()) {
                eventId = contents;
            }

            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(QrScanActivity.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Intent i = new Intent(QrScanActivity.this, EventDetailActivity.class);
            i.putExtra("event_id", eventId);
            startActivity(i);
            finish();
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                finish();
            }
        }
    }
}
