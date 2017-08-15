package com.sookmyung.heartbeatfornull;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Community extends AppCompatActivity{
    private WebView webView;
    private WebSettings webSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_layout);

        webView = (WebView)findViewById(R.id.webView);
        //클릭 시 새창 안뜨게
        webView.setWebViewClient(new WebViewClient());
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl("http://heartbeat.dothome.co.kr/?page_id=38");
    }
}
