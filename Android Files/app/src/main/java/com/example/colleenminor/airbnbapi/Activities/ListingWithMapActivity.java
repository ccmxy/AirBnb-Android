package com.example.colleenminor.airbnbapi.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.example.colleenminor.airbnbapi.AlertDialogFragment;
import com.example.colleenminor.airbnbapi.Models.Listing;
import com.example.colleenminor.airbnbapi.Preferences;
import com.example.colleenminor.airbnbapi.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ListingWithMapActivity extends FragmentActivity implements OnMapReadyCallback {

    public static FragmentManager fragmentManager;

    public static final String Tag = ListingWithMapActivity.class.getSimpleName();
    private GoogleMap mMap;

    @Bind(R.id.titleTextView)
    TextView mTitle;

    @Bind(R.id.noGuestsTextView)
    TextView mNoGuests;


    @Bind(R.id.priceTextview)
    TextView mPrice;

    @Bind(R.id.cityStateTextView)
    TextView mCityState;

    @Bind(R.id.updateButton)
    BootstrapButton mUpdateListingButton;

    @Bind(R.id.showCommentsButton)
    BootstrapButton mShowCommentsButton;

    @Bind(R.id.listOfListings)
    BootstrapButton mAllListingsButton;

    @Bind(R.id.yourProfileButton)
    BootstrapButton mYourProfileButton;

    private Listing mListing;
    private String mCity;
    private String mState;
    private String mUsername;
    private String mAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_listing);
        ButterKnife.bind(this);
        mUsername = Preferences.getUsername(this);


        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        Log.v(Tag, title);
        fillListing(title);

        mShowCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListingWithMapActivity.this, ListingWithCommentsActivity.class);
                intent.putExtra("title", mListing.getTitle());

                startActivity(intent);

            }
        });


        mUpdateListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListingWithMapActivity.this, UpdateOrDeleteListingsActivity.class);
                intent.putExtra("id", mListing.getId());
                intent.putExtra("title", mListing.getTitle());
                intent.putExtra("noGuests", mListing.getNoGuests());
                intent.putExtra("price", mListing.getPrice());
                intent.putExtra("city", mListing.getCity());
                intent.putExtra("state", mListing.getState());
                startActivity(intent);
            }
        });

        mAllListingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListingWithMapActivity.this,ListOfListingsActivity.class);
                startActivity(intent);
            }
        });

        mYourProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListingWithMapActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });

        SupportMapFragment mFragmentManager = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment);
        mFragmentManager.getMapAsync(this);

    }


    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void fillListing(String title) {
        String url = "http://52.38.127.124:3003/getListingByName/?name=" + title;
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
                            mListing = getListingObject(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateView();
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

    }

//Return the listing so that we can access it throughout the file in mListing
    private Listing getListingObject(String jsonData) throws JSONException {
        JSONArray currentListing = new JSONArray(jsonData);
        JSONObject explrObject = currentListing.getJSONObject(0);
        String author = explrObject.getString("author");
        mAuthor = author;
        String title = explrObject.getString("name");
        String noGuests;
        String price;
        String city;
        String id = explrObject.getString("_id");
        String state;
        if (!explrObject.isNull("noGuests")) {
            noGuests = explrObject.getString("noGuests");
        } else {
            noGuests = "";
        }
        if (!explrObject.isNull("price")) {
            price = "$" + explrObject.getString("price");
        } else {
            price = "";
        }

        if (!explrObject.isNull("location")) {
            JSONObject locationObject = explrObject.getJSONObject("location");
            if (!locationObject.isNull("city")) {
                city = locationObject.getString("city");
                mCity = city;
            } else {
                city = "";
            }
            if (!locationObject.isNull("state")) {
                state = locationObject.getString("state");
                mState = state;
                getStateLatLong();
            } else {
                state = "";
            }
        } //end of if location exists loop
        else {
            city = "";
            state = "";
        }
        Listing listing = new Listing(title, noGuests, price, city, state, author, id);
        return listing;
    } //end of getListingObjects()


    private void updateView() {
        mTitle.setText(mListing.getTitle());
        mNoGuests.setText(mListing.getNoGuests());
        mPrice.setText(mListing.getPrice());
        if (mListing.getCity() == "" & mListing.getState() == ""){
            mCityState.setVisibility(View.INVISIBLE);
        }
        if (mListing.getCity() != null & mListing.getState() != null) {
            mCityState.setText(mListing.getCity() + ", " + mListing.getState());
        }
        else if (mListing.getCity() != "") {
            mCityState.setText(mListing.getCity());
        } else if (mListing.getState() != "") {
            mCityState.setText(mListing.getState());
        } else {
            mCityState.setText("");
        }
        if(!mUsername.equals(mAuthor)){
            mUpdateListingButton.setVisibility(View.GONE);
        }
        else{
            mUpdateListingButton.setVisibility(View.VISIBLE);
        }

    }

    private boolean isNetworkAlailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }


    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }


//Put the marker on the map
    void getStateLatLong() {
        String apikey = "AIzaSyCs1FsifaNYggFCwqaFO4W7eRTqzePPFdc";
        String url = "https://maps.googleapis.com/maps/api/geocode/json?components=locality:" + mCity
                + "|administrative_area:"
                + mState + "|country:USA&key=" + apikey;
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
                            JSONArray stateJsonArray = jsonDataObject.getJSONArray("results");
                            JSONObject locationObject = stateJsonArray.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                            final double latitude = Double.parseDouble(locationObject.getString("lat"));
                            final double longitude = Double.parseDouble(locationObject.getString("lng"));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                            .title("My Location").icon(BitmapDescriptorFactory
                                                    .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 8));
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(Tag, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(Tag, "Exception caught: ", e);

                    }


                }
            });
        }


    }
}