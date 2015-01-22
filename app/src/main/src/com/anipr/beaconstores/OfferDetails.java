package com.anipr.beaconstores;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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
					.equals(DbHelper.OFFER_TYPE_ENTRY+"")) {
				welcomeMsg.setText(getString(R.string.entry_offer_welcome_msg));
			}else{
					welcomeMsg.setText(getString(R.string.exit_offer_welcome_msg));
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
	public static Bitmap getRoundedRectBitmap(Bitmap bitmap, int pixels) {
	    Bitmap result = null;
	    try { 
	        result = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
	        Canvas canvas = new Canvas(result);
	 
	        int color = 0xff424242;
	        Paint paint = new Paint();
	        Rect rect = new Rect(0, 0, 200, 200);
	 
	        paint.setAntiAlias(true);
	        canvas.drawARGB(0, 0, 0, 0);
	        paint.setColor(color);
	        canvas.drawCircle(50, 50, 50, paint);
	        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	        canvas.drawBitmap(bitmap, rect, rect, paint);
	 
	    } catch (NullPointerException e) {
	    } catch (OutOfMemoryError o) {
	    } 
	    return result;
	} 
}
