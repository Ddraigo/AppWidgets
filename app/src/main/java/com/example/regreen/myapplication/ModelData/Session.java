package com.example.regreen.myapplication.ModelData;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Session {

    private SharedPreferences prefs;

    public Session(Context cntx) {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setUserName(String usename) {
        prefs.edit().putString("usename", usename).commit();
    }

    public String getUserName() {
        String usename = prefs.getString("usename","");
        return usename;
    }

    public void setEmail(String email) {
        prefs.edit().putString("email", email).commit();
    }

    public String getEmail() {
        return prefs.getString("email", "");
    }
}
