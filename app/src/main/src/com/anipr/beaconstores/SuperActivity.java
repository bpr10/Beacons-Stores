package com.anipr.beaconstores;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SuperActivity extends ActionBarActivity {
	private String tag = getClass().getSimpleName();

	@Override
	protected void onStart() {
		super.onStart();
		if (!userLoggedIn()) {
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
			finish();
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

	private boolean userLoggedIn() {
		SharedPreferences loginPreferences = getSharedPreferences(
				CommonConstants.LOGIN_SHARED_PREF_NAME, Context.MODE_PRIVATE);
		if (loginPreferences.getString(
				CommonConstants.LOGIN_SHARED_PREF_NAME_LoggedUser,
				CommonConstants.NO_LOGGED_USER).equals(
				CommonConstants.NO_LOGGED_USER)) {
			return false;
		} else {
			return true;
		}

	}
}
