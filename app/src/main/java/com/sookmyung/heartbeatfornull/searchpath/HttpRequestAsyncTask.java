package com.sookmyung.heartbeatfornull.searchpath;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Yoon min on 2017-07-27.
 */

public class HttpRequestAsyncTask extends AsyncTask<Double, String, Void> {

    final int TIME_OUT  = 20;
    final String API_KEY = "&key=AIzaSyD3hFpHBVC8pDwH8mf3XzPc5yRu0LB5gf8";
    final String GOOGLE_URL = "https://maps.googleapis.com/maps/api/directions/";

    final String format = "json?";
    final String mode = "&mode=transit";

    double startLat;
    double startLon;

    double endLat;
    double endLon;

    String urlStr = null;
    URL url = null;
    String teststr  = null;

    HttpURLConnection conn = null;

    @Override
    protected Void doInBackground(Double... params) {

        startLat = params[0];
        startLon = params[1];

        endLat = params[2];
        endLon = params[3];

        String startX = String.format("%.6f", startLat);
        String startY = String.format("%.6f", startLon);

        String endX = String.format("%.6f", endLat);
        String endY = String.format("%.6f", endLon);

        String origin = "origin=" + startX + "," + startY;
        String destination = "&destination=" + endX + "," + endY;

        try {
            urlStr = GOOGLE_URL + format + origin + destination + mode + API_KEY;
            url = new URL(urlStr);

            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIME_OUT * 1000);
            conn.setReadTimeout(TIME_OUT * 1000);
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String stream = null;

                while ((stream = br.readLine()) != null) {
                    teststr += br.readLine();
                    Log.i("***stream", stream);
                }
                if(stream == null){
                    Log.e("***stream","null");
                    Log.e("***location0",Double.toString(startLat));
                    Log.e("***location1",Double.toString(startLon));
                    Log.e("***location2",Double.toString(endLat));
                    Log.e("***location3",Double.toString(endLon));
                }


                br.close();

            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                Log.e("***httpConnectionerror","responseCode");
            }

    }catch (Exception e) {
            e.printStackTrace();
            Log.e("***Exception", "er= ", e);
        }

        return null;
    }
}
