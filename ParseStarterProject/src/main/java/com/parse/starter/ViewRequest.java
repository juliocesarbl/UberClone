package com.parse.starter;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequest extends AppCompatActivity  implements LocationListener{
    ListView listView;
    ArrayList<String> listViewContent;
    ArrayList<String> usernames;
    ArrayList<Double> latitudes;
    ArrayList<Double> longitudes;
    ArrayAdapter arrayAdapter;
    LocationManager locationManager;
    String provider;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        locationManager = (LocationManager)  getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(),false);
        locationManager.requestLocationUpdates(provider,400,1,this);
        location = locationManager.getLastKnownLocation(provider);
        if(location != null)
        {
            updateLocation();
        }
        listView =  (ListView) findViewById(R.id.listView);
        listViewContent = new ArrayList<String>();
        usernames = new ArrayList<String>();
        latitudes = new ArrayList<Double>();
        longitudes = new ArrayList<Double>();

        listViewContent.add("Finding nearby requests...");

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listViewContent);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i  = new Intent(getApplicationContext(),ViewRiderLocation.class);
                i.putExtra("requesterUsername",usernames.get(position));
                i.putExtra("latitude",latitudes.get(position));
                i.putExtra("longitude",longitudes.get(position));
                i.putExtra("userLatitude",location.getLatitude());
                i.putExtra("userLongitude",location.getLongitude());
                startActivity(i);
            }
        });




    }

    public void updateLocation()
    {
        final ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
        ParseUser.getCurrentUser().put("location",userLocation);
        ParseUser.getCurrentUser().saveInBackground();


        ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
        query.whereDoesNotExist("driverUsername");
        query.whereNear("requesterLocation",userLocation);
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null)
                {
                    if(objects.size() > 0)
                    {
                        listViewContent.clear();
                        usernames.clear();
                        latitudes.clear();
                        longitudes.clear();
                        for( ParseObject object:objects)
                        {
                            Double distanceInKm = userLocation.distanceInKilometersTo((ParseGeoPoint)object.get("requesterLocation"));
                            distanceInKm = (double) Math.round(distanceInKm * 10 ) / 10;

                            listViewContent.add(distanceInKm.toString()+" Kms ");

                            usernames.add(object.getString("requesterUsername"));
                            latitudes.add(object.getParseGeoPoint("requesterLocation").getLatitude());
                            longitudes.add(object.getParseGeoPoint("requesterLocation").getLongitude());

                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider,400,1,this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Se comentou ,mesmo no google maps ele continua atualizando
        // locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation();
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
