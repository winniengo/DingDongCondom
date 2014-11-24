/**
 * Created by wngo1 on 11/23/14.
 */

package edu.swarthmore.cs.thesexbutton;
import android.app.Activity;
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
    EditText mEmail, mPassword;
    Button mLogin, mRegister;
    String mEmailString, mPasswordString;
    List<NameValuePair> mParams;
    SharedPreferences mSharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
    String mAccessToken, mAccessTokenExpires, mDeviceUUID, mPassphrase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAccessToken = mSharedPreferences.getString("access_token", null);
        mAccessTokenExpires = mSharedPreferences.getString("access_token_expires", null);
        mDeviceUUID = mSharedPreferences.getString("device_uuid", null);
        mPassphrase = mSharedPreferences.getString("secret", null);

        if(mAccessToken.equals(null)) {
            if(mDeviceUUID.equals(null)) { // new user
                Intent i = new Intent(LoginActivity.this, edu.swarthmore.cs.thesexbutton.RegisterActivity.class);
                startActivity(i);
                finish();
            }
            else { // retrieve valid access token
                Login(mDeviceUUID, mPassphrase);
            }
        }

        Intent i = new Intent(LoginActivity.this, edu.swarthmore.cs.thesexbutton.RequestCondomActivity.class);
        startActivity(i);
        finish();
    }

    public void Login(String deviceUUID, String passphrase) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("device_uuid", deviceUUID));
        params.add(new BasicNameValuePair("secret", passphrase));

        ServerRequest serverRequest = new ServerRequest();
        JSONObject json = serverRequest.getJSON("httpL//tsb.sccs.swarthmore.edu:8080/login", params);

        if (json != null) {
            try {
                String jsonString = json.getString("response");
                String sessionToken = json.getString("session_token");
                String sessionTokenExpires = json.getString("session_token_expries");

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