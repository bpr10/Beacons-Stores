package com.anipr.beaconstores;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.anipr.beaconstores.DataHandler.WebDataHandler;
import com.anipr.beaconstores.beaconhandler.BeaconDetectorService;
import com.estimote.sdk.BeaconManager;


public class MainActivity extends Activity {
    BeaconManager beaconManager;
    private WebDataHandler webDataHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webDataHandler = new WebDataHandler(getApplicationContext());
        webDataHandler.getOffers();
        webDataHandler.getRegisteredBeacons();
        Intent i = new Intent(this, BeaconDetectorService.class);
        startService(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
