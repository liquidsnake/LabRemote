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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;
import com.android.LabRemote.Utils.CustomDate;
import com.android.LabRemote.Utils.MListAdapter;
import com.android.LabRemote.Utils.MListItem;

/** 
 * Informations about a selected student
 */
public class StudentView extends ListActivity {
	private String mName, mGroup, mDate, mID, mCaller;
	private ArrayList<MListItem> mItems;
	private JSONObject data;
	private MListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.individual_view);

		receiveData();
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
		mDate = CustomDate.getCurrentDate();
		TextView dateView = (TextView)findViewById(R.id.dateHeader);
		dateView.setText(mDate);
		mID = getIntent().getStringExtra("ID"); 
		mCaller = getIntent().getStringExtra("AID");

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
		mItems = new ArrayList<MListItem>();

		try {
			JSONObject grades = data.getJSONObject("attendance"); 
			for (int i = 0; i < grades.length(); i++) {
				mItems.add(new MListItem(null, i+"", 
				grades.getJSONObject(i+"").getString("grade"), null));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {
			mName = data.getString("name"); 
			TextView nameView = (TextView)findViewById(R.id.individualName);
			nameView.setText(mName);
			mGroup = data.getString("group");
			TextView groupView = (TextView)findViewById(R.id.classHeader);
			groupView.setText(mGroup);
			TextView groupStudentView = (TextView)findViewById(R.id.individualGroup);
			groupStudentView.setText(mGroup);
			ImageView avatar = (ImageView)findViewById(R.id.individualPhoto);
			avatar.setBackgroundResource(R.drawable.frame);
			String avatarUrl = data.getString("avatar");
			setAvatar(avatar, avatarUrl);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mAdapter = new MListAdapter(this, mItems);
		setListAdapter(mAdapter);
	}

	public void setAvatar(ImageView avatar, String avatarUrl) {
		try
		{
			HttpURLConnection con = (HttpURLConnection)(new URL(avatarUrl)).openConnection();
			con.connect();
			Bitmap b = BitmapFactory.decodeStream(con.getInputStream());
			avatar.setImageBitmap(b);
		}  catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} finally {
			Bitmap b = BitmapFactory.decodeResource
			(getResources(), R.drawable.empty);
			avatar.setImageBitmap(b);
		}
	}
		
	@Override
	protected void onPause() {

		JSONObject result = new JSONObject();
		try {
			result.put("group", mGroup);
			result.put("id", mID);
			result.put("name", mName);
			JSONObject attendance = new JSONObject();
			for (int i = 0; i < mItems.size(); i++) {
				attendance.put(i+"", mItems.get(i).getGrade());
			}			
			new Connection(this).post(result, "student");
			result.put("attendance", attendance);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		super.onPause();

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
