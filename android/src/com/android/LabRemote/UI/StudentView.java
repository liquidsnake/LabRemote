/**
 * StudentView.java
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;
import com.android.LabRemote.Utils.CustomDate;

/** 
 * Informations about a selected student
 */
public class StudentView extends ListActivity {
	
	/** JSON data received from server */
	private JSONObject data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.individual_view);

		receiveData();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.cgroup:
	        Intent in = new Intent(this, GroupView.class);
	        startActivity(in);
	        return true;
	    case R.id.timetable:
	    	Intent inn = new Intent(this, TimeTable.class);
	        startActivity(inn);
	        return true;
	    case R.id.search:
	    	onSearchRequested();
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * Receive data from the previous activity
	 * name, group, date, course, stud_id
	 */
	private void receiveData() {

		/** Header information */
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String course = preferences.getString("course", null);
		TextView cv = (TextView) findViewById(R.id.courseName);
		cv.setText(course);
		String mDate = CustomDate.getCurrentDate();
		TextView dateView = (TextView)findViewById(R.id.dateHeader);
		dateView.setText(mDate);
		String mID = getIntent().getStringExtra("ID"); 

		/** From server */
		ServerResponse result = new Connection(this).getStudent(mID);
		data = (JSONObject) result.getRespone();
		if (data != null) 
			fillList();
		else
			exitServerError(result.getError());
	}

	/**
	 * Fill list with grades
	 */
	private void fillList() {
		List<HashMap<String, String>> contentMap = new ArrayList<HashMap<String, String>>();
	    String[] from = new String[] {"index", "grade"};
        int[] to = new int[] { R.id.labIndex, R.id.labGrade};
		int gradeSum = 0;
		
		try {
			JSONObject grades = data.getJSONObject("attendances"); 
            for(int i = 0; i < grades.length(); i++) {
                 HashMap<String, String> map = new HashMap<String, String>();
                 map.put("index", i+".");
                 map.put("grade", " " + grades.getJSONObject(i+"").getString("grade"));
                 gradeSum += Integer.parseInt(grades.getJSONObject(i+"").getString("grade"));
                 contentMap.add(map);
            }
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			String mName = data.getString("name"); 
			TextView nameView = (TextView)findViewById(R.id.individualName);
			nameView.setText(mName);
			String mGroup = data.getString("virtual_group");
			TextView groupView = (TextView)findViewById(R.id.classHeader);
			groupView.setText(mGroup);
			TextView groupStudentView = (TextView)findViewById(R.id.individualGroup);
			groupStudentView.setText(mGroup);
			ImageView avatar = (ImageView)findViewById(R.id.individualPhoto);
			avatar.setBackgroundResource(android.R.drawable.picture_frame); //era frame
			String avatarUrl = data.getString("avatar");
			setAvatar(avatar, avatarUrl);
		} catch (JSONException e) {
			e.printStackTrace();
		}

        SimpleAdapter mAdapter = new SimpleAdapter(this, contentMap, R.layout.individual_grade, from, to);
        setListAdapter(mAdapter);
		TextView gr = (TextView) findViewById(R.id.individualGrade);
		gr.setText(gradeSum + " p.");
	}

	public void setAvatar(ImageView avatar, String avatarUrl) {
		Bitmap b = BitmapFactory.decodeResource
		(getResources(), R.drawable.empty);
		avatar.setImageBitmap(b);
		try
		{
			HttpURLConnection con = (HttpURLConnection)(new URL(avatarUrl)).openConnection();
			con.connect();
			b = BitmapFactory.decodeStream(con.getInputStream());
			avatar.setImageBitmap(b);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} 
	}
		
	/**
	 * If the server request failed the activity exists
	 * returns an error message to the parent activity
	 * @param error
	 */
	private void exitServerError(String error) {
		Intent back = new Intent();
		back.putExtra("serverError", error);
		setResult(Activity.RESULT_CANCELED, back);
		finish();
	}
}
