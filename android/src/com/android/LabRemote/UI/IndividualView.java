/* 
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

import com.android.LabRemote.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class IndividualView extends Activity {
	private ListView attendance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.individual_view);
		
		/* Attendance/Grade list */
		attendance = (ListView)findViewById(R.id.individualList);
        String[] from = new String[] {"index", "grade"};
        int[] to = new int[] { R.id.labIndex, R.id.labGrade};

        List<HashMap<String, String>> contentMap = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < 10; i++){
        	HashMap<String, String> map = new HashMap<String, String>();
            map.put("index", " L" + i);
            map.put("grade", " " + i);
            contentMap.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, contentMap, R.layout.individual_grade, from, to);
        attendance.setAdapter(adapter);
        
	}
		 
}
