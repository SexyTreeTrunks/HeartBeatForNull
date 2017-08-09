package com.sookmyung.heartbeatfornull.virtualtravel;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sookmyung.heartbeatfornull.R;

import java.io.IOException;
import java.util.List;

public class Show2DMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker mMarker;
    private EditText editText_location_2d;
    private Button button_search;

    private double lat;
    private double lon;
    private LatLng markerLocation = defaultLocation;
    private static final LatLng defaultLocation = new LatLng(-14.359897, -170.751453);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_2d_map);

        editText_location_2d = (EditText) findViewById(R.id.editText_location_2d);
        button_search = (Button) findViewById(R.id.button_search);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Geocoder geocoder = new Geocoder(this);

        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Address> list = null;
                String str_location = editText_location_2d.getText().toString();
                try{
                    list = geocoder.getFromLocationName(str_location, 10);
                } catch (IOException e){
                    e.printStackTrace();

                    Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러 발생");
                }

                if(list != null) {
                    if (list.size() == 0) {
                        Toast.makeText(Show2DMap.this, "해당 주소 없음", Toast.LENGTH_LONG).show();
                    } else {
                        Address addr = list.get(0);
                        lat = addr.getLatitude();
                        lon = addr.getLongitude();

                        String str_lat = String.format("%.6f",lat);
                        lat = Double.parseDouble(str_lat);

                        String str_lon = String.format("%.6f",lon);
                        lon = Double.parseDouble(str_lon);

                        Toast.makeText(Show2DMap.this, Double.toString(lat) + ", " + Double.toString(lon), Toast.LENGTH_LONG).show();
                        markerLocation = new LatLng(lat, lon);
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(markerLocation).title("로드뷰보기 >"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLocation));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.addMarker(new MarkerOptions().position(markerLocation).title("로드뷰 보기 >"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLocation));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                markerLocation = latLng;
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(markerLocation).title("로드뷰보기 >"));
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                try {
                    Intent intent = new Intent(Show2DMap.this, StreetViewInVirtualTravel.class);
                    intent.putExtra("selectedLat", markerLocation.latitude);
                    intent.putExtra("selectedLng", markerLocation.longitude);
                    Log.e("markerWindow", "lat : " + markerLocation.latitude + "long : "+ markerLocation.longitude);
                    startActivity(intent);
                } catch(Exception e) {
                    Log.e("Show2DMap", e.toString());
                }
            }
        });
    }
}
