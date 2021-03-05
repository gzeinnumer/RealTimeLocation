package com.gzeinnumer.realtimelocation.data;

import android.content.Context;
import android.content.SharedPreferences;

public class LocationPref {
    public static final String PREFNAME = "APP";
    public static final String LA = "LATITUDE";
    public static final String LG = "LONGITUDE";
    public static final String ADDRESS = "ADDRESS";
    private final Context context;

    public LocationPref(Context context) {
        this.context = context;
    }

    public void saveString(String key, String value) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;

        sharedPreferences = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getValue(String key) {
        SharedPreferences sharedPreferences;
        String value;
        sharedPreferences = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        value = sharedPreferences.getString(key, "");
        return value;
    }

}
