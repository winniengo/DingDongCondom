package edu.swarthmore.cs.dingdongdeliverer;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;


/**
 * This IntentService handles the GCM message. GcmBroadcastReceiver holds a partial wake lock for
 * this service while it does its work. When this service is finished, it calls
 * completeWakefulIntent() to release the wake lock.
 */
public class GcmIntentService extends IntentService
{
    public static final int NOTIFICATION_ID = 1;
    public static final String TAG = "GcmIntentService";
    public GcmIntentService() { super("GcmIntentService"); }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // Using intent received in BroadcastReceiver
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        String campaignId;

        // Filter messages based on message type
        if(!extras.isEmpty()) {
            if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String type = extras.getString("type");
            }

            // Release the wake lock provided by the WakefulBroadcastReceiver
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    /**
     * Helper function for creating notifications
     */
    private void notificationHelper(String msg, PendingIntent intent)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                //.setSmallIcon(R.drawable.noti_icon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentTitle("DingDong: Condom!")
                .setContentText(msg)
                .setAutoCancel(true)
                //.setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.doubledong));
                ;
        if(intent != null) {
            builder.setContentIntent(intent);
        }

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}