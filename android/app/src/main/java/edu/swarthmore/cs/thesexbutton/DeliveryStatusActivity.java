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
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeliveryStatusActivity extends Activity {
    String mOrderNumber, mSessionToken;

    SharedPreferences mSharedPreferences;

    int mDeliveryEstimate = 15;
    boolean mAccepted = false;
    boolean mDelivered = false;
    boolean mFailed = false;

    private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler(); // used to queue code execution on thread
    private int mProgressDialogStatus;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status);

        mSharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        mSessionToken = mSharedPreferences.getString("session_token", null);
        mOrderNumber = mSharedPreferences.getString("order_number", null);

        Thread t;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mAccepted) {

                    checkDeliveryStatus(); // check delivery status via JSON
                    try { // sleep 5 seconds
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
                        Button restart = (Button) findViewById(R.id.restartButton);
                        restart.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(DeliveryStatusActivity.this, RequestCondomActivity.class);
                                startActivity(i);
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
        mProgressDialog.setTitle("Delivering...");
        mProgressDialog.setMessage("Condom is on its way!\nEstimated delivery time: " +
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
                int counter = 0;
                while (!mDelivered) {
                    // check delivery status every 10 seconds
                    checkDeliveryStatus();

                    if(mDelivered) {
                        mProgressDialogStatus = mProgressDialog.getMax();
                    }
                    else { // if loading bar isn't full, update every minute
                        if(mProgressDialogStatus < mProgressDialog.getMax() && counter%6 == 0) {
                            mProgressDialogStatus++;
                            // update the progress bar
                            mHandler.post(new Runnable() {
                                public void run() {
                                    mProgressDialog.setProgress(mProgressDialogStatus);
                                }
                            });
                        }
                    }

                    try { // sleep 10 seconds
                        Thread.sleep(00000);
                        counter++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // condom has been delivered
                mProgressDialog.setMessage("Thanks for waiting!");
                try { // sleep 1.5 seconds, display 100%
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // close the progress bar dialog
                 mProgressDialog.dismiss();
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

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}