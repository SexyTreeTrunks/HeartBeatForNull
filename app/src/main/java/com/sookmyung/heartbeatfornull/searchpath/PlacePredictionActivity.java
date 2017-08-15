package com.sookmyung.heartbeatfornull.searchpath;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.sookmyung.heartbeatfornull.R;

public class PlacePredictionActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE2 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeprediction);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this).build();//api 추가 부분
        mGoogleApiClient.connect();

        try {
            //검색어 자동완성 오버레이
            Intent search_intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(search_intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);


        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
            Log.e("***e", "er1", e);
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
            Log.e("***e", "er2", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("***", "Place: " + place.getName());
                //이전 액티비티 호출
                Intent return_intent = getIntent();
                return_intent.putExtra("place", place.getName());
                setResult(RESULT_OK, return_intent);
                finish();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("***", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled operation.
                Log.i("***er", "er");

            }
        }
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE2) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("***", "Place: " + place.getName());
                //이전 액티비티 호출
                Intent return_intent = getIntent();
                return_intent.putExtra("place", place.getName());
                setResult(RESULT_OK, return_intent);
                finish();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("***", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled operation.
                Log.i("***er", "er");
                PlacePredictionActivity.this.finish();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // ...

        Log.i("***er", "er");
        // TODO: 2017-08-13 https://developers.google.com/android/guides/api-client?hl=ko#HandlingFailures
    }
}