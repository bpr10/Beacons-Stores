package com.anipr.beaconstores;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.anipr.beaconstores.DbHandler.DbHelper;
import com.estimote.sdk.Beacon;

public class NotificationsHandler {
	private Context context;
	private NotificationManager notificationManager;

	private final String tag = getClass().getSimpleName();

	public NotificationsHandler(Context context) {
		this.context = context;
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

	}
	
	
	public void postExitNotification(Beacon beacon) {
		DbHelper dbhelper = DbHelper.getInstance(context);
		String query = "select * from " + DbHelper.BEACONS_HISTORY_TABLE
				+ " where " + DbHelper.beaconMAC + " = '"
				+ beacon.getMacAddress() + "' and " + DbHelper.BeaconExitTime
				+ " is null;";
		SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
		Cursor exitnotificationCursor = dbWrite.rawQuery(query, null);
		if (exitnotificationCursor.moveToFirst()) {
			if ((Calendar.getInstance().getTimeInMillis() - Long
					.parseLong(exitnotificationCursor
							.getString(exitnotificationCursor
									.getColumnIndex(DbHelper.BeaconEntryTime)))) > 10000) {
				// Post Exit notification
				ContentValues cv = new ContentValues();
				cv.put(DbHelper.BeaconExitTime, Calendar.getInstance()
						.getTimeInMillis());
				cv.put(DbHelper.BeaconPresenceStatus, DbHelper.BEACON_LEFT);
				dbWrite.update(
						DbHelper.BEACONS_HISTORY_TABLE,
						cv,
						DbHelper.BeaconEntryTableRowId
								+ " = "
								+ exitnotificationCursor.getInt(exitnotificationCursor
										.getColumnIndex(DbHelper.BeaconEntryTableRowId)),
						null);

				String msg = "Default";
				String storeCOdeQuery = "select * from "
						+ DbHelper.beaconsTable + " where "
						+ DbHelper.beaconMAC + " = '" + beacon.getMacAddress()
						+ "' ;";
				Cursor beaconsCursor = dbWrite.rawQuery(storeCOdeQuery, null);
				String storeCode = "";
				if (beaconsCursor.moveToFirst()) {
					storeCode = beaconsCursor.getString(beaconsCursor
							.getColumnIndex(DbHelper.beaconStore));
					String offersQuery = "select * from "
							+ DbHelper.OFFERS_TABLE + " where "
							+ DbHelper.storeCode + " = '" + storeCode
							+ "' and " + DbHelper.offerType + " = "
							+ DbHelper.OFFER_TYPE_EXIT + ";";
					Cursor offersCursor = dbWrite.rawQuery(offersQuery, null);
					if (offersCursor.moveToFirst()) {
						msg = "Offer :  "
								+ offersCursor.getString(offersCursor
										.getColumnIndex(DbHelper.offerName));
					} else {
						Log.i(tag, "Cursor Empty");

					}

				}
				Intent notifyIntent = new Intent(context, MainActivity.class);
				notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent pendingIntent = PendingIntent.getActivities(
						context, 0, new Intent[] { notifyIntent },
						PendingIntent.FLAG_UPDATE_CURRENT);
				Notification notification = new Notification.Builder(context)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle("Feedback Offer from " + storeCode)
						.setContentText(msg).setAutoCancel(true)
						.setContentIntent(pendingIntent).build();
				notification.defaults |= Notification.DEFAULT_ALL;
				notificationManager.notify(DbHelper.OFFER_TYPE_EXIT,
						notification);
			} else {
				Log.i(tag, "Stayed less than 10 secs ");
			}
		} else {
			Log.i(tag, "Entry record not found ");
		}
		
	}

	public void postEntryNotifiaction(Beacon beacon) {
		String msg = "Default";
		DbHelper dbhelper = DbHelper.getInstance(context);
		String query = "select * from " + DbHelper.beaconsTable + " where "
				+ DbHelper.beaconMAC + " = '" + beacon.getMacAddress() + "' ;";
		SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
		Cursor beaconsCursor = dbWrite.rawQuery(query, null);
		String storeCode = "";
		if (beaconsCursor.moveToFirst()) {
			storeCode = beaconsCursor.getString(beaconsCursor
					.getColumnIndex(DbHelper.beaconStore));
			String offersQuery = "select * from " + DbHelper.OFFERS_TABLE
					+ " where " + DbHelper.storeCode + " = '" + storeCode
					+ "' and " + DbHelper.offerType + " = "
					+ DbHelper.OFFER_TYPE_ENTRY + ";";
			Cursor offersCursor = dbWrite.rawQuery(offersQuery, null);
			if (offersCursor.moveToFirst()) {
				msg = "Offer :  "
						+ offersCursor.getString(offersCursor
								.getColumnIndex(DbHelper.offerName));
			} else {
				Log.i(tag, "Cursor Empty");
			}

		}
		Intent notifyIntent = new Intent(context, MainActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivities(context, 0,
				new Intent[] { notifyIntent },
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Entry Offer from " + storeCode)
				.setContentText(msg).setAutoCancel(true)
				.setContentIntent(pendingIntent).build();
		notification.defaults |= Notification.DEFAULT_ALL;

		notificationManager.notify(DbHelper.OFFER_TYPE_ENTRY, notification);

	}
}
