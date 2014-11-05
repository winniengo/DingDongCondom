package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Date;
import java.util.UUID;


public class RequestCondomActivity extends Activity implements AdapterView.OnItemSelectedListener {


    private String m_dorm_name;
    private String m_dorm_room;

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


        // set up da spinner
        Spinner spinner = (Spinner) findViewById(R.id.dorms_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dorms_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);


        Button request_button = (Button) findViewById(R.id.request_condom_button);
        request_button.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                generateDeliveryEmail(m_dorm_name);
                                              }
        });


    }

    // spinner methods


    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        m_dorm_name = (String) parent.getItemAtPosition(pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        m_dorm_name = "";
    }






    private void generateDeliveryEmail(String dorm_name) {
        Mail m = new Mail("tsbdaemon@gmail.com", "9854<>47f?8l05X");

        String[] recipients = {"tsbdaemon@gmail.com"};
        String body = "Condom Delivery Request\n\n";

        body += "Time and Date submitted:\t\t";
        body += new Date().toString();



        // get the user's IMEI number
        //!!! VERY QUESTIONABLE, DO DEFINITELY REMOVE FOR ANY PUBLIC RELEASE
        body += "\nDevice number:\t\t\t\t\t";

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String id = telephonyManager.getDeviceId();

        body += id;

        //get the text field contents


        // get the dorm name
        body += "\n\nDorm Name:\t\t\t\t\t";
        //EditText dorm_name = (EditText) findViewById(R.id.request_condom_edit_text_dorm_name);
        body += dorm_name;


        // get the dorm room number
        body += "\n\nDorm Room Number:\t\t\t";
        EditText dorm_room = (EditText) findViewById(R.id.request_condom_edit_text_dorm_room);
        body += dorm_room.getText();


        //get the delivery choice
        body += "\n\nDelivery Type:";

        RadioGroup radio_group = (RadioGroup) findViewById(R.id.request_condom_delivery_type_radio_group);
        int checked_button_id = radio_group.getCheckedRadioButtonId();

        if (checked_button_id == R.id.request_condom_delivery_type_radio_to_room) {
            body += "\t\t\t\t\tTo Room";
        } else if (checked_button_id == R.id.request_condom_delivery_type_radio_to_lounge) {
            body += "\t\t\t\t\tTo Lounge of Hall";
        }



        m.set_to(recipients);
        m.set_from("tsbdaemon@gmail.com");
        m.set_subject("Delivery Request from a user");



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



}
