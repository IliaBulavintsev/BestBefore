package com.rv150.bestbefore.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.rv150.bestbefore.R;

/**
 * Created by ivan on 20.02.17.
 */

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_about);
    }

    public void closeActivity (View view) {
        finish();
    }
}
