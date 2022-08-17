package com.smartweather.smartweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;
import com.smartweather.smartweather.Adapter.overmorrowAdapter;
import com.smartweather.smartweather.Adapter.tomorrowAdapter;
import com.smartweather.smartweather.Adapter.weatherAdapter;
import com.smartweather.smartweather.DataModel.conditonData;
import com.smartweather.smartweather.DataModel.weatherData;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
   private TextView tempTv,windTv,localityTv,weatherTypeTv,weatherDateTv,Title_todayDate,Title_tomorrowDate,Title_overmDate;
   private ImageView weatherIconImgv,searchBtn;
    private String base_url;
    private String search;
    int REQUEST_CODE = 101;
    LocationManager locationManager;
    List<Address> addressList;
    Location location;
    private ArrayList<weatherData> weatherDataList;
    private ArrayList<weatherData> weatherDataArrayList1;
    private ArrayList<weatherData> weatherDataArrayList2;
    private ArrayList<conditonData>conditonDataArrayList;
    private ArrayList<conditonData> conditonDataArrayList1;
    private ArrayList<conditonData> conditonDataArrayList2;
    private RecyclerView recyclerView,tmr_rv,ovrm_rv;
    private weatherAdapter adapter;
    private tomorrowAdapter adapter2;
    private overmorrowAdapter adapter3;
    Gson gson = new Gson();
    private String cnvtdTime, today_title_date;
    String city = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Remove status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Find View
        findById();
        //Retrive Data

        //Recycler View
        searchBtn();
        //Get search value
        getSearchValue();
        //get weather forecast data according to the search data
        if (search!=null){
            base_url="https://api.weatherapi.com/v1/forecast.json?key=ed7111cc88ee4769858141158222207&q="+search+"&days=10&aqi=yes&alerts=yes";
            getData(base_url);
        }else{
            givePermissions();
            base_url="https://api.weatherapi.com/v1/forecast.json?key=ed7111cc88ee4769858141158222207&q="+city+"&days=10&aqi=yes&alerts=yes";
            getData(base_url);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,true));
        tmr_rv.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true));
        ovrm_rv.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true));

    }
        //Asking users for Location Access..
    private void givePermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, "Please give location access...", Toast.LENGTH_SHORT).show();
                allPermission();
            }else{
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            getCity();
        }



    }

    private void getCity() {
        if (location!=null){
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                city = addressList.get(0).getLocality();
                Log.d("Locality ", "getLoation: "+city);
                Log.d("AddressList ", "getLoation: "+addressList.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }


    private void allPermission() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                givePermissions();
            }
        },3000);
    }


    //Getting search value form search bar
    private void getSearchValue() {
      Intent intent = getIntent();
      search = intent.getStringExtra("rj");
        Log.d("Search_m", "getSearchValue: "+search);
    }

    //Search button
    private void searchBtn() {
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Search.class);
                startActivity(intent);
            }
        });

    }

    //Getting data from API
    private void getData(String base_url) {

        AndroidNetworking.get(base_url).setPriority(Priority.HIGH).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {

                weatherDataList = new ArrayList<>();
                conditonDataArrayList = new ArrayList<>();
                weatherDataArrayList1 = new ArrayList<>();
                weatherDataArrayList2 = new ArrayList<>();

                if (response!=null){

                    try {
                        JSONObject result = response.getJSONObject("location");
                        String city_name = result.getString("name");
                        String localtime = result.getString("localtime");



                        JSONObject current = response.getJSONObject("current");
                        JSONObject condition = current.getJSONObject("condition");
                        int temp = current.getInt("temp_c");
                        int windSpeed = current.getInt("wind_kph");


                        JSONObject forecast = response.getJSONObject("forecast");
                        JSONArray forecastday = forecast.getJSONArray("forecastday");
                        JSONObject today = forecastday.getJSONObject(0);
                        JSONArray tdy_hour = today.getJSONArray("hour");



                        JSONObject tomorrow = forecastday.getJSONObject(1);
                        JSONArray tomorrow_hour = tomorrow.getJSONArray("hour");
                        String tmrw_date = tomorrow.getString("date");
                        setTmrDate(tmrw_date);

                        JSONObject overmorrow = forecastday.getJSONObject(2);
                        JSONArray overmorrow_hour = overmorrow.getJSONArray("hour");
                        String ovrm_date=overmorrow.getString("date");
                        setOvrmDate(ovrm_date);



                        String text = condition.getString("text");
                        String url = condition.getString("icon");
                        String today_date=today.getString("date");



                        //Today add data
                        todaySetData(tdy_hour);
                        //Setting tomorrow data
                        tomorrowData(tomorrow_hour);
                        //Setting Overmorrow data
                        overmorrowData(overmorrow_hour);
                        //setting data to view
                        setData(city_name,localtime,text,url,temp,windSpeed,today_date);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Sorry...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(ANError anError) {
                Log.d("Error", "Error after Response: "+anError.toString());
            }
        });
    }

    private void setOvrmDate(String ovrm_date) {
        String date_s=ovrm_date;
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date=dt.parse(date_s);
            SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yy");
            String ovr_date = dt1.format(date);
            Title_overmDate.setText(ovr_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setTmrDate(String tmrw_date) {
//        Log.d("s_date", "setTmrDate: "+tmrw_date);
        String date_s=tmrw_date;
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        Date date =null;
        try {
            date=dt.parse(date_s);
            SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yy");
            String f_date = dt1.format(date);
//            Log.d("F_date", "setTmrDate: "+f_date);
            Title_tomorrowDate.setText(f_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void overmorrowData(JSONArray overmorrow_hour) {
        conditonDataArrayList2 = new ArrayList<>();
        for (int i=overmorrow_hour.length()-1; i>=0; i--){
            try {
                JSONObject overmorrow_array = overmorrow_hour.getJSONObject(i);
                weatherData overmorrow_data = gson.fromJson(overmorrow_array.toString(), weatherData.class);
                JSONObject overmorrow_condition = overmorrow_array.getJSONObject("condition");
                conditonData overmorrow_condition_view  = gson.fromJson(overmorrow_condition.toString(),conditonData.class);
                conditonDataArrayList2.add(overmorrow_condition_view);
                weatherDataArrayList2.add(overmorrow_data);
                adapter3 = new overmorrowAdapter(weatherDataArrayList2,conditonDataArrayList2,MainActivity.this);
                ovrm_rv.setAdapter(adapter3);
//                Log.d("Overmorrow", "overmorrowData: "+overmorrow_array.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void tomorrowData(JSONArray tomorrow_hour) {
        conditonDataArrayList1 = new ArrayList<>();
        for (int i=tomorrow_hour.length()-1; i>=0;i--){

            try {
                JSONObject tomorrowData=tomorrow_hour.getJSONObject(i);
                JSONObject tmr_condition=tomorrowData.getJSONObject("condition");
                conditonData tmr_condition_view = gson.fromJson(tmr_condition.toString(),conditonData.class);
                weatherData tomorrow_data=gson.fromJson(tomorrowData.toString(),weatherData.class);
                //For tomorrow condition
                conditonDataArrayList1.add(tmr_condition_view);
                //For tomorrow data
                weatherDataArrayList1.add(tomorrow_data);
                adapter2 = new tomorrowAdapter(weatherDataArrayList1,conditonDataArrayList1,MainActivity.this);
                tmr_rv.setAdapter(adapter2);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private void todaySetData(JSONArray tdy_hour) {
        for (int i=tdy_hour.length()-1; i>=0;i--){
            try {
                JSONObject tdyData = tdy_hour.getJSONObject(i);
                JSONObject condition = tdyData.getJSONObject("condition");
                weatherData today_weather = gson.fromJson(tdyData.toString(),weatherData.class);
                conditonData condito_data = gson.fromJson(condition.toString(),conditonData.class);
                weatherDataList.add(today_weather);
                conditonDataArrayList.add(condito_data);
                //Setting adapter
                adapter = new weatherAdapter(MainActivity.this,weatherDataList,conditonDataArrayList);
                recyclerView.setAdapter(adapter);
//                Log.d("Today Hour", "todaySetData: "+tdyData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    //Setting data to Views
    private void setData(String city,String localTime,String weatherType,String url,int temp,int windSpeed, String today_date) {

        convetTime(localTime);
        convetTime2(today_date);
        localityTv.setText(city);
        weatherDateTv.setText("Last updated at: " +cnvtdTime);
        weatherTypeTv.setText(weatherType);
        Picasso.get().load("https:"+url).into(weatherIconImgv);
        tempTv.setText(String.valueOf(temp+" °C"));
        windTv.setText(String.valueOf(windSpeed)+" km/h");
        Title_todayDate.setText(today_title_date);
    }

    //Title Date
    private void convetTime2(String today_date) {
        String date_s = today_date;
        SimpleDateFormat dt = new SimpleDateFormat("yyyyy-mm-dd");
        Date date2 = null;
        try {
            date2 = dt.parse(date_s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dt1 = new SimpleDateFormat("dd-mm-yy");
        today_title_date = dt1.format(date2);
    }

    //Converting time for details card
    private void convetTime(String localTime) {
        String date_s = localTime;
        SimpleDateFormat dt = new SimpleDateFormat("yyyyy-mm-dd hh:mm");
        Date date = null;
        try {
            date = dt.parse(date_s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dt1 = new SimpleDateFormat("hh:mm a");
        cnvtdTime = dt1.format(date);


    }

    //Finding Views through id
    private void findById() {
        tempTv = findViewById(R.id.card_temp_id);
        windTv = findViewById(R.id.card_wind_id);
        localityTv = findViewById(R.id.card_locality_id);
        weatherDateTv = findViewById(R.id.card_weather_date_id);
        weatherTypeTv = findViewById(R.id.card_weather_type_id);
        weatherIconImgv = findViewById(R.id.card_weather_icon_id);
        recyclerView = findViewById(R.id.rv_id);
        Title_todayDate = findViewById(R.id.today_date_title_id);
        tmr_rv = findViewById(R.id.tomorrow_rv_id);
        ovrm_rv = findViewById(R.id.overm_rv_id);
        Title_tomorrowDate = findViewById(R.id.tomorrow_date_title_id);
        Title_overmDate = findViewById(R.id.trd_date_title_id);
        searchBtn = findViewById(R.id.search_button_id);

    }
}