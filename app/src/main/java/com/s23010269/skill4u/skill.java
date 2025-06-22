package com.s23010269.skill4u;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class skill extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private ToggleButton locationToggle;
    private TextView fingertext;
    private MapView mapView;
    private Button getstarted;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private DatabaseHelper dbHelper;
    private ImageView openMenu;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill);

        openMenu = findViewById(R.id.openmenu);
        locationToggle = findViewById(R.id.locationToggle);
        fingertext = findViewById(R.id.fingertext);
        mapView = findViewById(R.id.mapView);
        getstarted = findViewById(R.id.getstarted);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get current username passed from previous activity
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null) currentUsername = ""; // fallback

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initially disable the "Find Users Nearby" button if location not enabled
        getstarted.setEnabled(false);

        locationToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // User wants to enable location
                if (checkLocationPermission()) {
                    enableLocation();
                } else {
                    requestLocationPermission();
                    // We will handle enabling after permission granted
                }
            } else {
                // User wants to disable location
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

    // Enable location: get location and save to DB, update UI
    private void enableLocation() {
        fingertext.setText("Location Enabled");
        getstarted.setEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                saveUserLocationToDb(location);
                moveMapCamera(location);
                loadNearbyUsersOnMap();
            } else {
                Toast.makeText(skill.this, "Unable to get location. Make sure GPS is enabled.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(skill.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Disable location: remove from DB and update UI
    private void disableLocation() {
        fingertext.setText("Enable Location");
        getstarted.setEnabled(false);

        dbHelper.disableLocation(currentUsername);

        if (googleMap != null) {
            googleMap.clear();
        }
    }

    // Save user's current location in database
    private void saveUserLocationToDb(Location location) {
        dbHelper.saveUserLocation(currentUsername, location.getLatitude(), location.getLongitude());
    }

    // Move map camera to user's location
    private void moveMapCamera(Location location) {
        if (googleMap != null) {
            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f));
            googleMap.clear();
            // Add marker for current user
            googleMap.addMarker(new MarkerOptions()
                    .position(userLatLng)
                    .title("You")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

    // Load all users with location from DB and put markers on map except current user
    private void loadNearbyUsersOnMap() {
        if (googleMap == null) return;

        googleMap.clear();

        // First show current user marker (if location is known)
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.addMarker(new MarkerOptions()
                        .position(userLatLng)
                        .title("You")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f));
            }
        });

        ArrayList<UserLocation> userLocations = dbHelper.getAllUsersWithLocation();

        for (UserLocation ul : userLocations) {
            // Don't show current user's marker twice
            if (!ul.getUsername().equals(currentUsername)) {
                LatLng latLng = new LatLng(ul.getLatitude(), ul.getLongitude());
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(ul.getUsername()));
            }
        }
    }

    // Check if location permission is granted
    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Request location permission
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable location
                if (locationToggle.isChecked()) {
                    enableLocation();
                }
            } else {
                Toast.makeText(this, "Location permission is required to use this feature", Toast.LENGTH_SHORT).show();
                locationToggle.setChecked(false);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        // Optionally enable My Location button if permission granted
        if (checkLocationPermission()) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    // Lifecycle methods for mapView
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
