package com.example.colleenminor.airbnbapi.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.example.colleenminor.airbnbapi.Preferences;
import com.example.colleenminor.airbnbapi.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserProfileActivity extends AppCompatActivity {
    @Bind(R.id.allListingsButton)
    BootstrapButton mAllListingsButton;

    @Bind(R.id.userListingsButton)
    BootstrapButton mUserListingsButton;

    @Bind(R.id.registerLoginPageButton)
    BootstrapButton mRegisterLoginPageButton;

    @Bind(R.id.welcomeTextView)
    TextView mWelcomeTextView;

    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        mUsername = Preferences.getUsername(this);
        mWelcomeTextView.setText("Welcome, " + mUsername + ".");


        mAllListingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, ListOfListingsActivity.class);
                startActivity(intent);
            }
        });
        mUserListingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, UserCommentsAndListingsActivity.class);
                startActivity(intent);
            }
        });
        mRegisterLoginPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, LoginRegisterActivity.class);
                startActivity(intent);
            }
        });



    }

}
