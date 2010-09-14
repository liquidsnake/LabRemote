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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;

/** 
 * Informations about a selected student
 */
public class StudentView extends Activity {
	private ListView attendance;
	private String mName, mGroup, mDate, mID;
	private JSONObject data;
	private SimpleAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.individual_view);

		receiveData();
		fillList();
	}

	/**
	 * Receive data from the previous activity
	 * name, group, date, course, stud_id
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
		mID = getIntent().getStringExtra("ID"); 
		
		/* From server */
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String id = preferences.getString("userId", null);
		String code = preferences.getString("code", null);
		data = new Connection(this).getStudent(id, code, "USO", mID);
		ImageView avatar = (ImageView)findViewById(R.id.individualPhoto);
		avatar.setBackgroundResource(R.drawable.frame);
		setAvatar(avatar);
	}

	/**
	 * Fill list with grades
	 */
	private void fillList() {
		
		List<HashMap<String, String>> contentMap = new ArrayList<HashMap<String, String>>();
		attendance = (ListView)findViewById(R.id.individualList);
		String[] from = new String[] {"index", "grade"};
		int[] to = new int[] { R.id.labIndex, R.id.labGrade};

		try {
			JSONObject grades = data.getJSONObject("attendance");
			for (int i = 1; i <= grades.length(); i++) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("index", " L" + i);
				map.put("grade", " " + grades.get(i+""));
				contentMap.add(map);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mAdapter = new SimpleAdapter(this, contentMap, R.layout.individual_grade, from, to);
		attendance.setAdapter(mAdapter);
	}
	
	public void setAvatar(ImageView avatar) {
		try
		{
			String avatarUrl = data.getString("avatar");
			HttpURLConnection con = (HttpURLConnection)(new URL(avatarUrl)).openConnection();
			con.connect();
			Bitmap b = BitmapFactory.decodeStream(con.getInputStream());
			avatar.setImageBitmap(b);
		}  catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			Bitmap b = BitmapFactory.decodeResource
			(getResources(), R.drawable.empty);
			avatar.setImageBitmap(b);
		}
	}

}
