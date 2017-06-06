package com.goyo.grocery_goyo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.goyo.grocery.R;
import com.goyo.grocery_goyo.model.restaurantModel;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
public class SearchLocation extends AppCompatActivity{
    private URL url;
    private HttpURLConnection hc;
    private Integer TRESHOLD=2;
    private DelayAutoCompleteTextView geo_autocomplete;
    private  ImageView geo_autocomplete_clear;
    double new_latitude;
    double new_longtitude;
    GeoSearchResult georesult;
    //All are the libraries of Google Play Services where dependency have also been added to gradle file
    //to implement the services
    //Implementing GoogleApiClient Listeners Connec
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INITIALIZING VIEWS FROM THE LAYOUT FILE OF ACTIVITY
        setContentView(R.layout.activity_search_location);
        //Code to add Strict Thread Policy because NetworkMainThread Runtime Exception
                StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
        geo_autocomplete_clear=(ImageView)findViewById(R.id.geo_autocomplete_text_clear);
        geo_autocomplete=(DelayAutoCompleteTextView)findViewById(R.id.txtSearchBar);
        geo_autocomplete.setThreshold(TRESHOLD);
        geo_autocomplete.setAdapter(new GeoAutoCompleteAdapter(this));
        geo_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //Listener of auto complete textview onItemClick
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                 georesult = (GeoSearchResult) adapterView.getItemAtPosition(position);
                geo_autocomplete.setText(georesult.getAddress());
                geo_autocomplete_clear.setVisibility(View.GONE);
               // Intent io=new Intent(getApplicationContext(),HomeActivity.class);
                //io.putExtra("SearchAddress",georesult.getAddress());
                //startActivity(io);
               /* try {
                    GetNewLatLong();
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
               String address=georesult.getAddress();
                GeoCodingLocation locationAddress = new GeoCodingLocation();
                locationAddress.getAddressFromLocation(address, getApplicationContext(), new GeocoderHandler());
                Toast.makeText(getApplicationContext(),address,Toast.LENGTH_LONG).show();
            }
        });
        //Text Change Listener to AutoComplete TextView
        geo_autocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            //Some functionality provided in the method
            public void afterTextChanged(Editable s) {
               if(s.length()>0)
               {
                   //if there is text in autocomplete textview than the image view will be visible
                   geo_autocomplete_clear.setVisibility(View.VISIBLE);
               }
               else
               {
                   //if auto complete text view is clear than the image view will be invisible
                   geo_autocomplete_clear.setVisibility(View.GONE);
               }
            }
        });
        //listener of image view to clear the text
        geo_autocomplete_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   geo_autocomplete.setText("");
            }
        });
    }
    //Approach To get the Latitude and Longtitude of the location  selected by the user by it is only application when name
    //is single if there is detailed address than it is not applicable
    private void GetNewLatLong() throws JSONException {
        String response="";
        try {
             url=new URL("https://maps.googleapis.com/maps/api/geocode/json?address="+georesult.getAddress()+"&key=AIzaSyAXJbhK_apLxdfAqe3kvcW0LpVppuEehXQ");
             hc=(HttpURLConnection) url.openConnection();
            hc.setConnectTimeout(15000);
            hc.setReadTimeout(15000);
            hc.setRequestMethod("GET");
            hc.setDoInput(true);
            hc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            hc.setDoOutput(true);
            int res_code=hc.getResponseCode();
            if (res_code == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(hc.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
                br.close();
            }
            else
            {
                response = "";
            }
            JSONObject jsonObject1 = new JSONObject(response.toString());
            double lng = ((JSONArray)jsonObject1.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");
            Toast.makeText(this,String.valueOf(lng),Toast.LENGTH_LONG).show();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    //Approach to get Latitude and Longtitude of each and every place selected in the search bar
    private class GeocoderHandler extends Handler
    {
         public void handleMessage(Message message)
         {
             String locationAddress;
             switch(message.what)
             {
                 case 1:
                     Bundle bundle=message.getData();
                     locationAddress=bundle.getString("address");
                     new_latitude=bundle.getDouble("lat");
                     new_longtitude=bundle.getDouble("long");
                     break;
                 default:
                     locationAddress=null;
                     new_latitude=0.0;
                     new_longtitude=0.0;
             }
             Toast.makeText(getApplicationContext(),"Lat--->"+String.valueOf(new_latitude)+""+"Long--->"+String.valueOf(new_longtitude),Toast.LENGTH_LONG).show();
         }
    }
}
