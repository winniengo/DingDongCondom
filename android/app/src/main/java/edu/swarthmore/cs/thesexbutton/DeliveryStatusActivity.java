package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.os.Handler;

/**
 * Created by wngo1 on 11/29/14.
 */
public class DeliveryStatusActivity extends Activity {
    Bundle b = getIntent().getExtras();
    String mOrderNumber = b.getString("order_number");
    boolean mOrderAccepted, mOrderDelivered, mOrderFailed;
    Integer mDeliveryEstimate;

    private ProgressDialog mProgressDialog;
    private Thread mThread; // used to execute code in parallel with UI thread
    private Handler mHandler; // used to queue code execution on thread

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(DeliveryStatusActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Delivering...");
        mProgressDialog.setMessage("Condoms on their way! Please wait...");
        mProgressDialog.setCancelable(false); // dialog can't be cancelled by pressing back
        mProgressDialog.setIndeterminate(false);
        //mProgressDialog.setMax(100); // the max number of progress items is 100
        mProgressDialog.setMax(mDeliveryEstimate);
        mProgressDialog.setProgress(0); // set the current progress to zero
        mProgressDialog.show();

        mHandler = new Handler();
        mThread = new Thread("ProgressDialogThread");
        mThread.start();
    }

    int counter = 0;

    public void run() {
        try
        {
            synchronized(mThread) // obtain the thread's token
            {
                while(counter <= 4)
                {
                    mThread.wait(850); // wait 850 milliseconds
                    counter++; // increment counter

                    mHandler.post(new Runnable() { // update changes to the UI thread
                        @Override
                        public void run() {
                            mProgressDialog.setProgress(counter*25);
                        }
                    });
                }
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //Close the progress dialog
                mProgressDialog.dismiss();

                //Call the application's main View
                setContentView(R.layout.activity_delivery_status);
            }
        });

        //Try to "kill" the thread, by interrupting its execution
        synchronized (mThread) {
            mThread.interrupt();
        }
    }
}