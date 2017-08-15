package com.sookmyung.heartbeatfornull.footprint;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

public class DownloadFootprintActivity extends AsyncTask<String, Void, FootprintResponse> {

    public DownloadFootprintActivity(DownloadFootprintActivity.Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onLoaded(List<FootprintInfo> footprintInfoList);
        void onError();
    }

    private DownloadFootprintActivity.Listener mListener;

    @Override
    protected void onPostExecute(FootprintResponse response) {

        if (response != null) {
            Log.e("onLoadedOK", "onLoadedOK~~~~~~~~~~~~~~~~~~~~");
            mListener.onLoaded(response.getResult());
        } else {
            Log.e("onLoadedError", "onLoadedError~~~~~~~~~~~~~~~~~~~~");
            mListener.onError();
        }

        super.onPreExecute();
    }

    @Override
    protected FootprintResponse doInBackground(String... params) {
        try {
            Log.e("dinInBackground", "params1"+params[0]+"params2"+params[1]);
            String stringResponse = loadJSON(params[0], params[1]);
            Gson gson = new Gson();
            return gson.fromJson(stringResponse, FootprintResponse.class);
        } catch (IOException e) {
            Log.e("****ReviewDownload","io exception");
            return null;
        } catch (Exception e) {
            Log.e("****ReviewDownload","other exception");
            return null;
        }
    }

    private String loadJSON(String fp_lat, String fp_lon) throws IOException {
        //String URL = "http://203.252.195.34/phpfile/select_footprint.php";
        String URL = "http://203.252.195.34/phpfile/get_gps_radius.php";
        String data = URLEncoder.encode("gps_lat", "UTF-8") + "=" + URLEncoder.encode(fp_lat, "UTF-8");
        data += "&" + URLEncoder.encode("gps_lon", "UTF-8") + "=" + URLEncoder.encode(fp_lon, "UTF-8");

        URL url = new URL(URL);
        URLConnection conn =  url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setDoOutput(true);
        conn.connect();



        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        String line = null;
        StringBuilder sb = new StringBuilder();

        while ((line = in.readLine()) != null) {
            sb.append(line);
            Log.i("****loadJSON", "http_ok: " + line);
        }
        wr.close();
        in.close();

        return sb.toString();
    }

}
