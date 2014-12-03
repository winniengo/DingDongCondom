package edu.swarthmore.cs.thesexbutton;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wngo1 on 12/1/14.
 */
public class CondomRequest { // constructor
    private String mOrderNumber;

    private boolean mOrderAccepted;
    private boolean mOrderDelivered;
    private boolean mOrderFailed;

    private Date mDateRequested;
    private Date mDateAccepted;
    private Date mDateDelivered;

    private int mDeliveryEstimate;

    private String mDeliveryDestination;

    SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

    public CondomRequest(JSONObject json) throws JSONException {
        mOrderNumber = json.getString("order_number");
        mOrderAccepted = json.getBoolean("order_accepted");
        mOrderDelivered = json.getBoolean("order_delivered");
        mOrderFailed = json.getBoolean("order_failed");

        //mDateRequested = new Date(json.getLong("date_requested"));

        String requestedStr = json.getString("date_requested");
        String acceptedStr = json.getString("date_accepted");
        String deliveredStr = json.getString("date_delivered");
        try {
            if (mDateRequested!=null) {
                mDateRequested = sdf.parse(requestedStr);
            }
            if (mDateAccepted!=null) {
                mDateAccepted = sdf.parse(acceptedStr);
            }
            if (mDateDelivered!=null) {
                mDateDelivered = sdf.parse(deliveredStr);
            }
        } catch (ParseException e) {
            Log.e("DateTime Parser", "Problem parsing: " + requestedStr + acceptedStr + deliveredStr);
        }

        mDeliveryEstimate = json.getInt("delivery_estimate");

        // parse destination object into string
        JSONObject des = json.getJSONObject("delivery_destination");
        mDeliveryDestination = des.getString("dorm_name") +
                " - " + des.getString("delivery_type") +
                " - " + des.getString("dorm_room");

        Log.i("CondomRequest", "" + mOrderNumber + mDeliveryDestination);

    }

    public String getOrderNumber() {
        return mOrderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        mOrderNumber = orderNumber;
    }

    public boolean isOrderAccepted() {
        return mOrderAccepted;
    }

    public void setOrderAccepted(boolean orderAccepted) {
        mOrderAccepted = orderAccepted;
    }

    public boolean isOrderDelivered() {
        return mOrderDelivered;
    }

    public void setOrderDelivered(boolean orderDelivered) {
        mOrderDelivered = orderDelivered;
    }

    public boolean isOrderFailed() {
        return mOrderFailed;
    }

    public void setOrderFailed(boolean orderFailed) {
        mOrderFailed = orderFailed;
    }

    public Date getDateRequested() {
        return mDateRequested;
    }

    public void setDateRequested(Date dateRequested) {
        mDateRequested = dateRequested;
    }

    public Date getDateAccepted() {
        return mDateAccepted;
    }

    public void setDateAccepted(Date dateAccepted) {
        mDateAccepted = dateAccepted;
    }

    public Date getDateDelivered() {
        return mDateDelivered;
    }

    public void setDateDelivered(Date dateDelivered) {
        mDateDelivered = dateDelivered;
    }

    public int getDeliveryEstimate() {
        return mDeliveryEstimate;
    }

    public void setDeliveryEstimate(int deliveryEstimate) {
        mDeliveryEstimate = deliveryEstimate;
    }

    public String getDeliveryDestination() {
        return mDeliveryDestination;
    }

    public void setDeliveryDestination(String deliveryDestination) {
        mDeliveryDestination = deliveryDestination;
    }
}