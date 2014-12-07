package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class SurveyActivity extends Activity
{
    private JSONObject mSurveyJson;
    // private String mResponse;  // use to debug
    private String mSurveyBody;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        try {
            mSurveyJson = new JSONObject(getIntent().getStringExtra("survey"));
        } catch (JSONException e) {
        }

        if(mSurveyJson != null) {
            try {
                mSurveyBody = mSurveyJson.getString("survey_body");
            } catch (JSONException e) {
            }
        }

        Log.i("Survey Body", mSurveyBody);
        mTextView = (TextView) findViewById(R.id.survey_link);
        mTextView.setText(mSurveyBody);
    }
}
