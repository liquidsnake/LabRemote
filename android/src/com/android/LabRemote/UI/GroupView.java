/* 
 * Listview - the students of a specific class
 * @see GroupViewItem
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.UI;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.android.LabRemote.R;
import com.android.LabRemote.Utils.GroupItem;


public class GroupView extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		setContentView(R.layout.group_view);
		
		ListView list = (ListView) findViewById(R.id.studentsList);
		ArrayList<GroupItem> mList = new ArrayList<GroupItem>();
			
		/* Test */
		for (int i = 0; i < 10; i++) {
			mList.add(new GroupItem(R.drawable.mi, "anca", i + " "));
		}

		GroupAdapter mAdapter = new GroupAdapter(this, mList);
		list.setAdapter(mAdapter);
	}
}






