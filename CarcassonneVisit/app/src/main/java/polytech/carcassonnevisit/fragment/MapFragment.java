package polytech.carcassonnevisit.fragment;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MapFragment extends SupportMapFragment implements OnMapReadyCallback
{
    private GoogleMap map;

    public MapFragment()
    {
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MyMap", "onResume");
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {

        if (map == null)
        {
            Log.d("MyMap", "setUpMapIfNeeded");
            getMapAsync(this);
        }
    }

    @Override
    //Called by getMapAsync to give us the map newly created
    public void onMapReady(GoogleMap googleMap)
    {
        Log.d("MyMap", "onMapReady");
        map = googleMap;
        setUpMap();
    }

    private void setUpMap()
    {
        //Options
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMapToolbarEnabled(false);

        //Markers
        addAllMarkers(map);

        //Camera
        /*map.moveCamera(CameraUpdateFactory.newLatLngZoom(QUENTIN, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);*/
    }

    private void addAllMarkers(GoogleMap map)
    {
        getAllJSONMarkers();
    }

    private void getAllJSONMarkers()
    {
        HttpURLConnection urlConnection = null;
        String stringResult = null;
        InputStream input;
        try
        {
            URL url = new URL("http://cvisit.gauchoux.com/media/com_carcassonne/ajax/getAllPoints.php");
            urlConnection = (HttpURLConnection) url.openConnection();
            input = urlConnection.getInputStream();
            stringResult = readStream(input);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

        finally
        {
            if (urlConnection != null)
                urlConnection.disconnect();

            if(!stringResult.isEmpty())
            {
                
            }
        }


    }

    private String readStream(InputStream input)
    {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = input.read();
            while(i != -1) {
                bo.write(i);
                i = input.read();
            }
            Log.d("Debug", "Valeur retournée par le script: " + bo.toString("UTF-8"));
            return bo.toString("UTF-8");
        } catch (IOException e) {
            return "";
        }
    }
}