package com.serhiyboiko.taskmanager.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.internal.FusedLocationProviderResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.serhiyboiko.taskmanager.R;

/**
 * Created by user on 19.07.2016.
 */
public class TaskLocationSelectActivity
        extends AppCompatActivity
        implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks {

    private static final int ASK_FOR_LOCATION_PERMISSIONS_REQUEST = 0;
    private GoogleMap mMap;
    private FloatingActionButton mSelectLocationFAB;
    private Marker mMarker;
    private Intent mIntent;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.task_location_select_activity_title);
        setContentView(R.layout.task_location_select_activity);

        mSelectLocationFAB = (FloatingActionButton) findViewById(R.id.save_location_fab);
        mSelectLocationFAB.setOnClickListener(this);

        mIntent = getIntent();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        boolean taskAssignedToLocation;
        double latitude;
        double longitude;
        LatLng position;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, ASK_FOR_LOCATION_PERMISSIONS_REQUEST);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        taskAssignedToLocation = mIntent.getBooleanExtra(TaskListActivity.IS_ASSIGNED_TO_LOCATION_EXTRA, false);

        if (taskAssignedToLocation){
            latitude = mIntent.getDoubleExtra(TaskListActivity.LATITUDE_EXTRA, 0);
            longitude = mIntent.getDoubleExtra(TaskListActivity.LONGITUDE_EXTRA, 0);
            position = new LatLng(latitude, longitude);
            mMarker = mMap.addMarker(new MarkerOptions().position(position));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        } else {
            mSelectLocationFAB.setVisibility(View.GONE);
            if (LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient).isLocationAvailable()){
                Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                latitude = currentLocation.getLatitude();
                longitude = currentLocation.getLongitude();
                position = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
            }
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMarker != null) {
                    mMarker.remove();
                } else {
                    mSelectLocationFAB.setVisibility(View.VISIBLE);
                }
                mMarker = mMap.addMarker(new MarkerOptions().position(latLng));
            }
        });

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId){
            case R.id.save_location_fab:
                double latitude = mMarker.getPosition().latitude;
                double longitude = mMarker.getPosition().longitude;

                Intent data = new Intent();
                data.putExtra(TaskListActivity.LATITUDE_EXTRA, latitude);
                data.putExtra(TaskListActivity.LONGITUDE_EXTRA, longitude);

                setResult(RESULT_OK, data);
                finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ASK_FOR_LOCATION_PERMISSIONS_REQUEST:

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                break;

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("onConnected", "");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
