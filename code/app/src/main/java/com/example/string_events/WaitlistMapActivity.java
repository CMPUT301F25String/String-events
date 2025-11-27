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

import java.util.ArrayList;
import java.util.List;

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

    private static final int REQ_LOCATION = 2001;           // Request code for permission

    private ArrayList<String> waitlistNames;

    private MapView mapView;                                 // MapView from XML (id: map_view)
    private GoogleMap map;                                   // GoogleMap instance from getMapAsync
    private FusedLocationProviderClient fused;               // To read last known location

    // A non-empty default so the screen never looks blank even if location fails.
    private static final LatLng EDMONTON = new LatLng(53.5461, -113.4938);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_map);      // Inflate XML with MapView + refresh

        mapView = findViewById(R.id.map_view);               // 1) Grab MapView
        mapView.onCreate(savedInstanceState);                //    Forward lifecycle state
        mapView.getMapAsync(this);                           //    Request async map init

        fused = LocationServices.getFusedLocationProviderClient(this); // 2) Fused location client

        waitlistNames = getIntent().getStringArrayListExtra(EXTRA_WAITLIST_NAMES);

        ImageButton refresh = findViewById(R.id.btn_refresh);          // 3) Refresh button
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

        // Always show something visible first to avoid a blank look.
        map.addMarker(new MarkerOptions().position(EDMONTON).title("Edmonton (test)"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(EDMONTON, 9f));

        plotFakeEntrantsAndZoom();                           // Place fake entrant markers

        enableMyLocationBlueDot();                           // Ask/enable blue dot
        tryShowCurrentLocationMarker();                      // Place a marker at last known location
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

        // Safe to call fused.getLastLocation()
        fused.getLastLocation().addOnSuccessListener(this, (Location loc) -> {
            if (loc == null) {
                Toast.makeText(this,
                        "No last location (Emulator → ⋮ → Location → Send a coordinate).",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            LatLng me = new LatLng(loc.getLatitude(), loc.getLongitude());
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

    // When permission dialog returns, enable the blue dot and try dropping current-location marker.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocationBlueDot();
                tryShowCurrentLocationMarker();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
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
