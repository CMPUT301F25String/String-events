package com.example.string_events;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.firebase.geofire.GeoLocation;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
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
    private static final int REQ_LOC_FINE_FOR_MAP_ENABLE = 6000;
    private static final LatLng EDMONTON = new LatLng(53.5461, -113.4938);

    private GoogleMap map;
    private FusedLocationProviderClient fused;
    private FirebaseFirestore db;
    private DocumentReference eventRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_map);

        MapView mapView = findViewById(R.id.map_view);
        if (mapView == null) {
            throw new IllegalStateException("Missing @id/map_view in activity_waitlist_map.xml");
        }
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        String eventId = getIntent().getStringExtra("eventId");
        assert eventId != null;

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events").document(eventId);

        fused = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        boolean anyGranted =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (anyGranted) {
            map.setMyLocationEnabled(true);
        }
        enableMyLocation();
        loadWaitlistAndPlot();
    }

    private void enableMyLocation() {
        // check if permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            fused.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null && map != null) {
                    // Location found, create a LatLng object
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    // Animate the camera to the user's location with a zoom level of 15
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                } else {
                    // Location not found, move to a default location
                    Log.w("WaitlistMapActivity", "Could not get last known location to focus.");
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(EDMONTON, 10f));
                }
            });
        } else {
            // permission hasn't been granted so request it from the user
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOC_FINE_FOR_MAP_ENABLE);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadWaitlistAndPlot() {
        CollectionReference userRef = db.collection("users");
        eventRef.get().addOnSuccessListener(eventSnap -> {
            ArrayList<String> waitlist = (ArrayList<String>) eventSnap.get("waitlist");
            assert waitlist != null;
            for (String username : waitlist) {
                userRef.document(username).get().addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString("name");
                    GeoPoint location = documentSnapshot.getGeoPoint("location");
                    assert location != null;
                    LatLng mapMarker = new LatLng(location.getLatitude(), location.getLongitude());
                    map.addMarker(new MarkerOptions()
                            .position(mapMarker)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .title(name));
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC_FINE_FOR_MAP_ENABLE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                enableMyLocation();
            } else {
                // permission denied, inform the user and default to a location
                Toast.makeText(this, "Location permission denied. Showing default location.", Toast.LENGTH_SHORT).show();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(EDMONTON, 10f));
            }
        }
    }
}
