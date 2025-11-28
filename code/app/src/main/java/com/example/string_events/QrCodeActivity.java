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

    // a key used to receive event id from other parts of the app
    public static final String EXTRA_EVENT_ID = "extra_event_id";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageView imgQr = findViewById(R.id.img_qr);

        btnBack.setOnClickListener(v -> onBackPressed());

        // only use the event id from the intent
        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // load the stored QR image from Firestore + Storage
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
