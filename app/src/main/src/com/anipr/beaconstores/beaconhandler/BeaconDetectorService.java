package com.anipr.beaconstores.beaconhandler;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.anipr.beaconstores.MainActivity;
import com.anipr.beaconstores.NotificationsHandler;
import com.anipr.beaconstores.R;
import com.anipr.beaconstores.DbHandler.DbHelper;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.BeaconManager.RangingListener;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.estimote.sdk.Utils.Proximity;
import com.estimote.sdk.utils.L;

public class BeaconDetectorService extends Service {
	private static final Region[] BEACONS = new Region[] {
			new Region("beacon1", "b9407f30f5f8466eaff925556b57fe6d", null,
					null),
			new Region("fffffff", "B9407F30F5F8466EAFF925556B57FE6D", null,
					null) // uuid without "-"
	};
	private static final String ESTIMOTE_PROXIMITY_UUID = "b9407f30-f5f8-466e-aff9-25556b57fe6d";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId",
			ESTIMOTE_PROXIMITY_UUID, 500, 10000);
	private BeaconManager beaconManager;
	String TAG = "BeaconService";

	private String tag = getClass().getSimpleName();
	private NotificationsHandler notificationHandler;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(getApplicationContext(), "service started", 1000).show();
		Log.i(tag, "Beacon Detector Service started ");
		if (beaconManager == null) {
			beaconManager = new BeaconManager(this);
		}
		notificationHandler = new NotificationsHandler(getApplicationContext());
		// Configure verbose debug logging.
		L.enableDebugLogging(false);
		beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);
		beaconManager.setForegroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);
		beaconManager.setRangingListener(new RangingListener() {

			@Override
			public void onBeaconsDiscovered(Region paramRegion,
					List<Beacon> paramList) {
				if (paramList != null && !paramList.isEmpty()) {
					Beacon beacon = paramList.get(0);

					Proximity proximity = Utils.computeProximity(beacon);
					if (proximity == Proximity.IMMEDIATE) {
						// Log.d(TAG,
						// "entered in region "
						// + paramRegion.getProximityUUID());
						// check for already notified
						if (!checkBeaconIfNotified(beacon)) {
							// notify about offer
							notificationHandler.postEntryNotifiaction(beacon);
							// postNotification("Entered into "
							// + beacon.getMinor() + "");
							markEntryTime(beacon);
						} else {
							// Log.d(TAG,
							// "already notified "
							// + paramRegion.getProximityUUID());
							// already notifiied
						}

					} else if (proximity == Proximity.NEAR) {
						// Log.d(TAG,
						// "exiting in region "
						// + paramRegion.getProximityUUID());
						notificationHandler.postExitNotification(beacon);
						// + beacon.getMinor());
					}
				}
			}
		});

		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					Log.d(TAG, "connected");
					for (Region region : BEACONS) {
						beaconManager.startRanging(region);
					}
				} catch (RemoteException e) {
					Log.d("TAG", "Error while starting monitoring");
				}
			}
		});
		return super.onStartCommand(intent, Service.START_STICKY, startId);
	}

	protected void markEntryTime(Beacon beacon) {

		DbHelper dbhelper = DbHelper.getInstance(getApplicationContext());
		SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DbHelper.beaconMAC, beacon.getMacAddress());
		cv.put(DbHelper.BeaconEntryTime, Calendar.getInstance()
				.getTimeInMillis());
		cv.put(DbHelper.BeaconPresenceStatus, DbHelper.BEACON_PRESENT);
		dbWrite.insert(DbHelper.BEACONS_HISTORY_TABLE, null, cv);
	}

	protected boolean checkBeaconIfNotified(Beacon beacon) {
		DbHelper dbhelper = DbHelper.getInstance(getApplicationContext());
		String query = "select * from " + DbHelper.beaconsTable + " where "
				+ DbHelper.beaconMAC + " = '" + beacon.getMacAddress() + "' ;";
		SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
		Cursor beaconsCursor = dbWrite.rawQuery(query, null);
		if (beaconsCursor.getCount() != 0) {

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
					// STayed More than 2 hours , mark exit time
					ContentValues cv = new ContentValues();
					cv.put(DbHelper.BeaconExitTime, Calendar.getInstance()
							.getTimeInMillis() - 10800000);
					cv.put(DbHelper.BeaconPresenceStatus, DbHelper.BEACON_LEFT);
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

		} else {
			// do not notify
			Log.i(tag, "Beacon Does not belong to any store");
			return true;
		}

	}

	@Override
	public boolean onUnbind(Intent intent) {
		try {
			beaconManager.stopRanging(BEACONS[0]);
		} catch (RemoteException e) {
			Log.e("", "Cannot stop but it does not matter now", e);
		}

		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("Service", "started");

		return null;
	}

//	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
//	@SuppressLint("NewApi")
//	private void postNotification(String msg) {
//		Intent notifyIntent = new Intent(getApplicationContext(),
//				MainActivity.class);
//		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		PendingIntent pendingIntent = PendingIntent.getActivities(
//				getApplicationContext(), 0, new Intent[] { notifyIntent },
//				PendingIntent.FLAG_UPDATE_CURRENT);
//		Notification notification = new Notification.Builder(
//				getApplicationContext()).setSmallIcon(R.drawable.ic_launcher)
//				.setContentTitle("Notify Demo").setContentText(msg)
//				.setAutoCancel(true).setContentIntent(pendingIntent).build();
//		notification.defaults |= Notification.DEFAULT_SOUND;
//		notification.defaults |= Notification.DEFAULT_LIGHTS;
//		notificationManager.notify(123, notification);
//
//	}
}
