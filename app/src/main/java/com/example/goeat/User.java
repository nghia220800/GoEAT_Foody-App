package com.example.goeat;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.goeat.auth.Auth;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class User {
    String uid;
    String username;
    String email;
    String gender;
    long dateCreated;

    public User() {
        this.uid = "";
        this.username = "";
        this.email = "";
        this.gender = "";
        this.dateCreated = (new Date()).getTime();
    }

    public User(String username, String email, String gender) {
        this.uid = "";
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.dateCreated = (new Date()).getTime();
    }

    private User(String username, String email, String gender, long dateCreated) {
        this.uid = "";
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.dateCreated = dateCreated;
    }
    private User(String username, String email, String gender, long dateCreated,String uid) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.dateCreated = dateCreated;
    }

    public void setUid(String uid, Auth.UserUIDSetter setter) {
        if (setter != null) {
            this.uid = uid;
        }
    }

    @Exclude
    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", dateCreated=" + dateCreated +
                '}';
    }
    public User copy(){
        return new User(username, email, gender, dateCreated,uid);
    }
}