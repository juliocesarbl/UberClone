package com.parse.starter;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class YourLocation extends FragmentActivity implements OnMapReadyCallback, android.location.LocationListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    String provider;
    TextView infoTextView;
    Button requestUberButton;
    Boolean requestActive = false;
    ParseGeoPoint userLocation;
    String driverUsername = "";
    ParseGeoPoint driverLocation = new ParseGeoPoint(0,0);
    Handler handler = new Handler();


    public void requestUber(View view)
    {
        if(requestActive == false) {
            Log.i("MyApp", "Uber requested");
            ParseObject request = new ParseObject("Requests");
            if (ParseUser.getCurrentUser().getUsername() == null) {
                ParseUser.getCurrentUser().setUsername(UUID.randomUUID().toString());
                ParseUser.getCurrentUser().saveInBackground();
            }
            request.put("requesterUsername", ParseUser.getCurrentUser().getUsername());
            ParseACL parseACL = new ParseACL();
            parseACL.setPublicWriteAccess(true);
            parseACL.setPublicReadAccess(true);
            request.setACL(parseACL);
            Location location = locationManager.getLastKnownLocation(provider);
            userLocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
            request.put("requesterLocation",userLocation);


            request.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        infoTextView.setText("Finding Uber driver...");
                        requestUberButton.setText("Cancel Uber");
                        requestActive = true;
                    }
                }
            });
        }
        else{
            infoTextView.setText("Finding Canceled.");
            requestUberButton.setText("Request Uber");
            requestActive = false;
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
            query.whereEqualTo("requesterUsername",ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null){
                        if(objects.size() > 0)
                        {
                            for(ParseObject object: objects)
                            {
                                object.deleteInBackground();
                            }
                        }
                    }
                }
            });
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(),false);
        locationManager.requestLocationUpdates(provider,400,1,this);
        infoTextView = (TextView) findViewById(R.id.infoTextView);
        requestUberButton = (Button) findViewById(R.id.requestUber);

    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Location location = locationManager.getLastKnownLocation(provider);
        if(location != null)
        {
            updateLocation(location);
        }
    }
    public void updateLocation(final Location location){
        mMap.clear();

        if(requestActive == false)
        {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
            query.whereEqualTo("requesterUsername", ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null)
                    {
                        if(objects.size() > 0)
                        {
                            for(ParseObject object:objects)
                            {
                                requestActive = true;
                                infoTextView.setText("Finding uber Driver...");
                                requestUberButton.setText("Cancel Uber");
                                if(object.get("driverUsername") != null)
                                {
                                    driverUsername = object.getString("driverUsername");
                                    infoTextView.setText("Your driver is on their way");
                                    requestUberButton.setVisibility(View.INVISIBLE);
                                    Log.i("AppInfo",driverUsername);
                                }
                            }
                        }
                    }
                }
            });
        }
        if(driverUsername.equals(""))
        {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(location.getLatitude(),location.getLongitude()),10));
            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("Your Location"));
        }

       if(requestActive == true)
        {
            if(!driverUsername.equals(""))
            {
                ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                userQuery.whereEqualTo("username",driverUsername);
                userQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        if(e== null){
                            if(objects.size() > 0)
                            {
                                for(ParseUser driver: objects)
                                {
                                    driverLocation = driver.getParseGeoPoint("location");
                                }
                            }
                        }
                    }
                });
                if(driverLocation.getLatitude() != 0 && driverLocation.getLongitude() != 0)
                {
                    Log.i("AppInfo",driverLocation.toString());
                    Double distanceInKm = driverLocation.distanceInKilometersTo(new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                    distanceInKm = (double) Math.round(distanceInKm * 10)/10;
                    infoTextView.setText("Your driver is "+distanceInKm+" Kms away");

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    ArrayList<Marker> markers = new ArrayList<Marker>();

                    markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("Your location")));
                    markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(driverLocation.getLatitude(),driverLocation.getLongitude())).title("Driver Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

                    for(Marker marker:markers)
                    {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();
                    int padding = 300;
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,padding);
                    mMap.animateCamera(cu);


                }
            }
            userLocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
            query.whereEqualTo("requesterUsername",ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null){
                        if(objects.size() > 0)
                        {
                            for(ParseObject object: objects)
                            {
                                object.put("requesterLocation",userLocation);
                                object.saveInBackground();
                            }
                        }
                    }
                }
            });

        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLocation(location);
            }
        },2000);
    }
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider,400,1,this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mMap.clear();
        updateLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
