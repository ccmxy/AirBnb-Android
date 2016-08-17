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
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapSize;
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

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginRegisterActivity extends AppCompatActivity {
    private static final String Tag = LoginRegisterActivity.class.getSimpleName();

    @Bind(R.id.usernameEditText)
    BootstrapEditText mUsernameEditText;

    @Bind(R.id.passwordEditText)
    BootstrapEditText mPasswordEditText;

    @Bind(R.id.emailEditText)
    BootstrapEditText mEmailEditText;

    @Bind(R.id.logInButton)
    BootstrapButton mLoginButton;

    @Bind(R.id.goButton)
    BootstrapButton mGoButton;

    @Bind(R.id.signUpButton)
    BootstrapButton mSignupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        ButterKnife.bind(this);
        mLoginButton.setActivated(false);
        mSignupButton.setActivated(false);

        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Register was pressed:
                if (mSignupButton.isActivated()) {
                    String username = mUsernameEditText.getText().toString().trim();
                    String password = mPasswordEditText.getText().toString().trim();
                    String email = mEmailEditText.getText().toString().trim();
                    addUser(username, password, email);

                }
                //Log in was pressed
                if (mLoginButton.isActivated()){
                    String username = mUsernameEditText.getText().toString().trim();
                    String password = mPasswordEditText.getText().toString().trim();
                    logUserIn(username, password);
                }

            }
        });

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignupButton.setActivated(true);
                mLoginButton.setActivated(false);
                mUsernameEditText.setVisibility(View.VISIBLE);
                mPasswordEditText.setVisibility(View.VISIBLE);
                mEmailEditText.setVisibility(View.VISIBLE);
                mGoButton.setVisibility(View.VISIBLE);
                mLoginButton.setBootstrapBrand(DefaultBootstrapBrand.REGULAR);
                mSignupButton.setBootstrapBrand(DefaultBootstrapBrand.PRIMARY);
                mLoginButton.setBootstrapSize(DefaultBootstrapSize.MD);
                mSignupButton.setBootstrapSize(DefaultBootstrapSize.LG);
            }

        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginButton.setActivated(true);
                mSignupButton.setActivated(false);
                mUsernameEditText.setVisibility(View.VISIBLE);
                mPasswordEditText.setVisibility(View.VISIBLE);
                mEmailEditText.setVisibility(View.GONE);
                mGoButton.setVisibility(View.VISIBLE);
                mLoginButton.setBootstrapBrand(DefaultBootstrapBrand.PRIMARY);
                mSignupButton.setBootstrapBrand(DefaultBootstrapBrand.REGULAR);
                mLoginButton.setBootstrapSize(DefaultBootstrapSize.LG);
                mSignupButton.setBootstrapSize(DefaultBootstrapSize.MD);
            }
        });
    } //End of onCreate


    /*
    Method to add a new user.
     */
    public void addUser(String username, String password, String email) {

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        //Format the title and city for OKHttp request:
        username.replace(" ", "%20");
        final String usernameFinal = username;
        final Context mContext = this;
        if (isNetworkAlailable()) {

            RequestBody body = RequestBody.create(mediaType, "username=" + username + "&password="
                            + password + "&email=" + email);

            Request request = new Request.Builder()
                    .url("http://52.38.127.124:3003/addUser")
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
                                        Preferences.setUsername(mContext, usernameFinal);
                                        Toast.makeText(LoginRegisterActivity.this, "Thank you for registering!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginRegisterActivity.this, UserProfileActivity.class);

                                        startActivity(intent);
                                    }
                                });
                            }
                            else if (jsonString.toLowerCase().contains("duplicate key error index: colleen.users.$username_1")) {
                                //Duplicate listing title
                                failMessage("That username is already taken. Try another!");

                            } else if (jsonString.toLowerCase().contains("duplicate key error index: colleen.users.$email_1")) {
                                //User didn't include title
                                failMessage("That email is already registered.");

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

    public void logUserIn(String username, String password) {
        final String usernameFinal = username;
        final Context mContext = this;
        if (isNetworkAlailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://52.38.127.124:3003/getUserAuthed/?username=" + username + "&password=" + password)
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
                            if (jsonData.toLowerCase().contains("success")) {
                                //Log in success
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Preferences.setUsername(mContext, usernameFinal);
                                       Intent intent = new Intent(LoginRegisterActivity.this, UserProfileActivity.class);
                                        startActivity(intent);
                                    }
                                });
                            }
                            if (jsonData.toLowerCase().contains("password bad")) {
                                failMessage("That was not the correct password!");
                            }
                            if (jsonData.toLowerCase().contains("user not found")) {
                                failMessage("That username was not found in our system!");
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
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginRegisterActivity.this);
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

}//end of class
