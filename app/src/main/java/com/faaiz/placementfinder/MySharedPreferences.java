package com.faaiz.placementfinder;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
/*
User type - "user" , "employer"

user - "emailActivity" , "personalDetailsActivity" , "mainActivity"

employer - "emailActivity" , "mobileVerificationActivity" , "companyDetailsActivity" , "mainActivity"

 */
public class MySharedPreferences {

    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_USER_PROGRESS = "userProgress";

    private final SharedPreferences sharedPreferences;

    public MySharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public void saveUserType(String userType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_TYPE, userType);
        editor.apply();
    }

    public void saveUserProgress(String userProgress) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_PROGRESS, userProgress);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getUserType() {
        return sharedPreferences.getString(KEY_USER_TYPE, null);
    }

    public String getUserProgress() {
        return sharedPreferences.getString(KEY_USER_PROGRESS, null);
    }

    // Clear saved userId
    public void clearUserId() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_ID);
        editor.apply();
    }

    public void clearUserType() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_TYPE);
        editor.apply();
    }

    public void clearUserProgress() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_PROGRESS);
        editor.apply();
    }
}

