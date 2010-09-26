/**
 * Main.java
 *     
 * Copyright (C) 2010 LabRemote Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.LabRemote.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.LabRemote.R;
import com.android.LabRemote.Utils.CustomDate;

/** 
 * Application's main menu layout <br>
 * Appears after autentification and lets us choose a specific view
 */
public class Main extends Activity {

	/** Requests that a child activity returns with error message 
	 * if there was a server communication error during its initialization */
	public static final int REQUEST_FROM_SERVER = 1;
	/** Starts current group activity on click */
	private FrameLayout mCurrentButton;
	/** Starts current timetable activity on click */
	private FrameLayout mTimetableButton;
	/** Starts current search activity on click */
	private FrameLayout mSearchButton;
	/** Starts current settings activity on click */
	private FrameLayout mSettingsButton;
	/** Intent for timetable activity */
	private Intent mTimetableIntent;
	/** Intent for current group activity */
	private Intent mCurrentIntent;
	/** Intent for settings activity */
	private Intent mSettingsIntent;

	/**
	 * Initialize menu buttons
	 * On click event, a button takes us to the selected view
	 */
	private void initMenuButtons() {

		/** Timetable button */
		mTimetableButton = (FrameLayout) findViewById(R.id.timetableButton);
		mTimetableButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivityForResult(mTimetableIntent, REQUEST_FROM_SERVER);
			}
		});

		/** Current course button */
		mCurrentButton = (FrameLayout) findViewById(R.id.currentCourseButton);
		mCurrentButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mCurrentIntent.putExtra("Group", "");
				mCurrentIntent.putExtra("Current", true); 
				mCurrentIntent.putExtra("Date", CustomDate.getCurrentDate());
				startActivityForResult(mCurrentIntent, REQUEST_FROM_SERVER);
			}
		});

		/** Search button */
		mSearchButton = (FrameLayout) findViewById(R.id.searchButton);
		mSearchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSearchRequested();
			}
		});
		
		/** Settings button */
		mSettingsButton = (FrameLayout) findViewById(R.id.otherButton);
		mSettingsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(mSettingsIntent);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == Activity.RESULT_CANCELED) 
	    	if (data != null)
	    		if (data.getStringExtra("serverError") != null)
	    			Toast.makeText(this, data.getStringExtra("serverError"), 1).show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		setContentView(R.layout.main);

		mTimetableIntent = new Intent(this, TimeTable.class);
		mCurrentIntent = new Intent(this, GroupView.class);
		mSettingsIntent = new Intent(this, Settings.class);

		initMenuButtons();
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