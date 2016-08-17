package com.example.colleenminor.airbnbapi.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.example.colleenminor.airbnbapi.AlertDialogFragment;
import com.example.colleenminor.airbnbapi.ErrorHelper;
import com.example.colleenminor.airbnbapi.R;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import butterknife.ButterKnife;

public class UpdateOrDeleteListingsActivity extends AppCompatActivity {
    public static final String Tag = UpdateOrDeleteListingsActivity.class.getSimpleName();

    private String mId;
    private String mTitle;
    private String mCity;
    private String mState;
    private String mPrice;
    private String mNoGuests;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_listings);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        mId = intent.getStringExtra("id");
        Log.v(Tag, mId);
        mTitle = intent.getStringExtra("title");
        mNoGuests = intent.getStringExtra("noGuests");
        mState = intent.getStringExtra("state");
        mCity = intent.getStringExtra("city");
        mPrice = intent.getStringExtra("price");

        final TextView titleEditText= (TextView) findViewById(R.id.titleEditText);
        titleEditText.setText(mTitle);
        //Make the title non-editable
        titleEditText.setKeyListener(null);

        final TextView noGuestsEditText = (TextView) findViewById(R.id.noGuestsEditText);
        noGuestsEditText.setText(mNoGuests);

        final TextView priceEditText = (TextView) findViewById(R.id.priceEditText);
        priceEditText.setText(mPrice);


        final TextView stateEditText = (TextView) findViewById(R.id.stateEditText);
        stateEditText.setText(mState);

        final TextView cityEditText = (TextView) findViewById(R.id.cityEditText);
        cityEditText.setText(mCity);

        BootstrapButton mSubmitChangesButton = (BootstrapButton) findViewById(R.id.submitChangesButton);

        BootstrapButton mDeleteListingButton = (BootstrapButton) findViewById(R.id.showCommentsButton);


        mDeleteListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = mId;
                String title = mTitle;
                Log.v(Tag, "UR IT!" + id);
                deleteBuilder(id, title);

            }
        });

        mSubmitChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTitle = titleEditText.getText().toString().trim();

                if(mTitle != null) {

                    mCity = cityEditText.getText().toString().trim();
                    mCity = fixIfNull(mCity);

                    mNoGuests = noGuestsEditText.getText().toString().trim();
                    mNoGuests = fixIfNull(mNoGuests);

                    mPrice = priceEditText.getText().toString().trim();
                    mPrice = fixIfNull(mPrice);

                    mState = stateEditText.getText().toString().trim();
                    mState = fixIfNull(mState);

                    updateListing(mTitle, mNoGuests, mPrice, mCity, mState);
                }
                else{
                    failMessage("You can't submit a listing without a title.");
                }
            }
        });
    }

    //To prevent null strings which stops the request from going through,
    // though I guess I could have just set them all when I declared them
    public String fixIfNull(String string) {
        if (string == null) {
            string = "";
        }
        return string;
    }

    /*
    Method to delete a listing
    This method deletes all the comments on the listing,
    but you could also send in the id instead of the name
    And keep the comments but have them have no listing id
    to just grey them out on a list.
     */
    public void deleteListing(String id, final String title){
        if (isNetworkAlailable()) {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "appid=sBQMW3SiC4&name=" + mTitle);
            Request request = new Request.Builder()
                    .url("http://52.38.127.124:3003/deleteListing")
                    .delete(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
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
                        if (response.isSuccessful()) {
                            String jsonData = response.body().string();
                            Log.v(Tag, jsonData);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(UpdateOrDeleteListingsActivity.this, DeletedListingIntermissionActivity.class);
                                    intent.putExtra("title", title);
                                    startActivity(intent);
                                }

                            });

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

    }

    /*A dialog that pops up and forces the user to type in the name of the listing they want to delete
    * in order to delete it.*/
    public void deleteBuilder(String id, String title){

        final String finalTitle = title;
        final String finalId = id;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateOrDeleteListingsActivity.this);
                builder.setTitle("WARNING: THIS CANNOT BE UNDONE");
                final EditText titleInput = new EditText(UpdateOrDeleteListingsActivity.this);
                titleInput.setInputType(InputType.TYPE_CLASS_TEXT);
                LinearLayout ll = new LinearLayout(UpdateOrDeleteListingsActivity.this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.addView(titleInput);
                builder.setView(ll);

                builder.setMessage("This listing and all of its comments will be permanently deleted. " +
                        "If you wish to proceed, please type the title of this listing in the dialog below:").
                        setPositiveButton("Delete Listing", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                String userEnteredTitle = titleInput.getText().toString();
                                if (!userEnteredTitle.equals(finalTitle)) {
                                    ErrorHelper.displayAlertDialog(UpdateOrDeleteListingsActivity.this, "The title that you entered did not " +
                                            "match this listing. Are you sure you are trying to delete the right listing?");
                                } else {
                                    deleteListing(finalId, finalTitle);
                                }
                            }


                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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


    public void updateListing(final String title, String noGuests, String priceWithCashSymbol, String city, String state) {

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        //Format the title and city for OKHttp request:
        title.replace(" ", "%20");
        city.replace(" ", "%20");
        String price= priceWithCashSymbol.replaceAll("\\$", "");
        if (isNetworkAlailable()) {
            RequestBody body = RequestBody.create(mediaType, "appid=sBQMW3SiC4&name=" + title + "&city=" + city + "&state=" +
                    state + "&noGuests=" + noGuests + "&price=" + price);
            Request request = new Request.Builder()
                    .url("http://52.38.127.124:3003/putListing")
                    .put(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "10cd7f91-f6b8-8044-a201-98d78233259a")
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        final String jsonString = response.body().string();
                        Log.v(Tag, jsonString);

                        if (response.isSuccessful()) {
                            if (jsonString.toLowerCase().contains("success")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Go to the page for this listing with the updated info
                                        Intent intent = new Intent(UpdateOrDeleteListingsActivity.this, ListingWithMapActivity.class);
                                        intent.putExtra("title", title);
                                        startActivity(intent);
                                    }
                                });

                            }
                            else {
                                //Response sent back something other than success message
                                failMessage("Unknown error");
                            }

                        } else { //Did not receive response
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
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateOrDeleteListingsActivity.this);
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

}
