/** 
 * Informations about a selected student
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.UI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.LabRemote.R;

public class IndividualView extends Activity {
	private ListView attendance;
	private String mName, mGroup, mDate;
	private List<HashMap<String, String>> contentMap;
	private SimpleAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.individual_view);
		
		receiveData();
		
		/* Attendance/Grade list */
		fillList();
        attendance.setAdapter(mAdapter);
  
	}
	
	/**
	 * Receive data from the previous activity
	 */
	private void receiveData() {
		
		mName = getIntent().getStringExtra("Name"); 
		TextView nameView = (TextView)findViewById(R.id.individualName);
		nameView.setText(mName);
		mGroup = getIntent().getStringExtra("Group"); 
		TextView groupView = (TextView)findViewById(R.id.classHeader);
		groupView.setText(mGroup);
		TextView groupStudentView = (TextView)findViewById(R.id.individualGroup);
		groupStudentView.setText(mGroup);
		mDate = getIntent().getStringExtra("Date"); 
		TextView dateView = (TextView)findViewById(R.id.dateHeader);
		dateView.setText(mDate);
	}
	
	/**
	 * Fill list with grades
	 */
	private void fillList() {
		
		attendance = (ListView)findViewById(R.id.individualList);
        String[] from = new String[] {"index", "grade"};
        int[] to = new int[] { R.id.labIndex, R.id.labGrade};

        contentMap = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < 10; i++){
        	HashMap<String, String> map = new HashMap<String, String>();
            map.put("index", " L" + i);
            map.put("grade", " " + i);
            contentMap.add(map);
        }
        
        mAdapter = new SimpleAdapter(this, contentMap, R.layout.individual_grade, from, to);
	}

}
