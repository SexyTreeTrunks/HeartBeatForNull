package com.sookmyung.heartbeatfornull.searchpath;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Yoon min on 2017-07-27.
 */

public class HttpMgrThread extends Thread {

    public HttpMgrThread() {

    }

    public void run() {
        reqHttp();
    }

    public void reqHttp() {
        URL url = null;
        try
        {
            url = new URL("http://www.naver.com");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();
        }
        catch (MalformedURLException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
