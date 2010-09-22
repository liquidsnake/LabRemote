/**
 * GroupView.java
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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;
import com.android.LabRemote.Utils.AvatarCallback;
import com.android.LabRemote.Utils.MListAdapter;
import com.android.LabRemote.Utils.MListItem;
import com.android.LabRemote.Utils.ShowAvatar;

/** TODO: cum ma intorc
 * Lists the students of a specific group
 * @see ListItemView
 * @see MListAdapter
 * @see MListItem
 */
public class GroupView extends ListActivity implements AvatarCallback {

	private String mGroup, mDate, mAID;
	private ArrayList<MListItem> mList;
	private Intent mIndividualIntent;
	private JSONObject mData;
	private MListAdapter mAdapter;
	public static final int REQUEST_FROM_SERVER = 3;

	/**
	 * Displays a newly downloaded avatar
	 * @see ShowAvatar
	 */
	public void onImageReceived(ShowAvatar displayer) {
		this.runOnUiThread(displayer);
	}

	/**
	 * On click on a list item, opens individual activity for the selected student
	 * @see StudentView
	 */
	private OnClickListener onItemClick = new OnClickListener() {
		public void onClick(View v) {
			mIndividualIntent = new Intent(getApplicationContext(), StudentView.class);
			mIndividualIntent.putExtra("Name", ((ListItemView)v).getName().getText()); 
			mIndividualIntent.putExtra("ID", ((ListItemView)v).getmId()); 
			mIndividualIntent.putExtra("AID", mAID); 
			startActivityForResult(mIndividualIntent, REQUEST_FROM_SERVER);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.group_view);

		receiveData();
	}

	/**
	 * Receives data from the previous activity and from the server
	 * @see Connection
	 */
	private void receiveData() {

		/** Header informations */
		mDate = getIntent().getStringExtra("Date");
		TextView dateText = (TextView) findViewById(R.id.dateHeader);
		dateText.setText(mDate); 
		mAID = getIntent().getStringExtra("AID");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String course = preferences.getString("course", null);
		TextView cv = (TextView) findViewById(R.id.courseName);
		cv.setText(course);
		mGroup = getIntent().getStringExtra("Group");

		/** Data from the server */
		ServerResponse response;
		if (getIntent().getBooleanExtra("Current", false) == true) {
			response = new Connection(this).getCurrentGroup();
			System.out.println("cer current group " + response.getError());
		}
		else
			response = new Connection(this).getGroup(mGroup, mAID);
		
		mData = (JSONObject) response.getRespone();
		if (mData != null) 
			fillList();
		else
			exitServerError(response.getError());
	}

	/**
	 * Fills list with students
	 */
	private void fillList() {
		JSONArray students;
		mList = new ArrayList<MListItem>();
		
		try {
			students = mData.getJSONArray("students"); 
			for (int i = 0; i < students.length(); i++) {
				JSONObject stud = students.getJSONObject(i);
				mList.add(new MListItem(stud.getString("avatar"), stud.getString("name"), 
						stud.getString("grade"), stud.getString("id"))); 
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
			
		try {
			mGroup = mData.getString("name");
			TextView groupText = (TextView) findViewById(R.id.classHeader);
			groupText.setText(mGroup);
			mAID = mData.getString("activity_id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
			
		mAdapter = new MListAdapter(this, mList, this, onItemClick);
		setListAdapter(mAdapter);
	}
	
	@Override
	protected void onPause() {
		JSONObject result = new JSONObject();
		try {
			result.put("name", mGroup);
			result.put("activity_id", mAID);
			JSONArray students = new JSONArray();
			for (int i = 0; i < mList.size(); i++) {
				JSONObject stud = new JSONObject();
				stud.put("id", mList.get(i).getID());
				stud.put("name", mList.get(i).getName());
				stud.put("grade", mList.get(i).getGrade());
				students.put(stud);
			}			
			result.put("students", students);
			new Connection(this).post(result, "group");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == Activity.RESULT_CANCELED) 
	    	if (data != null)
	    		if (data.getStringExtra("serverError") != null)
	    			Toast.makeText(this, data.getStringExtra("serverError"), 1).show();
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




