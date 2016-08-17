package com.example.colleenminor.airbnbapi.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.example.colleenminor.airbnbapi.AlertDialogFragment;
import com.example.colleenminor.airbnbapi.Models.Listing;
import com.example.colleenminor.airbnbapi.Preferences;
import com.example.colleenminor.airbnbapi.R;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ListOfListingsActivity extends AppCompatActivity {

    public static final String Tag = ListOfListingsActivity.class.getSimpleName();
    private ArrayList<Listing> mListingList;
    private String mLongStateName;
    private String mShortStateName;
    private String mCityName;
    private String mNarrowBy;
    private String mLatitude;
    private String mLongitude;

    @Bind(R.id.listView)
    ListView lv;

    @Bind(R.id.userLocationTextView)
    TextView mUserLocationTextView;

    @Bind(R.id.onlyUserStateButton)
    BootstrapButton mUserStateButton;

    @Bind(R.id.onlyUserCityButton)
    BootstrapButton mUserCityButton;


    @Bind(R.id.goToNewListingPageButton)
    BootstrapButton mGoToNewListingPageButton;

    @Bind(R.id.allListingsButton)
    BootstrapButton mAllListingsButton;

    @Bind(R.id.turnOnLocationOptionsButton)
    BootstrapButton mTurnOnLocationOptionsButton;

    @Bind(R.id.updateLocationButton)
    BootstrapButton mUpdateLocationButton;

    @Bind(R.id.yourProfileButton)
    BootstrapButton mYourProfileButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        Intent intent = getIntent();
        mLatitude = intent.getStringExtra("latitude");
        mLongitude = intent.getStringExtra("longitude");
        //If user arrived at this page through UserLatLongActivity
        if(mLongitude != null){
            mNarrowBy = "city";
            Preferences.setLastLocation(this, mLatitude, mLongitude);
            showLocationButtons();
            getListingsByLocation(mLatitude, mLongitude);
        }
        //If User just opened this page from closed app
        else {
            mNarrowBy = "all";
            setAllText();
            getListingsFromAirbnbApi();
        }

        String locationAllowed = Preferences.getLocationAllowed(this);
        //If we already retrieved user location another time
        if(locationAllowed.equals("true")){
            mLatitude = Preferences.getLastLat(this);
            mLongitude = Preferences.getLastLong(this);
            showLocationButtons();
        }

        mTurnOnLocationOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListOfListingsActivity.this, UserLatLongActivity.class);
                startActivity(intent);
            }
        });

        mGoToNewListingPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListOfListingsActivity.this, NewListingActivity.class);
                startActivity(intent);
            }
        });

        mUserStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserCityButton.setShowOutline(true);
                mUserStateButton.setShowOutline(false);
                mAllListingsButton.setShowOutline(true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNarrowBy = "state";
                        getListingsByLocation(mLatitude, mLongitude);
                    }
                });

            }
        });

        mUserCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserCityButton.setShowOutline(false);
                mUserStateButton.setShowOutline(true);
                mAllListingsButton.setShowOutline(true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNarrowBy = "city";
                        getListingsByLocation(mLatitude, mLongitude);
                    }
                });
            }
        });

        mAllListingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserCityButton.setShowOutline(true);
                mUserStateButton.setShowOutline(true);
                mAllListingsButton.setShowOutline(false);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNarrowBy = "all";
                        getListingsFromAirbnbApi();
                        setAllText();
                    }
                });
            }
        });


        //Refresh the page with new location data
        mUpdateLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListOfListingsActivity.this, UserLatLongActivity.class);
                startActivity(intent);
            }
        });

        mYourProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListOfListingsActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });


    }

    void getListingsByLocation(String latitude, String longitude) {
        String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                + latitude + "," + longitude + "&sensor=false";

        if (isNetworkAlailable()) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(Tag, jsonData);

                        if (response.isSuccessful()) {

                            JSONObject jsonDataObject = new JSONObject(jsonData);
                            JSONArray resultsJsonArray = jsonDataObject.getJSONArray("results");
                            JSONObject firstResult = resultsJsonArray.getJSONObject(0);
                            //Get the city:
                            String address = firstResult.getString("formatted_address");
                            String[] addressArray =  address.split(",");
                            String cityName = addressArray[1];
                            mCityName = cityName.substring(1);
                            Log.v(Tag, "City name: " + mCityName);

                            JSONArray addressComponentsArray = firstResult.getJSONArray("address_components");
                            //Get the state:
                            for (int idx = 0; idx < addressComponentsArray.length(); idx++){
                                JSONArray typeJSON = addressComponentsArray.getJSONObject(idx).getJSONArray("types");
                                String[] typeStrings = new String[typeJSON.length()];
                                for(int i = 0; i < typeJSON.length(); i++){
                                    typeStrings[i] = typeJSON.getString(i);
                                }
                                //If it is a state
                                if (Arrays.asList(typeStrings).contains("administrative_area_level_1")){
                                    //Long name, like New York
                                    mLongStateName = addressComponentsArray.getJSONObject(idx).getString("long_name");
                                    //Short name, like NY
                                    mShortStateName = addressComponentsArray.getJSONObject(idx).getString("short_name");
                                }

                            }
                            Log.v(Tag, mShortStateName);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getListingsFromAirbnbApi();
                                }
                            });


                        }else{
                            alertUserAboutError();
                        }
                    }catch(IOException e){
                        Log.e(Tag, "Exception caught: ", e);
                    }
                    catch(JSONException e){
                        Log.e(Tag, "Exception caught: ", e);

                    }


                }
            });
        } else

        {
            Toast.makeText(this, "Network is not available", Toast.LENGTH_LONG).show();
        }

        Log.d(Tag, "ListingByState UI code is running!");
    }


    public void getListingsFromAirbnbApi() {
        if (isNetworkAlailable()) {
            OkHttpClient client = new OkHttpClient();
            String url = "";
            if(mNarrowBy.equals("state")) {
                setStateText();
                url = "http://52.38.127.124:3003/getListingsByLocation/?state=" + mShortStateName;

            }
            if (mNarrowBy.equals("city")){
                setCityText();
                url = "http://52.38.127.124:3003/getListingsByLocation/?state=" + mShortStateName + "&city=" + mCityName;
            }
            if (mNarrowBy.equals("all")){
                url = "http://52.38.127.124:3003/getAllListings";
            }
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(Tag, jsonData);

                        if (response.isSuccessful()) {
                            mListingList = getListingObjects(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setListingListView();
                                }
                            });


                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(Tag, "Exception caught: ", e);
                    }
                    //In case of JSON error during getListingObjects:
                    catch (JSONException e) {
                        Log.e(Tag, "Exception caught: ", e);

                    }
                }
            });
        } else {
            Toast.makeText(this, "Network is not available", Toast.LENGTH_LONG).show();
        }
        Log.d(Tag, "ListingByState UI code is running!");
    }



    private ArrayList<Listing> getListingObjects(String jsonData) throws JSONException {
        JSONArray currentListing = new JSONArray(jsonData);
        ArrayList<Listing> allListings = new ArrayList<Listing>();


        for (int i = 0; i < currentListing.length(); i++) {
            JSONObject explrObject = currentListing.getJSONObject(i);
            String title = explrObject.getString("name");
            String author = explrObject.getString("author");
            String noGuests;
            String price;
            String city;
            String state;
            if (!explrObject.isNull("noGuests")) {
                noGuests = explrObject.getString("noGuests");
            } else {
                noGuests = "";
            }
            if (!explrObject.isNull("price")) {
                price = explrObject.getString("price");

            } else {
                price = "";
            }

            if (!explrObject.isNull("location")) {
                JSONObject locationObject = explrObject.getJSONObject("location");
                if (!locationObject.isNull("city")) {
                    city = locationObject.getString("city");
                } else {
                    city = "";
                }
                if (!locationObject.isNull("state")) {
                    state = locationObject.getString("state");
                } else {
                    state = "";
                }
            } //end of if location exists loop
            else {
                city = "";
                state = "";
            }
            Listing listing = new Listing(title, noGuests, price, city, state, author);
            allListings.add(listing);
        } //end of for loop
        return allListings;
    } //end of getListingObjects()

    //Check if network is avaliable
    private boolean isNetworkAlailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

