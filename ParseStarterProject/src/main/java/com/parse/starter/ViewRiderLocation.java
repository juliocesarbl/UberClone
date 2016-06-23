package com.parse.starter;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ViewRiderLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Intent i;

    public void back(View view)
    {
        Intent intentBack = new Intent(getApplicationContext(),ViewRequest.class);
        startActivity(intentBack);
    }

    public void acceptRequest(View view)
    {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
        query.whereEqualTo("requesterUsername",i.getStringExtra("requesterUsername"));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null)
                {
                    if(objects.size() > 0)
                    {
                        for(ParseObject object:objects)
                        {
                            object.put("driverUsername", ParseUser.getCurrentUser().getUsername());
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null)
                                    {
                                        Log.i("MyApp","http://maps.google.com/maps?daddr="+ i.getDoubleExtra("latitude",0)+","+ i.getDoubleExtra("longitude",0));
                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                                //Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));
                                                Uri.parse("http://maps.google.com/maps?daddr="+ i.getDoubleExtra("latitude",0)+","+ i.getDoubleExtra("longitude",0)));

                                        startActivity(intent);
                                    }

                                }
                            });

                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rider_location);
        i = getIntent();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.mapLayout);

        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                ArrayList<Marker> markers = new ArrayList<Marker>();
                Log.i("MyApp","Entrou no global layout listener");
                if(i == null)
                {
                    Log.i("MyApp","i== null");

                }
                else
                {
                    Log.i("MyApp lat", String.valueOf(i.getDoubleExtra("latitude",0)));
                    Log.i("MyApp long", String.valueOf(i.getDoubleExtra("longitude",0)));
                    Log.i("MyApp Usrlat", String.valueOf(i.getDoubleExtra("userLatitude",0)));
                    Log.i("MyApp Usrlong", String.valueOf(i.getDoubleExtra("userLongitude",0)));
                    Log.i("Myapp rqUsername",i.getStringExtra("requesterUsername"));
                }

                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(i.getDoubleExtra("latitude",0),i.getDoubleExtra("longitude",0))).title(i.getStringExtra("requesterUsername")+ " location")));
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(i.getDoubleExtra("userLatitude",0),i.getDoubleExtra("userLongitude",0))).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

                for(Marker marker:markers)
                {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                int padding = 300;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,padding);
                mMap.animateCamera(cu);
            }
        });
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

        // Add a marker in Sydney and move the camera
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(i.getDoubleExtra("latitude",0.0),i.getDoubleExtra("longitude",0.0)),10));

    }
}
