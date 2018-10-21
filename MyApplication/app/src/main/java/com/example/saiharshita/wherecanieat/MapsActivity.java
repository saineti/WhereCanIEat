package com.example.saiharshita.wherecanieat;


import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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

import android.content.Intent;

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
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 18));
                System.out.println(place.getWebsiteUri());
            }
        }
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        System.out.println(poi.name);

    }
}
