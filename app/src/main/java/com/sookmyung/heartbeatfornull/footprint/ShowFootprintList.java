package com.sookmyung.heartbeatfornull.footprint;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sookmyung.heartbeatfornull.R;

import java.util.ArrayList;
import java.util.List;

public class ShowFootprintList extends AppCompatActivity implements DownloadFootprintActivity.Listener{
    String user_id, lat, lon;
    DownloadFootprintActivity downloadFootprintActivity;
    ArrayList<FootprintInfoListViewItem> listViewItemList;
    private List<FootprintInfo> footprintInfoList;
    ListView mListView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_footprint_list);


        Intent footprint_intent = getIntent();

        user_id = "hb4null";

        lat = footprint_intent.getStringExtra("lat");
        lon = footprint_intent.getStringExtra("lon");

        downloadFootprintActivity = new DownloadFootprintActivity(ShowFootprintList.this);
        //DownloadFootprintActivity task = new DownloadFootprintActivity();

        String str_lat = String.format("%.6f",Math.floor(Double.parseDouble(lat)*1000000)/1000000);
        Log.e("Stringlat", "lat: " + lat + "lon: " + lon);
        String str_lon = String.format("%.6f",Math.floor(Double.parseDouble(lon)*1000000)/1000000);
        downloadFootprintActivity.execute(str_lat, str_lon);

        //downloadFootprintActivity = new DownloadFootprintActivity(ShowFootprintMap.this);
        //DownloadFootprintActivity task = new DownloadFootprintActivity();
        //downloadFootprintActivity.execute(Double.toString(lat), Double.toString(lon));
    }

    private class ViewHolder {
        public TextView mId;
        public TextView mContents;
    }

    @Override
    public void onError() {
        Toast.makeText(this, "Error !", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaded(List<FootprintInfo> footprintInfoList) {
        Log.e("****infoActiv", "onloaded 진입");
        this.footprintInfoList = footprintInfoList;
        listViewItemList = new ArrayList<FootprintInfoListViewItem>();

        if(footprintInfoList.size() > 0) {
            for (int i = 0; i < footprintInfoList.size(); i++) {
                FootprintInfoListViewItem listViewItem = new FootprintInfoListViewItem();
                listViewItem.setUser_id(footprintInfoList.get(i).getUser_id());
                listViewItem.setFp_contents(footprintInfoList.get(i).getFp_contents());
                //Toast.makeText(Show2DMap.this, "userid:"+footprintInfoList.get(i).getUser_id(), Toast.LENGTH_LONG).show();
                Log.e("****footprintInfoList", "userId:" + footprintInfoList.get(i).getUser_id());
                Log.e("****footprintInfoList", "contents:" + footprintInfoList.get(i).getFp_contents());

                listViewItemList.add(listViewItem);
            }
        }
        else{
            Toast.makeText(ShowFootprintList.this,"발자취 없음",Toast.LENGTH_LONG).show();
        }
    }
}
