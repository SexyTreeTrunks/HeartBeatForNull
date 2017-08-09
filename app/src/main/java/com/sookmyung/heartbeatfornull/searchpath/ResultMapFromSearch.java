package com.sookmyung.heartbeatfornull.searchpath;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.sookmyung.heartbeatfornull.R;

import java.util.HashMap;
import java.util.List;

import static com.sookmyung.heartbeatfornull.R.id.map;

public class ResultMapFromSearch extends AppCompatActivity implements SensorEventListener, GoogleMap.OnMarkerDragListener, StreetViewPanorama.OnStreetViewPanoramaChangeListener {

    private static final int PAN_BY = 50;//좌우사방팔방 각도, 0(북) 90(동) 180(남) 270(서)
    private static final float ZOOM_BY = 0.5f;
    private static final int TILT_BY = 0;//각도, -90(위) ~ 90(아래) 값 가짐

    double startlat =0;
    double startlon=0;

    double endlat =0;
    double endlon=0;

    LatLng SAN_FRAN;

    private int bearing;
    private int tilt;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SupportStreetViewPanoramaFragment streetViewPanoramaFragment;
    private StreetViewPanorama mStreetViewPanorama;
    private StreetViewPanoramaCamera camera;
    private GoogleMap mMap;

    private Marker mMarker;
    private static final String MARKER_POSITION_KEY = "MarkerPosition";

    private HttpRequestAsyncTask httpRequestAsyncTask;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_path_layout);
        Intent map_intent = getIntent();

        //임시 춟발지: 숙명여자대학교
        startlat = map_intent.getDoubleExtra("startlat", 37.545062);
        startlon = map_intent.getDoubleExtra("startlon", 126.964184);

        //임시 도착지: 걸포공원
        endlat = map_intent.getDoubleExtra("endlat",37.635956);
        endlon = map_intent.getDoubleExtra("endlon",126.704334);

        SAN_FRAN = new LatLng(endlat, endlon);

        try {
            Log.e("***loc1", Double.toString(startlat));
            Log.e("***loc2", Double.toString(startlon));
            Log.e("***loc3", Double.toString(endlat));
            Log.e("***loc4", Double.toString(endlon));

            httpRequestAsyncTask = new HttpRequestAsyncTask(){
                //doinbackground함수가 다 실행되고 자동으로 실행되는 함수임!
                @Override
                protected void onPostExecute(List<List<HashMap<String, String>>> result) {

                    for (int i = 0; i < result.size(); i++) {

                        List<HashMap<String, String>> path = result.get(i);

                        for (int j = 0; j < path.size(); j++) {
                            HashMap<String, String> point = path.get(j);

                            Log.d("///onPostExecute","latlng: " + point.get("lat") + "," + point.get("lng"));
                        }
                    }
                }
            };
            httpRequestAsyncTask.execute(startlat, startlon, endlat, endlon);
        }
        catch(Exception e){
            Log.e("***AsynchTask오류","mapactivity85");
        }

        final LatLng markerPosition;
        if (savedInstanceState == null) {
            markerPosition = SAN_FRAN;
        } else {
            markerPosition = savedInstanceState.getParcelable(MARKER_POSITION_KEY);
        }

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        // SensorManager 를 이용해서 방향 센서 객체를 얻는다.
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //SensorManager.getOrientation()
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

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

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setOnMarkerDragListener(ResultMapFromSearch.this);
                // Creates a draggable marker. Long press to drag.
                googleMap.moveCamera((CameraUpdateFactory.newLatLng(SAN_FRAN)));
                mMarker = googleMap.addMarker(new MarkerOptions()
                        .position(markerPosition)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                        .draggable(true));
            }
        });

    }


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
        outState.putParcelable(MARKER_POSITION_KEY, mMarker.getPosition());
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

    private int getNetworkType(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork.getType();
    }

}