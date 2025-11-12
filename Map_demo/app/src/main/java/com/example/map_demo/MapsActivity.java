package com.example.map_demo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.example.map_demo.databinding.ActivityMapsBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private FusedLocationProviderClient mClient;
    private LocationCallback callback;
    private Marker homeMarker;
    private static final int REQUEST_CODE = 1;
    private static final long UPDATE_INTERVAL = 5_000L;
    private static final long FASTEST_INTERVAL = 3_000L;
    private boolean firstLocationUpdate = true;

    private List<Marker> markers = new ArrayList<>();
    private static final int POLYGON_SIDES = 3;
    private Polygon shape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClient = LocationServices.getFusedLocationProviderClient(this);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        if (!isGrantedLocationPermission()) {
            requestLocationPermission();
        } else {
            startLocationUpdates();
        }

        loadSavedMarkers();

        mMap.setOnMapLongClickListener(latLng -> {
            addMarker(latLng);
            saveLocation(latLng);
        });
    }

    private void addMarker(LatLng latLng) {
        if (mMap == null) return;

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Favorite Place")
                .snippet("Lat: " + latLng.latitude + ", Lng: " + latLng.longitude)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        Marker favMarker = mMap.addMarker(options);
        markers.add(favMarker);

        if (shape != null) {
            shape.remove();
            shape = null;
        }

        if (markers.size() == POLYGON_SIDES) {
            drawShape();
        } else if (markers.size() > POLYGON_SIDES) {
            for (Marker marker : markers) {
                marker.remove();
            }
            markers.clear();
            markers.add(favMarker);
        }

        Toast.makeText(this, "Marker added!", Toast.LENGTH_SHORT).show();
    }

    private void drawShape() {
        PolygonOptions options = new PolygonOptions()
                .fillColor(0x33ff2200)
                .strokeColor(0xFF00FF00)
                .strokeWidth(7);

        for (Marker marker : markers) {
            options.add(marker.getPosition());
        }

        shape = mMap.addPolygon(options);
    }

    private boolean isGrantedLocationPermission() {
        return ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CODE
        );
    }

    private void startLocationUpdates() {
        if (!isGrantedLocationPermission()) return;

        LocationRequest request = new LocationRequest.Builder(UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (homeMarker == null) {
                        homeMarker = mMap.addMarker(new MarkerOptions()
                                .position(userLatLng)
                                .title("You are here!"));
                    }

                    if (firstLocationUpdate) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                        firstLocationUpdate = false;
                    }
                }
            }
        };

        try {
            mClient.requestLocationUpdates(request, callback, null);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (isGrantedLocationPermission()) {
                startLocationUpdates();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Location permission is required to use this feature.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
    }

    private void saveLocation(LatLng latLng) {
        SharedPreferences prefs = getSharedPreferences("places_prefs", MODE_PRIVATE);
        String json = prefs.getString("places_list", "[]");

        try {
            JSONArray arr = new JSONArray(json);
            arr.put("Lat: " + latLng.latitude + ", Lng: " + latLng.longitude);
            prefs.edit().putString("places_list", arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadSavedMarkers() {
        SharedPreferences prefs = getSharedPreferences("places_prefs", MODE_PRIVATE);
        String json = prefs.getString("places_list", "[]");

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                String str = arr.getString(i);
                // Parse Lat and Lng
                String[] parts = str.replace("Lat: ", "").replace("Lng: ", "").split(",");
                double lat = Double.parseDouble(parts[0].trim());
                double lng = Double.parseDouble(parts[1].trim());
                addMarker(new LatLng(lat, lng));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


