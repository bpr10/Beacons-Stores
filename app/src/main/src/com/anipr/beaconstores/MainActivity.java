package com.anipr.beaconstores;

import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anipr.beaconstores.beaconhandler.BeaconDetectorService;
import com.anipr.beaconstores.datahandler.WebDataHandler;
import com.anipr.beaconstores.dbhandler.DbHelper;
import com.squareup.picasso.Picasso;

public class MainActivity extends SuperActivity {
	private WebDataHandler webDataHandler;
	private String tag = getClass().getSimpleName();
	private ListView offersGrid;
	private OffersGridAdapter mOffersGridAdapter;
	private SQLiteDatabase dbRead;
	private Cursor offersCursor;

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(tag, "OnCreate Called");
		webDataHandler = new WebDataHandler(getApplicationContext());
		webDataHandler.getOffers();
		webDataHandler.getRegisteredBeacons();
		new GCMUtility(this);
		Intent i = new Intent(this, BeaconDetectorService.class);
		startService(i);
		Log.i(tag, "Service Started");
		offersGrid = (ListView) findViewById(R.id.offers_listview);
		String offersQurey = "SELECT * FROM  " + DbHelper.OFFERS_TABLE;
		dbRead = DbHelper.getInstance(getApplicationContext())
				.getWritableDatabase();
		offersCursor = dbRead.rawQuery(offersQurey, null);
		if (offersCursor.moveToFirst()) {
			mOffersGridAdapter = new OffersGridAdapter(getApplicationContext(),
					offersCursor, true);
			offersGrid.setAdapter(mOffersGridAdapter);
			offersGrid.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent i = new Intent(MainActivity.this, OfferDetails.class);
					offersCursor.moveToPosition(position);
					i.putExtra(DbHelper.offerCode, offersCursor
							.getString(offersCursor
									.getColumnIndex(DbHelper.offerCode)));
					startActivity(i);
				}
			});
		}
	}

	class OffersGridAdapter extends CursorAdapter {
		public final String TAG = OffersGridAdapter.class.getSimpleName();

		public OffersGridAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}

		@Override
		public void bindView(View view, Context context, Cursor offersCursor) {
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			ImageView offerImage = (ImageView) view
					.findViewById(R.id.offer_image);
			;

			viewHolder.offerName.setText(offersCursor.getString(offersCursor
					.getColumnIndex(DbHelper.offerName)));
			viewHolder.offerDesc.setText(offersCursor.getString(offersCursor
					.getColumnIndex(DbHelper.offerDesc)));
			String imageString = "http://loremflickr.com/700/300/"
					+ CommonConstants.imageKeywords[new Random()
							.nextInt(CommonConstants.imageKeywords.length)];
			Log.i("Image Link", imageString);
			Picasso.with(context).load(imageString).into(offerImage);
		}

		@Override
		public View newView(Context context, Cursor arg1, ViewGroup parent) {
			View view = LayoutInflater.from(context).inflate(
					R.layout.offer_grid_item, parent, false);
			ViewHolder viewHolder = new ViewHolder(view);
			view.setTag(viewHolder);
			return view;
		}

		class ViewHolder {
			TextView offerName, offerDesc;

			public ViewHolder(View convertView) {
				offerName = (TextView) convertView
						.findViewById(R.id.offer_name);
				offerDesc = (TextView) convertView
						.findViewById(R.id.offer_desc);

			}
		}

	}

}
