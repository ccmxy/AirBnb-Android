package com.example.colleenminor.airbnbapi.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.example.colleenminor.airbnbapi.AlertDialogFragment;
import com.example.colleenminor.airbnbapi.Models.Comment;
import com.example.colleenminor.airbnbapi.Models.Listing;
import com.example.colleenminor.airbnbapi.Preferences;
import com.example.colleenminor.airbnbapi.R;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
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

public class ListingWithCommentsActivity extends AppCompatActivity {
    public static final String Tag = ListingWithCommentsActivity.class.getSimpleName();

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

    @Bind(R.id.showMapButton)
    BootstrapButton mShowMapButton;

    @Bind(R.id.commentsListView)
    ListView mCommentsListView;

    @Bind(R.id.addCommentButton)
    BootstrapButton mAddCommentButton;

    @Bind(R.id.commentText)
    EditText mCommentEditText;

    @Bind(R.id.warningTextView)
    TextView mWarningTextView;



    private Listing mListing;
    private String mUsername;

    private String mLatitude;
    private String mLongitude;
    private String mCity;
    private String mState;
    private String mAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_comments);
        ButterKnife.bind(this);
        //Get the current user's username for adding comments or hide/show update and delete buttons:
        mUsername = Preferences.getUsername(this);
        //Fill the listing header based on the title with a call to the server:
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        fillListing(title);

        mShowMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListingWithCommentsActivity.this, ListingWithMapActivity.class);
                intent.putExtra("title", mListing.getTitle());
                startActivity(intent);
            }
        });

        mUpdateListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListingWithCommentsActivity.this, UpdateOrDeleteListingsActivity.class);
                intent.putExtra("id", mListing.getId());
                intent.putExtra("title", mListing.getTitle());
                intent.putExtra("noGuests", mListing.getNoGuests());
                intent.putExtra("price", mListing.getPrice());
                intent.putExtra("city", mListing.getCity());
                intent.putExtra("state", mListing.getState());
                startActivity(intent);
            }
        });

        mAddCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAddCommentButton.isActivated()) {
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(mAddCommentButton.getWindowToken(), 0);
                    String listing_title = mListing.getTitle();
                    String words = mCommentEditText.getText().toString().trim();
                    addComment(listing_title, words);
                    mCommentEditText.setVisibility(View.GONE);
                    mShowMapButton.setVisibility(View.VISIBLE);
                    mUpdateListingButton.setVisibility(View.VISIBLE);
                    mAddCommentButton.setActivated(false);
                    mWarningTextView.setVisibility(View.GONE);
                    mCommentEditText.setText("");
                } else {
                    mWarningTextView.setText("You are commenting as " + mUsername);
                    mWarningTextView.setVisibility(View.VISIBLE);
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    mAddCommentButton.setActivated(true);
                    mCommentEditText.setVisibility(View.VISIBLE);
                    mCommentEditText.setFocusableInTouchMode(true);
                    mCommentEditText.requestFocus();
                    mShowMapButton.setVisibility(View.INVISIBLE);
                    mUpdateListingButton.setVisibility(View.INVISIBLE);

                }

            }
        });


    }

    public void addComment(String listing_name, String words) {

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        //Format for OKHttp request:
        words.replace(" ", "%20");
        listing_name.replace(" ", "%20");
        final String title = listing_name;

        if (isNetworkAlailable()) {

            RequestBody body = RequestBody.create(mediaType, "appid=R18sJNEyOj&listing_name=" + listing_name + "&words=" +
                    words + "&author=" + mUsername);
            Request request = new Request.Builder()
                    .url("http://52.38.127.124:3003/postCommentUser")
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "10cd7f91-f6b8-8044-a201-98d78233259a")
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
                        final String jsonString = response.body().string();
                        Log.v(Tag, jsonString);

                        if (response.isSuccessful()) {
                            if (jsonString.toLowerCase().contains("success")) {
                                //Posting successful
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    //Could also use this instead of making title final:
                                    // String title = mListing.getTitle();
                                        fillListing(title);

                                    }
                                });
                            }

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(Tag, "Exception caught: ", e);
                    }

                }
            });
        } else {
            Toast.makeText(this, "Network is not available", Toast.LENGTH_LONG).show();
        }
        Log.d(Tag, "Main UI code is running!");
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

    private Listing getListingObject(String jsonData) throws JSONException {


        JSONArray currentListing = new JSONArray(jsonData);
        JSONObject explrObject = currentListing.getJSONObject(0);
        String title = explrObject.getString("name");

        String author = explrObject.getString("author");
        mAuthor = author;
        String noGuests;
        String price;
        String city;
        String state;
        JSONArray commentsJSON = explrObject.getJSONArray("comments");
        final List<Comment> commentsList = getComments(commentsJSON);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCommmentsListView(commentsList);
            }
        });


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
            } else {
                state = "";
            }
        } //end of if location exists loop
        else {
            city = "";
            state = "";
        }
        Listing listing = new Listing(title, noGuests, price, city, state, author);
        return listing;
    } //end of getListingObjects()


    private ArrayList<Comment> getComments(JSONArray currentComment) throws JSONException {
        //JSONArray currentComment = new JSONArray(jsonData);
        ArrayList<Comment> listingComments = new ArrayList<Comment>();

        for (int i = 0; i < currentComment.length(); i++) {
            JSONObject explrObject = currentComment.getJSONObject(i);
            String words = explrObject.getString("words");
            String author = explrObject.getString("author");
            String time = explrObject.getString("time");
            Comment comment = new Comment(author, words, time);

            listingComments.add(comment);


        } //end of for loop
        return listingComments;
    } //end of getCommentsObjects()


//Prepare list of Java maps to set list view with:
    public void setCommmentsListView(List<Comment> commentsList){

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

        for (Comment comment : commentsList) {
            String author = "        -" + comment.getAuthor();
            String words = comment.getWords();
            Map<String, String> datum = new HashMap<String, String>(3);
            datum.put("Author", author);
            datum.put("Words", words);
            data.add(datum);
           setThatList(mCommentsListView, data);

        }

}
    //Set the list view:
    public void setThatList(ListView listView,  List<Map<String, String>> data) {
        SimpleAdapter simpleAdapter = new SimpleAdapter(
                this,
                data,
                android.R.layout.simple_list_item_2,
                new String[] {"Words", "Author"},
                new int[] {android.R.id.text1, android.R.id.text2, android.R.id.custom});
        listView.setAdapter(simpleAdapter);
    }
//Just for setting the header area with the list info
    private void updateView() {
        mTitle.setText(mListing.getTitle());
        mNoGuests.setText(mListing.getNoGuests());
        mPrice.setText(mListing.getPrice());
        Log.v(Tag, mListing.getCity());
        Log.v(Tag, mListing.getState());
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
        if(mUsername.equals(mAuthor)){
            mUpdateListingButton.setVisibility(View.VISIBLE);
        }

    }
//Check if network avaliable:
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