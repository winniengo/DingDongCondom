package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
    String mOrderNumber, mSessionToken;

    SharedPreferences mSharedPreferences;

    int mDeliveryEstimate;
    boolean mAccepted;
    boolean mDelivered;
    boolean mFailed;

    private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler(); // used to queue code execution on thread
    private int mProgressDialogStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
                while (!mAccepted && !mFailed) {

                    checkDeliveryStatus(); // check delivery status via JSON
                    try { // sleep 10 seconds
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // delivery has been accepted
                        launchProgressDialog(DeliveryStatusActivity.this);
                        setContentView(R.layout.delivery_arrival);
                        TextView orderNum = (TextView) findViewById(R.id.text_order_number_arrival);
                        orderNum.setText("Order " + mOrderNumber);

                        SharedPreferences.Editor edit = mSharedPreferences.edit();
                        edit.putBoolean("order_failed", false);
                        edit.apply();

                        // TODO: I feel like this restart buttos is unnecessary
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


    // launches and loads progress bar
    public void launchProgressDialog(Context context) {
        // prepare for a progress bar dialog
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle("Delivering Order " + mOrderNumber + "...");
        mProgressDialog.setMessage("Condoms are on their way!\nEstimated delivery time: " +
                mDeliveryEstimate + " min.");
        mProgressDialog.setCancelable(false); // dialog can't be cancelled by pressing back
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(mDeliveryEstimate);
        mProgressDialog.setProgress(0); // set the current progress to zero
        mProgressDialog.show();

        //reset progress bar status
        mProgressDialogStatus = 0;

        Thread thread = new Thread(new Runnable() { // used to execute in parallel with UI thread
            public void run() {
                int counter = 1;
                while (!mDelivered && !mFailed) {
                    Log.i("StatusActivity", mDelivered + " " + mFailed);
                    // check delivery status every 10 seconds
                    checkDeliveryStatus();

                    if(mDelivered) {
                        mProgressDialogStatus = mProgressDialog.getMax();
                    }
                    else { // if loading bar isn't full, update every minute
                        if(mProgressDialogStatus < mProgressDialog.getMax() && counter%6 == 0) {
                            // update the progress bar
                            mHandler.post(new Runnable() {
                                public void run() {
                                    mProgressDialog.setProgress(mProgressDialogStatus);
                                }
                            });
                            mProgressDialogStatus++;
                        }
                        try { // sleep 10 seconds
                            Thread.sleep(10000);
                            counter++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // condom has been delivered
                try { // sleep 1.5 seconds, display 100%
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // close the progress bar dialog
                 mProgressDialog.dismiss();

                if(mFailed) {
                    SharedPreferences.Editor edit = mSharedPreferences.edit();
                    edit.putBoolean("order_failed", true);
                    edit.putString("order_number", mOrderNumber);

                    edit.apply();

                    Intent i = new Intent(DeliveryStatusActivity.this, RequestCondomActivity.class);
                    startActivity(i);
                }
            }
        });
        thread.start();
    }

    public void checkDeliveryStatus() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("session_token", mSessionToken));
        params.add(new BasicNameValuePair("order_number", mOrderNumber));

        ServerRequest serverRequest = new ServerRequest();
        JSONObject json = serverRequest.getJSON("http://tsb.sccs.swarthmore.edu:8080/api/delivery/status", params);

        if (json != null) {
            try {
                mAccepted = json.getBoolean("order_accepted");
                mDelivered = json.getBoolean("order_delivered");
                mFailed = json.getBoolean("order_failed");
                mDeliveryEstimate = json.getInt("delivery_estimate");

                Log.i("checkDeliveryStatus", mAccepted + " " + mDelivered + " " + mFailed);

                if (mDeliveryEstimate == -1) {
                    mDeliveryEstimate = 15;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}