/**
 * Created by wngo1 on 11/23/14.
 *
 * Main Activity
 */

package edu.swarthmore.cs.thesexbutton;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
//    import com.google.android.gms.common.ConnectionResult;
//    import com.google.android.gms.common.GooglePlayServicesUtil;
//    import com.google.android.gms.gcm.GoogleCloudMessaging;
//    import java.io.IOException;
//    import java.util.Date;
//    import java.util.concurrent.atomic.AtomicInteger;

    String mAccessToken, mAccessTokenExpires, mDeviceUUID, mPassphrase;
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {  // sleep 1.5 seconds
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
                        mAccessToken = mSharedPreferences.getString("access_token", null);
                        mAccessTokenExpires = mSharedPreferences.getString("access_token_expires", null);
                        mDeviceUUID = mSharedPreferences.getString("device_uuid", null);
                        mPassphrase = mSharedPreferences.getString("passphrase", null);

                        if (mAccessToken == null) {
                            if (mDeviceUUID == null) {
                                // New user; call Register Activity
                                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                // Call Login, retrieve access token, call Request Condom Activity
                                Login(mDeviceUUID, mPassphrase);
                                Intent i = new Intent(LoginActivity.this, RequestCondomActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    public void Login(String deviceUUID, String passphrase) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("device_uuid", deviceUUID));
        params.add(new BasicNameValuePair("passphrase", passphrase));

        ServerRequest serverRequest = new ServerRequest();
        JSONObject json = serverRequest.getJSON("http://tsb.sccs.swarthmore.edu:8080/api/login", params);

        if (json != null) {
            try {
                String jsonString = json.getString("response");
                String sessionToken = json.getString("session_token");
                String sessionTokenExpires = json.getString("session_token_expires");

                SharedPreferences.Editor edit = mSharedPreferences.edit();
                edit.putString("session_token", sessionToken);
                edit.putString("session_token_expires", sessionTokenExpires);
                edit.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}