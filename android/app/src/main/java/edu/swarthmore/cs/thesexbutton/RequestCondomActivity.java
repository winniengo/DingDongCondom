package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RequestCondomActivity extends Activity implements AdapterView.OnItemSelectedListener {
    String mSessionToken = SavedSharedPreferences.getSessionToken(RequestCondomActivity.this);
    String mDormName = null;
    String mDormNumber = null;
    String mDeliveryType = null;
    Button mRequestButton;
    List<NameValuePair> mParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_condom);

        // allow networking in the main thread
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // generate dorm name spinner
        Spinner spinner = (Spinner) findViewById(R.id.dorms_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dorms_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // collect delivery details
        onRadioButtonClicked(findViewById(R.id.request_condom_delivery_type_radio_group));
        spinner.setOnItemSelectedListener(this);
        EditText DormNumber = (EditText)findViewById(R.id.dorm_number);
        mDormNumber = DormNumber.getText().toString();

        // submit request
        mRequestButton = (Button) findViewById(R.id.request_condom_button);
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParams = new ArrayList<NameValuePair>();
                mParams.add(new BasicNameValuePair("session_token", mSessionToken));
                mParams.add(new BasicNameValuePair("dorm_name", mDormName));
                mParams.add(new BasicNameValuePair("dorm_room", mDormNumber));
                mParams.add(new BasicNameValuePair("delivery_type", mDeliveryType));

                ServerRequest serverRequest = new ServerRequest();
                JSONObject json = serverRequest.getJSON("http://tsb.sccs.swarthmore.edu:8080/delivery/request", mParams);

                if (json != null){
                    try{
                        String jsonString = json.getString("response");
                        String orderNumber = json.getString("order_number");

                        Toast.makeText(getApplication(),jsonString,Toast.LENGTH_LONG).show();
                        // send notification email
                        generateDeliveryEmail(mDormName, mDormNumber, mDeliveryType, orderNumber);
                        Log.d("Order Requested:", orderNumber);

                        // call Delivery Status Activity
                        Intent i = new Intent(RequestCondomActivity.this, DeliveryStatusActivity.class);
                        Bundle b = new Bundle();
                        b.putString("order_number", orderNumber);
                        b.putString("session_token", mSessionToken);
                        i.putExtras(b);
                        startActivity(i);
                        //finish();
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.request_condom, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Radio Button method
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        // check which radio button was clicked
        switch(view.getId()) {
            case R.id.request_condom_delivery_type_radio_to_lounge:
                if (checked)
                    // Lounge Delivery
                    mDeliveryType = "Lounge";
                break;
            case R.id.request_condom_delivery_type_radio_to_room:
                if (checked)
                    // Room delivery
                    mDeliveryType = "Room";
                break;
        }
    }


    // Spinner methods
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        mDormName = (String) parent.getItemAtPosition(pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        mDormName = null;
    }

    // Mail method
    private void generateDeliveryEmail(String dorm_name, String dorm_number, String delivery_type, String order_number) {
        Mail m = new Mail("tsbdaemon@gmail.com", "9854<>47f?8l05X");

        String[] recipients = {"tsbdaemon@gmail.com"};
        String body = "Condom Delivery Request\n\n";

        body += "Time and Date submitted:\t\t";
        body += new Date().toString();

        body += "Order number:\t\t";
        body += order_number;

        /*
        body += "\nDevice number:\t\t\t\t\t";
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String id = telephonyManager.getDeviceId();
        body += id;
        */

        body += "\n\nDorm Name:\t\t\t\t\t";
        body += dorm_name;

        //get the delivery choice
        body += "\n\nDelivery Type:";
        body += "\t\t\t\t\t" + delivery_type;

        // get the dorm room number
        body += "\n\nFloor/Room Number:\t\t\t";
        body += dorm_number;

        m.set_to(recipients);
        m.set_from("tsbdaemon@gmail.com");
        m.set_subject("Delivery Request from user");
        m.set_body(body);

        try {
            if(m.send()) {
                Toast.makeText(RequestCondomActivity.this, "Request was sent successfully", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(RequestCondomActivity.this, "Request failed. Please try again.", Toast.LENGTH_LONG).show();
            }
        } catch(Exception e) {
            //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
            Log.e("MailApp", "Could not send email", e);
        }
    }

}
