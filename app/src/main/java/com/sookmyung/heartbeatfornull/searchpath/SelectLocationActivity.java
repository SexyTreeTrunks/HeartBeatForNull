package com.sookmyung.heartbeatfornull.searchpath;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sookmyung.heartbeatfornull.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jsm95 on 2017-07-24.
 */

public class SelectLocationActivity extends AppCompatActivity implements GoogleMap.OnMarkerDragListener {
    double slat;
    double slon;
    double elat;
    double elon;
    LatLng ROUTE;

    private GoogleMap mMap;
    private HttpRequestAsyncTask httpRequestAsyncTask;
    private List<List<HashMap<String, String>>> wayPointsList ;

    String strStartloc = null;
    String strEndloc = null;

    String testStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectlocations);

        Button button_endlocation = (Button)findViewById(R.id.button_endlocation); //goto 3D

        final EditText editText_location = (EditText)findViewById(R.id.editText_location); //출발지 입력박스 문구
        final EditText editText_endlocation = (EditText)findViewById(R.id.editText_endlocation); //도착지 입력박스 문구

        editText_location.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(SelectLocationActivity.this, PlacePredictionActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        editText_endlocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(SelectLocationActivity.this, PlacePredictionActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        button_endlocation.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Intent intent = new Intent(SelectLocationActivity.this, ResultMapFromSearch.class);
                intent.putExtra("startlat", slat);
                intent.putExtra("startlon", slon);
                intent.putExtra("endlat", elat);
                intent.putExtra("endlon", elon);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        final EditText editText_location = (EditText)findViewById(R.id.editText_location);
        final EditText editText_endlocation = (EditText)findViewById(R.id.editText_endlocation);

        List<Address> list = null;

        String result_txt = null;

        final Geocoder geocoder = new Geocoder(SelectLocationActivity.this);

        //위도 경도 표시용 txt
        final TextView tv = (TextView)findViewById(R.id.textView);
        final TextView endtv = (TextView)findViewById(R.id.textView_endloc);

        if(resultCode==RESULT_OK) // 액티비티가 정상적으로 종료되었을 경우
        {
            if(requestCode==1)
                {
                    list = null;
                    result_txt = data.getStringExtra("place");
                    editText_location.setText(result_txt);
                    try{
                        list = geocoder.getFromLocationName(result_txt, 10);
                    } catch (IOException e){
                        e.printStackTrace();
                    }

                    if(list != null) {
                        if (list.size() == 0) {
                            Toast.makeText(SelectLocationActivity.this,"해당 주소 없음", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Address addr = list.get(0);
                            double lat = addr.getLatitude();
                            double lon = addr.getLongitude();

                            slat = Double.parseDouble(String.format("%.6f",lat));
                            slon = Double.parseDouble(String.format("%.6f",lon));

                            tv.setText(Double.toString(slat) +", " +Double.toString(slon));
                    }
                }

            }
            else if(requestCode==2)
            {
                list = null;
                result_txt = data.getStringExtra("place");
                editText_endlocation.setText(result_txt);
                try{
                    list = geocoder.getFromLocationName(result_txt, 10);
                } catch (IOException e){
                    e.printStackTrace();
                }
                if(list != null) {
                    if (list.size() == 0) {
                        Toast.makeText(SelectLocationActivity.this,"해당 주소 없음", Toast.LENGTH_LONG).show();
                    } else {
                        Address addr = list.get(0);
                        double lat = addr.getLatitude();
                        double lon = addr.getLongitude();

                        String str_lat = String.format("%.6f",lat);
                        elat = Double.parseDouble(str_lat);

                        String str_lon = String.format("%.6f",lon);
                        elon = Double.parseDouble(str_lon);

                        endtv.setText(Double.toString(elat) +", " +Double.toString(elon));

                    }
                    try {

                        httpRequestAsyncTask = new HttpRequestAsyncTask(){
                            //doinbackground함수가 다 실행되고 자동으로 실행되는 함수임!
                            @Override
                            protected void onPostExecute(List<List<HashMap<String, String>>> result) {
                                //경로정보 전역변수에 저장
                                wayPointsList = result;
                                //2d지도에 폴리라인 그리깅
                                set2dMapView();
                            }
                        };
                        httpRequestAsyncTask.execute(slat, slon, elat, elon);
                    }
                    catch(Exception e){
                        Log.e("***AsynchTask오류","mapactivity85");
                    }
                }
            }
        }
    }

    private void set2dMapView() {
        //2D map setting
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.route_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                ROUTE = new LatLng(slat, slon);
                googleMap.setOnMarkerDragListener(SelectLocationActivity.this);
                // Creates a draggable marker. Long press to drag.
                googleMap.moveCamera((CameraUpdateFactory.newLatLng(ROUTE)));

                mMap = googleMap;

                drawMarkerOn2dMap();
                drawPathLineOn2dMap();
            }
        });
    }

    private void drawMarkerOn2dMap() {
        //시작, 출발점 마커를 그려용
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng startLatLng = new LatLng(slat,slon);
        markerOptions.position(startLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_mark));
        mMap.addMarker(markerOptions);

        markerOptions = new MarkerOptions();
        LatLng endLatLng = new LatLng(elat, elon);
        markerOptions.position(endLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_mark));
        mMap.addMarker(markerOptions);
    }

    private void drawPathLineOn2dMap() {
        ArrayList points = null;
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();

        for (int i = 0; i < wayPointsList.size(); i++) {
            points = new ArrayList();
            lineOptions = new PolylineOptions();

            List<HashMap<String, String>> path = wayPointsList.get(i);

            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(12);
            lineOptions.color(Color.RED);
            lineOptions.geodesic(true);
        }

        // Drawing polyline in the Google Map for the i-th route
        mMap.addPolyline(lineOptions);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putParcelable(MARKER_POSITION_KEY, mMarker.getPosition());
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        //mStreetViewPanorama.setPosition(marker.getPosition(), 150);

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }
}