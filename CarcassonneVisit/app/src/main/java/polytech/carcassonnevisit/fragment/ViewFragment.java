package polytech.carcassonnevisit.fragment;


import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import polytech.carcassonnevisit.observer.LocatorObserver;

public abstract class ViewFragment extends SupportMapFragment implements
        OnMapReadyCallback, LocatorObserver {

    protected GoogleMap map;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setUpMapSettings();
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

    protected abstract void setUpMapSettings();
    public abstract void notifyLocation(Location location);

}
