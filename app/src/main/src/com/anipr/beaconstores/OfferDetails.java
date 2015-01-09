package com.anipr.beaconstores;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.anipr.beaconstores.dbhandler.DbHelper;

public class OfferDetails extends Activity {
	private TextView welcomeMsg, offerName, offerDetails;
	private String offerCode;
	private Cursor offerDetailCursor;
	private SQLiteDatabase dbWrite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offer_details);
		welcomeMsg = (TextView) findViewById(R.id.welcome_msg);
		offerName = (TextView) findViewById(R.id.offer_name);
		offerDetails = (TextView) findViewById(R.id.offer_desc);
		offerCode = getIntent().getStringExtra(DbHelper.offerCode);
		dbWrite = DbHelper.getInstance(getApplicationContext())
				.getWritableDatabase();
		String query = "SELECT * FROM " + DbHelper.OFFERS_TABLE
				+ " WHERE "+DbHelper.offerCode+" = '" + offerCode + "'";
		offerDetailCursor = dbWrite.rawQuery(query, null);
		if (offerDetailCursor.moveToFirst()) {
			if (offerDetailCursor.getString(
					offerDetailCursor.getColumnIndex(DbHelper.offerType))
					.equals(DbHelper.OFFER_TYPE_ENTRY)) {
				welcomeMsg.setText(getString(R.string.entry_offer_welcome_msg));
			}
		}
		offerName.setText(offerDetailCursor.getString(offerDetailCursor
				.getColumnIndex(DbHelper.offerName)));
		offerDetails.setText(offerDetailCursor.getString(offerDetailCursor
				.getColumnIndex(DbHelper.offerDesc)));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.offer_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
