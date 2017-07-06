package com.sookmyung.googlemap;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLink;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

import java.util.Timer;
import java.util.TimerTask;

import static android.os.SystemClock.sleep;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {
    private static final int PAN_BY = 50;//좌우사방팔방 각도, 0(북) 90(동) 180(남) 270(서)
    private static final float ZOOM_BY = 0.5f;
    private static final int TILT_BY = 0;//각도, -90(위) ~ 90(아래) 값 가짐
    //private static final LatLng SAN_FRAN = new LatLng(37.765927, -122.449972);
    private static final LatLng SAN_FRAN = new LatLng(37.579131, 126.975827);

    private int bearing;
    private int tilt;

    //
    private Button button;
    private Switch switch1;
    private final Handler handler = new Handler();
    private TimerTask timerTask;
    //

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SupportStreetViewPanoramaFragment streetViewPanoramaFragment;
    private StreetViewPanorama mStreetViewPanorama;
    private StreetViewPanoramaCamera camera;
    private GoogleMap mMap;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        button = (Button) findViewById(R.id.buttonForward);
        switch1 = (Switch) findViewById(R.id.switch1);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                StreetViewPanoramaLocation location = mStreetViewPanorama.getLocation();
                StreetViewPanoramaCamera camera = mStreetViewPanorama.getPanoramaCamera();
                if (location != null && location.links != null) {
                    StreetViewPanoramaLink link = findClosestLinkToBearing(location.links, camera.bearing);
                    mStreetViewPanorama.setPosition(link.panoId);
                }
            }
        });

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
                        //TODO: 여기부터!!
                        //StreetViewPanoramaCamera panoramaCamera = new StreetViewPanoramaCamera.Builder().zoom().tilt().bearing().build();

                        //float tilt = mStreetViewPanorama.getPanoramaCamera().tilt + TILT_BY;
                        //tilt = (tilt > 90) ? 90 : tilt;
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
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                StreetViewPanoramaLocation location = mStreetViewPanorama.getLocation();
                StreetViewPanoramaCamera camera = mStreetViewPanorama.getPanoramaCamera();
                if (location != null && location.links != null) {
                    StreetViewPanoramaLink link = findClosestLinkToBearing(location.links, camera.bearing);
                    mStreetViewPanorama.setPosition(link.panoId);
                }
            }
        };
        handler.post(updater);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
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
                                        .bearing(bearing) //왼쪽으로 PAN_BY값만큼 각도 이동
                                        .build();
                                mStreetViewPanorama.animateTo(camera, 300);
                            }
                        }
                );
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static StreetViewPanoramaLink findClosestLinkToBearing(StreetViewPanoramaLink[] links,
                                                                  float bearing) {
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
}