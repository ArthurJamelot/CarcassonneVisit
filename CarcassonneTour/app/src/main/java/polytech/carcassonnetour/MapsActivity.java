/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package polytech.carcassonnetour;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener,
        GoogleMap.OnMarkerClickListener {

    private static final String URL_ALL_HOTSPOTS = "http://cvisit.gauchoux.com/media/com_carcassonne/ajax/getAllPoints.php";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int ZOOM = 17;
    private static final int RADIUS = 50;
    private static final int WAIT = 3000;

    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    private LocationManager locationManager;

    private List<Hotspot> hotspots;

    private ImageView overlay;
    private boolean firstCenter;
    private boolean autoCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_layout);

        overlay = (ImageView) findViewById(R.id.overlay);
        overlay.setVisibility(View.INVISIBLE);
        firstCenter = true;
        autoCenter = false;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.hotspots = getAllHotspots();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(this);
        enableMyLocation();
        showNearbyHotspots();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else {
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void onLocationChanged(Location location2) {
        Location newLocation = mMap.getMyLocation();
        if (newLocation != null && (firstCenter || autoCenter)) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(newLocation.getLatitude(), newLocation
                            .getLongitude()))
                    .zoom(ZOOM).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            checkHotspotProximity(newLocation);
            if (firstCenter) {
                firstCenter = false;
            }
        }
    }

    public void checkHotspotProximity(Location userLocation) {
        for (Hotspot hotspot : hotspots) {
            Location hotspotLocation = new Location(hotspot.getName());
            hotspotLocation.setLatitude(hotspot.getLatitude());
            hotspotLocation.setLongitude(hotspot.getLongitude());
            int dist = (int) userLocation.distanceTo(hotspotLocation);
            Log.d("hééé", hotspot.getName() + " : " + dist + " pour " + hotspot.getRadius());
            if (!hotspot.isAlreadySee() && dist <= hotspot.getRadius()) {
                showHotspotPopup(hotspot.getName(), hotspot.getText());
                hotspot.setAlreadySee(true);
                break;
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void showNearbyHotspots() {
        for (Hotspot hotspot : this.hotspots) {
            this.mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(hotspot.getLatitude(), hotspot.getLongitude()))
                    .title(hotspot.getName())
                    .snippet(hotspot.getText())
            );
        }
    }

    private List<Hotspot> getAllHotspots() {
        HttpURLConnection urlConnection = null;
        String stringResult = null;
        InputStream input;

        try {
            URL url = new URL(URL_ALL_HOTSPOTS);
            urlConnection = (HttpURLConnection) url.openConnection();
            input = urlConnection.getInputStream();
            stringResult = readStream(input);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        List<Hotspot> hotspotList = new ArrayList<>();
        try {
            JSONArray hotspotsJSON = new JSONArray(stringResult);
            for (int i = 1; i < hotspotsJSON.length(); i++) {
                JSONObject hotspotJSON = hotspotsJSON.getJSONObject(i);
                String text = hotspotJSON.getString("textuel");
                String[] parts = text.split(";");
                if (parts.length == 2) {
                    hotspotList.add(new Hotspot(
                            parts[0],
                            parts[1],
                            Tag.valueOf(hotspotJSON.getString("tag")),
                            hotspotJSON.getDouble("latitude"),
                            hotspotJSON.getDouble("longitude"),
                            RADIUS));
                } else {
                    hotspotList.add(new Hotspot(
                            Tag.valueOf(hotspotJSON.getString("tag")).toString(),
                            text,
                            Tag.valueOf(hotspotJSON.getString("tag")),
                            hotspotJSON.getDouble("latitude"),
                            hotspotJSON.getDouble("longitude"),
                            RADIUS));
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hotspotList;
    }

    private String readStream(InputStream input) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = input.read();
            while(i != -1) {
                bo.write(i);
                i = input.read();
            }
            return bo.toString("UTF-8");
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        showHotspotPopup(marker.getTitle(), marker.getSnippet());
        return true;
    }

    public void showHotspotPopup(String name, String text) {
        new AlertDialog.Builder(this)
                .setTitle(name)
                .setMessage(text)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .show();
    }

    public void toggleOnClickListener(View view) {
        ToggleButton toggleButton = (ToggleButton) view;
        if (toggleButton.isChecked()) { // Radar
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setAllGesturesEnabled(false);
            overlay.setVisibility(View.VISIBLE);
            autoCenter = true;
            Toast.makeText(this, R.string.waitALittle, WAIT).show();
        } else { // Carte
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            overlay.setVisibility(View.INVISIBLE);
            autoCenter = false;
        }
    }

}