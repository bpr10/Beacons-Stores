package com.anipr.beaconstores;

import com.anipr.beaconstores.beaconhandler.BeaconDetectorService;
import com.estimote.sdk.BeaconManager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class SuperActivity extends ActionBarActivity {
	private String tag = getClass().getSimpleName();
	private BeaconManager beaconManager;
	private static final int REQUEST_ENABLE_BT = 1234;

	@Override
	protected void onStart() {
		super.onStart();
		if (!AppController.getInstance().userLoggedIn()) {
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
			finish();
		} else {
			beaconManager = new BeaconManager(getApplicationContext());
			// Check if device supports Bluetooth Low Energy.
			if (!beaconManager.hasBluetooth()) {
				Toast.makeText(this,
						"Device does not have Bluetooth Low Energy",
						Toast.LENGTH_LONG).show();
				return;
			}

			// If Bluetooth is not enabled, let user enable it.
			if (!beaconManager.isBluetoothEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			} else {
				Intent i = new Intent(this, BeaconDetectorService.class);
				startService(i);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.actionbar_menuitems, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// no inspection SimplifiableIfStatement
		if (id == R.id.action_logout) {
			logout();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void logout() {
		SharedPreferences loginPreferences = getSharedPreferences(
				CommonConstants.LOGIN_SHARED_PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = loginPreferences.edit();
		editor.remove(CommonConstants.LOGIN_SHARED_PREF_NAME_LoggedUser);
		editor.commit();
		Log.i(tag, "User Logged Out");
		Intent i = new Intent(this, Login.class);
		startActivity(i);
		finish();
	}

}
