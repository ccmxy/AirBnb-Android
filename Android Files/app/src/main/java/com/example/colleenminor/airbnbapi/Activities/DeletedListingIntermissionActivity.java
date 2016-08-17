package com.example.colleenminor.airbnbapi.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.colleenminor.airbnbapi.R;

import java.util.Timer;
import java.util.TimerTask;
//This is just the intermission page after the user deletes a listing.
// The deleting itself happens in UpdateListingsActivity.
public class DeletedListingIntermissionActivity extends AppCompatActivity {
    public static final String Tag = DeletedListingIntermissionActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted_listing_page);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {

                Intent intent = new Intent(DeletedListingIntermissionActivity.this, ListOfListingsActivity.class);
                startActivity(intent);
            }

        }, 1000);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

}
