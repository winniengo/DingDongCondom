package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class LoginActivity extends Activity {
    String mAccessToken, mAccessTokenExpires, mDeviceUUID, mPassphrase;
    SharedPreferences mSharedPreferences;

    // GCM vars
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String TAG = "LoginActivity";
    String SENDER_ID = "764780160177";  // GCM project number
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String regid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();
        gcm = GoogleCloudMessaging.getInstance(this);

        // Allow networking in the main thread
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

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
                        // Make sure device has Play Services APK and register for GCM
                        if (checkPlayServices()) {
                            regid = getRegistrationId(context);
                            if (regid.isEmpty()) {
                                registerInBackground();
                            } else {
                                Log.i(TAG, "Prev gcm regid found: " + regid);
                            }
                        } else {
                            Log.i(TAG, "No valid Google Play Services APK found.");
                        }

                        // Look for previous registration
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


    /**
     * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a
     * dialog that allows users to enable it or download it from the Play Store.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    /**
     * Gets the current registration ID for application on GCM service.
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        // If app was updated, it must clear the registration ID since the existing regID
        // is not guaranteed to work with the new app version
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }

        return registrationId;
    }


    /**
     * return Application's sharedPreferences.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(RequestCondomActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }


    /**
     * return Application's version code from the PackageManager.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);  // should never happen
        }
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * Stores the registration ID and app versionCode in the application's shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered; regid=" + regid;

                    sendRegistrationIdToBackend();
                    storeRegistrationId(context, regid);  // persist the regID
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    finish();
                }
                Log.i(TAG, msg);
                return msg;
            }
        }.execute(null, null, null);
    }


    /**
     * Sends the registration ID to your server over HTTP
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }


    /**
     * Stores the registration ID and app versionCode in the application's
     * SharedPreferences
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }


    /**
     * Requests login to our server, sending deviceID, passphrase, and gcm regid.
     * Server sends back an authorization token that is stored in sharedPreferences.
     */
    public void Login(String deviceUUID, String passphrase) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("device_uuid", deviceUUID));
        params.add(new BasicNameValuePair("passphrase", passphrase));
        params.add(new BasicNameValuePair("push_id", regid));

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