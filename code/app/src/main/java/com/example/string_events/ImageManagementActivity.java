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

public class ImageManagementActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ArrayList<AdminImageAdapter.EventImage> eventImages;
    private AdminImageAdapter.EventImage selectedImage;
    private AdminImageAdapter adapter;
    private Button btnDelete;

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
