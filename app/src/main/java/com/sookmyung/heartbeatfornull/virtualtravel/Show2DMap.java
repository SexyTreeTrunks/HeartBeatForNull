package com.sookmyung.heartbeatfornull.virtualtravel;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.sookmyung.heartbeatfornull.R;
import com.sookmyung.heartbeatfornull.searchpath.SelectLocationActivity;

import java.io.IOException;
import java.util.List;

public class Show2DMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    EditText editText_location_2d;
    Button button_search;

    double lat;
    double lon;

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
                        double lat = addr.getLatitude();
                        double lon = addr.getLongitude();

                        String str_lat = String.format("%.6f",lat);
                        lat = Double.parseDouble(str_lat);

                        String str_lon = String.format("%.6f",lon);
                        lon = Double.parseDouble(str_lon);

                        Toast.makeText(Show2DMap.this, Double.toString(lat) + ", " + Double.toString(lon), Toast.LENGTH_LONG).show();
                        LatLng searchedLocation = new LatLng(lat, lon);
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(searchedLocation).title(str_location));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(searchedLocation));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                    }
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
}
