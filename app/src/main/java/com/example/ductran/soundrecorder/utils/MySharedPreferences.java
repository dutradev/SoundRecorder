package com.example.ductran.soundrecorder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MySharedPreferences {
    private static String PREF_HIGH_QUALITY = "pref_high_quality";

    public static void setPrefHighQuality(Context context, boolean isEnable){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_HIGH_QUALITY, isEnable);
        editor.apply();
    }

    public static boolean getPrefHighQuality(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_HIGH_QUALITY,false);
    }
}
