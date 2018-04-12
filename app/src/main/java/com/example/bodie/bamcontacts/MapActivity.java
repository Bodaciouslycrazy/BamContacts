package com.example.bodie.bamcontacts;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GeoLocationHandler, LocationListener {

    private GoogleMap mMap;

    protected TextView Lat;
    protected TextView Lng;
    protected TextView Dist;

    //protected LatLng UserPos;
    protected Marker UserMarker;
    protected Marker ContactMarker;

    AsyncGeocode Geocoder;

    protected LocationManager LocManager;

    //PERMISSIONS STUFF
    public static final int PermissionRequestCode = 10;
    public static final String[] LocPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        LocManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        //REQUEST LOCATION UPDATES

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Lat = findViewById(R.id.lblLat);
        Lng = findViewById(R.id.lblLng);
        Dist = findViewById(R.id.lblDist);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onPause()
    {
        LocManager.removeUpdates(this);
        super.onPause();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Geocoder = new AsyncGeocode();
        int keyId = getResources().getIdentifier("google_maps_key", "string", getPackageName());
        Geocoder.apiKey = getString(keyId);
        Geocoder.handler = this;
        Geocoder.execute( (MapAddress)getIntent().getSerializableExtra("address") );

        startLocationUpdates();

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }



    /**
     * callback function from AsyncGeocode.
     * places a marker and zooms in on the location of the address.
     * @param location
     */
    @Override
    public void onContactFound(LatLng location)
    {
        if(location != null)
        {

            String lat = location.latitude + "";
            String lon = location.longitude + "";

            Lat.setText(lat);
            Lng.setText(lon);

            MarkerOptions mo = new MarkerOptions().position(location);
            mo.title(getIntent().getStringExtra("name"));

            ContactMarker = mMap.addMarker(mo);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));

            //Call method to possibly calculate distance.
        }
        else
        {
            //There was an error finding the location.
            Toast.makeText(this, "There was an error mapping that address.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Checks for permissions, and then registers a listener.
     */
    public void startLocationUpdates()
    {
        for(int i = 0; i < LocPermissions.length; i++)
        {
            if( PermissionChecker.checkSelfPermission(this, LocPermissions[i]) != PackageManager.PERMISSION_GRANTED)
            {
                //We don't have a permission, so ask the user for permissions.
                Log.w("berror", "Don't have permissions. Ask user.");

                askForLocationPermissions();
                return;
            }
        }

        Log.d("bebug", "Got permissions! LETS-A-GOOOOO");

        Criteria crit = new Criteria();
        //crit.setPowerRequirement(Criteria.POWER_MEDIUM);
        //crit.setAccuracy(Criteria.ACCURACY_MEDIUM);
        //crit.setAltitudeRequired(false);
        //crit.setBearingRequired(false);
        //crit.setSpeedRequired(false);
        String prov = LocManager.getBestProvider(crit ,true);

        if(prov == null)
            Log.w("location", "No providor was found! Scream!");
        else
            Log.w("location", "Using provider: "+ prov);

        LocManager.requestLocationUpdates(prov, 0, 0, this);
    }


    /**
     * Opens a dialogue that asks for location permissions.
     */
    public void askForLocationPermissions()
    {
        ActivityCompat.requestPermissions(this, LocPermissions, PermissionRequestCode);
    }


    /**
     * Checks to make sure that all permissions were granted. If so, try to start location updates again.
     *
     * @param reqCode
     * @param perms
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int reqCode, String[] perms, int[] grantResults)
    {
        for(int i = 0; i < grantResults.length; i++)
        {
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
        }
        //All permissions granted, start location updates
        startLocationUpdates();

    }


    /**
     * Calculates the difference im position between two LatLng objects
     * using the haversine formula.
     *
     * Returns meters.
     * @param posa
     * @param posb
     * @return
     */
    public double haversine(LatLng posa, LatLng posb)
    {
        double R = 6371000; //radius of earth in meters.

        double latDiff = Math.toRadians(posb.latitude - posa.latitude );
        double lngDiff = Math.toRadians(posb.longitude - posa.longitude);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(posa.latitude)) * Math.cos(Math.toRadians(posb.latitude)) *
                Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = R * c;

        return distance;
    }

    public void showDistance(double dist)
    {
        String form = String.format("Dist: %.0f", dist);
        Dist.setText(form);
    }



    ///region LOCATION LISTENER METHODS
    @Override
    public void onLocationChanged(Location loc)
    {
        if(mMap == null)
            return;

        if(UserMarker == null)
        {
            Log.w("location", "Added a use circle to the map!");
            MarkerOptions mo = new MarkerOptions();
            mo.position(new LatLng(loc.getLatitude(), loc.getLongitude()));


            UserMarker = mMap.addMarker(mo);
        }
        else
        {
            UserMarker.setPosition(new LatLng(loc.getLatitude(), loc.getLongitude()));

            //Log.w("location", "Updated the user location.");
        }

        showDistance(haversine(UserMarker.getPosition(), ContactMarker.getPosition()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }
    ///endregion
}
