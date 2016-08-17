package com.example.colleenminor.airbnbapi.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.example.colleenminor.airbnbapi.AlertDialogFragment;
import com.example.colleenminor.airbnbapi.Preferences;
import com.example.colleenminor.airbnbapi.R;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewListingActivity extends AppCompatActivity {
    public static final String Tag = NewListingActivity.class.getSimpleName();
    @Bind(R.id.submitButton)
    BootstrapButton mSubmitButton;

    @Bind(R.id.titleEditText)
    BootstrapEditText mTitleEditText;

    @Bind(R.id.priceEditText)
    BootstrapEditText mNoGuestsEditText;

    @Bind(R.id.noGuestsEditText)
    BootstrapEditText mPriceEditText;

    @Bind(R.id.cityEditText)
    BootstrapEditText mCityEditText;

    @Bind(R.id.stateEditText)
    BootstrapEditText mStateEditText;

    @Bind(R.id.postListingSuccessTextView)
    TextView mPostListingSuccessTextView;

    private String mTitle;
    private String mNoGuests;
    private String mPrice;
    private String mCity;
    private String mState;
    private String mAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_listing);
        ButterKnife.bind(this);

        mAuthor = Preferences.getUsername(this);


        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTitle = mTitleEditText.getText().toString().trim();

                mCity = mCityEditText.getText().toString().trim();
                mCity = fixIfNull(mCity);

                mNoGuests = mNoGuestsEditText.getText().toString().trim();
                mNoGuests = fixIfNull(mNoGuests);

                mPrice = mPriceEditText.getText().toString().trim();
                mPrice = fixIfNull(mPrice);

                mState = mStateEditText.getText().toString().trim();
                mState = fixIfNull(mState);

                addListing(mTitle, mNoGuests, mPrice, mCity, mState, mAuthor);
            }
        });


    }

    public String fixIfNull(String string) {
        if (string == null) {
            string = "";
        }
        return string;
    }

    public void addListing(String title, String noGuests, String price, String city, String state, String author) {

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        //Format the title and city and author for OKHttp request:
        title.replace(" ", "%20");
        city.replace(" ", "%20");
        author.replace(" ", "%20");
        if (isNetworkAlailable()) {

            RequestBody body = RequestBody.create(mediaType, "appid=sBQMW3SiC4&name=" + title + "&city=" + city + "&state=" +
                    state + "&noGuests=" + noGuests + "&price=" + price + "&author=" + author);
            Request request = new Request.Builder()
                    .url("http://52.38.127.124:3003/postListing")
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
                                        postListingSuccess();
                                    }
                                });
                            } else if (jsonString.toLowerCase().contains("dup key")) {
                                //Duplicate listing title
                                failMessage("Another listing has that title. Try again with a different title!");

                            } else if (jsonString.toLowerCase().contains("`name` is required")) {
                                //User didn't include title
                                failMessage("You need to give your listing a title.");

                            } else {
                                //other error that prevented listing from being posted
                                failMessage("Unknown error");
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
    } //End of addListing

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
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

    public void failMessage(String message){
        final String finalMessage = message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewListingActivity.this);
                builder.setTitle("Ruh roh!");
                builder.setMessage(finalMessage).
                        setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void postListingSuccess() {
        mSubmitButton.setVisibility(View.GONE);
        mTitleEditText.setVisibility(View.GONE);
        mPriceEditText.setVisibility(View.GONE);
        mNoGuestsEditText.setVisibility(View.GONE);
        mCityEditText.setVisibility(View.GONE);
        mStateEditText.setVisibility(View.GONE);
        mPostListingSuccessTextView.setVisibility(View.VISIBLE);
        //Hide the keyboard:
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(mSubmitButton.getWindowToken(), 0);
        //Redirect after 2 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {

                Intent intent = new Intent(NewListingActivity.this, ListOfListingsActivity.class);
                startActivity(intent);
            }

        }, 1000);


    }
}
