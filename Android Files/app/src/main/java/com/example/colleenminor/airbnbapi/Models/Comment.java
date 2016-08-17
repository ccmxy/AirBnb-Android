package com.example.colleenminor.airbnbapi.Models;

/**
 * Created by colleenminor on 8/10/16.
 */
public class Comment {
    private String mAuthor;
    private String mWords;
    private String mTime;

    public Comment(String author, String words, String time)
    {
        mAuthor = author;
        mWords = words;
        mTime = time;

    }

    public String getWords(){
        return mWords;
    }

    public String getAuthor(){
        return mAuthor;
    }

    public String getTime(){
        return mTime;
    }

}
