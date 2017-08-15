package com.sookmyung.heartbeatfornull.footprint;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sookmyung.heartbeatfornull.R;

public class Footprint extends AppCompatActivity {
    EditText editText_contents;
    Button button_contents;

    String user_id, contents, lat, lon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.footprint_layout);

        editText_contents = (EditText)findViewById(R.id.editText_fp_contents);
        button_contents = (Button)findViewById(R.id.button_contents);

        Intent footprint_intent = getIntent();

        user_id = "hb4null";


        lat = footprint_intent.getStringExtra("lat");
        lon = footprint_intent.getStringExtra("lon");



        button_contents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contents = editText_contents.getText().toString();
                //new UploadFootprintActivity().execute(user_id, contents, lat, lon);

                    UploadFootprintActivity task = new UploadFootprintActivity();
                    task.execute(user_id, contents, lat, lon);
                Log.d(lat, "lat값~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                Log.d(contents, "contents~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

                editText_contents.setText("");
                Toast.makeText(Footprint.this, "업로드 완료", Toast.LENGTH_LONG).show();
            }
        });

    }


    
}

