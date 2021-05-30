package com.example.weatherapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    TextView textResult;
    EditText editText;
    Button searchBtn;
    ImageView imgWeather;
    final static int REQUEST = 112;
    LocationManager locationManager;
    private final String url = "https://api.openweathermap.org/data/2.5/weather?q=";
    private final String appid = "&appid=92d644ce834dabec08203e3770499ee9";
    DecimalFormat decimalFormat = new DecimalFormat("#.###");

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    public void search() {


        try {
            String cName = editText.getText().toString().trim();
            String tUrl = "";

            if (cName.equals("")) {
                Toast.makeText(this, "City can not be empty!", Toast.LENGTH_SHORT).show();
            } else {
                tUrl = url + cName + appid;
            }

            StringRequest stringRequest = new StringRequest(Request.Method.POST, tUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String resultText = "";
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                        JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                        String weatherDesc = jsonObjectWeather.getString("description");
                        String weatherImg = jsonObjectWeather.getString("icon");
                        JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                        double temp = jsonObjectMain.getDouble("temp") - 273.15;
                        double visibility = Double.parseDouble(jsonResponse.getString("visibility")) / 1000;
                        JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
                        String countryN = jsonObjectSys.getString("country");
                        String cityN = jsonResponse.getString("name");
                        String iconPom = "https://api.openweathermap.org/img/w/" + weatherImg + ".png";
                        resultText += " Weather of " + cityN + "(" + countryN + ")" +
                                "\n Temperature: " + decimalFormat.format(temp) + (char) 0x00B0 + "C" +
                                "\n Visibility: " + visibility + " km" +
                                "\n Description: " + capitalize(weatherDesc);
                        Glide
                                .with(MainActivity.this)
                                .load(iconPom)
                                .centerCrop()
                                .into(imgWeather);
                        textResult.setText(resultText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setComponents();
        getLocation();
        search();
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
    }

    private void setComponents() {

        editText = findViewById(R.id.edit_search);
        searchBtn = findViewById(R.id.btn_search);
        textResult = findViewById(R.id.txt_results);
        imgWeather = findViewById(R.id.img_weather);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }


    private void getLocation() {

        String pomS;
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {

                Geocoder geocoder = new Geocoder(MainActivity.this);
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        pomS = addresses.get(0).getLocality();
                        editText.setText(pomS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                    search();
                    return;
                } else {
                    Toast.makeText(this, "Required permissions are not granted!", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }


}