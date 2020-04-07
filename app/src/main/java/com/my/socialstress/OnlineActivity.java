package com.my.socialstress;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

public class OnlineActivity extends BaseActivity implements View.OnClickListener {
    WebView online_webview;
    ImageView back_img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e){}
        setContentView(R.layout.screen_online);

        initUI();
    }

    private void initUI() {
        back_img = findViewById(R.id.back_img);
        back_img.setOnClickListener(this);
        online_webview = findViewById(R.id.online_webview);
        String url = "https://www.betterhelp.com/helpme/?utm_source=AdWords&utm_medium=Search_PPC_c&utm_term=online+counseling+therapy_e&utm_content=50465455664&network=g&placement=&target=&matchtype=e&utm_campaign=861113656&ad_type=text&adposition=&gclid=CjwKCAjwpqv0BRABEiwA-TySwWO8ezlwxtoGBWPCi7tAobpl05ASHDBprSYXrBE8x2GA1HGj2glVfBoCY_AQAvD_BwE&not_found=1&gor=helpme";
        loadWebView(online_webview, url);
    }
    private void loadWebView(WebView webview, String url) {
        webview.setInitialScale(100);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportMultipleWindows(true); // This forces ChromeClient enabled.

        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                getWindow().setTitle(title); //Set Activity tile to page title.
            }
        });

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        webview.loadUrl(url);
    }

    //Load External web browser
    private void loadExternalWebView(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.back_img) finish();
    }
}
