package com.sookmyung.heartbeatfornull.searchpath;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Yoon min on 2017-07-27 & modified & 깔짝 by SexyTreetrunks
 */

public class HttpRequestAsyncTask extends AsyncTask<Double, String, List<List<HashMap<String, String>>>> {

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
    String streamStr = null;

    HttpURLConnection conn = null;

    List<List<HashMap<String, String>>> routes = null;

    @Override
    protected List<List<HashMap<String, String>>> doInBackground(Double... params) {

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

                //json데이터 string형태로 받아옴
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String stream = null;

                StringBuffer sb = new StringBuffer();
                while ((stream = br.readLine()) != null) {
                    //testStr += br.readLine();
                    sb.append(stream);
                    //Log.i("***stream", stream);
                }
                br.close();

                streamStr = sb.toString();
                //json파싱&위도경도 데이터 추출하여 list에 저장
                JSONObject jObject = new JSONObject(streamStr);
                routes = parse(jObject);

            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                Log.e("***httpConnectionerror","responseCode");
            }

    }catch (Exception e) {
            e.printStackTrace();
            Log.e("***Exception", "er= ", e);
        }

        return routes; //needed to be fixed!
    }


    private List<List<HashMap<String,String>>> parse(JSONObject jObject){//JSON 파싱 함수

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List list = decodePoly(polyline);

                        /** Traversing all points */
                        for(int l=0;l <list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }

        return routes;
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
