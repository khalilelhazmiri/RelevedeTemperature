package com.khalil.mapapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.khalil.mapapp.databinding.ActivityMapsBinding;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;

    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_NAME = "name";

    SharedPreferences sharedPreferences;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    RequestQueue requestQueue;
    private String base_url;
    Gson gson;
    Mesure[] mesures;

    List<Mesure> mesureList;


    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    EditText mesureText;
    Button posterMesure;
    ImageView ajouterMesure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        getLocationPermission();


        gson = new GsonBuilder().create();
        mesureList = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestQueue = Volley.newRequestQueue(this);
        base_url = "https://daviddurand.info/D228/carte/";


        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        ajouterMesure = (ImageButton) findViewById(R.id.ajoutermesure);
    }


    private void getDeviceLocation() {
        Log.d("Getting device location", "Getting device location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d("loc", "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();

//                            String latLng = currentLocation.getLatitude()+","+currentLocation.getLongitude();
                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
//                                    DEFAULT_ZOOM);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));


                        } else {
                            Log.d("loc", "connot find current location");
                            Toast.makeText(MapsActivity.this, "Impossible de trouver ma position!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("myLoc", "Getting device location: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

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
//        getLocationPermission();


            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);

        sendRequest();

        ajouterMesure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posterNouveauReleve();
            }
        });


    }

    public void sendRequest() {

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.base_url, onSucceess, onError);
        requestQueue.add(stringRequest);
    }


    public Response.Listener<String> onSucceess = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            mesures = gson.fromJson(response, Mesure[].class);

            Log.d("Mesure", "Number of Mesure: " + mesures.length);

            for (Mesure mesure : mesures) {
                if (!mesure.getLatLng().isEmpty()) {
                    String latlng[] = mesure.getLatLng().split(",");
                    Double lat = Double.parseDouble(latlng[0]);
                    Double lng = Double.parseDouble(latlng[1]);

                    Log.d("latlng", "onResponse: " + lat);
                    String user = mesure.getUser();
                    String country = mesure.getCountry();
                    String city = mesure.getCity();
                    Double place_mesure = Double.parseDouble(mesure.getMesure());
                    int id_mesure = Integer.parseInt(mesure.getIdMesure());
                    String timestamp = mesure.getTimestamp();
                    LatLng places = new LatLng(lat, lng);
                    MarkerOptions markerOptions = new MarkerOptions().position(places)
                            .title(String.valueOf(place_mesure))
                            .snippet(city);

                    mMap.addMarker(markerOptions);
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(places));
                }
            }
        }
    };

    public Response.ErrorListener onError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void createNewMesureDialog(){

    }

    public void posterNouveauReleve() {

        dialogBuilder = new AlertDialog.Builder(this);
        final View mesurePopupView = getLayoutInflater().inflate(R.layout.popupwindow,null);

        mesureText = mesurePopupView.findViewById(R.id.nMesure);
        posterMesure = mesurePopupView.findViewById(R.id.ajoutermesure);

        dialogBuilder.setView(mesurePopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        posterMesure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d("loc", "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                            String LatLng = currentLocation.getLatitude()+","+currentLocation.getLongitude();
                            String name = sharedPreferences.getString(KEY_NAME, null);
                            String user = null;
                            if (name != null ){
                                user = name;
                            }

                            Log.d(TAG, "onComplete: "+user);
                            String url = base_url+"?action=envoi&LatLng="+LatLng+"&user="+user+"&mesure="+mesureText.getText();

                            Log.d(TAG, "onComplete: url: "+ url);
//                            Log.d(TAG, "latitude,longitude"+ currentLocation.getLongitude()+", "+currentLocation.getLongitude()+" Mesure: "+mesureText.getText()+"other infos: ");
                            Locale locale = new Locale("fr_FR");
                            Geocoder geocoder = new Geocoder(getApplicationContext(),locale);
                            try {
                                List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(),1);
                                Address address = addresses.get(0);
                                Log.d(TAG, "latitude,longitude"+ address.getLatitude()+", "+address.getLongitude()+" Mesure: "+mesureText.getText()+"other infos: "+address.toString());
                                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, null, new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    Log.d(TAG, "onResponse: mesure poster avec success");
                                    Toast.makeText(MapsActivity.this, "Mesure posté avec succès", Toast.LENGTH_SHORT).show();
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(new LatLng(address.getLatitude(), address.getLongitude()));
                                    markerOptions.title(mesureText.getText().toString());

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(TAG, "onErrorResponse: "+error.getMessage());
                                }
                            });

                            requestQueue.add(jsonArrayRequest);


                            } catch (IOException e) {
                                e.printStackTrace();
                            }


//

                        } else {
                            Log.d("loc", "connot find current location");
                            Toast.makeText(MapsActivity.this, "Impossible de trouver ma position!", Toast.LENGTH_SHORT).show();
                        }
                        dialog.cancel();
                    }
                });
            }

        });
    }
}