package com.khalil.mapapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

public class MesureListActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    private String base_url;
    Gson gson;
    List<Mesure> mesures;
    ListAdapter adapter;
    RecyclerView recyclerView;
    Button open_in_map;
    Spinner countriesspinner;
    Spinner citiesspinner;

    ArrayList<String> countries;
    ArrayList<String> cities;

    String spinnerUrlCountries;
    String spinnerUrlCities;
    ArrayAdapter<String> citiesAdapter;

    ImageView popup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesure_list);

        this.base_url = "https://daviddurand.info/D228/carte/";
        mesures = new ArrayList<>();


        recyclerView = findViewById(R.id.liste_mesure);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listedesMesures();
        countriesspinner = (Spinner) findViewById(R.id.countrylist);
        citiesspinner = (Spinner) findViewById(R.id.citylist);

        countries = new ArrayList<>();
        cities = new ArrayList<>();
        spinnerUrlCountries = base_url + "?action=liste-pays";
        spinnerUrlCities = base_url + "?action=liste-villes";

        citiesAdapter = new ArrayAdapter<String>(MesureListActivity.this,
                android.R.layout.simple_spinner_item, cities);

        citiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        loadCountries(spinnerUrlCountries);
        countriesspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String country = countriesspinner.getItemAtPosition(countriesspinner.getSelectedItemPosition()).toString();
                Toast.makeText(getApplicationContext(), "Visualisation des mesures pour: " + country, Toast.LENGTH_LONG).show();

                listedesMesuresParPays(country);
                loadCities(spinnerUrlCities);


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });
        loadCities(spinnerUrlCities);
        citiesspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String city = citiesspinner.getItemAtPosition(citiesspinner.getSelectedItemPosition()).toString();
                Toast.makeText(getApplicationContext(), city, Toast.LENGTH_LONG).show();
                listedesMesuresParVille(city);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        popup = (ImageView) findViewById(R.id.popup);
        popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(getApplicationContext(), view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.actions, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.open_in_map:
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), MapsActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.filter:
                                countriesspinner.setVisibility(View.VISIBLE);
                                citiesspinner.setVisibility(View.VISIBLE);
                                return true;

                            default:
                                return false;
                        }
                    }
                });
            }
        });
    }

    private void loadCountries(String url){
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("respo", "onResponse: "+ response);
                    for (int i = 0;i<response.length();i++){
                        try{
                            JSONObject jsonObject = response.getJSONObject(i);

                            String country = jsonObject.getString("Country");
                            if(country != "null")
                            countries.add(country);

                            countriesspinner.setAdapter(new ArrayAdapter<String>(MesureListActivity.this,
                                    android.R.layout.simple_spinner_dropdown_item,countries));

                            countriesspinner.setVisibility(View.GONE);

                        }catch(JSONException e){
                            e.getMessage();
                        }
                    }
                    countriesspinner.setSelection(0);
                    loadCities(spinnerUrlCities);

            }

            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    private void loadCities(String url){
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                cities.clear();
                String country = countriesspinner.getSelectedItem().toString();
                Log.d("respo", "onResponse: "+ response);

                for (int i = 0;i<response.length();i++){
                    try {

                            JSONObject jsonObject = response.getJSONObject(i);

                            Log.d("pp", "onResponse: "+country);
                            String newCountry = jsonObject.getString("Country");

                            if (country.equals(newCountry)) {

                                String city = jsonObject.getString("City");

                                Log.d("kkk", "onResponse: "+city);
                                if (!city.equals("null")) {
                                    cities.add(city);

                                    citiesspinner.setAdapter(citiesAdapter);
                                    citiesAdapter.notifyDataSetChanged();


                                }
                            }
                    }catch(JSONException e){
                        e.getMessage();
                    }
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonArrayRequest);
    }


    private void listedesMesures() {
        String url = this.base_url;
        Context context = this;
        requestQueue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                Log.d("respo", "onResponse: "+ response);
                for (int i = 0; i < response.length(); i++) {
                    try {

                        JSONObject jsonObject = response.getJSONObject(i);
                        Log.d("respo", "onResponse2: "+ response.getJSONObject(i));
                        if(!jsonObject.getString("LatLng").isEmpty() && !jsonObject.getString("City").equals("null") && !jsonObject.getString("Country").equals("null")) {
                            Log.d("respo", "onResponse3: "+ response.getJSONObject(i));
                            Mesure mesure = new Mesure();
                            mesure.setIdMesure(jsonObject.getString("idMesure"));
                            mesure.setUser(jsonObject.getString("User"));
                            mesure.setMesure(jsonObject.getString("Mesure"));
                            mesure.setLatLng(jsonObject.getString("LatLng"));
                            mesure.setCity(jsonObject.getString("City"));
                            mesure.setCountry(jsonObject.getString("Country"));

                            Timestamp timestamp = new Timestamp(Long.valueOf(jsonObject.getString("Timestamp")));
                            String pattern = "EEEE dd MMMM yyyy à H:mm";
                            SimpleDateFormat simpleDateFormat =new SimpleDateFormat(pattern, new Locale("fr", "FR"));
                            String date = simpleDateFormat.format(new Date(timestamp.getTime() * 1000));

                            mesure.setTimestamp(date);


                            mesures.add(mesure);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter = new ListAdapter(mesures);
                recyclerView.setAdapter(adapter);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onErrorResponse: " + error.getMessage());
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    private void listedesMesuresParPays(String country) {
        String url = this.base_url+"?action=liste-par-pays&country="+country;
        Context context = this;
        requestQueue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                mesures.clear();
                Log.d("respo", "onResponse: "+ response);
                for (int i = 0; i < response.length(); i++) {
                    try {

                        JSONObject jsonObject = response.getJSONObject(i);
                        Log.d("respo", "onResponse2: "+ response.getJSONObject(i));
                        if(!jsonObject.getString("LatLng").isEmpty() && !jsonObject.getString("City").equals("null") && !jsonObject.getString("Country").equals("null")) {
                            Log.d("respo", "onResponse3: "+ response.getJSONObject(i));
                            Mesure mesure = new Mesure();
                            mesure.setIdMesure(jsonObject.getString("idMesure"));
                            mesure.setUser(jsonObject.getString("User"));
                            mesure.setMesure(jsonObject.getString("Mesure"));
                            mesure.setLatLng(jsonObject.getString("LatLng"));
                            mesure.setCity(jsonObject.getString("City"));
                            mesure.setCountry(jsonObject.getString("Country"));

                            Timestamp timestamp = new Timestamp(Long.valueOf(jsonObject.getString("Timestamp")));
                            String pattern = "EEEE dd MMMM yyyy à H:mm";
                            SimpleDateFormat simpleDateFormat =new SimpleDateFormat(pattern, new Locale("fr", "FR"));
                            String date = simpleDateFormat.format(new Date(timestamp.getTime() * 1000));

                            mesure.setTimestamp(date);

                                mesures.add(mesure);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter = new ListAdapter(mesures);
                recyclerView.setAdapter(adapter);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onErrorResponse: " + error.getMessage());
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    private void listedesMesuresParVille(String city) {
        String url = this.base_url+"?action=liste-par-ville&city="+city;
        Context context = this;
        requestQueue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                mesures.clear();
                Log.d("respo", "onResponse: "+ response);
                for (int i = 0; i < response.length(); i++) {
                    try {

                        JSONObject jsonObject = response.getJSONObject(i);
                        Log.d("respo", "onResponse2: "+ response.getJSONObject(i));
                        if(!jsonObject.getString("LatLng").isEmpty() && !jsonObject.getString("City").equals("null") && !jsonObject.getString("Country").equals("null")) {
                            Log.d("respo", "onResponse3: "+ response.getJSONObject(i));
                            Mesure mesure = new Mesure();
                            mesure.setIdMesure(jsonObject.getString("idMesure"));
                            mesure.setUser(jsonObject.getString("User"));
                            mesure.setMesure(jsonObject.getString("Mesure"));
                            mesure.setLatLng(jsonObject.getString("LatLng"));
                            mesure.setCity(jsonObject.getString("City"));
                            mesure.setCountry(jsonObject.getString("Country"));

                            Timestamp timestamp = new Timestamp(Long.valueOf(jsonObject.getString("Timestamp")));
                            String pattern = "EEEE dd MMMM yyyy à H:mm";
                            SimpleDateFormat simpleDateFormat =new SimpleDateFormat(pattern, new Locale("fr", "FR"));
                            String date = simpleDateFormat.format(new Date(timestamp.getTime() * 1000));

                            mesure.setTimestamp(date);

                            mesures.add(mesure);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter = new ListAdapter(mesures);
                recyclerView.setAdapter(adapter);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onErrorResponse: " + error.getMessage());
            }
        });
        requestQueue.add(jsonArrayRequest);
    }


}