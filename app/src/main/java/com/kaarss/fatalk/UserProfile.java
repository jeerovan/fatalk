package com.kaarss.fatalk;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "profiles")
public class UserProfile {
    @PrimaryKey
    @NonNull
    private String userId;
    private String userName;
    private String country;
    private int userAge;
    private int userGender;
    private int dpVersion; // trigger download if changes
    private String bio = "";
    private long bioChangedAt;
    private long interactedAt = 0;

    public UserProfile(@NonNull String userId, String userName, String country, int userAge, int userGender, int dpVersion,String bio,long bioChangedAt) {
        this.userId = userId;
        this.userName = userName;
        this.country = country;
        this.userAge = userAge;
        this.userGender = userGender;
        this.dpVersion = dpVersion;
        this.bio = bio;
        this.bioChangedAt = bioChangedAt;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }
    public void setBioChangedAt(long bioChangedAt){this.bioChangedAt = bioChangedAt;}
    public void setInteractedAt(long interactedAt){this.interactedAt = interactedAt;}

    @NonNull
    public String getUserId(){
        return userId;
    }
    public String getUserName(){
        return userName;
    }
    public String getCountry() {
        return country;
    }

    public int getUserAge() {
        return userAge;
    }

    public int getUserGender() {
        return userGender;
    }

    public int getDpVersion() {
        return dpVersion;
    }

    public String getBio() {
        return bio;
    }

    public long getBioChangedAt(){return bioChangedAt;}

    public long getInteractedAt(){return interactedAt;}
}
