package com.example.string_events;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import androidx.appcompat.app.AlertDialog;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * WaitlistMapActivity
 * - Only draw waitlist users (blue markers)
 * - Organizer chooses precise/approx via EXTRA_REQUIRE_PRECISE
 * - A button asks COARSE only and saves to users/{uid}
 */
public class WaitlistMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // ---- Intent extras ----
    public static final String EXTRA_EVENT_ID =
            "com.example.string_events.extra.EVENT_ID";
    public static final String EXTRA_WAITLIST_NAMES =
            "com.example.string_events.extra.WAITLIST_NAMES";
    public static final String EXTRA_REQUIRE_PRECISE =
            "com.example.string_events.extra.REQUIRE_PRECISE";

    // ---- Permission request codes ----
    private static final int REQ_LOC_FINE_FOR_WAITLIST   = 6001;
    private static final int REQ_LOC_COARSE_FOR_WAITLIST = 6002;
    private static final int REQ_LOC_COARSE_FOR_USERS    = 6003;

    // ---- Defaults ----
    private static final LatLng EDMONTON = new LatLng(53.5461, -113.4938);

    // ---- State ----
    private MapView mapView;
    private GoogleMap map;
    private FusedLocationProviderClient fused;
    private FirebaseFirestore db;

    private String currentUsername;

    private String eventId;
    private boolean requirePrecise;

    private LatLng lastCaptured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_map);

        // --- Map & services ---
        mapView = findViewById(R.id.map_view);
        if (mapView == null) {
            throw new IllegalStateException("Missing @id/map_view in activity_waitlist_map.xml");
        }
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        db    = FirebaseFirestore.getInstance();
        fused = LocationServices.getFusedLocationProviderClient(this);

        // --- Intent params ---
        eventId        = getIntent().getStringExtra(EXTRA_EVENT_ID);
        requirePrecise = getIntent().getBooleanExtra(EXTRA_REQUIRE_PRECISE, false);

        // --- UI buttons ---
        ImageButton btnRefresh      = findViewById(R.id.btn_refresh);
        ImageButton btnCapture      = findViewById(R.id.btn_capture_location);
        ImageButton btnSaveApprox   = findViewById(R.id.btn_save_join_location);

        if (btnRefresh == null || btnCapture == null || btnSaveApprox == null) {
            throw new IllegalStateException("Missing refresh/capture/save buttons in XML");
        }

        btnRefresh.setOnClickListener(v -> loadWaitlistAndPlot());

        btnCapture.setOnClickListener(v -> {
            if (requirePrecise) {
                requestFineThenSaveToWaitlist();
            } else {
                requestCoarseThenSaveToWaitlist();
            }
        });

        findViewById(R.id.btn_capture_location).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Save to waitlist")
                    .setItems(new CharSequence[]{"Approximate", "Precise"}, (d, which) -> {
                        boolean precise = (which == 1);
                        if (precise) requestFineThenSaveToWaitlist();
                        else requestCoarseThenSaveToWaitlist();
                    })
                    .show();
        });

        btnSaveApprox.setOnClickListener(v -> requestCoarseThenSaveToUsers());
    }

    // ---- Map lifecycle ----
    @Override public void onStart()  { super.onStart();  mapView.onStart();  }
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause()  { mapView.onPause();  super.onPause();  }
    @Override public void onStop()   { mapView.onStop();   super.onStop();   }
    @Override public void onDestroy(){ mapView.onDestroy();super.onDestroy();}
    @Override public void onLowMemory(){ super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);

        boolean anyGranted =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (anyGranted) {
            try { map.setMyLocationEnabled(true); } catch (SecurityException ignore) {}
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(EDMONTON, 10f));
        loadWaitlistAndPlot();
    }

    private void loadWaitlistAndPlot() {
        if (map == null || eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing eventId.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    map.clear();
                    LatLngBounds.Builder bb = LatLngBounds.builder();
                    int count = 0;

                    for (DocumentSnapshot doc : qs) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        if (lat == null || lng == null) continue;

                        LatLng p = new LatLng(lat, lng);
                        map.addMarker(new MarkerOptions()
                                .position(p)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)) // 蓝色
                                .title("Waitlist user"));
                        bb.include(p);
                        count++;
                    }
                    if (count > 0) {
                        try { map.animateCamera(CameraUpdateFactory.newLatLngBounds(bb.build(), 120)); }
                        catch (IllegalStateException ignored) {}
                    } else {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(EDMONTON, 10f));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load waitlist failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void requestFineThenSaveToWaitlist() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOC_FINE_FOR_WAITLIST);
        } else {
            saveLastLocationToWaitlist();
        }
    }

    private void requestCoarseThenSaveToWaitlist() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOC_COARSE_FOR_WAITLIST);
        } else {
            saveLastLocationToWaitlist();
        }
    }

    private void saveLastLocationToWaitlist() {
        fused.getLastLocation().addOnSuccessListener(this, (Location loc) -> {
            if (loc == null) { Toast.makeText(this, "No last location yet.", Toast.LENGTH_SHORT).show(); return; }

            lastCaptured = new LatLng(loc.getLatitude(), loc.getLongitude());
            String uid = safeUid();

            Map<String, Object> data = new HashMap<>();
            data.put("userId", uid);
            data.put("lat", lastCaptured.latitude);
            data.put("lng", lastCaptured.longitude);
            data.put("updatedAt", System.currentTimeMillis());

            db.collection("events").document(eventId)
                    .collection("waitlist").document(uid)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this,
                                String.format(Locale.US, "Saved to waitlist: %.5f, %.5f",
                                        lastCaptured.latitude, lastCaptured.longitude),
                                Toast.LENGTH_SHORT).show();
                        loadWaitlistAndPlot(); // 立即刷新
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void requestCoarseThenSaveToUsers() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOC_COARSE_FOR_USERS);
        } else {
            saveLastLocationToUsers();
        }
    }

    private void saveLastLocationToUsers() {
        fused.getLastLocation().addOnSuccessListener(this, (Location loc) -> {
            if (loc == null) { Toast.makeText(this, "No last location yet.", Toast.LENGTH_SHORT).show(); return; }

            Map<String, Object> data = new HashMap<>();
            data.put("username", currentUsername);
            data.put("lat", loc.getLatitude());
            data.put("lng", loc.getLongitude());
            data.put("permission", "approx");
            data.put("updatedAt", System.currentTimeMillis());

            db.collection("users").document(currentUsername)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Saved to users (approx)", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }


    // ---- Permissions callback ----
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (requestCode == REQ_LOC_FINE_FOR_WAITLIST) {
            if (granted) saveLastLocationToWaitlist();
            else Toast.makeText(this, "Precise denied", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQ_LOC_COARSE_FOR_WAITLIST) {
            if (granted) saveLastLocationToWaitlist();
            else Toast.makeText(this, "Approx denied", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQ_LOC_COARSE_FOR_USERS) {
            if (granted) saveLastLocationToUsers();
            else Toast.makeText(this, "Approx denied (users)", Toast.LENGTH_SHORT).show();
        }
    }

    // ---- Helpers ----
    private String safeUid() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return (u != null) ? u.getUid() : "anonymous";
    }
}
