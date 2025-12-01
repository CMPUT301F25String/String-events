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

/**
 * Activity responsible for displaying a QR code for a specific event.
 *
 * <p>Core responsibilities:</p>
 * <ul>
 *     <li>Receive an {@code eventId} via {@link #EXTRA_EVENT_ID}.</li>
 *     <li>Check Firestore for an existing stored QR code URL for this event.</li>
 *     <li>If found: load the QR code image via Glide and display it.</li>
 *     <li>If not found: generate a QR code bitmap, upload it to Firebase Storage,
 *         save the download URL to Firestore (field {@code qrCodeUrl}), and display it.</li>
 * </ul>
 *
 * <p>Deep Link Format:</p>
 * <ul>
 *     <li>The content encoded into the QR code is of the form:
 *     {@code stringevents://event/<eventId>}.</li>
 *     <li>These links are handled by {@link EventQrEntryActivity}, which forwards
 *     the user into {@link MainActivity} / detail screens.</li>
 * </ul>
 */
public class QrCodeActivity extends AppCompatActivity {

    /**
     * Intent extra key used by other Activities to pass an event id into QrCodeActivity.
     * <p>
     * Example usage:
     * <pre>
     * Intent i = new Intent(context, QrCodeActivity.class);
     * i.putExtra(QrCodeActivity.EXTRA_EVENT_ID, eventId);
     * startActivity(i);
     * </pre>
     */
    public static final String EXTRA_EVENT_ID = "extra_event_id";

    /**
     * Firestore instance used for reading/writing the event document (qrCodeUrl field).
     */
    private FirebaseFirestore db;

    /**
     * Tag used in Logcat.
     */
    private static final String TAG = "QrCodeActivity";

    /**
     * Standard Activity lifecycle: called when the activity is first created.
     *
     * <p>Steps:</p>
     * <ol>
     *     <li>Inflate the layout.</li>
     *     <li>Get views (back button, ImageView for QR).</li>
     *     <li>Extract {@code eventId} from the launching Intent.</li>
     *     <li>If eventId is valid, try to load stored QR from Firestore/Storage,
     *         otherwise generate and upload it.</li>
     * </ol>
     *
     * @param savedInstanceState saved state, if any
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageView imgQr = findViewById(R.id.img_qr);

        // Simple back navigation
        btnBack.setOnClickListener(v -> onBackPressed());

        // Only use the event id from the intent
        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load the stored QR image from Firestore; if missing, generate + upload
        loadStoredQrIfAvailable(eventId, imgQr);
    }

    /**
     * Attempts to load an existing QR code URL for the given event from Firestore.
     *
     * <p>Behavior:</p>
     * <ul>
     *     <li>If the event exists and {@code qrCodeUrl} is present:
     *         <ul>
     *             <li>Load the image via Glide and display it in {@code imgQr}.</li>
     *         </ul>
     *     </li>
     *     <li>If the event exists but {@code qrCodeUrl} is empty/missing:
     *         <ul>
     *             <li>Call {@link #generateAndUploadQrForEvent(String, ImageView)} to backfill.</li>
     *         </ul>
     *     </li>
     *     <li>If event does not exist:
     *         <ul>
     *             <li>Show a Toast and do nothing else.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param eventId id of the event whose QR is needed
     * @param imgQr   ImageView in which we should display the QR image
     */
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
                    Log.e(TAG, "Failed to load qrCodeUrl from Firestore", e);
                    Toast.makeText(this, "Failed to load QR code", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Generates a QR code bitmap for the given event, uploads it to Firebase Storage,
     * stores the URL in Firestore, and displays it in the UI.
     *
     * <p>Steps:</p>
     * <ol>
     *     <li>Build the deep link string {@code stringevents://event/<eventId>}.</li>
     *     <li>Use {@link QRUtils#generateQrCode(String, int, int)} to generate a Bitmap.</li>
     *     <li>Compress Bitmap into PNG bytes using {@link Bitmap#compress(Bitmap.CompressFormat, int, ByteArrayOutputStream)}.</li>
     *     <li>Upload byte array to Storage at {@code qr_code/<eventId>.png}.</li>
     *     <li>Get download URL and update Firestore {@code events/{eventId}.qrCodeUrl}.</li>
     *     <li>Display the image via Glide in {@code imgQr}.</li>
     * </ol>
     *
     * @param eventId id of the event we are generating a QR for
     * @param imgQr   ImageView that will eventually display the generated QR code
     */
    private void generateAndUploadQrForEvent(String eventId, ImageView imgQr) {
        // Deep link content that will be encoded into the QR
        String qrContent = "stringevents://event/" + eventId;

        try {
            // Generate QR bitmap locally using QRUtils
            Bitmap bitmap = QRUtils.generateQrCode(qrContent, 800, 800);

            if (bitmap == null) {
                // Defensive check: if generation failed, avoid NPE
                Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
                return;
            }

            // Compress bitmap into PNG bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            // Upload to Firebase Storage under qr_code/{eventId}.png
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference rootRef = storage.getReference();
            StorageReference qrRef = rootRef.child("qr_code/" + eventId + ".png");

            UploadTask uploadTask = qrRef.putBytes(data);
            uploadTask
                    .addOnSuccessListener(taskSnapshot ->
                            qrRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String qrUrl = uri.toString();
                                Log.d(TAG, "Generated QR uploaded: " + qrUrl);

                                // Save qrCodeUrl back into Firestore so future opens re-use it
                                db.collection("events")
                                        .document(eventId)
                                        .update("qrCodeUrl", qrUrl)
                                        .addOnSuccessListener(unused ->
                                                Log.d(TAG, "qrCodeUrl updated for existing event"))
                                        .addOnFailureListener(e ->
                                                Log.e(TAG, "Failed to update qrCodeUrl", e));

                                // Display the uploaded QR image
                                Glide.with(QrCodeActivity.this)
                                        .load(qrUrl)
                                        .into(imgQr);
                            }))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to upload generated QR", e);
                        Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error generating QR code", e);
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }
}
