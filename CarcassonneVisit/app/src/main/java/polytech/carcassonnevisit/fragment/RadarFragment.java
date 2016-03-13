package polytech.carcassonnevisit.fragment;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

import polytech.carcassonnevisit.observer.LocatorObserver;

public class RadarFragment extends SupportMapFragment implements
        OnMapReadyCallback, LocatorObserver, Serializable {

    private final int ZOOM_FACTOR = 20;

    private GoogleMap map;

    public RadarFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (map == null) {
            getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        MapsInitializer.initialize(this.getContext());
        setUpMapSettings();
    }

    private void setUpMapSettings() {
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(false);
    }

    @Override
    public void notifyLocation(Location location) {
        Log.d("hééé", "Coucou");
        while (getMap() == null) {
            Log.d("hééé", "Attente");
        }

            Log.d("hééé", "Vrai coucou");
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(ZOOM_FACTOR);
            map.moveCamera(center);
            map.animateCamera(zoom, 2000, null);
            Log.d("hééé", "Fin coucou");

    }

}