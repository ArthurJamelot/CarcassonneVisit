package polytech.carcassonnevisit.observer;

import android.location.Location;

public interface LocatorObserver {
    void notifyLocation(Location location);
}
