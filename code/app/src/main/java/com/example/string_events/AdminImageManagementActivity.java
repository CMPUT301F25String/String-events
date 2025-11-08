package com.example.string_events;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;

/**
 * Administration screen for listing event images and deleting a selected one.
 * <p>
 * Images are loaded from Firestore {@code events} documents (field {@code imageUrl});
 * deletion removes the underlying file from Firebase Storage and clears the
 * {@code imageUrl} field in Firestore.
 * </p>
 *
 * @since 1.0
 */
public class AdminImageManagementActivity extends AppCompatActivity {
    /** Firestore database handle. */
    private FirebaseFirestore db;
    /** Firebase Storage handle. */
    private FirebaseStorage storage;
    /** Backing list of images shown in the RecyclerView. */
    private ArrayList<AdminImageAdapter.EventImage> eventImages;
    /** Currently selected image (nullable). */
    private AdminImageAdapter.EventImage selectedImage;
    /** Adapter that renders and handles selection. */
    private AdminImageAdapter adapter;
    /** Delete button UI control. */
    private Button btnDelete;

    /**
     * Initializes UI, wires adapter and actions, and loads images from Firestore.
     *
     * @param savedInstanceState state bundle; may be {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_image_management_screen);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        eventImages = new ArrayList<>();

        RecyclerView recycler = findViewById(R.id.recycler_images);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setEnabled(false);

        adapter = new AdminImageAdapter(this, eventImages, image -> {
            selectedImage = image;
            btnDelete.setEnabled(true);
        });
        recycler.setAdapter(adapter);

        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());

        loadImages();
        setupDeleteButton();
    }

    /**
     * Fetches all events from Firestore and populates the list with those having a non-empty {@code imageUrl}.
     * Updates the adapter once data is collected.
     */
    private void loadImages() {
        db.collection("events").get()
                .addOnSuccessListener(snapshot -> {
                    eventImages.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        String imageUrl = doc.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            eventImages.add(new AdminImageAdapter.EventImage(id, title, imageUrl));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show());
    }

    /**
     * Wires the delete button to remove the selected image from Storage and clear its reference in Firestore.
     * <ul>
     *   <li>Deletes the file at {@code selectedImage.imageUrl} via {@link FirebaseStorage}.</li>
     *   <li>Sets the corresponding event document's {@code imageUrl} field to {@code null}.</li>
     *   <li>Refreshes the list on success.</li>
     * </ul>
     */
    private void setupDeleteButton() {
        btnDelete.setOnClickListener(v -> {
            if (selectedImage == null) {
                Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show();
                return;
            }

            StorageReference ref = storage.getReferenceFromUrl(selectedImage.imageUrl);

            // Delete image from Firebase Storage
            ref.delete().addOnSuccessListener(unused -> {
                // Remove imageUrl from Firestore
                db.collection("events").document(selectedImage.id)
                        .update("imageUrl", null)
                        .addOnSuccessListener(unused2 -> {
                            Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                            selectedImage = null;
                            btnDelete.setEnabled(false);
                            loadImages();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to update Firestore", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to delete image from Storage", Toast.LENGTH_SHORT).show());
        });
    }
}
