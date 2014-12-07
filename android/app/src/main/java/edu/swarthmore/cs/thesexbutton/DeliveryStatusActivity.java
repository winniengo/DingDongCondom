package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeliveryStatusActivity extends Activity
{
    private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler();  // used to queue code execution on thread
    private int mProgressDialogStatus;
    String mOrderNumber, mSessionToken;
    SharedPreferences mSharedPreferences;
    int mDeliveryEstimate;
    boolean mAccepted;
    boolean mDelivered;
    boolean mFailed;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status);

        mSharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        mSessionToken = mSharedPreferences.getString("session_token", null);
        mOrderNumber = mSharedPreferences.getString("order_number", null);

        TextView orderNum = (TextView)findViewById(R.id.text_order_number_status);
        orderNum.setText("Order " + mOrderNumber);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Poll the server every 10 secs until order accepted or fails
                while (!mAccepted && !mFailed) {
                    checkDeliveryStatus();
                    mySleep(5000);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Delivery has been accepted
                        launchProgressDialog(DeliveryStatusActivity.this);

                        setContentView(R.layout.delivery_arrival);
                        TextView orderNum = (TextView) findViewById(R.id.text_order_number_arrival);
                        orderNum.setText("Order " + mOrderNumber);

                        SharedPreferences.Editor edit = mSharedPreferences.edit();
                        edit.putBoolean("order_failed", false);
                        edit.apply();

                        // TODO: I feel like this restart button is unnecessary -Awj
//                        Button restart = (Button) findViewById(R.id.restartButton);
//                        restart.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Intent i = new Intent(DeliveryStatusActivity.this, RequestCondomActivity.class);
//                                startActivity(i);
//                                finish();
//                            }
//                        });

                        Button guide = (Button) findViewById(R.id.guideButton);
                        guide.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(DeliveryStatusActivity.this, MenuGuideActivity.class);
                                startActivity(i);
                                finish();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    /**
     * Launches and loads progress bar
     */
    public void launchProgressDialog(Context context) {
        // Prepare for a progress bar dialog
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle("Delivering Order " + mOrderNumber + "...");
        mProgressDialog.setMessage("Condoms are on their way!\nEstimated delivery time: " +
                mDeliveryEstimate + " min.");
        mProgressDialog.setCancelable(false);  // dialog can't be cancelled by pressing back
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(mDeliveryEstimate);
        mProgressDialog.setProgress(0);  // set the current progress to zero
        mProgressDialog.show();

        // Reset progress bar status
        mProgressDialogStatus = 0;

        Thread thread = new Thread(new Runnable() {  // used to execute in parallel with UI thread
            public void run() {
                int counter = 1;

                // Check delivery status every 10 seconds
                while(!mDelivered && !mFailed) {
                    checkDeliveryStatus();
                    if(mDelivered) {
                        mProgressDialogStatus = mProgressDialog.getMax();
                    } else { // if loading bar isn't full, update progress
                        if(mProgressDialogStatus < mProgressDialog.getMax() && counter%6 == 0) {
                            mHandler.post(new Runnable() {
                                public void run() {
                                    mProgressDialog.setProgress(mProgressDialogStatus);
                                }
                            });
                            mProgressDialogStatus++;
                        }
                        mySleep(10000);
                        counter++;
                    }
                }

                // Condom has been delivered
                mySleep(1500);

                // Close the progress bar dialog
                mProgressDialog.dismiss();

                if(mFailed) {
                    SharedPreferences.Editor edit = mSharedPreferences.edit();
                    edit.putBoolean("order_failed", true);
                    edit.putString("order_number", mOrderNumber);
                    edit.apply();

                    Intent i = new Intent(DeliveryStatusActivity.this, RequestCondomActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });
        thread.start();
    }

    /**
     * Thread.sleep wrapper
     */
    private void mySleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hits our server to determine delivery status of order
     */
    public void checkDeliveryStatus()
    {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("session_token", mSessionToken));
        params.add(new BasicNameValuePair("order_number", mOrderNumber));

        ServerRequest serverRequest = new ServerRequest();
        JSONObject json = serverRequest.getJSON("http://tsb.sccs.swarthmore.edu:8080/api/delivery/status", params);

        if(json != null) {
            try {
                mAccepted = json.getBoolean("order_accepted");
                mDelivered = json.getBoolean("order_delivered");
                mFailed = json.getBoolean("order_failed");
                mDeliveryEstimate = json.getInt("delivery_estimate");

                if(mDeliveryEstimate == -1) {
                    mDeliveryEstimate = 15;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}