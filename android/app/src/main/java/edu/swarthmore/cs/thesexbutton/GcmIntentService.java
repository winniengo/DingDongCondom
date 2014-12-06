package edu.swarthmore.cs.thesexbutton;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

                if(type.equals("broadcast")) {
                // App availability
                    String msg = extras.getString("message");
                    sendBroadcastNotification(msg);
                } else if(type.equals("survey")) {
                // Survey availability
                    campaignId = extras.getString("campaign_id");
                    JSONObject surveyJson = retrieveSurvey(campaignId);
                    sendSurveyNotification("We had your back, and now we're asking you to have ours. Click here to take our survey.", surveyJson);
                } else if(type.equals("delivery")) {
                // Condom delivered
                    sendDeliveryNotification("Your condom is here!");
                }
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
                .setSmallIcon(R.drawable.noti_icon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentTitle("DingDong: Condom!")
                .setContentText(msg)
                .setAutoCancel(true)
                .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.doubledong));

        if(intent != null) {
            builder.setContentIntent(intent);
        }

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Notifies user that the condom is delivered
     */
    private void sendDeliveryNotification(String msg)
    {
        Intent i = new Intent(this, DeliveryStatusActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
        notificationHelper(msg, contentIntent);
    }

    /**
     * Notifies user about the availability status of the app
     */
    private void sendBroadcastNotification(String msg)
    {
        Intent i = new Intent(this, LoginActivity.class);  // add JSON to intent
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
        notificationHelper(msg, contentIntent);
    }

    /**
     * Notifies the user that a survey is available
     */
    private void sendSurveyNotification(String msg, JSONObject survey)
    {
        // Get the link from JSON object
        String link;
        try {
            link = survey.getString("survey_body");
        } catch (JSONException j) {
            link = "http://tinyurl.com/dingdongc";
            Log.e(TAG, "Handled Json Exception");
        }

        // Add JSON to intent
        Intent i = new Intent(this, SurveyActivity.class);
        i.putExtra("survey", link);

        // Pending intent launches SurveyActivity when user clicks on notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
        notificationHelper(msg, contentIntent);
    }

    /**
     * Retrieves new survey from the server
     */
    private JSONObject retrieveSurvey(String campaignId)
    {
        String token =
                getSharedPreferences("SharedPreferences", MODE_PRIVATE).getString("session_token", null);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("campaign_id", campaignId));
        params.add(new BasicNameValuePair("session_token", token));

        ServerRequest serverRequest = new ServerRequest();
        return serverRequest.getJSON("http://tsb.sccs.swarthmore.edu:8080/api/survey/retrieve", params);
    }
}