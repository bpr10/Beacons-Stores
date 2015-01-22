package com.anipr.beaconstores;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.anipr.beaconstores.datahandler.DateUtility;
import com.anipr.beaconstores.dbhandler.DbHelper;
import com.google.android.gms.games.Notifications;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	final String TAG = "Notification";
	private final String tag = getClass().getSimpleName();

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {

			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				Log.e("Notification Error", "Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString());

			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

				// Post notification of received message.
				sendNotification(extras.toString());
				handlePush(extras);

				Log.i(TAG, "Received: " + extras);
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, Notifications.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("GCM ")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

	}

	private void handlePush(Bundle extras) {
		int pushCode = Integer.parseInt(extras.getString("action"));
		Log.d(tag, "action : " + pushCode);
		switch (pushCode) {
		case 1:
			addOffer(extras.getString("data"));
			break;
		case 2:
			addBeacon(extras.getString("data"));
			break;
		case 3:
			updateOffer(extras.getString("data"));
			break;
		case 4:
			updateBeacon(extras.getString("data"));
			break;
		case 6:
			try {
				deleteBeacon(new JSONObject(extras.getString("data"))
						.getString("beaconMAC"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case 5:
			try {
				deleteOffer(new JSONObject(extras.getString("data"))
						.getString("offer_code"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		default:
			sendNotification(extras.toString());
			break;
		}
	}

	private void addBeacon(String string) {
		try {
			JSONObject currentObj = new JSONObject(string);
			DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
			SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(DbHelper.beaconMAC, currentObj.getString("becon_id"));
			cv.put(DbHelper.beaconStore, currentObj.getString("store_id"));
			cv.put(DbHelper.beaconDepartment,
					currentObj.getString("department"));
			cv.put(DbHelper.minimunDetectionDistance,
					currentObj.getString("distance"));
			cv.put(DbHelper.notificationInterval, currentObj.getString("time"));

			dbWrite.insert(DbHelper.beaconsTable, "", cv);
			Log.e(tag, "Beacon Inserted");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void addOffer(String string) {
		try {
			JSONObject currentObj = new JSONObject(string);
			DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
			SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(DbHelper.offerCode, currentObj.getString("offer_code"));
			cv.put(DbHelper.storeCode, currentObj.getString("store_code"));

			cv.put(DbHelper.offerType, currentObj.getString("entry_exit_type"));
			cv.put(DbHelper.offerName, currentObj.getString("offername"));

			cv.put(DbHelper.minimumDuration,
					currentObj.getString("min_stay_time"));

			cv.put(DbHelper.offerDesc, currentObj.getString("description"));

			cv.put(DbHelper.offerMinMembership,
					currentObj.getString("membership"));
			cv.put(DbHelper.offerStartDate,
					new DateUtility().convertSerevrDatetoLocalDate(
							currentObj.getString("start_time"))
							.getTimeInMillis());
			cv.put(DbHelper.offerEndDate,
					new DateUtility().convertSerevrDatetoLocalDate(
							currentObj.getString("end_time")).getTimeInMillis());
			dbWrite.insert(DbHelper.OFFERS_TABLE, null, cv);
			Log.e(tag, "Offers Inserted");
			Log.e(tag, currentObj.getString("start_time"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void deleteOffer(String offerCode) {
		try {
			DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
			SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
			dbWrite.delete(DbHelper.OFFERS_TABLE, DbHelper.offerCode + "='"
					+ offerCode + "'", null);
			Log.e(tag, "Offer Deleted " + offerCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateBeacon(String string) {
		try {
			JSONObject currentObj = new JSONObject(string);
			DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
			SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
			String query = "SELECT * FROM " + DbHelper.beaconsTable + " WHERE "
					+ DbHelper.beaconMAC + " = '"
					+ currentObj.getString("becon_id") + "'";
			Cursor cursor = dbWrite.rawQuery(query, null);

			if (cursor.moveToFirst()) {

				ContentValues cv = new ContentValues();
				cv.put(DbHelper.beaconMAC, currentObj.getString("becon_id"));
				cv.put(DbHelper.beaconStore, currentObj.getString("store"));
				cv.put(DbHelper.beaconDepartment,
						currentObj.getString("department"));
				cv.put(DbHelper.minimunDetectionDistance,
						currentObj.getString("distance"));
				cv.put(DbHelper.notificationInterval,
						currentObj.getString("time"));
				dbWrite.update(DbHelper.beaconsTable, cv, DbHelper.beaconMAC
						+ " = '" + currentObj.getString("becon_id") + "'", null);
				Log.e(tag, "Beacon Updated");
			} else {
				Log.e(tag, "Beacon Did not Update");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteBeacon(String beaconMac) {
		try {
			DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
			SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
			dbWrite.delete(DbHelper.beaconsTable, DbHelper.beaconMAC + "='"
					+ beaconMac + "'", null);
			Log.e(tag, "Beacon Deleted " + beaconMac);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateOffer(String string) {
		try {
			JSONObject currentObj = new JSONObject(string);
			DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
			SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
			String query = "SELECT * FROM " + DbHelper.OFFERS_TABLE + " WHERE "
					+ DbHelper.offerCode + " = '"
					+ currentObj.getString("offer_code") + "'";
			Cursor cursor = dbWrite.rawQuery(query, null);

			if (cursor.moveToFirst()) {

				ContentValues cv = new ContentValues();
				cv.put(DbHelper.offerCode, currentObj.getString("offer_code"));
				cv.put(DbHelper.storeCode, currentObj.getString("store_code"));

				cv.put(DbHelper.offerType,
						currentObj.getString("entry_exit_type"));
				cv.put(DbHelper.offerName, currentObj.getString("offername"));

				cv.put(DbHelper.minimumDuration,
						currentObj.getString("min_stay_time"));

				cv.put(DbHelper.offerDesc, currentObj.getString("description"));

				cv.put(DbHelper.offerMinMembership,
						currentObj.getString("membership"));
				cv.put(DbHelper.offerStartDate,
						new DateUtility().convertSerevrDatetoLocalDate(
								currentObj.getString("start_time"))
								.getTimeInMillis());
				cv.put(DbHelper.offerEndDate,
						new DateUtility().convertSerevrDatetoLocalDate(
								currentObj.getString("end_time"))
								.getTimeInMillis());
				dbWrite.update(DbHelper.OFFERS_TABLE, cv, DbHelper.offerCode
						+ "='" + currentObj.getString("offer_code") + "'", null);
				Log.d(tag, "Offer" + currentObj.getString("offer_code")
						+ "updated");
				Log.d(tag, currentObj.getString("start_time"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
