package com.anipr.beaconstores.beaconhandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Techanipr on 1/5/2015.
 */
public class OnDeviceBootBroadcastReciever extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, BeaconDetectorService.class);
        context.startService(startServiceIntent);
    }
}
