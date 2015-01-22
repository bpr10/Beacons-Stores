package com.anipr.beaconstores.beaconhandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.anipr.beaconstores.NotificationsHandler;
import com.anipr.beaconstores.dbhandler.DbHelper;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.BeaconManager.RangingListener;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.estimote.sdk.utils.L;

public class BeaconDetectorService extends Service {
	private static final Region[] BEACONS = new Region[] { new Region(
			"fffffff", "B9407F30F5F8466EAFF925556B57FE6D", null, null) // uuid
																		// without
																		// "-"
	};
	// private static final String ESTIMOTE_PROXIMITY_UUID =
	// "b9407f30-f5f8-466e-aff9-25556b57fe6d";
	// private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId",
	// ESTIMOTE_PROXIMITY_UUID, 500, 10000);
	private BeaconManager beaconManager;
	String TAG = "BeaconService";

	private String tag = getClass().getSimpleName();
	private NotificationsHandler notificationHandler;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(getApplicationContext(), "service started",
				Toast.LENGTH_SHORT).show();
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
					checkBeacon(beacon);

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

	private void checkBeacon(Beacon beacon) {
		DbHelper dbhelper = DbHelper.getInstance(getApplicationContext());
		String query = "select * from " + DbHelper.beaconsTable + " where "
				+ DbHelper.beaconMAC + " = '" + beacon.getMacAddress() + "' ;";
		SQLiteDatabase dbWrite = dbhelper.getWritableDatabase();
		Cursor beaconsCursor = dbWrite.rawQuery(query, null);

		if (beaconsCursor.moveToFirst()) {
			int minDetectionDIstance = beaconsCursor.getInt(beaconsCursor
					.getColumnIndex(DbHelper.minimunDetectionDistance));
			Log.e(tag, "Min Detection DIstance " + minDetectionDIstance);
			if (Utils.computeAccuracy(beacon) < minDetectionDIstance) {
				Log.e(tag,
						"Performing entry action "
								+ Utils.computeAccuracy(beacon));
				notificationHandler.performBeaconEntryAction(beacon);
			} else {
				Log.e(tag,
						"Performing exit action "
								+ Utils.computeAccuracy(beacon));
				notificationHandler.performBeaconExitAction(beacon);
			}
		}
	}

}
