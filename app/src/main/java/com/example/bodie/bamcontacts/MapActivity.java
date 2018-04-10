package com.example.bodie.bamcontacts;

import android.Manifest;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.PermissionChecker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.Provider;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, LocationHandler {

    private GoogleMap mMap;

    protected TextView Lat;
    protected TextView Lon;

    AsyncGeocode Geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Lat = findViewById(R.id.lblLat);
        Lon = findViewById(R.id.lblLon);
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
    public void onLocationFound(LatLng location)
    {
        if(location != null)
        {
            String lat = location.latitude + "";
            String lon = location.longitude + "";

            Lat.setText(lat);
            Lon.setText(lon);

            MarkerOptions mo = new MarkerOptions().position(location);
            mo.title(getIntent().getStringExtra("name"));

            mMap.addMarker(mo);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
        }
        else
        {
            //There was an error finding the location.
            Toast.makeText(this, "There was an error mapping that address.", Toast.LENGTH_SHORT).show();
        }
    }

    public void findCurrentLocation()
    {
        int permFine = PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permCoarse = PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(permFine != PermissionChecker.PERMISSION_GRANTED && permCoarse != PermissionChecker.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Do not have location permissions.", Toast.LENGTH_SHORT).show();
        }

        LocationManager locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria crit = new Criteria();
        crit.setPowerRequirement(Criteria.POWER_MEDIUM);
        crit.setAltitudeRequired(false);
        crit.setBearingRequired(false);
        crit.setSpeedRequired(false);
        String prov = locMan.getBestProvider(crit ,true);

        Location loc = locMan.getLastKnownLocation(prov);
    }
}
