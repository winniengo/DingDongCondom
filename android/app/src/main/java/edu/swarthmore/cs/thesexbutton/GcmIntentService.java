package edu.swarthmore.cs.thesexbutton;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
* This IntentService handles the GCM message. GcmBroadcastReceiver holds a partial wake lock for
* this service while it does its work. When this service is finished, it calls
* completeWakefulIntent() to release the wake lock.
*/
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GcmIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {
        // Using intent received in BroadcastReceiver
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
//            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
//                sendNotification("Send error: " + extras.toString());
//            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
//                sendNotification("Deleted messages on server: " + extras.toString());
//        } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

            // Filter messages based on message type
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.i(TAG, "Received: " + extras.toString());
                JSONObject surveyJson = retrieveSurvey(extras.toString());
                sendNotification("New survey available. Click here to take it!", surveyJson);
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Retrieves new survey from the server
     */
    private JSONObject retrieveSurvey(String campaignId) {
        String token =
                getSharedPreferences("SharedPreferences", MODE_PRIVATE).getString("session_token", null);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("campaign_id", campaignId));
        params.add(new BasicNameValuePair("session_token", token));

        ServerRequest serverRequest = new ServerRequest();
        return serverRequest.getJSON("http://tsb.sccs.swarthmore.edu:8080/api/server/retrieve", params);
    }

    /**
     * Notifies the user that a survey is available
     */
    private void sendNotification(String msg, JSONObject survey) {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // TODO: create a survey activity
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, RequestCondomActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo_1)
                .setContentTitle("GCM Notification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}