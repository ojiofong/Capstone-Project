package com.ojiofong.arounda.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.ojiofong.arounda.R;
import com.ojiofong.arounda.utils.AlertDialogManager;
import com.ojiofong.arounda.utils.Utils;

public class StreetViewActivity extends AppCompatActivity {

    private StreetViewPanorama svp;
    String  nameFromIntent, addyFromIntent;
    Double latFromIntent, lonFromIntent;

    // private LatLng SYDNEY = new LatLng(-33.87365, 151.20689);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panorama);
        receiveIntents();
        initToolBar();

        if (!Utils.isNetworkConnected(this)) {
            new AlertDialogManager().showAlertDialog(this, getString(R.string.internet_error_title), getString(R.string.internet_error_message), null);
        }

        if (latFromIntent != null || lonFromIntent != null) {

            setUpStreetViewPanoramaIfNeeded(savedInstanceState);
            checkIfSvpIsAvailable();
        }

    }

    private void receiveIntents() {
        nameFromIntent = getIntent().getStringExtra("key_name");
        addyFromIntent = getIntent().getStringExtra("key_addy");
        latFromIntent = Double.parseDouble(getIntent().getStringExtra("key_lat"));
        lonFromIntent = Double.parseDouble(getIntent().getStringExtra("key_lon"));
    }

    private void initToolBar() {
        // initialize ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(nameFromIntent);
            getSupportActionBar().setSubtitle(addyFromIntent);
        }
    }

    private void setUpStreetViewPanoramaIfNeeded(Bundle savedInstanceState) {
        if (svp == null) {
            svp = ((SupportStreetViewPanoramaFragment) getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama)).getStreetViewPanorama();
            if (svp != null) {
                if (savedInstanceState == null) {
                    svp.setPosition(getLocation());
                }
            }
        }
    }

    private LatLng getLocation() {
        return new LatLng(latFromIntent, lonFromIntent);
    }

    private void checkIfSvpIsAvailable() {

        //wait 1 second to make sure street view has been created
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (svp.getLocation() == null) {
                    String s = getString(R.string.street_view_is_not_available);
                    Toast.makeText(StreetViewActivity.this, s, Toast.LENGTH_LONG).show();
                }

            }
        }, 3000);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        this.finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

}
