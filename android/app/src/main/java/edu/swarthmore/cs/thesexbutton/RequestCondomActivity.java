package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.List;

public class RequestCondomActivity extends Activity implements AdapterView.OnItemSelectedListener {
    String mSessionToken = null;
    String mDormName = null;
    String mDeliveryType = null;
    EditText mDormNumber;
    String mDormNumberString = null;
    boolean mDeliveryTypeFilled = false;
    boolean mDormFilled = false;
    Button mRequestButton;
    List<NameValuePair> mParams;
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_condom);

        mSharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        mSessionToken = mSharedPreferences.getString("session_token", null);

        // Allow networking in the main thread
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Generate dorm name spinner
        Spinner spinner = (Spinner) findViewById(R.id.dorms_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dorms_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // Dorm number field
        mDormNumber = (EditText)findViewById(R.id.dorm_number);
        mDormNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mDormNumberString = s.toString();
                mDormFilled = true;
                if (mDeliveryTypeFilled) {
                    mRequestButton.setEnabled(true);
                    mRequestButton.setBackgroundColor(getResources().getColor(R.color.green));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // do nothing
            }
        });

        mRequestButton = (Button) findViewById(R.id.request_condom_button);
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestButton.setEnabled(false);

                mParams = new ArrayList<NameValuePair>();
                mParams.add(new BasicNameValuePair("session_token", mSessionToken));
                mParams.add(new BasicNameValuePair("dorm_name", mDormName));
                mParams.add(new BasicNameValuePair("dorm_room", mDormNumberString));
                mParams.add(new BasicNameValuePair("delivery_type", mDeliveryType));

                ServerRequest serverRequest = new ServerRequest();
                JSONObject json = serverRequest.getJSON("http://tsb.sccs.swarthmore.edu:8080/api/delivery/request", mParams);

                if (json != null) {
                    try {
                        String jsonString = json.getString("response");
                        String orderNumber = json.getString("order_number");

                        Toast.makeText(getApplication(),jsonString,Toast.LENGTH_LONG).show();
                        Log.d("Order Requested:", orderNumber);

                        // call Delivery Status Activity
                        Intent i = new Intent(RequestCondomActivity.this, DeliveryStatusActivity.class);
                        Bundle b = new Bundle();
                        b.putString("order_number", orderNumber);
                        i.putExtras(b);
                        startActivity(i);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    // Radio Button method
    public void onRadioButtonClicked(View view) {
        mDeliveryTypeFilled = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.request_condom_delivery_type_radio_to_lounge:
                if (mDeliveryTypeFilled)
                    mDeliveryType = "Lounge";
                break;
            case R.id.request_condom_delivery_type_radio_to_room:
                if (mDeliveryTypeFilled)
                    mDeliveryType = "Room";
                break;
        }

        if(mDormFilled) {
            mRequestButton.setEnabled(true);
            mRequestButton.setBackgroundColor(getResources().getColor(R.color.green));
        }
    }


    // Spinner methods
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        mDormName = (String) parent.getItemAtPosition(pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        mDormName = null;
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
        return (id == R.id.action_settings || super.onOptionsItemSelected(item));
    }
}