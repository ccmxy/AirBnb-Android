package com.example.colleenminor.airbnbapi.Models;


/**
 * Created by colleenminor on 7/28/16.
 */
public class Listing {
    private String mTitle;
    private String mNoGuests = "";
    private String mPrice = "";
    private String mCity = "";
    private String mState = "";
    private String mId = "";
    private String mAuthor = "anon";
    //Constructor
//    public Listing(String id, String title, String noGuests, String price, String city, String state, String author)
//    {
//        mTitle = title;
//        mNoGuests = noGuests;
//        mPrice = price;
//        mCity = city;
//        mState = state;
//        mId = id;
//        mAuthor = author;
//    }

    public Listing(String title, String noGuests, String price, String city, String state, String author, String id)
    {
        mTitle = title;
        mNoGuests = noGuests;
        mPrice = price;
        mCity = city;
        mState = state;
        mAuthor = author;
        mId = id;
    }


    public Listing(String title, String noGuests, String price, String city, String state, String author)
    {
        mTitle = title;
        mNoGuests = noGuests;
        mPrice = price;
        mCity = city;
        mState = state;
        mAuthor = author;
    }



    public String getId () {return mId;}
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getNoGuests() {
        return mNoGuests;
    }

    public void setNoGuests(String mNoGuests) {
        this.mNoGuests = mNoGuests;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String mPrice) {
        this.mPrice = mPrice;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String mCity) {
        this.mCity = mCity;
    }

    public String getState() {
        return mState;
    }

    public void setState(String mState) {
        this.mState = mState;
    }

}

