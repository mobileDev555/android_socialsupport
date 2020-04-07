package com.my.socialstress;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.my.socialstress.utils.Global;
import static com.my.socialstress.utils.Global.PREF_NAME;
import static com.my.socialstress.utils.Global.editor;

public class SplashActivity extends AppCompatActivity {

    private View mainView;
    private ImageView img_load;
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.screen_splash);

        mainView = findViewById(R.id.view);
        Global.hideSystemUI(mainView);

        img_load = findViewById(R.id.img_loading);
        Glide.with(this).load(R.drawable.load).into(img_load);

        Global.pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        editor = Global.pref.edit();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String email = Global.pref.getString(Global.EMAIL, "");
                if(email.equals("")) {
                    Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                }
                finish();
            }
        }, SPLASH_TIME_OUT);


    }


}

