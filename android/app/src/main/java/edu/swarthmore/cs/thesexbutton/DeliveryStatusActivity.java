package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wngo1 on 11/29/14.
 */
public class DeliveryStatusActivity extends Activity {
    Bundle b = getIntent().getExtras();
    String mOrderNumber = b.getString("order_number");
    String mSessionToken = b.getString("session_token");

    int mDeliveryEstimate = 15;
    boolean mAccepted = false;
    boolean mDelivered = false;
    boolean mFailed = false;

    private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler(); // used to queue code execution on thread
    private int mProgressDialogStatus;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status);

        checkDeliveryStatus(); // check delivery status via JSON
        if (mAccepted== true) {
            launchProgressDialog(DeliveryStatusActivity.this);
        }
    }

    // launches and loads progress bar
    public void launchProgressDialog(Context context) {
        // prepare for a progress bar dialog
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle("Delivering...");
        mProgressDialog.setMessage("Condom is on its way! Please wait...");
        mProgressDialog.setCancelable(false); // dialog can't be cancelled by pressing back
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(mDeliveryEstimate);
        mProgressDialog.setProgress(0); // set the current progress to zero
        mProgressDialog.show();

        //reset progress bar status
        mProgressDialogStatus = 0;

        Thread thread = new Thread(new Runnable() { // used to execute in parallel with UI thread
            public void run() {
                while (mProgressDialogStatus < mProgressDialog.getMax()) {

                    // check delivery status
                    checkDeliveryStatus();
                    if(mDelivered == true) {
                        mProgressDialogStatus = mProgressDialog.getMax();
                    }
                    else {
                        mProgressDialogStatus++;
                    }

                    try { // sleep 10 seconds
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgressDialog.setProgress(mProgressDialogStatus);
                        }
                    });
                }

                // condom has been delivered
                if (mProgressDialogStatus >= mProgressDialog.getMax()) {
                    try { // sleep 2 seconds, display 100%
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // close the progress bar dialog
                    mProgressDialog.dismiss();
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
            }
        });

        thread.start();

    }

    public void checkDeliveryStatus() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("session_token", mSessionToken));
        params.add(new BasicNameValuePair("order_number", mOrderNumber));

        ServerRequest serverRequest = new ServerRequest();
        JSONObject json = serverRequest.getJSON("http://tsb.sccs.swarthmore.edu:8080/delivery/status", params);

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