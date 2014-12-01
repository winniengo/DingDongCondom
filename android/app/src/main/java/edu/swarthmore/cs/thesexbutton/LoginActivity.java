/**
 * Created by wngo1 on 11/23/14.
 *
 * Main Activity
 */

package edu.swarthmore.cs.thesexbutton;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
    String mAccessToken, mAccessTokenExpires, mDeviceUUID, mPassphrase;

    Context c = LoginActivity.this;
    //SavedSharedPreferences mSavedSharedPreferences = new SavedSharedPreferences();
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

         /*
        mAccessToken = mSavedSharedPreferences.getSessionToken(c);
        mAccessTokenExpires = mSavedSharedPreferences.getSessionTokenExpires(c);
        mDeviceUUID = mSavedSharedPreferences.getDeviceUUID(c);
        mPassphrase = mSavedSharedPreferences.getPassphrase(c);
        */

        mSharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        mAccessToken = mSharedPreferences.getString("access_token", null);
        mAccessTokenExpires = mSharedPreferences.getString("access_token_expires", null);
        mDeviceUUID = mSharedPreferences.getString("device_uuid", null);
        mPassphrase = mSharedPreferences.getString("passphrase", null);

        if(mAccessToken==null) {
            if(mDeviceUUID==null) { // new user, call Register Activity
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                //finish();
            }
            else { // call Login Activity, retrieve valid access token
                Login(mDeviceUUID, mPassphrase);
                // switch to Request Condom Activity
                Intent i = new Intent(LoginActivity.this, RequestCondomActivity.class);
                startActivity(i);
            }
        }
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

                /*
                mSavedSharedPreferences.setSessionToken(c, sessionToken);
                mSavedSharedPreferences.setSessionTokenExpires(c, sessionTokenExpires);
                */

                SharedPreferences.Editor edit = mSharedPreferences.edit();
                edit.putString("session_token", sessionToken);
                edit.putString("session_token_expires", sessionTokenExpires);
                edit.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

}