package com.sookmyung.heartbeatfornull;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class Community extends AppCompatActivity{
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_layout);

        webView = (WebView)findViewById(R.id.webView);
        webView.loadUrl("http://heartbeat.dothome.co.kr/?page_id=38");
    }
}
