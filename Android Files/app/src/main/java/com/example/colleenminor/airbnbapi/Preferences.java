package com.example.colleenminor.airbnbapi;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by colleenminor on 7/28/16.
 */
public class Preferences {
    private SharedPreferences sharedPreferences;
    private static String PREF_NAME = "prefs";

    public Preferences() {
        // Blank
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getLocationAllowed(Context context) {
        return getPrefs(context).getString("location_allowed_key", "false");
    }

    public static void setLocationAllowed(Context context) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("location_allowed_key", "true");
        editor.commit();
    }

    public static void setUsername(Context context, String username){
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("username_key", username);
        editor.commit();
    }

    public static String getUsername(Context context){
        return getPrefs(context).getString("username_key", "default");
    }

    public static void setLastLocation(Context context, String latitude, String longitude) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("latitude_key", latitude);
        editor.putString("longitude_key", longitude);
        editor.commit();
    }
    public static String getLastLat(Context context) {
        return getPrefs(context).getString("latitude_key", "default");
    }
    public static String getLastLong(Context context){
        return getPrefs(context).getString("longitude_key", "default");
    }




}

