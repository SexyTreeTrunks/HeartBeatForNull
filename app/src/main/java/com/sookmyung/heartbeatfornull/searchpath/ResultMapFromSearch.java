package com.sookmyung.heartbeatfornull.searchpath;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.sookmyung.heartbeatfornull.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.sookmyung.heartbeatfornull.R.id.map;

public class ResultMapFromSearch extends AppCompatActivity implements SensorEventListener, GoogleMap.OnMarkerDragListener, StreetViewPanorama.OnStreetViewPanoramaChangeListener {

    private static final int PAN_BY = 50; //좌우사방팔방 각도, 0(북) 90(동) 180(남) 270(서)
    private static final float ZOOM_BY = 0.5f;
    private static final int TILT_BY = 0; //각도, -90(위) ~ 90(아래) 값 가짐

    double startlat =0;
    double startlon=0;

    double endlat =0;
    double endlon=0;

    LatLng SAN_FRAN;

    private int bearing;
    private int tilt;

    private Switch switchWalk;
    private final Handler handler = new Handler();
    private TimerTask timerTask;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SupportStreetViewPanoramaFragment streetViewPanoramaFragment;
    private StreetViewPanorama mStreetViewPanorama;
    private StreetViewPanoramaCamera camera;
    private GoogleMap mMap;

    private HttpRequestAsyncTask httpRequestAsyncTask;
    private List<List<HashMap<String, String>>> wayPointsList;
    //
    LatLng postion;
    int i = 0, j = 0;
    boolean isEndOfPath = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_path_layout);
        Intent map_intent = getIntent();

        switchWalk = (Switch) findViewById(R.id.switchPathWalk);
        switchWalk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Timer timer = new Timer();
                if(isChecked) {
                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            walk();
                        }
                    };
                    timer.schedule(timerTask, 0, 3000);
                    if(isEndOfPath) {
                        timer.cancel();
                        timerTask.cancel();
                        isEndOfPath = false;
                    }
                } else {
                    timer.cancel();
                    timerTask.cancel();
                }
            }
        });

        //임시 춟발지: 숙명여자대학교
        startlat = map_intent.getDoubleExtra("startlat", 37.545062);
        startlon = map_intent.getDoubleExtra("startlon", 126.964184);

        //임시 도착지: 걸포공원
        endlat = map_intent.getDoubleExtra("endlat",37.635956);
        endlon = map_intent.getDoubleExtra("endlon",126.704334);

        SAN_FRAN = new LatLng(startlat, startlon);

        try {
            Log.e("***loc1", Double.toString(startlat));
            Log.e("***loc2", Double.toString(startlon));
            Log.e("***loc3", Double.toString(endlat));
            Log.e("***loc4", Double.toString(endlon));

            httpRequestAsyncTask = new HttpRequestAsyncTask(){
                //doinbackground함수가 다 실행되고 자동으로 실행되는 함수임!
                @Override
                protected void onPostExecute(List<List<HashMap<String, String>>> result) {
                    //경로정보 전역변수에 저장
                    wayPointsList = result;
                    //2d지도에 폴리라인 그리깅
                    //set2dMapView();
                }
            };
            httpRequestAsyncTask.execute(startlat, startlon, endlat, endlon);
        }
        catch(Exception e){
            Log.e("***AsynchTask오류","mapactivity85");
        }

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        // SensorManager 를 이용해서 방향 센서 객체를 얻는다.
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //SensorManager.getOrientation()
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //3D map
        streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment)
                        getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);

        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        mStreetViewPanorama = panorama;
                        mStreetViewPanorama.setStreetNamesEnabled(false);//거리 이름 표시
                        mStreetViewPanorama.setUserNavigationEnabled(true);// 유저 이동 조작
                        mStreetViewPanorama.setZoomGesturesEnabled(true);// 줌인줌아웃 관련된듯
                        mStreetViewPanorama.setPanningGesturesEnabled(true);//유저 view 조작

                        long duration = 0;
                        camera = new StreetViewPanoramaCamera.Builder()
                                .zoom(mStreetViewPanorama.getPanoramaCamera().zoom + ZOOM_BY)
                                .tilt(TILT_BY)
                                .bearing(mStreetViewPanorama.getPanoramaCamera().bearing - PAN_BY) //왼쪽으로 PAN_BY값만큼 각도 이동
                                .build();
                        mStreetViewPanorama.animateTo(camera,duration);

                        // Only set the panorama to SAN_FRAN on startup (when no panoramas have been
                        // loaded which is when the savedInstanceState is null).
                        if (savedInstanceState == null) {
                            mStreetViewPanorama.setPosition(SAN_FRAN);
                        }
                    }
                });
    }

    protected void walk() {
        Log.d("****walk","걸어용");
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                try {
                    /*for (int i = 0; i < wayPointsList.size(); i++) {

                        List<HashMap<String, String>> path = wayPointsList.get(i);

                        for (int j = 0; j < path.size(); j++) {
                            HashMap<String, String> point = path.get(j);

                            double lat = Double.parseDouble(point.get("lat"));
                            double lng = Double.parseDouble(point.get("lng"));
                            postion = new LatLng(lat, lng);
                            mStreetViewPanorama.setPosition(postion);
                            Log.d("****walk",postion.toString());
                        }
                    }*/

                    List<HashMap<String, String>> path = wayPointsList.get(i);

                    //=======
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    postion = new LatLng(lat, lng);
                    mStreetViewPanorama.setPosition(postion);
                    Log.d("****walk",postion.toString());
                    j++;
                    if(j == path.size()) {
                        j = 0;
                        i++;
                    }

                    if(i == wayPointsList.size()) {
                        i = 0;
                        isEndOfPath = true;
                    }
                } catch (Exception e) {
                    Log.d("StreetViewVT","walkError=" + e);
                }
            }
        };
        handler.post(updater);
    }