//Fill the list with listings
    public void setListingListView(){
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

        for (Listing listing : mListingList) {
            String title = listing.getTitle();
            String city = listing.getCity();
            String state = listing.getState();
            String location = "";
            if(city.length() > 0 && state.length() > 0){
                location = city + ", " + state;
            }
            if(city.length() > 0 && state.length() < 1){
                location = city;
            }
            if(city.length() < 1 && state.length() > 0){
                location = state;
            }
            Map<String, String> datum = new HashMap<String, String>(3);
            datum.put("Title", title);
            datum.put("CityState", location);
            data.add(datum);
            setThatList(lv, data);

        }

    }
    //Set the list view:
    public void setThatList(ListView listView,  List<Map<String, String>> data) {
        SimpleAdapter simpleAdapter = new SimpleAdapter(
                ListOfListingsActivity.this,
                data,
                android.R.layout.simple_list_item_2,
                new String[] {"Title", "CityState"},
                new int[] {android.R.id.text1, android.R.id.text2, android.R.id.custom});
        lv.setAdapter(simpleAdapter);
        makeListClickable();
    }

    //Make each item in the list view take the user to the page for that listing:
    public void makeListClickable() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Map<String, String> datum = (Map<String, String>) arg0.getItemAtPosition(position);
                Object value = datum.get("Title");
                final String title = (String) value;
                Intent intent = new Intent(ListOfListingsActivity.this, ListingWithMapActivity.class);
                intent.putExtra("title", title);
                startActivity(intent);
            }
        });
    }


    public void setCityText(){
        mUserLocationTextView.setText("Ah, I see that you are in " + mCityName + ".");
    }

    public void setStateText(){
        mUserLocationTextView.setText("Ah, I see that you are in " + mLongStateName + ".");
    }

    public void setAllText(){
        mUserLocationTextView.setText("These are all the listings in the API database.");
    }


    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

//Set user preferences to show that they have allowed location services for this app, so now they
    //will not have to approve it to see the location service buttons anymore.
    public void showLocationButtons(){
        Preferences.setLocationAllowed(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTurnOnLocationOptionsButton.setVisibility(View.GONE);
                mUserStateButton.setVisibility(View.VISIBLE);
                mUserCityButton.setVisibility(View.VISIBLE);
                mUpdateLocationButton.setVisibility(View.VISIBLE);
            }
        });
    }


}

