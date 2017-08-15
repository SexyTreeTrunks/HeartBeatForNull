package com.sookmyung.heartbeatfornull.footprint;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class UploadFootprintActivity  extends AsyncTask<String, Void, String> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //loading = ProgressDialog.show(getApplicationContext(), "Please Wait", null, true, true);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        //loading.dismiss();
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            Log.i("****DataUpload", "user의 review data upload");
            String user_id = (String) params[0];
            String fp_contents = (String) params[1];
            String fp_lat = (String) params[2];
            String fp_lon = (String) params[3];
            String URL = "http://203.252.195.34/phpfile/insert_footprint.php";
            String data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_id, "UTF-8");
            data += "&" + URLEncoder.encode("fp_contents", "UTF-8") + "=" + URLEncoder.encode(fp_contents, "UTF-8");
            data += "&" + URLEncoder.encode("fp_lat", "UTF-8") + "=" + URLEncoder.encode(fp_lat, "UTF-8");
            data += "&" + URLEncoder.encode("fp_lon", "UTF-8") + "=" + URLEncoder.encode(fp_lon, "UTF-8");

            URL url = new URL(URL);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            Log.i("****doInBackground", user_id + "," + fp_contents + "," + fp_lat + "," + fp_lon);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write(data);
            wr.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                Log.d("****DataUpload", "line - " + line);
                break;
            }
            wr.close();
            reader.close();
            return sb.toString();

        } catch (UnsupportedEncodingException e) {
            Log.e("****encoding error", "");
            return null;
        } catch (MalformedURLException e) {
            Log.e("****URL 형식 에러", "");
            return null;
        } catch (IOException e) {
            Log.e("****IO error", "");
            return null;
        } catch (Exception ex) {
            Log.e("****error", "");
            return null;
        }
    }
}
