package com.anipr.beaconstores;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.anipr.beaconstores.dbhandler.DbHelper;
import com.estimote.sdk.Beacon;

public class NotificationsHandler {
	private Context context;
	private NotificationManager notificationManager;
	private static final String ENTRY_ROW_ID = "rowID";
	private static final String TIME_OF_STAY = "timeOfStay";
	private final String tag = getClass().getSimpleName();

	public NotificationsHandler(Context context) {
		this.context = context;
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

	}

	public void performBeaconEntryAction(Beacon beacon) {
		if (beaconBelongstoStore(beacon)) {
			if (!checkBeaconIfNotified(beacon)) {
				postEntryNotifiaction(beacon);
			} else {
				Log.i(tag, "Beacon Entry Notified");
			}
		} else {
			Log.i(tag, "Beacon not Registered");
		}

	}

	public void performBeaconExitAction(Beacon beacon) {
		postExitNotification(beacon);
	}

	private boolean beaconBelongstoStore(Beacon beacon) {
		DbHelper dbhelper = DbHelper.getInstance(context);
		String query = "select * from " + DbHelper.beaconsTable + " where "
				+ DbHelper.beaconMAC + " = '" + beacon.getMacAddress() + "' ;";
		SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
		Cursor beaconsCursor = dbWrite.rawQuery(query, null);
		if (beaconsCursor.moveToFirst()) {
			return true;
		} else {
			return false;
		}

	}

	public void postEntryNotifiaction(Beacon beacon) {

		DbHelper dbhelper = DbHelper.getInstance(context);
		String query = "select * from " + DbHelper.beaconsTable + " where "
				+ DbHelper.beaconMAC + " = '" + beacon.getMacAddress() + "' ;";
		SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
		Cursor beaconsCursor = dbWrite.rawQuery(query, null);
		String storeCode = "";
		String offerName = "";
		String offerCode = "";
		if (beaconsCursor.moveToFirst()) {
			storeCode = beaconsCursor.getString(beaconsCursor
					.getColumnIndex(DbHelper.beaconStore));

			int userMembership = getUserMembership();
			String offerQuery = "SELECT * FROM " + DbHelper.OFFERS_TABLE
					+ " WHERE " + DbHelper.offerType + " = "
					+ DbHelper.OFFER_TYPE_ENTRY + " AND " + DbHelper.storeCode
					+ " = '" + storeCode + "' AND "
					+ DbHelper.offerMinMembership + " <= +" + userMembership
					+ " ORDER BY " + DbHelper.offerMinMembership + " DESC, "
					+ DbHelper.offerEndDate + " ASC";

			Cursor offersCursor = dbWrite.rawQuery(offerQuery, null);
			if (offersCursor.moveToFirst()) {
				offerName = "Offer :  "
						+ offersCursor.getString(offersCursor
								.getColumnIndex(DbHelper.offerName));
				offerCode = offersCursor.getString(offersCursor
						.getColumnIndex(DbHelper.offerCode));
			} else {
				offerName = "";
				Log.i(tag, "Cursor Empty");
			}
			if (canPostNotification()) {
				postNotification("Welcome to " + storeCode, offerName,
						markEntryTime(beacon), offerCode);
			}

		}

	}

	public void postExitNotification(Beacon beacon) {
		// Get Entry Record Row ID
		String offerCode = "";
		Map<String, Integer> entryRowInfo = new HashMap<String, Integer>();
		entryRowInfo.putAll(getEntryRowId(beacon));
		if (entryRowInfo.get(ENTRY_ROW_ID) != 0) {
			Log.i(tag, "Entry record found " + entryRowInfo.get(ENTRY_ROW_ID));
			int timeOfStay = entryRowInfo.get(TIME_OF_STAY);

			String feedbackOfferQuery = "SELECT * FROM "
					+ DbHelper.OFFERS_TABLE + " WHERE " + DbHelper.offerType
					+ " = " + DbHelper.OFFER_TYPE_EXIT + " AND "
					+ DbHelper.offerMinMembership + " <= "
					+ getUserMembership() + " AND " + DbHelper.minimumDuration
					+ " < " + timeOfStay + " ORDER BY "
					+ DbHelper.offerMinMembership + " DESC, "
					+ DbHelper.offerEndDate + " ASC, "
					+ DbHelper.minimumDuration + " DESC";
			Log.i("feedback cursor query", feedbackOfferQuery);
			DbHelper dbhelper = DbHelper.getInstance(context);
			SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
			Cursor feedbackOfferDetailsCursor = dbWrite.rawQuery(
					feedbackOfferQuery, null);
			String storeCode, offerName;
			if (feedbackOfferDetailsCursor.moveToFirst()) {
				storeCode = feedbackOfferDetailsCursor
						.getString(feedbackOfferDetailsCursor
								.getColumnIndex(DbHelper.storeCode));
				offerName = feedbackOfferDetailsCursor
						.getString(feedbackOfferDetailsCursor
								.getColumnIndex(DbHelper.offerName));
				offerCode = feedbackOfferDetailsCursor
						.getString(feedbackOfferDetailsCursor
								.getColumnIndex(DbHelper.offerCode));
			} else {
				storeCode = "Thank you for visiting us";
				offerName = "";
			}
			if (canPostNotification()) {
				postNotification(storeCode, offerName,
						markExitTime(entryRowInfo.get(ENTRY_ROW_ID)), offerCode);
			}

		}

	}

