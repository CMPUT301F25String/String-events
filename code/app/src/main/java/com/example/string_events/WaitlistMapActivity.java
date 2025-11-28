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
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * WaitlistMapActivity
 *
 * This screen satisfies US 02.02.02:
 * 1) Show a Google Map.
 * 2) Plot fake entrants (lat/lng) as markers (first validate UI).
 * 3) Request/enable location and show user's last known location (marker + blue dot).
 * 4) Provide a refresh button to repeat the flow quickly.
 */
public class WaitlistMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_EVENT_ID =
            "com.example.string_events.extra.EVENT_ID";     // (Future use) Filter Firebase by eventId

    public static final String EXTRA_WAITLIST_NAMES =
            "com.example.string_events.extra.WAITLIST_NAMES";

    private static final int REQ_LOCATION = 5010;           // Request code for permission

    private ArrayList<String> waitlistNames;

    private MapView mapView;                                 // MapView from XML (id: map_view)
    private GoogleMap map;                                   // GoogleMap instance from getMapAsync
    private FusedLocationProviderClient fused;
    private FirebaseFirestore db;
    private String eventId;    // Int// To read last known location

    private String pendingJoinEventId = null;
    // A non-empty default so the screen never looks blank even if location fails.
    private static final LatLng EDMONTON = new LatLng(53.5461, -113.4938);
    private LatLng lastFix = null;
    private LatLng lastCaptured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_map);      // Inflate XML with MapView + refresh

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        ImageButton capture = findViewById(R.id.btn_capture_location);
        ImageButton save    = findViewById(R.id.btn_save_join_location);
        ImageButton refresh = findViewById(R.id.btn_refresh);

        final boolean[] userTriggeredCapture = { false };

        capture.setOnClickListener(v -> {
            enableMyLocationBlueDot();
            tryShowCurrentLocationMarker();
        });

        save.setOnClickListener(v -> {
            if (lastCaptured == null) {
                Toast.makeText(this, "No location yet. Tap the target icon first.", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = (user != null) ? user.getUid() : "anonymous";

            Map<String, Object> data = new HashMap<>();
            data.put("lat", lastCaptured.latitude);
            data.put("lng", lastCaptured.longitude);
            data.put("capturedAt", System.currentTimeMillis());
            data.put("source", "device_last_known");

            // 路径：events/{eventId}/waitlist/{uid}
            db.collection("events")
                    .document(eventId == null ? "unknown_event" : eventId)
                    .collection("waitlist")
                    .document(uid)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        String s = String.format(Locale.US, "Saved: %.6f, %.6f",
                                lastCaptured.latitude, lastCaptured.longitude);
                        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });


        refresh.setOnClickListener(v -> {
            if (map == null) return;
            map.clear();
            plotFakeEntrantsAndZoom();
            enableMyLocationBlueDot();
            tryShowCurrentLocationMarker();
        });


        mapView = findViewById(R.id.map_view);               // 1) Grab MapView
        mapView.onCreate(savedInstanceState);                //    Forward lifecycle state
        mapView.getMapAsync(this);                           //    Request async map init

        fused = LocationServices.getFusedLocationProviderClient(this); // 2) Fused location client

        waitlistNames = getIntent().getStringArrayListExtra(EXTRA_WAITLIST_NAMES);

        refresh.setOnClickListener(v -> {
            if (map == null) return;
            map.clear();                                     // Clear any existing markers
            plotFakeEntrantsAndZoom();                       // Re-draw fake test markers
            enableMyLocationBlueDot();                       // Enable blue dot if permission granted
            tryShowCurrentLocationMarker();                  // Drop "Current Location" marker once
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {   // MapView finished initializing
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);    // Basic UI to help testing
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
        }

        // Always show something visible first to avoid a blank look.
        map.addMarker(new MarkerOptions().position(EDMONTON).title("Edmonton (test)"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(EDMONTON, 9f));

        plotFakeEntrantsAndZoom();                           // Place fake entrant markers
        enableMyLocationBlueDot();                           // Ask/enable blue dot
        tryShowCurrentLocationMarker();                      // Place a marker at last known location
    }

    private void onJoinClicked(String eventId) {
        joinWaitlistWithLocation(eventId);
    }

    private void joinWaitlistWithLocation(String eventId) {
        boolean fineGranted   = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            pendingJoinEventId = eventId;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    REQ_LOCATION
            );
            return;
        }

        fused.getLastLocation().addOnSuccessListener(this, loc -> {
            Double lat = null, lng = null;
            if (loc != null) {
                lat = loc.getLatitude();
                lng = loc.getLongitude();
            }

            String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map<String, Object> data = new java.util.HashMap<>();
            data.put("userId", uid);
            data.put("joinedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
            if (lat != null && lng != null) {
                data.put("lat", lat);
                data.put("lng", lng);
                data.put("locationSource", "lastKnown");
            } else {
                data.put("locationSource", "unknown");
            }

            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            db.collection("events").document(eventId)
                    .collection("waitlist").document(uid)
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Joined waitlist with location saved.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Join saved but location write failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted && pendingJoinEventId != null) {
                String eid = pendingJoinEventId;
                pendingJoinEventId = null;
                joinWaitlistWithLocation(eid);
            } else {
                String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (pendingJoinEventId != null) {
                    String eid = pendingJoinEventId;
                    pendingJoinEventId = null;

                    Map<String, Object> data = new java.util.HashMap<>();
                    data.put("userId", uid);
                    data.put("joinedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                    data.put("locationSource", "denied");

                    com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                    db.collection("events").document(eid)
                            .collection("waitlist").document(uid)
                            .set(data, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Joined waitlist (no location).", Toast.LENGTH_SHORT).show()
                            );
                }
            }
        }
    }

    /**
     * Plot a list of fake entrants and fit the camera to show them.
     * This is the "use fake lat/lng first" validation stage.
     */
    private void plotFakeEntrantsAndZoom() {
        if (map == null) return;

        // ---- Fake entrant coordinates (replace with Firebase later) ----
        List<LatLng> entrants = new ArrayList<>();
        entrants.add(new LatLng(53.6316, -113.3230)); // Sherwood Park
        entrants.add(new LatLng(53.5444, -113.4909)); // Downtown Edmonton
        entrants.add(new LatLng(53.5700, -113.5820)); // West Edmonton
        entrants.add(new LatLng(53.4690, -113.5087)); // Southgate
        entrants.add(new LatLng(53.5461, -113.4938)); // Central Edmonton (duplicate on purpose)
        // -----------------------------------------------------------------

        LatLngBounds.Builder bb = LatLngBounds.builder();     // Collect bounds for all points
        for (int i = 0; i < entrants.size(); i++) {
            LatLng p = entrants.get(i);

            String label = (waitlistNames != null && i < waitlistNames.size()
                    && waitlistNames.get(i) != null && !waitlistNames.get(i).isEmpty())
                    ? waitlistNames.get(i)
                    : "Entrant #" + (i + 1);

            map.addMarker(new MarkerOptions()
                    .position(p)
                    .title(label)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            bb.include(p);
        }

        // Smoothly move/zoom the camera to include all markers with some padding.
        try {
            LatLngBounds bounds = bb.build();
            int padding = 120;                                // px padding on each side
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            map.animateCamera(cu);
        } catch (IllegalStateException ignore) {
            // If < 2 points were included, bounds may not be valid -> fall back to a safe zoom.
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(EDMONTON, 10f));
        }
    }

    /**
     * Request ACCESS_FINE_LOCATION if needed; enable the blue "My Location" dot when granted.
     */
    private void enableMyLocationBlueDot() {
        if (map == null) return;

        if (!hasFineLocationPermission()) {                   // No permission yet -> ask the user
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOCATION
            );
            return;
        }
        // Permission already granted -> enable the blue dot.
        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException ignored) {}
    }

    private void plotEntrantsFromFirestore(String eventId) {
        if (map == null || eventId == null || eventId.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 假设 Firestore 结构：events/{eventId}/waitlist/{userId}，字段：name, lat, lng
        db.collection("events").document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    LatLngBounds.Builder bb = LatLngBounds.builder();
                    map.clear();

                    for (DocumentSnapshot doc : qs) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        String name = doc.getString("name");

                        if (lat == null || lng == null) continue;
                        LatLng p = new LatLng(lat, lng);
                        map.addMarker(new MarkerOptions()
                                .position(p)
                                .title(name != null ? name : "Entrant"));
                        bb.include(p);
                    }

                    try {
                        LatLngBounds b = bb.build();
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(b, 120));
                    } catch (IllegalStateException ignore) {}
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load waitlist failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    /**
     * Read one last-known location and drop a red marker titled "Current Location".
     * We re-check permission right here to satisfy the lint (call requires permission).
     */
    private void tryShowCurrentLocationMarker() {
        if (map == null) return;

        boolean fineGranted   = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {                 // Neither granted -> ask again
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    REQ_LOCATION
            );
            return;
        }

        fused.getLastLocation().addOnSuccessListener(this, (Location loc) -> {
            if (loc == null) {
                Toast.makeText(this,
                        "No last location (Emulator → ⋮ → Location → Send a coordinate).",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            LatLng me = new LatLng(loc.getLatitude(), loc.getLongitude());

            lastCaptured = me;
            String capturedStr = String.format(Locale.US, "%.6f,%.6f", me.latitude, me.longitude);
            Toast.makeText(this, "Captured: " + capturedStr, Toast.LENGTH_SHORT).show();
            // ↑↑↑

            map.addMarker(new MarkerOptions()
                    .position(me)
                    .title("Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(me, 13f));
        });

    }

    /** Convenience wrapper for checking FINE_LOCATION permission. */
    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // MapView lifecycle — must be forwarded or the map may be blank / leak.
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
}
