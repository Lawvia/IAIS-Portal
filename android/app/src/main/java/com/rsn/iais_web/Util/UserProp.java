package com.rsn.iais_web.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

public class UserProp {

    @Nullable
    public static String getUserId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String user_id = preferences.getString("user_id",   null);

        return user_id;
    }

    public static void saveUserId(Context context, String user_id) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = preferences.edit();

        prefEditor.putString("user_id", user_id);
        prefEditor.commit();
    }

    @Nullable
    public static String getToken(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String token = preferences.getString("token",   null);

        return token;
    }

    public static void saveToken(Context context, String token) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = preferences.edit();

        prefEditor.putString("token", token);
        prefEditor.commit();
    }

    @Nullable
    public static String getFcmToken(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String token = preferences.getString("fcm_token",   null);

        return token;
    }

    public static void saveFcmToken(Context context, String token) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = preferences.edit();

        prefEditor.putString("fcm_token", token);
        prefEditor.commit();
    }

    public static void saveFormType(Context context, int form) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = preferences.edit();

        prefEditor.putInt("form", form);
        prefEditor.commit();
    }
}
