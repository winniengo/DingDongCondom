package edu.swarthmore.cs.thesexbutton;

import android.content.Context;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

/**
 * Created by wngo1 on 12/1/14.
 */
public class CondomRequestStore {
    private static CondomRequestStore sCondomRequestStore;
    private ArrayList<CondomRequest> mCondomRequests;
    private static final String TAG = "CondomRequestStore";
    private static final String API = "http:///tsb.sccs.swarthmore.edu:8080/api/";

    private String mSessionToken;
    List<NameValuePair> mParams;
    private CondomRequestStore(Context context) {
        //TODO get session_token
        try {
            mParams = new ArrayList<NameValuePair>();
            final ServerRequest serverRequest = new ServerRequest();
            mParams.add(new BasicNameValuePair("session_token", mSessionToken));
            JSONObject json = serverRequest.getJSON(API + "delivery/requests/all", mParams);
            if (json != null) {
                try {
                    String jsonString = json.getString("response");
                    // TODO what actually gets returned
                    // parse into individual CondomRequest and List

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            mCondomRequests = new ArrayList<CondomRequest>();
            Log.e(TAG, "Error loading Health Events");
        }
    }

    public static CondomRequestStore get(Context c) {
        if(sCondomRequestStore == null) {
            sCondomRequestStore = new CondomRequestStore(c.getApplicationContext());
        }
        return sCondomRequestStore;
    }

    public ArrayList<CondomRequest> getCondomRequests() {
        return mCondomRequests;
    }

    public CondomRequest getCondomRequest(String orderNumber) {
        for(CondomRequest condomRequest : mCondomRequests) {
            if(condomRequest.getOrderNumber().equals(orderNumber)) {
                return condomRequest;
            }
        }
        return null; // no CondomRequest exists with given order number
    }
}
