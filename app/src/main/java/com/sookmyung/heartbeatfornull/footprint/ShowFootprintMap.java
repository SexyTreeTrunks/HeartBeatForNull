package com.sookmyung.heartbeatfornull.footprint;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sookmyung.heartbeatfornull.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sookmyung.heartbeatfornull.R.id.map;

public class ShowFootprintMap extends FragmentActivity implements OnMapReadyCallback, DownloadFootprintActivity.Listener {

    private GoogleMap mMap;
   // EditText editText_location_2d;
    Button button_search;
   // Button button_leave_fp;
    //Button button_get_fp;
    Button button_getGPS_fp;

    private Location lastKnownLocation;
    private LocationManager locationManager;

    double lat;
    double lon;

    DownloadFootprintActivity downloadFootprintActivity;
    ArrayList<FootprintInfoListViewItem> listViewItemList;
    private List<FootprintInfo> footprintInfoList;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_footprint_map);

        //editText_location_2d = (EditText) findViewById(R.id.editText_location_2d);
        //button_search = (Button) findViewById(R.id.button_search_fp);
        button_getGPS_fp = (Button) findViewById(R.id.button_getGPS_fp);
        //button_leave_fp = (Button) findViewById(R.id.button_leave_fp);
        //button_get_fp = (Button) findViewById(R.id.button_get_fp);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);
        // checkLocationPermission();


        final Geocoder geocoder = new Geocoder(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lastKnownLocation = null;
        /*
        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Address> list = null;
                String str_location = editText_location_2d.getText().toString();
                try {
                    list = geocoder.getFromLocationName(str_location, 10);
                } catch (IOException e) {
                    e.printStackTrace();

                    Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러 발생");

                }

                if (list != null) {
                    if (list.size() == 0) {
                        Toast.makeText(ShowFootprintMap.this, "해당 주소 없음", Toast.LENGTH_LONG).show();
                    } else {
                        Address addr = list.get(0);
                        lat = addr.getLatitude();
                        lon = addr.getLongitude();

                        String str_lat = String.format("%.6f", lat);
                        lat = Double.parseDouble(str_lat);

                        String str_lon = String.format("%.6f", lon);
                        lon = Double.parseDouble(str_lon);

                        Toast.makeText(ShowFootprintMap.this, Double.toString(lat) + ", " + Double.toString(lon), Toast.LENGTH_LONG).show();
                        LatLng searchedLocation = new LatLng(lat, lon);
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(searchedLocation).title(str_location));

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(searchedLocation));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                    }
                }
            }
        });*/

        button_getGPS_fp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMyLocation();
            }
        });

        /*
        button_leave_fp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent footprint_intent = new Intent(ShowFootprintMap.this, Footprint.class);
                footprint_intent.putExtra("user_id", "hb4n");
                footprint_intent.putExtra("lat", Double.toString(lat));
                footprint_intent.putExtra("lon", Double.toString(lon));
                startActivity(footprint_intent);
            }
        });


        button_get_fp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // downloadFootprintActivity = new DownloadFootprintActivity(ShowFootprintMap.this);
                //DownloadFootprintActivity task = new DownloadFootprintActivity();
                //  downloadFootprintActivity.execute(Double.toString(lat), Double.toString(lon));
            }
        }); */
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

   /* private Location getGPSLocation() {
        Location currentLocation = null;

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, mLocationListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return TODO;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, mLocationListener);

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                lon = currentLocation.getLongitude();
                lat = currentLocation.getLatitude();
                Log.e("Main", "longtitude=" + lon + ", latitude=" + lat);


            }
        //}
        if(currentLocation == null){
            currentLocation = getGPSLocation();
            return currentLocation;
        }else {
            return currentLocation;
        }
    }*/

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.e("test", "onLocationChanged, location:"+location);

            lon = location.getLongitude();
            lat = location.getLatitude();
            String provider = location.getProvider();

            Toast.makeText(ShowFootprintMap.this, "위치정보:"+provider+"위도: "+lon+"경도: "+lat, Toast.LENGTH_LONG).show();
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(mLocationListener);
        }

        @Override
        public void onStatusChanged(String s, int status, Bundle bundle) {
            Log.e("test", "onLocationChanged, status:"+status);
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.e("test", "onProviderEnabled, provider: "+s);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.e("test", "onProviderDisnabled, provider: "+s);
        }
    };


    @Override
    public void onError() {
        Toast.makeText(this, "Error !", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaded(List<FootprintInfo> footprintInfoList) {
        Log.e("****infoActiv", "onloaded 진입");
        this.footprintInfoList = footprintInfoList;
        listViewItemList = new ArrayList<FootprintInfoListViewItem>();
        mMap.clear();
        if(footprintInfoList.size() > 0) {
            for (int i = 0; i < footprintInfoList.size(); i++) {
                FootprintInfoListViewItem listViewItem = new FootprintInfoListViewItem();
                listViewItem.setUser_id(footprintInfoList.get(i).getUser_id());
                listViewItem.setFp_contents(footprintInfoList.get(i).getFp_contents());
                //Toast.makeText(Show2DMap.this, "userid:"+footprintInfoList.get(i).getUser_id(), Toast.LENGTH_LONG).show();
                Log.e("****footprintInfoList", "userId:" + footprintInfoList.get(i).getUser_id());
                Log.e("****footprintInfoList", "contents:" + footprintInfoList.get(i).getFp_contents());

                    // 1. 마커 옵션 설정 (만드는 과정)
                MarkerOptions makerOptions = new MarkerOptions();
                makerOptions // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                            .position(new LatLng(Double.parseDouble(footprintInfoList.get(i).getFp_lat()), Double.parseDouble(footprintInfoList.get(i).getFp_lon())))
                            .title("마커" + i); // 타이틀.

                 // 2. 마커 생성 (마커를 나타냄)
                 mMap.addMarker(makerOptions);
                 mMap.setOnInfoWindowClickListener(infoWindowClickListener);
                 this.mMap.setOnMarkerClickListener(markerClickListener);


                listViewItemList.add(listViewItem);
            }
        }
        else{
            Toast.makeText(ShowFootprintMap.this,"발자취 없음",Toast.LENGTH_LONG).show();
        }
    }
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(ShowFootprintMap.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Permission was granted, do your thing!
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            String markerId = marker.getId();
            Toast.makeText(ShowFootprintMap.this, "정보창 클릭 Marker ID : "+markerId, Toast.LENGTH_SHORT).show();
            Intent footprint_intent = new Intent(ShowFootprintMap.this, Footprint.class);
            footprint_intent.putExtra("user_id", "hb4n");
            footprint_intent.putExtra("lat", Double.toString(lat));
            footprint_intent.putExtra("lon", Double.toString(lon));
            startActivity(footprint_intent);
        }
    };

    //마커 클릭 리스너
    GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            String markerId = marker.getId();
            //선택한 타겟위치
            LatLng location = marker.getPosition();
            Toast.makeText(ShowFootprintMap.this, "마커 클릭 Marker ID : "+markerId+"("+location.latitude+" "+location.longitude+")", Toast.LENGTH_SHORT).show();

            return false;
        }
    };

    public void getMyLocation(){

        try {
            Log.e("GPS in", "GPS in~~~~~~~~~");
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, mLocationListener);
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, mLocationListener);
            //lastKnownLocation = getGPSLocation();

            mMap.clear();
            //LatLng currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            LatLng currentLocation = new LatLng(37.546425,126.962532);
            mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title("현재위치"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    try {
                        Intent intent = new Intent(ShowFootprintMap.this, Footprint.class);

                        startActivity(intent);
                    } catch(Exception e) {
                        Log.e("ShowFootprintMap", e.toString());
                    }
                }
            });

        } catch (SecurityException e) {

        }

    }
}
