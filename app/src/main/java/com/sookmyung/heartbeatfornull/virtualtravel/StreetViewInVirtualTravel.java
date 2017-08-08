package com.sookmyung.heartbeatfornull.virtualtravel;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.sookmyung.heartbeatfornull.R;

public class StreetViewInVirtualTravel extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {
    private static final int PAN_BY = 50;
    private static final float ZOOM_BY = 0.5f;
    private static final int TILT_BY = 0;
    private LatLng markerLocation;
    private double lat;
    private double lon;

    private int bearing;
    private int tilt;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SupportStreetViewPanoramaFragment streetViewPanoramaFragment;
    private StreetViewPanorama mStreetViewPanorama;
    private StreetViewPanoramaCamera camera;
    private GoogleMap mMap;

    //TODO: streetViewPanoramaFragment.getStreetViewPanoramaAsyn의 null Exception 수정 필요
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streetview_layout);

        Intent intent = getIntent();
        lat = intent.getExtras().getDouble("selectedLat");
        lon = intent.getExtras().getDouble("selectedLng");
        Log.e("marker", "lat : " + lat + "long : " + lon);
        markerLocation = new LatLng(lat, lon);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        streetViewPanoramaFragment = (SupportStreetViewPanoramaFragment)
                        getSupportFragmentManager().findFragmentById(R.id.streetviewpanoramaInVirtualTravel);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        mStreetViewPanorama = panorama;
                        mStreetViewPanorama.setStreetNamesEnabled(false);//거리 이름 표시
                        mStreetViewPanorama.setUserNavigationEnabled(true);// 유저 이동 조작
                        mStreetViewPanorama.setZoomGesturesEnabled(true);// 줌인줌아웃 조작
                        mStreetViewPanorama.setPanningGesturesEnabled(true);//유저 view 조작

                        long duration = 0;
                        camera = new StreetViewPanoramaCamera.Builder()
                                .zoom(mStreetViewPanorama.getPanoramaCamera().zoom + ZOOM_BY)
                                .tilt(TILT_BY)
                                .bearing(mStreetViewPanorama.getPanoramaCamera().bearing - PAN_BY)
                                .build();
                        mStreetViewPanorama.animateTo(camera,duration);

                        if (savedInstanceState == null) {
                            mStreetViewPanorama.setPosition(markerLocation);

                        }
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            bearing = (int) event.values[0];
            tilt = (int) (-event.values[1] - 50);
            if(tilt > -90 && tilt < 90) {
                streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                        new OnStreetViewPanoramaReadyCallback() {
                            @Override
                            public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
                                camera = new StreetViewPanoramaCamera.Builder()
                                        .zoom(mStreetViewPanorama.getPanoramaCamera().zoom)
                                        .tilt(tilt)
                                        .bearing(bearing)
                                        .build();
                                mStreetViewPanorama.animateTo(camera, 300);
                            }
                        }
                );
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
