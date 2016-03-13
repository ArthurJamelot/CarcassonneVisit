package polytech.carcassonnevisit.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import polytech.carcassonnevisit.observer.LocatorObserver;

public class LocatorService extends Service implements LocationListener, SensorEventListener {

    private final String PROVIDER = LocationManager.GPS_PROVIDER;
    private final int MIN_DISTANCE = 1;
    private final int REFRESH_DELAY = 50;

    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity;
    private float[] geomagnetic;

    private List<LocatorObserver> observers;

    @Override
    public void onCreate() {
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (canUseLocationManager()) {
            this.locationManager.requestLocationUpdates(PROVIDER, 0, 0, this);

            // Initialize azimuth observer
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d("coucou", "ta soeur");
        this.observers = (ArrayList<LocatorObserver>) intent.getSerializableExtra("observers");
        Log.d("coucou", "ta soeur2");
        if (canUseLocationManager()) {
            Log.d("coucou", "" + this.observers.size());
            for (LocatorObserver observer : this.observers) {
                Log.d("coucou", "ta soeur4");
                observer.notifyLocation(this.locationManager.getLastKnownLocation(PROVIDER));
            }
            Log.d("coucou", "ta soeur5");
        }
    }

    public boolean canUseLocationManager() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * When the location of the device changed, update the location of user.
     * @param location The new location of the device
     */
    public void onLocationChanged(Location location) {
        for (LocatorObserver observer : this.observers) {
            observer.notifyLocation(location);
        }
        if (canUseLocationManager()) {
            this.locationManager.requestLocationUpdates(PROVIDER, REFRESH_DELAY, MIN_DISTANCE, this);
        }
    }

    /**
     * When provider is disabled, alert the Model.
     * @param provider The current provider used
     */
    public void onProviderDisabled(String provider) {
        //this.view.alertGPSOrNetworkIsDisabled();
    }

    /**
     * When provider is enabled, request location updates and alert the Model
     * @param provider The current provider used
     */
    public void onProviderEnabled(String provider) {
        //this.locationManager.requestLocationUpdates(PROVIDER, this.view.getRefreshmentRateInSeconds()*1000, MIN_DISTANCE, this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Get data from accelerometer
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;

        // Get data from magnetometer
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values;

        /*
        // Compute and send to the Model the new user's azimuth
        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                this.observer.notifyAzimuth((float) (Math.toDegrees((double) orientation[0])));
            }
        }
        */
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}