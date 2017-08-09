package com.sookmyung.heartbeatfornull.virtualtravel;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLink;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.sookmyung.heartbeatfornull.R;

import java.util.Timer;
import java.util.TimerTask;

public class StreetViewInVirtualTravel extends FragmentActivity implements SensorEventListener {
    private static final int PAN_BY = 50;
    private static final float ZOOM_BY = 0.5f;
    private static final int TILT_BY = 0;
    private int bearing;
    private int tilt;

    private LatLng markerLocation = new LatLng(37.765927, -122.449972);
    private double lat;
    private double lon;
    boolean isAutoSwitchChecked = false;
    private Switch switchAuto;
    private Switch switchWalk;
    private final Handler handler = new Handler();
    private TimerTask timerTask;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SupportStreetViewPanoramaFragment streetViewPanoramaFragment;
    private StreetViewPanorama mStreetViewPanorama;
    private StreetViewPanoramaCamera camera;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streetview_layout);

        initialize();

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

    private void initialize() {
        Intent intent = getIntent();
        lat = intent.getExtras().getDouble("selectedLat");
        lon = intent.getExtras().getDouble("selectedLng");
        Log.e("marker", "lat : " + lat + "long : " + lon);
        markerLocation = new LatLng(lat, lon);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        switchAuto = (Switch) findViewById(R.id.switchAuto);
        switchWalk = (Switch) findViewById(R.id.switchWalk);
        switchAuto.setChecked(true);
        isAutoSwitchChecked = true;

        switchAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked)
                    isAutoSwitchChecked = true;
                else
                    isAutoSwitchChecked = false;
            }
        });

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
                } else {
                    timer.cancel();
                    timerTask.cancel();
                }
            }
        });
    }

    protected void walk() {
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                StreetViewPanoramaLocation location = mStreetViewPanorama.getLocation();
                StreetViewPanoramaCamera camera = mStreetViewPanorama.getPanoramaCamera();
                try {
                    if (location != null && location.links != null) {
                        StreetViewPanoramaLink link = findClosestLinkToBearing(location.links, camera.bearing);
                        mStreetViewPanorama.setPosition(link.panoId);
                    }
                } catch(Exception e) {
                    Log.d("StreetViewVT", "walkError=" + e);
                }
            }
        };
        handler.post(updater);
    }

    public static StreetViewPanoramaLink findClosestLinkToBearing(StreetViewPanoramaLink[] links, float bearing) {
        float minBearingDiff = 360;
        StreetViewPanoramaLink closestLink = links[0];
        for (StreetViewPanoramaLink link : links) {
            if (minBearingDiff > findNormalizedDifference(bearing, link.bearing)) {
                minBearingDiff = findNormalizedDifference(bearing, link.bearing);
                closestLink = link;
            }
        }
        return closestLink;
    }

    public static float findNormalizedDifference(float a, float b) {
        float diff = a - b;
        float normalizedDiff = diff - (float) (360 * Math.floor(diff / 360.0f));
        return (normalizedDiff < 180.0f) ? normalizedDiff : 360.0f - normalizedDiff;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(isAutoSwitchChecked) {
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                bearing = (int) event.values[0];
                tilt = (int) (-event.values[1] - 50);
                if (tilt > -90 && tilt < 90) {
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
