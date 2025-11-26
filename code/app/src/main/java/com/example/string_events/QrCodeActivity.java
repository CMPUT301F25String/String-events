package com.example.string_events;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class QrCodeActivity extends AppCompatActivity {

    // Key used to receive event id from other parts of the app
    public static final String EXTRA_EVENT_ID = "extra_event_id";

    // Temporary hardcoded id for event3
    private static final String EVENT3_ID = "07d4dd53-3efe-4613-b852-0720a924be8b";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageView imgQr = findViewById(R.id.img_qr);

        btnBack.setOnClickListener(v -> onBackPressed());

        // Try to get event id from intent, otherwise fall back to event3
        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            eventId = EVENT3_ID;
        }

        // Load the stored QR image from Firestore + Storage
        loadStoredQrIfAvailable(eventId, imgQr);
    }

    private void loadStoredQrIfAvailable(String eventId, ImageView imgQr) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String qrUrl = snapshot.getString("qrCodeUrl");
                        if (qrUrl != null && !qrUrl.isEmpty()) {
                            // Normal path: we already have a stored QR code URL
                            Glide.with(QrCodeActivity.this)
                                    .load(qrUrl)
                                    .into(imgQr);
                        } else {
                            // Backfill for old events: generate, upload, save, then display
                            generateAndUploadQrForEvent(eventId, imgQr);
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("QrCodeActivity", "Failed to load qrCodeUrl from Firestore", e);
                    Toast.makeText(this, "Failed to load QR code", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Generates a QR code for an existing event, uploads it to Firebase Storage under
     * "qr_code/{eventId}.png", saves the download URL into the event document as "qrCodeUrl",
     * and then displays the image using Glide.
     */
    private void generateAndUploadQrForEvent(String eventId, ImageView imgQr) {
        String qrContent = "stringevents://event/" + eventId;

        try {
            // generate qr bitmap locally using qr utils
            Bitmap bitmap = QRUtils.generateQrCode(qrContent, 800, 800);

            // compress bitmap into PNG bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            // upload to Firebase Storage under qr_code/{eventId}.png
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference rootRef = storage.getReference();
            StorageReference qrRef = rootRef.child("qr_code/" + eventId + ".png");

            UploadTask uploadTask = qrRef.putBytes(data);
            uploadTask
                    .addOnSuccessListener(taskSnapshot ->
                            qrRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String qrUrl = uri.toString();
                                Log.d("QrCodeActivity", "Generated QR uploaded: " + qrUrl);

                                // save qrCodeUrl back into Firestore
                                db.collection("events")
                                        .document(eventId)
                                        .update("qrCodeUrl", qrUrl)
                                        .addOnSuccessListener(unused ->
                                                Log.d("QrCodeActivity", "qrCodeUrl updated for existing event"))
                                        .addOnFailureListener(e ->
                                                Log.e("QrCodeActivity", "Failed to update qrCodeUrl", e));

                                // display the uploaded QR image
                                Glide.with(QrCodeActivity.this)
                                        .load(qrUrl)
                                        .into(imgQr);
                            }))
                    .addOnFailureListener(e -> {
                        Log.e("QrCodeActivity", "Failed to upload generated QR", e);
                        Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
                    });

        }
        catch (Exception e) {
            Log.e("QrCodeActivity", "Error generating QR code", e);
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }
}
