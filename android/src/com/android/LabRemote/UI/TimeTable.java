/* 
 * Timetable view
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.UI;

import com.android.LabRemote.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class TimeTable extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.time_table);
		
		/* Test code */
		TextView test = (TextView) findViewById(R.id.tuesday_12);
		test.setClickable(true);
		test.setLongClickable(true);
		
		test.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Toast.makeText(v.getContext(), "click mic", 1).show();	
				System.out.println("click mic");
			}
		});
		
		test.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				Toast.makeText(v.getContext(), "click mare", 1).show();	
				System.out.println("click mare");
				return false;
			}
		});
	}
}
