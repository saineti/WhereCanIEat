package com.example.saiharshita.wherecanieat;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    private Place place;
    private Double stat;

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
        // mMap.setOnPoiClickListener(this);
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
                place = PlacePicker.getPlace(this, data);
                // retrieve menu information from website
                if (place.getWebsiteUri() != null) {
                    AsyncTask<String, Void, Void> t = new RetreiveFeedTask().execute(place.getWebsiteUri().toString());
                    try {
                        t.get(5000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Dietary Statistics");
                alertDialog.setMessage("Add a slider for vegetarian friendly with : " + stat);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Go Back",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                OnButtonCont();
                            }
                        });
                alertDialog.show();
            }
        }
    }

    protected void OnButtonCont() {
        mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Selected place marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),18));
        System.out.println(place.getWebsiteUri());
        try {
           LatLng sw = new LatLng(place.getLatLng().latitude - 0.001, place.getLatLng().longitude - 0.001);
           LatLng ne = new LatLng(place.getLatLng().latitude + 0.001, place.getLatLng().longitude + 0.001);
           builder.setLatLngBounds(new LatLngBounds(sw, ne));
           startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (Exception ex) {
           ex.printStackTrace();
        }
    }

    class RetreiveFeedTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            try {
                URL url = new URL(stdizeURL(new URL(urls[0]).getHost()));
                System.out.println(url);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                InputStream is = null;
                if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    is = conn.getInputStream();
                }

                if (is == null) {
                    System.out.println("Website not found");
                    return null;
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                String line;
                int lineCount = 0;
                int meatCount = 0;
                while ((line = in.readLine()) != null) {
                    if (lineContainsMeat(line)) {
                        meatCount++;
                    }
                    lineCount++;
                }
                System.out.println((double) meatCount / (double) lineCount * 100);
                in.close();
                stat = (double) meatCount / (double) lineCount * 100;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private boolean lineContainsMeat(String line) {
            return line.contains("meat") || line.contains("pork") ||
                    line.contains("chicken") || line.contains("beef") || line.contains("bbq");
        }

        private String stdizeURL(String host) {
            return "https://" + host + "/menu";
        }
    }
}