/*
    private void set2dMapView() {
        //2D map setting
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setOnMarkerDragListener(ResultMapFromSearch.this);
                // Creates a draggable marker. Long press to drag.
                googleMap.moveCamera((CameraUpdateFactory.newLatLng(SAN_FRAN)));

                mMap = googleMap;

                drawMarkerOn2dMap();
                drawPathLineOn2dMap();
            }
        });
    }

    private void drawMarkerOn2dMap() {
        //시작, 출발점 마커를 그려용
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng startLatLng = new LatLng(startlat,startlon);
        markerOptions.position(startLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        mMap.addMarker(markerOptions);

        markerOptions = new MarkerOptions();
        LatLng endLatLng = new LatLng(endlat, endlon);
        markerOptions.position(endLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        mMap.addMarker(markerOptions);
    }

    private void drawPathLineOn2dMap() {
        ArrayList points = null;
        PolylineOptions lineOptions = null;

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
    */

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.

     @Override
     public void onMapReady(GoogleMap googleMap) {
     mMap = googleMap;
     // Add a marker in Sydney and move the camera
     LatLng location = new LatLng(lat, lon);
     mMap.addMarker(new MarkerOptions().position(location).title("Marker in Sydney"));
     mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
     }
     */

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            bearing = (int) event.values[0];
            tilt = (int) event.values[1];
            streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                    new OnStreetViewPanoramaReadyCallback() {
                        @Override
                        public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
                            camera = new StreetViewPanoramaCamera.Builder()
                                    .zoom(mStreetViewPanorama.getPanoramaCamera().zoom + ZOOM_BY)
                                    .tilt(tilt)
                                    .bearing(bearing) //왼쪽으로 PAN_BY값만큼 각도 이동
                                    .build();
                        }
                    }
            );


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
        mStreetViewPanorama.setPosition(marker.getPosition(), 150);

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {

    }


}