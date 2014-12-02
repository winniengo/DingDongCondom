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
            mDateRequested = sdf.parse(requestedStr);
            mDateAccepted = sdf.parse(acceptedStr);
            mDateDelivered = sdf.parse(deliveredStr);
        } catch (ParseException e) {
            Log.e("DateTime Parser", "Problem parsing: " + requestedStr + acceptedStr + deliveredStr);
        }

        mDeliveryEstimate = json.getInt("delivery_estimate");
        mDeliveryDestination = json.getString("delivery_destination"); //TODO parse destination
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

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("order_number", mOrderNumber);
        json.put("order_accepted", mOrderAccepted);
        json.put("oder_delivered", mOrderDelivered);
        json.put("order_failed", mOrderFailed);
        json.put("date_requested", mDateRequested);
        json.put("date_accepted", mDateAccepted);
        json.put("date_delivered", mDateDelivered);
        json.put("delivery_estimate", mDeliveryEstimate);
        json.put("delivery_destination", mDeliveryDestination);

        return json;
    }
}
