package com.my.socialstress.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

public class Global {
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    public static final String PREF_NAME = "socialstress";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String SOCIALTYPE = "social_type";
    public static final String COUNT = "count";
    public static final String TOKEN = "token";
    public static final String SHARED_IMEI = "shared_imei";
    public static final int REQUEST_WRITE_STORAGE_REQUEST_CODE = 101;
    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 102;
    public static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 103;


    public static void showAlert(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Warning!")
                .setMessage(msg)
                .setNeutralButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public static void showAlertWithTitle(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(msg)
                .setNeutralButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void hideSystemUI(View mainView) {
        mainView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public static void showSystemUI(View mainView) {
        mainView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}