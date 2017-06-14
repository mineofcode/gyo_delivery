package com.goyo.grocery_goyo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.goyo.grocery.R;
import com.goyo.grocery_goyo.Global.global;
import com.goyo.grocery_goyo.model.restaurantModel;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static ListView resturant_list;
    private Button search;
    private ImageView filterOption;
    public TextView txtLocation, txtLocDesc;
    private LinearLayout layout_location;
    private SearchView etSearchRestaurants;
    Context context;
    public ActionBar action;
    String addressLine, newAddress;
    AppLocationService appLocationService;
    SharedPreferences settings;
    CustomResturantAdapter resturantAdapter = null;
    List<restaurantModel> myList;
    Intent io;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        io = getIntent();
        newAddress = SearchLocation.address;
        appLocationService = new AppLocationService(this);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        context = this;
        filterOption = (ImageView) findViewById(R.id.imageFilter);
        filterOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), FilterScreen.class));
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
        etSearchRestaurants = (SearchView) findViewById(R.id.searchRestaurants);
        global.resturantNames = new ArrayList<>();
        settings = context.getSharedPreferences("PREF_BILL", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("Total Amount", 0);
        editor.putInt("CurrentCart", 0);
        editor.commit();
        resturant_list = (ListView) findViewById(R.id.list_display_resturants);
        InitAppBar();
        //Helps to set the details of user current location
        if (appLocationService.getIsGPSTrackingEnabled()) {
            if (newAddress == null) {
                addressLine = String.valueOf(appLocationService.getLocality(this));
                txtLocation.setText(appLocationService.getAddressLine(this));
                txtLocDesc.setText(appLocationService.getAddressLine(this) + "," + String.valueOf(addressLine));
            } else {
                txtLocation.setText(io.getStringExtra("Area"));
                txtLocDesc.setText(io.getStringExtra("AddressLine"));
            }
        } else {
            appLocationService.showSettingsAlert();
        }
        getRestaurant();
        etSearchRestaurants.setOnQueryTextListener(this);
    }

    private void getRestaurant() {
        final JsonObject json = new JsonObject();
        json.addProperty("flag", "all");
        Ion.with(context)
                .load("http://35.154.230.244:8085/getRestaurantMaster")
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (result != null) {
                            Gson gson = new Gson();
                            myList = gson.fromJson(result.get("data"), new TypeToken<List<restaurantModel>>() {
                            }.getType());
                            resturantAdapter = new CustomResturantAdapter(HomeActivity.this, myList);
                            resturant_list.setAdapter(resturantAdapter);
                            //               Toast.makeText(context, result.get("data").getAsJsonArray().get(0).getAsJsonObject().get("tm").getAsJsonArray().get(0).getAsJsonObject().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    //A method to get GPS provider
    public void InitAppBar() {
        ActionBar action = getSupportActionBar();
        action.setDisplayShowCustomEnabled(true);
        action.setCustomView(R.layout.layout_location_select);
        //Initializing both the textView to display current Location
        txtLocation = (TextView) action.getCustomView().findViewById(R.id.txtLocation);
        txtLocDesc = (TextView) action.getCustomView().findViewById(R.id.txtLocDesc);
        layout_location = (LinearLayout) action.getCustomView().findViewById(R.id.layout_location_select);
        //Creating Listener of Layout of action bar to go in next fragment for searching location
        layout_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SearchLocation.class));
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

    //Request to set permission runtime because of 6.0 and SecurityException Fatal Error
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
        resturantAdapter.filter(text);
        return false;
    }
    //getRestaurantMaster
    //flag = 'all'
}

