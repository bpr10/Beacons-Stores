package com.anipr.beaconstores.beaconhandler;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;

import com.anipr.beaconstores.MainActivity;
import com.anipr.beaconstores.R;
import com.anipr.beaconstores.DbHandler.DbHelper;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.estimote.sdk.utils.L;

import java.util.List;

/**
 * Created by Techanipr on 1/5/2015.
 */
public class BeaconDetectionService extends Service {

    private static final String ESTIMOTE_PROXIMITY_UUID = " b9407f30-f5f8-466e-aff9-25556b57fe6d ";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId",
            ESTIMOTE_PROXIMITY_UUID, null, null);
    private BeaconManager beaconManager;
    private NotificationManager notificationManager;
    private DbHelper dbhelper;

    @Override
    public IBinder onBind(Intent intent) {
        //Initialising BaconManager For Service
        if (beaconManager == null) {
            beaconManager = new BeaconManager(this);
        }
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //Initialising Logging
        L.enableDebugLogging(true);

        //setting Up Ranging Listener

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                if (beacons.size() != 0) {
                    for (int i = 0; i < beacons.size(); i++) {
                        if (Utils.computeAccuracy(beacons.get(i)) < .3){
                               createNotification(beacons.get(i));
                        }else{

                        }
                    }
                }
            }
        });
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void createNotification(Beacon beacon) {
        dbhelper = DbHelper.getInstance(getApplicationContext());
        String query = "select * from " + DbHelper.beaconsTable + " where " + DbHelper.beaconMAC + " = " + beacon.getMacAddress() + " ;";
        Cursor beaconsCursor = dbhelper.getReadableDatabase().rawQuery(query, null);
        if (beaconsCursor.getCount() != 0) {
            String storeName = beaconsCursor.getString(beaconsCursor.getColumnIndex(DbHelper.beaconMAC));
            Intent notifyIntent = new Intent(getApplicationContext(),
                    MainActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivities(
                    getApplicationContext(), 0, new Intent[]{notifyIntent},
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new Notification.Builder(
                    getApplicationContext()).setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("StoreFound").setContentText("StoreOffer")
                    .setAutoCancel(true).setContentIntent(pendingIntent).build();
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_LIGHTS;
            notificationManager.notify(123, notification);
        }
    }
    private void markEntryTime(String macAddress){

    }
}

