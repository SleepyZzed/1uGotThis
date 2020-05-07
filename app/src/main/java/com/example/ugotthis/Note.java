package com.example.ugotthis;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Note implements Serializable {
    private String title;
    private String description;
    private int priority;
    private String uid;
    private String imgurl;
    private String timeStamp;
    @Exclude private String noteid;

    public Note(){

        //Empty needed
    }


    public Note(String title, String description, int priority, String userId, String imgurl, String timeStamp)
    {


        this.title = title;
        this.description = description;
        this.priority = priority;
        this.uid = userId;
        this.imgurl = imgurl;
        this.timeStamp = timeStamp;
    }


    public String getNoteid() {
        return noteid;
    }

    public void setNoteid(String noteid) {
        this.noteid = noteid;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public int getPriority()
    {
        return priority;
    }
    public String getUid()
    {
        return uid;
    }

   public String getImgurl()
    {
       return imgurl;
    }

    public String getTimeStamp() {return timeStamp;}
}
