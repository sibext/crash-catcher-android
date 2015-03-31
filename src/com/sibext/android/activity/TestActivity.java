package com.sibext.android.activity;

import android.os.Bundle;
import android.view.View;

import com.sibext.crashcatcher.R;

/**
 * Created by santaev on 3/25/15.
 */
public class TestActivity extends CrashCatcherActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer i = null;
                i.byteValue();
            }
        });


    }
}