	int markExitTime(int rowId) {
		DbHelper dbhelper = DbHelper.getInstance(context);
		SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DbHelper.BeaconExitTime, Calendar.getInstance()
				.getTimeInMillis());
		dbWrite.update(DbHelper.BEACONS_HISTORY_TABLE, cv,
				DbHelper.BeaconEntryTableRowId + " = " + rowId, null);
		return Integer.parseInt(rowId + "" + DbHelper.OFFER_TYPE_EXIT);
	}

	Map<String, Integer> getEntryRowId(Beacon beacon) {
		Log.i(tag, "Gettign Entry Record Row Id");
		Map<String, Integer> entryRowInfo = new HashMap<String, Integer>();

		DbHelper dbhelper = DbHelper.getInstance(context);
		String query = "select * from " + DbHelper.BEACONS_HISTORY_TABLE
				+ " where " + DbHelper.beaconMAC + " = '"
				+ beacon.getMacAddress() + "' and " + DbHelper.BeaconExitTime
				+ " is null;";
		SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
		Cursor entryCursor = dbWrite.rawQuery(query, null);
		if (entryCursor.moveToFirst()) {
			entryRowInfo.put(ENTRY_ROW_ID, Integer.parseInt(entryCursor
					.getString(entryCursor
							.getColumnIndex(DbHelper.BeaconEntryTableRowId))));
			entryRowInfo
					.put(TIME_OF_STAY,
							(int) (Calendar.getInstance().getTimeInMillis() - Long.parseLong(entryCursor.getString(entryCursor
									.getColumnIndex(DbHelper.BeaconEntryTime)))) / 1000);

		} else {
			entryRowInfo.put(ENTRY_ROW_ID, 0);
			Log.i(tag, "Entry Record Row Id Not Found");
		}
		return entryRowInfo;
	}

	protected int markEntryTime(Beacon beacon) {

		DbHelper dbhelper = DbHelper.getInstance(context);
		SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DbHelper.beaconMAC, beacon.getMacAddress());
		long entryTime = Calendar.getInstance().getTimeInMillis();
		cv.put(DbHelper.BeaconEntryTime, entryTime);
		// cv.put(DbHelper.BeaconPresenceStatus, DbHelper.BEACON_PRESENT);
		dbWrite.insert(DbHelper.BEACONS_HISTORY_TABLE, null, cv);
		String rowIdQuery = "select * from " + DbHelper.BEACONS_HISTORY_TABLE
				+ " where " + DbHelper.beaconMAC + " = '"
				+ beacon.getMacAddress() + "' and " + DbHelper.BeaconEntryTime
				+ " = " + entryTime + " and " + DbHelper.BeaconExitTime
				+ " is null;";
		Cursor rowIDCursor = dbWrite.rawQuery(rowIdQuery, null);
		if (rowIDCursor.moveToFirst()) {
			return Integer.parseInt(rowIDCursor.getString(rowIDCursor
					.getColumnIndex(DbHelper.BeaconEntryTableRowId))
					+ ""
					+ DbHelper.OFFER_TYPE_ENTRY);
		} else {
			return 1;
		}
	}

	private int getUserMembership() {
		// checkUserLogin
		// if(true){
		// return userMembership;
		// }else{
		// return 1;
		// }
		return 1;
	}

	void postNotification(String title, String content, int notificationId,
			String offerCode) {
		Intent notifyIntent;
		if (offerCode.length() == 0) {
			notifyIntent = new Intent(context, MainActivity.class);

			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		} else {
			notifyIntent = new Intent(context, OfferDetails.class);
			notifyIntent.putExtra(DbHelper.offerCode, offerCode);
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		}
		PendingIntent pendingIntent = PendingIntent.getActivities(context, 0,
				new Intent[] { notifyIntent },
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher).setContentTitle(title)
				.setContentText(content).setAutoCancel(true)
				.setContentIntent(pendingIntent).build();
		
		notification.defaults |= Notification.DEFAULT_ALL;

		notificationManager.notify(notificationId, notification);

	}

	boolean canPostNotification() {
		SharedPreferences mPreferences = context
				.getSharedPreferences(
						CommonConstants.NOTIFICATION_SHARED_PREFS,
						Context.MODE_PRIVATE);
		Long lastNotificationTime = mPreferences.getLong(
				CommonConstants.LAST_NOTIFICATION_PUSH_TIME, Calendar
						.getInstance().getTimeInMillis() - 7200000);
		String notificationIntervalQuery = "SELECT * FROM "
				+ DbHelper.beaconsTable;
		int notificationInterval = 10;
		Cursor notificationIntervalCursor = DbHelper.getInstance(context)
				.getReadableDatabase()
				.rawQuery(notificationIntervalQuery, null);
		if (notificationIntervalCursor.moveToFirst()) {
			Log.e(notificationIntervalQuery, "rows found");
			notificationInterval = notificationIntervalCursor
					.getInt(notificationIntervalCursor
							.getColumnIndex(DbHelper.notificationInterval));
		}
		Log.e(tag, "Notification Interval " + notificationInterval);
		if (((Calendar.getInstance().getTimeInMillis() - lastNotificationTime) / 1000) > notificationInterval) {
			Editor mPreferencesEditor = mPreferences.edit();
			mPreferencesEditor.putLong(
					CommonConstants.LAST_NOTIFICATION_PUSH_TIME, Calendar
							.getInstance().getTimeInMillis());
			mPreferencesEditor.commit();
			Log.e("can push",
					"Notification Interval "
							+ (Calendar.getInstance().getTimeInMillis() - lastNotificationTime)
							/ 1000);
			return true;

		} else {
			Log.e("Can not post notification", (Calendar.getInstance()
					.getTimeInMillis() - lastNotificationTime)
					/ 1000
					+ " Secs ago");
			return false;
		}

	}

	public boolean checkBeaconIfNotified(Beacon beacon) {
		DbHelper dbhelper = DbHelper.getInstance(context);
		SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
		String beaconEntryQuery = "select * from "
				+ DbHelper.BEACONS_HISTORY_TABLE + " where "
				+ DbHelper.beaconMAC + " = '" + beacon.getMacAddress()
				+ "' and " + DbHelper.BeaconExitTime + " is null;";
		Cursor beaconEntryCursor = dbhelper.getReadableDatabase().rawQuery(
				beaconEntryQuery, null);
		if (beaconEntryCursor.getCount() != 0) {
			beaconEntryCursor.moveToFirst();
			if ((Calendar.getInstance().getTimeInMillis() - Long
					.parseLong(beaconEntryCursor.getString(beaconEntryCursor
							.getColumnIndex(DbHelper.BeaconEntryTime)))) > 7200000) {
				// Stayed More than 2 hours , mark exit time
				ContentValues cv = new ContentValues();
				cv.put(DbHelper.BeaconExitTime, Calendar.getInstance()
						.getTimeInMillis() - 10800000);
				dbWrite.update(
						DbHelper.BEACONS_HISTORY_TABLE,
						cv,
						DbHelper.BeaconEntryTableRowId
								+ " = "
								+ beaconEntryCursor.getInt(beaconEntryCursor
										.getColumnIndex(DbHelper.BeaconEntryTableRowId)),
						null);
				Log.i(tag, DbHelper.BEACONS_HISTORY_TABLE
						+ " Updated and notification Pushed");

				// notify
				return false;
			} else {
				// do not notify
				Log.i(tag,
						"Notified "
								+ (Calendar.getInstance().getTimeInMillis() - Long.parseLong(beaconEntryCursor.getString(beaconEntryCursor
										.getColumnIndex(DbHelper.BeaconEntryTime))))
								/ 1000 + " secs ago");
				return true;
			}

		} else {
			// notify
			Log.i(tag, "Notify at enrty");
			return false;
		}

	}

}
