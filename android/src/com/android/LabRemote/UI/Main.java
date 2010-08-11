/* 
 * Application main layout, after autentification
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.UI;

import com.android.LabRemote.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity {
	
	private Intent mTimetableIntent, mCurrentIntent, mSearchIntent;
	private Button mTimetableButton, mCurrentButton, mExitButton, mSearchButton;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        mTimetableIntent = new Intent(this, TimeTable.class);
        mCurrentIntent = new Intent(this, GroupView.class);
        mSearchIntent = new Intent(this, IndividualView.class);
        
        setContentView(R.layout.main);
        
        mTimetableButton = (Button) findViewById(R.id.timetableButton);
        mTimetableButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(mTimetableIntent);				
			}
		});
        
        /* Current course button */
        mCurrentButton = (Button) findViewById(R.id.currentCourseButton);
        mCurrentButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(mCurrentIntent);				
			}
		});
        
        /* Search button */
        mSearchButton = (Button) findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(mSearchIntent);				
			}
		});
        
        /* Exit button */
        mExitButton = (Button) findViewById(R.id.exitButton);
        mExitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

    }
}