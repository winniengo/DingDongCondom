package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class SurveyActivity extends Activity
{
    private JSONObject mSurveyJson;
    private String mResponse;
    private String mSurveyBody;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mSurveyJson = new JSONObject(getIntent().getStringExtra("survey"));
        } catch (JSONException e) {
            // TODO: display error to user?
        }

        if(mSurveyJson != null){
            try{
                mSurveyBody = mSurveyJson.getString("survey_body");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mTextView = (TextView) findViewById(R.id.survey_link);
        mTextView.setText(mSurveyBody);
    }
}
