package com.anipr.beaconstores;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.anipr.beaconstores.R;
import com.google.android.gms.games.Notifications;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    final String TAG = "Notification";
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  
            
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
               Log.e("Notification Error","Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
            
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
               
                // Post notification of received message.
                sendNotification( extras);
                Log.i(TAG, "Received: " + extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String  msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Notifications.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("GCM ")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
    private void sendNotification(Bundle extras) {
        try
        {
        	
    	mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = null ;
        if(extras.getString("source").equals("Stampitgo"))
        {
        	contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, Notifications.class), 0);
        }else{
        	
           Intent i =new Intent(this, MainActivity.class);
		   
		contentIntent = PendingIntent.getActivity(this, 0,
		            i, 0);
        	
        }
        
        Notification notification = new Notification.Builder(
                getApplicationContext()).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(extras.getString("title")).setContentText(extras.getString("message"))
                .setAutoCancel(true)
                .setContentIntent(contentIntent).build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        mNotificationManager.notify(NOTIFICATION_ID,notification);
    }catch(Exception e){
    	e.printStackTrace();
    }
    }
}
