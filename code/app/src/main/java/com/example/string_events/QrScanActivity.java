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

/**
 * Activity that scans a QR code and navigates the user to the related event.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Request and handle camera permission.</li>
 *     <li>Initialize and display a {@link DecoratedBarcodeView} inside a MaterialCardView.</li>
 *     <li>Continuously scan QR codes using JourneyApps barcode scanner.</li>
 *     <li>Parse QR content to extract an event ID, then open {@link EventDetailActivity}.</li>
 *     <li>Provide flash toggle and basic bottom navigation (home, notifications, profile).</li>
 * </ul>
 * <p>
 * Expected QR formats:
 * <ul>
 *     <li>Custom deep link: {@code stringevents://event/<eventId>}</li>
 *     <li>Fallback: raw {@code eventId} string in the code contents.</li>
 * </ul>
 */
public class QrScanActivity extends AppCompatActivity {

    /**
     * Request code used when asking for camera permission.
     */
    private static final int CAMERA_PERMISSION_REQUEST = 1001;

    /**
     * View used to display the camera preview and decode barcodes.
     */
    private DecoratedBarcodeView barcodeView;

    /**
     * Flag to ensure that the same scan result is not processed multiple times.
     */
    private boolean handledResult = false;

    /**
     * Tracks whether the flashlight (torch) is currently enabled.
     */
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

        // Navigate back to the previous screen
        btnBack.setOnClickListener(v -> finish());

        // Toggle the flashlight (torch) on the barcode scanner
        btnFlash.setOnClickListener(v -> {
            if (torchOn) {
                barcodeView.setTorchOff();
                torchOn = false;
            } else {
                barcodeView.setTorchOn();
                torchOn = true;
            }
        });

        // Navigate to the main events screen
        btnHome.setOnClickListener(v -> {
            Intent i = new Intent(QrScanActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        // Navigate to the notification screen
        btnNotification.setOnClickListener(v -> {
            Intent i = new Intent(QrScanActivity.this, NotificationScreen.class);
            startActivity(i);
        });

        // Navigate to the profile screen
        btnProfile.setOnClickListener(v -> {
            Intent i = new Intent(QrScanActivity.this, ProfileScreen.class);
            startActivity(i);
        });

        // Camera permission check: if not granted, request it; otherwise start scanning immediately
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

    /**
     * Starts the continuous QR scanning process.
     * <p>
     * This method resets the {@code handledResult} flag and begins decoding
     * barcodes continuously using the {@link #callback}.
     */
    private void startScanning() {
        handledResult = false;
        barcodeView.decodeContinuous(callback);
        barcodeView.resume();
    }

    /**
     * Callback invoked each time the scanner detects a barcode.
     * <p>
     * This implementation:
     * <ol>
     *     <li>Ensures only the first result is processed (via {@code handledResult}).</li>
     *     <li>Parses the scanned text as a URI, expecting the scheme {@code stringevents}
     *     and host {@code event}.</li>
     *     <li>Extracts the last path segment as an event ID, or falls back to raw contents.</li>
     *     <li>Launches {@link EventDetailActivity} with the extracted {@code event_id}.</li>
     * </ol>
     */
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            // Guard clause to ensure we only handle a single result
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
                // Expected format: "stringevents://event/<id>"
                // scheme = "stringevents", host = "event", lastPathSegment = <id>
                if ("stringevents".equals(uri.getScheme())
                        && "event".equals(uri.getHost())) {

                    String last = uri.getLastPathSegment();
                    if (last != null && !last.isEmpty()) {
                        eventId = last;
                    }
                }
            } catch (Exception ignored) {
                // If parsing fails, we fall back to treating the contents as a plain eventId
            }

            // Fallback: if not a custom URI, treat the entire contents as the event ID
            if (eventId == null || eventId.isEmpty()) {
                eventId = contents;
            }

            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(QrScanActivity.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Navigate to the event details screen with the resolved event ID
            Intent i = new Intent(QrScanActivity.this, EventDetailActivity.class);
            i.putExtra("event_id", eventId);
            startActivity(i);
            finish();
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        // Resume camera preview and scanning when the activity becomes visible
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause camera preview when the activity is not in the foreground
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure scanner resources are released when the activity is destroyed
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    /**
     * Handles the result of the camera permission request.
     *
     * @param requestCode  the request code passed when requesting permission
     * @param permissions  the array of requested permissions
     * @param grantResults the grant results corresponding to the requested permissions
     */
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
                // Permission granted → start scanning
                startScanning();
            } else {
                // Permission denied → close the activity because scanning cannot work
                finish();
            }
        }
    }
}
