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
import android.widget.Toast;

import com.example.colleenminor.airbnbapi.AlertDialogFragment;
import com.example.colleenminor.airbnbapi.Models.Comment;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserCommentsAndListingsActivity extends AppCompatActivity {
    public static final String Tag = UpdateOrDeleteListingsActivity.class.getSimpleName();

    private String mUsername;
    private ArrayList<Listing> mListingList;
    private ArrayList<Comment> mCommentsList;

    @Bind(R.id.lv)
    ListView lv;

    @Bind(R.id.commentsListView)
    ListView commentsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_listings);

        ButterKnife.bind(this);

        mUsername = Preferences.getUsername(this);
        getListingsByAuthor(mUsername);
        getCommentsByAuthor(mUsername);

    }

    public void getListingsByAuthor(String username) {
        if (isNetworkAlailable()) {
            OkHttpClient client = new OkHttpClient();
            String url = "http://52.38.127.124:3003/getListingByAuthor/?author=" + username;

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
                    //catch for JSON error during getListingObjects:
                    catch (JSONException e) {
                        Log.e(Tag, "Exception caught: ", e);

                    }
                }
            });
        } else {
            Toast.makeText(this, "Network is not available", Toast.LENGTH_LONG).show();
        }
        Log.d(Tag, "Main UI code is running!");
    }


    public void getCommentsByAuthor(String username) {
        if (isNetworkAlailable()) {
            OkHttpClient client = new OkHttpClient();
            String url = "http://52.38.127.124:3003/getCommentsByAuthor/?author=" + username;

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
                            mCommentsList = getCommentsObjects(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setCommentsListView();
                                }
                            });


                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(Tag, "Exception caught: ", e);
                    }
                    //catch for JSON error during getListingObjects:
                    catch (JSONException e) {
                        Log.e(Tag, "Exception caught: ", e);

                    }
                }
            });
        } else {
            Toast.makeText(this, "Network is not available", Toast.LENGTH_LONG).show();
        }
        Log.d(Tag, "Main UI code is running!");
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


    public void setCommentsListView(){
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

        for (Comment comment : mCommentsList) {
            String words = comment.getWords();
            String author = comment.getAuthor();
            String time = comment.getTime();


            Map<String, String> datum = new HashMap<String, String>(3);
            datum.put("Words", words);
            datum.put("AuthorTime", author + " @" + time);
            data.add(datum);
            setCommentsList(commentsListView, data);

        }

    }

    //Set the list view:
    public void setCommentsList(ListView listView,  List<Map<String, String>> data) {
        SimpleAdapter simpleAdapter = new SimpleAdapter(
                UserCommentsAndListingsActivity.this,
                data,
                android.R.layout.simple_list_item_2,
                new String[] {"Words", "AuthorTime"},
                new int[] {android.R.id.text1, android.R.id.text2, android.R.id.custom});
        commentsListView.setAdapter(simpleAdapter);
        makeListClickable();
    }
    //Set the list view:
    public void setThatList(ListView listView,  List<Map<String, String>> data) {
        SimpleAdapter simpleAdapter = new SimpleAdapter(
                UserCommentsAndListingsActivity.this,
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
                Intent intent = new Intent(UserCommentsAndListingsActivity.this, ListingWithMapActivity.class);
                intent.putExtra("title", title);
                startActivity(intent);
            }
        });
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


    private ArrayList<Comment> getCommentsObjects(String jsonData) throws JSONException {
        JSONArray currentComment = new JSONArray(jsonData);
        ArrayList<Comment> allComments = new ArrayList<Comment>();


        for (int i = 0; i < currentComment.length(); i++) {
            JSONObject explrObject = currentComment.getJSONObject(i);
            String words = explrObject.getString("words");
            String author = explrObject.getString("author");
            String time = explrObject.getString("time");

            Comment comment = new Comment(words, author, time);
            allComments.add(comment);
        } //end of for loop
        return allComments;
    } //end of getListingObjects()


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

}
