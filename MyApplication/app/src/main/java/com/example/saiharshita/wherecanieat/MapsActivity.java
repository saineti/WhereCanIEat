package com.example.saiharshita.wherecanieat;


import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.PointOfInterest;


import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.Task;

import android.content.Intent;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener {

    private GoogleMap mMap;
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;

    int PLACE_PICKER_REQUEST = 1;
    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady (GoogleMap googleMap){
        mMap = googleMap;

        // Add a marker in UW Ave and move the camera
        LatLng ave = new LatLng(47.6, -122.3);
        mMap.setOnPoiClickListener(this);
        mMap.addMarker(new MarkerOptions().position(ave).title("Marker in Ave"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ave, 18));

        try {
            builder.setLatLngBounds(new LatLngBounds(new LatLng(47.656851, -122.313826), new LatLng(47.659790, -122.31261)));
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException ex) {
            ex.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Selected place marker"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                System.out.println(place.getWebsiteUri());
                try {
                    builder.setLatLngBounds(new LatLngBounds(place.getLatLng(), place.getLatLng()));
                    startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        Task<PlaceBufferResponse> task = mGeoDataClient.getPlaceById(poi.placeId);
        while(!task.isSuccessful()) {
            // ...
        }
        Iterator<Place> iter = task.getResult().iterator();
        while (iter.hasNext()) {
            try {
                // only works for websites with their menus in the /menu subdirectory?
                System.out.println("http://" + new URI(iter.next().getWebsiteUri().toString()).toURL().getHost() + "/menu");
            } catch (Exception e) {
                System.out.println("ERROR");
            }
        }
    }
}
