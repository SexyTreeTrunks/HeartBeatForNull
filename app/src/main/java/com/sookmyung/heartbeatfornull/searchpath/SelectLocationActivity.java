package com.sookmyung.heartbeatfornull.searchpath;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sookmyung.heartbeatfornull.R;

import java.io.IOException;
import java.util.List;

/**
 * Created by jsm95 on 2017-07-24.
 */

public class SelectLocationActivity extends Activity{
    double slat;
    double slon;
    double elat;
    double elon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectlocations);

        Button button_select =(Button)findViewById(R.id.button_select); //출발지 선택 버튼
        Button button_endlocation = (Button)findViewById(R.id.button_endlocation); //도착지 선택 버튼

        final TextView tv = (TextView)findViewById(R.id.textView); //출발지 위도 경도 txt
        final TextView endtv = (TextView)findViewById(R.id.textView_endloc); //도착지 위도 경도 txt

        final EditText editText_location = (EditText)findViewById(R.id.editText_location); //출발지 입력박스 문구
        final EditText editText_endlocation = (EditText)findViewById(R.id.editText_endlocation); //도착지 입력박스 문구

        final Geocoder geocoder = new Geocoder(this);

        editText_location.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (editText_location.length() >= 0) {
                    editText_location.setText(null);
                    Intent intent = new Intent(SelectLocationActivity.this, PlacePredictionActivity.class);
                    startActivity(intent);
                    Log.i("***intent",editText_location.toString());
                }

                Log.i("***","intent");
            }
        });

        button_select.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                List<Address> list = null;
                String str_location = editText_location.getText().toString();
                try{
                    list = geocoder.getFromLocationName(str_location, 10);
                } catch (IOException e){
                    e.printStackTrace();

                    Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러 발생");
                }

                if(list != null) {
                    if (list.size() == 0) {
                       Toast.makeText(SelectLocationActivity.this,"해당 주소 없음", Toast.LENGTH_LONG).show();
                    } else {
                        Address addr = list.get(0);
                        double lat = addr.getLatitude();
                        double lon = addr.getLongitude();

                        String str_lat = String.format("%.6f",lat);
                        slat = Double.parseDouble(str_lat);

                        String str_lon = String.format("%.6f",lon);
                        slon = Double.parseDouble(str_lon);

                        // TODO: 2017-08-01 append settext로 바꾸기
                        tv.append(Double.toString(slat) +", " +Double.toString(slon));
                    }
                }
            }
        });

        button_endlocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                List<Address> list = null;
                String str_location = editText_endlocation.getText().toString();
                try{
                    list = geocoder.getFromLocationName(str_location, 10);
                } catch (IOException e){
                    e.printStackTrace();

                    Log.e("Endloc failed", "입출력 오류 - 서버에서 주소변환시 에러 발생");
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

                        endtv.append(Double.toString(elat) +", " +Double.toString(elon));

                        Intent endloc_intent = new Intent(SelectLocationActivity.this, ResultMapFromSearch.class); //intent 바꾸기
                        endloc_intent.putExtra("startlat",slat);
                        endloc_intent.putExtra("startlon",slon);
                        endloc_intent.putExtra("endlat", elat);
                        endloc_intent.putExtra("endlon", elon);
                        startActivity(endloc_intent);
                    }
                }
            }
        });
    }
}