package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopupActivity extends Activity {
    PopupWindow mPopUp;
    LinearLayout mLayout;
    LinearLayout mMainLayout;
    TextView mTextView;
    LayoutParams mParams;
    Button mButton;
    boolean click = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPopUp = new PopupWindow(this);
        mLayout = new LinearLayout(this);
        mMainLayout = new LinearLayout(this);
        mTextView = new TextView(this);

        mButton = new Button(this);
        mButton.setText("Click Me");
        mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (click) {
                    mPopUp.showAtLocation(mMainLayout, Gravity.BOTTOM, 10, 10);
                    mPopUp.update(50, 50, 300, 80);
                    click = false;
                } else {
                    mPopUp.dismiss();
                    click = true;
                }
            }
        });

        mParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mTextView.setText("Hi this is a sample text for popup window");
        mLayout.addView(mTextView, mParams);
        mPopUp.setContentView(mLayout);
        mPopUp.showAtLocation(mLayout, Gravity.BOTTOM, 10, 10);
        mMainLayout.addView(mButton, mParams);
        setContentView(mMainLayout);
    }
}