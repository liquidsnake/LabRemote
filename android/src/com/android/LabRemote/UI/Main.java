/** 
 * Application main layout 
 * Appears after autentification and lets us choose a specific view
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.LabRemote.R;
import com.android.LabRemote.Utils.CustomDate;


public class Main extends Activity {

	private Intent mTimetableIntent, mCurrentIntent;
	private TextView mTimetableButton, mSearchButton;
	private TextView mCurrentButton;
	private Button mExitButton;
	private String currentGroup = "333 CC";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		setContentView(R.layout.main);

		mTimetableIntent = new Intent(this, DayList.class);
		mCurrentIntent = new Intent(this, GroupView.class);

		initMenuButtons();
	}
	
	/**
	 * Initialize menu buttons
	 * On click event, a button takes us to the selected view
	 */
	private void initMenuButtons() {
		
		/** Timetable button */
		mTimetableButton = (TextView) findViewById(R.id.timetableButton);
		mTimetableButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(mTimetableIntent);
			}
		});

		/** Current course button */
		mCurrentButton = (TextView) findViewById(R.id.currentCourseButton);
		mCurrentButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mCurrentIntent.putExtra("Group", currentGroup);
				mCurrentIntent.putExtra("Date", CustomDate.getCurrentDate());
				startActivity(mCurrentIntent);
			}
		});

		/** Search button */
		mSearchButton = (TextView) findViewById(R.id.searchButton);
		mSearchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSearchRequested();
			}
		});

		/** Exit button */
		mExitButton = (Button) findViewById(R.id.exitButton);
		mExitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	/** 
	 * Passes search data to the searchable activity
	 * @see android.app.Activity#onSearchRequested()
	 */
	public boolean onSearchRequested() {
		startSearch(null, false, null, false); 
		return true;
	}
}