package com.anipr.beaconstores;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.anipr.beaconstores.beaconhandler.BeaconDetectorService;
import com.anipr.beaconstores.datahandler.WebDataHandler;
import com.anipr.beaconstores.gcmhandler.GCMUtility;

public class MainActivity extends ActionBarActivity {
	private WebDataHandler webDataHandler;
	private EditText userName, password;
	private Button loginButton;
	private String tag = getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (userLoggedIn()) {

		} else {

		}

		webDataHandler = new WebDataHandler(getApplicationContext());
		webDataHandler.getOffers();
		webDataHandler.getRegisteredBeacons();
		new GCMUtility(this);
		Intent i = new Intent(this, BeaconDetectorService.class);
		startService(i);
		userName = (EditText) findViewById(R.id.login_username_field);
		password = (EditText) findViewById(R.id.login_password_field);
		loginButton = (Button) findViewById(R.id.button1);
		loginButton.setOnClickListener(new OnClickListener() {

			
			@Override
			public void onClick(View v) {
				if(userName.getText().length()!=0){
					if(userName.getText().length()!=0){
						//Fire Request
					}else{
						Log.d(tag, "Password Blank");
					}
				}else{
					Log.d(tag, "UserName Blank");
				}	
			}
		});
	}

	private boolean userLoggedIn() {
		SharedPreferences loginPreferences = getSharedPreferences(
				CommonConstants.LOGIN_SHARED_PREF_NAME, Context.MODE_PRIVATE);
		if (loginPreferences.getString(
				CommonConstants.LOGIN_SHARED_PREF_NAME_LoggedUser,
				CommonConstants.NO_LOGGED_USER).equals(
				CommonConstants.NO_LOGGED_USER)) {
			return true;
		} else {
			return false;
		}

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

		// noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
