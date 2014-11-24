/**
 * Created by wngo1 on 11/23/14.
 */

package edu.swarthmore.cs.thesexbutton;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.provider.Settings.Secure;
import java.util.UUID;

public class RegisterActivity extends Activity {
    EditText mSignupToken;
    Button mRegister;
    String mSignupTokenString, mDeviceUUID, mDeviceOS, mPassphrase;
    List<NameValuePair> mParams;

    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mSignupToken = (EditText)findViewById(R.id.signupToken);
        mRegister = (Button)findViewById(R.id.registerButton);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignupTokenString = mSignupToken.getText().toString();
                mDeviceOS = "ANDROID_OS";
                mPassphrase = UUID.randomUUID().toString();
                mDeviceUUID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
                if(mDeviceUUID.equals(null)) {
                    mDeviceUUID = UUID.randomUUID().toString();
                }


                mParams = new ArrayList<NameValuePair>();
                mParams.add(new BasicNameValuePair("signup_token", mSignupTokenString));
                mParams.add(new BasicNameValuePair("device_uuid", mDeviceUUID));
                mParams.add(new BasicNameValuePair("device_os", mDeviceOS));
                mParams.add(new BasicNameValuePair("passphrase", mPassphrase));

                ServerRequest serverRequest = new ServerRequest();
                JSONObject json = serverRequest.getJSON("http://tsb.sccs.swarthmore.edu:8080/register", mParams);

                if(json != null){
                    try{
                        String jsonString = json.getString("response");
                        String sessionToken = json.getString("session_token");
                        String sessionTokenExpires = json.getString("session_token_expires");

                        SharedPreferences.Editor edit = mSharedPreferences.edit();
                        edit.putString("session_token", sessionToken);
                        edit.putString("session_token_expires", sessionTokenExpires);
                        edit.putString("device_uuid", mDeviceUUID);
                        edit.putString("secret", mPassphrase);
                        edit.commit();

                        Toast.makeText(getApplication(),jsonString,Toast.LENGTH_LONG).show();
                        Log.d("Hello!", jsonString);
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}