package com.guc.ahmed.carsimulator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button availability;
    private Button location;
    private Button start;
    private Button end;
    private Button cancel;
    private Button findAv;
    private Button cont;
    private Button pickup;
    private Button arrFinal;
    private Button destination;
    private EditText carID;
    private TextView apiResponse;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LatLng lastLocation;
    private Handler handler;
    private JSONObject latLng;
    private Runnable updateLocation;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        carID = findViewById(R.id.carID);

        apiResponse = findViewById(R.id.api_response);

        availability = findViewById(R.id.availabilty);
        availability.setOnClickListener(this);

        start = findViewById(R.id.start);
        start.setOnClickListener(this);

        location = findViewById(R.id.location);
        location.setOnClickListener(this);

        end = findViewById(R.id.end);
        end.setOnClickListener(this);

        pickup = findViewById(R.id.pickup);
        pickup.setOnClickListener(this);

        cont = findViewById(R.id.continue_trip);
        cont.setOnClickListener(this);

        destination = findViewById(R.id.destination);
        destination.setOnClickListener(this);

        arrFinal = findViewById(R.id.arr_final);
        arrFinal.setOnClickListener(this);

        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(this);

        findAv = findViewById(R.id.find_available);
        findAv.setOnClickListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        latLng = new JSONObject();

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.v("LocationCallback", "UPDATING LOCATION");
                for (Location location : locationResult.getLocations()){
                    if(lastLocation == null){
                        lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        updateLocationOnServer();
                    }else {
                        lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    }

                    url = getResources().getString(R.string.url_update_location) + carID.getText().toString() ;

                    try {
                        latLng.put("longitude",lastLocation.longitude);
                        latLng.put("latitude",lastLocation.latitude);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            }else{
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void updateLocationOnServer() {
        handler = new Handler();

        updateLocation = new Runnable(){
            @Override
            public void run() {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.POST, url, latLng, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.v("ANAAA","yoooo");
                                apiResponse.setText(response.toString());
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.getMessage()!=null){
                                    apiResponse.setText(error.getMessage());
                                } else{
                                    apiResponse.setText("Error");
                                }
                            }
                        });

                MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                handler.postDelayed(updateLocation,3000);
            }
        };

        handler.postDelayed(updateLocation,0);

    }


    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        checkLocationPermission();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Permission Missing")
                        .setMessage("Please give the missing permissions for the app to function")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create().show();
            }else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        else{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1: if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }

            } else {
                Toast.makeText(this, "Please provide location permission", Toast.LENGTH_LONG).show();
            }
                break;
        }
    }


    @Override
    public void onClick(View view) {

        String url = "";

        switch (view.getId()) {

            case R.id.availabilty:
                url = getResources().getString(R.string.url_update_availability) + getCarID();
                break;

            case R.id.location:
                url = getResources().getString(R.string.url_update_location) + getCarID();
                break;

            case R.id.start:
                url = getResources().getString(R.string.url_start) + getCarID();
                break;

            case R.id.end:
                url = getResources().getString(R.string.url_end) + getCarID();
                break;

            case R.id.cancel:
                url = getResources().getString(R.string.url_cancel) + getCarID();
                break;

            case R.id.find_available:
                url = getResources().getString(R.string.url_available) + getCarID();
                break;

            case R.id.arr_final:
            url = getResources().getString(R.string.url_final) + getCarID();
            break;

            case R.id.destination:
            url = getResources().getString(R.string.url_destination) + getCarID();
            break;

            case R.id.continue_trip:
                url = getResources().getString(R.string.url_continue) + getCarID();
                break;

            case R.id.pickup:
                url = getResources().getString(R.string.url_pickup) + getCarID();
                break;

            default:
                break;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        apiResponse.setText(response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.getMessage()!=null){
                            apiResponse.setText(error.getMessage());
                        } else{
                            apiResponse.setText("Error");
                        }
                    }
                });

// Access the RequestQueue through your singleton class.
        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public String getCarID(){
        return carID.getText().toString();
    }
}
