package com.s23010269.skill4u;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.*;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;

public class skill extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private ToggleButton locationToggle;
    private TextView fingertext;
    private MapView mapView;
    private Button getstarted;
    private ImageView openMenu;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private String currentUsername;
    private DatabaseReference locationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill);

        // UI
        locationToggle = findViewById(R.id.locationToggle);
        fingertext     = findViewById(R.id.fingertext);
        mapView        = findViewById(R.id.mapView);
        getstarted     = findViewById(R.id.getstarted);
        openMenu       = findViewById(R.id.openmenu);

        // Firebase
        locationsRef = FirebaseDatabase.getInstance().getReference("Locations");

        // Username
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null) currentUsername = "guest";

        // Location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Map init
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        getstarted.setEnabled(false);

        locationToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (checkLocationPermission()) {
                    enableLocation();
                } else {
                    requestLocationPermission();
                }
            } else {
                disableLocation();
            }
        });

        getstarted.setOnClickListener(v -> {
            if (locationToggle.isChecked()) {
                loadNearbyUsersOnMap();
            } else {
                Toast.makeText(this, "Please enable location first", Toast.LENGTH_SHORT).show();
            }
        });

        openMenu.setOnClickListener(view -> {
            Intent intent = new Intent(skill.this, menu.class);
            startActivity(intent);
        });
    }

    private void enableLocation() {
        fingertext.setText("Location Enabled");
        getstarted.setEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                saveUserLocationToFirebase(location);
                moveMapCamera(location);
                loadNearbyUsersOnMap();
            } else {
                Toast.makeText(this, "Unable to get location. Ensure GPS is on.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Location failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void disableLocation() {
        fingertext.setText("Enable Location");
        getstarted.setEnabled(false);

        locationsRef.child(currentUsername).removeValue();

        if (googleMap != null) googleMap.clear();
    }

    private void saveUserLocationToFirebase(Location location) {
        UserLocation userLocation = new UserLocation(currentUsername, location.getLatitude(), location.getLongitude());
        locationsRef.child(currentUsername).setValue(userLocation);
    }

    private void moveMapCamera(Location location) {
        if (googleMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("You")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

    private void loadNearbyUsersOnMap() {
        if (googleMap == null) return;

        googleMap.clear();

        fusedLocationClient.getLastLocation().addOnSuccessListener(currentLoc -> {
            if (currentLoc != null) {
                LatLng myLatLng = new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude());
                googleMap.addMarker(new MarkerOptions()
                        .position(myLatLng)
                        .title("You")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 14f));
            }

            locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        UserLocation ul = child.getValue(UserLocation.class);
                        if (ul != null && !ul.getUsername().equals(currentUsername)) {
                            LatLng latLng = new LatLng(ul.getLatitude(), ul.getLongitude());
                            googleMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(ul.getUsername()));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(skill.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] permissions, @NonNull int[] results) {
        if (code == LOCATION_PERMISSION_REQUEST_CODE) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                if (locationToggle.isChecked()) enableLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                locationToggle.setChecked(false);
            }
        }
        super.onRequestPermissionsResult(code, permissions, results);
    }

    @Override public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        if (checkLocationPermission()) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    // Map lifecycle
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
