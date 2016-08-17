package com.example.colleenminor.airbnbapi.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Window;

import com.example.colleenminor.airbnbapi.R;
import com.yayandroid.locationmanager.LocationBaseActivity;
import com.yayandroid.locationmanager.LocationConfiguration;
import com.yayandroid.locationmanager.LocationManager;
import com.yayandroid.locationmanager.constants.LogType;
import com.yayandroid.locationmanager.constants.ProviderType;

/********** This class uses the gradle package com.yayandroid:LocationManager to get
 * the user's latitude and longitude location values
 * from their GPS or Network.*******
 *******************************/
public class UserLatLongActivity extends LocationBaseActivity {
    private static final String Tag = UserLatLongActivity.class.getSimpleName();


    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_lat_long);
        LocationManager.setLogType(LogType.GENERAL);
        getLocation();

    }

    @Override
    public LocationConfiguration getLocationConfiguration() {
        return new LocationConfiguration()
                .keepTracking(true)
                .askForGooglePlayServices(true)
                .setMinAccuracy(200.0f)
                .setWaitPeriod(ProviderType.GOOGLE_PLAY_SERVICES, 5 * 1000)
                .setWaitPeriod(ProviderType.GPS, 10 * 1000)
                .setWaitPeriod(ProviderType.NETWORK, 5 * 1000)
                .setGPSMessage("Would you mind turning GPS on?")
                .setRationalMessage("Please give permission");
    }

    @Override
    public void onLocationChanged(Location location) {
        dismissProgress();
        setText(location);
    }

    @Override
    public void onLocationFailed(int failType) {
        dismissProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getLocationManager().isWaitingForLocation()
                && !getLocationManager().isAnyDialogShowing()) {
            displayProgress();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        dismissProgress();
    }

    private void displayProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.getWindow().addFlags(Window.FEATURE_NO_TITLE);
            progressDialog.setMessage("Getting location...");
        }

        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void setText(Location location) {

         Intent intent = new Intent(UserLatLongActivity.this, ListOfListingsActivity.class);
        intent.putExtra("narrowBy", "state");
        intent.putExtra("latitude", location.getLatitude() + "");
        intent.putExtra("longitude", location.getLongitude() + "");
        startActivity(intent);

    }

}


