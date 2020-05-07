package com.example.ugotthis;

public class Users {
    private String email;


    private String uid;

    public Users(){

        //Empty needed
    }


    public Users(String email, String userId)
    {
        this.email = email;

        this.uid = userId;
    }

    public String getEmail() {
        return email;
    }


    public String getUid() {
        return uid;
    }
}
