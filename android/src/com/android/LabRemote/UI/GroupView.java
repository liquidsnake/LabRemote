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

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Utils.AvatarCallback;
import com.android.LabRemote.Utils.MListAdapter;
import com.android.LabRemote.Utils.MListItem;
import com.android.LabRemote.Utils.ShowAvatar;

/** 
 * Lists the students of a specific group
 * @see ListItemView
 * @see MListAdapter
 * @see MListItem
 */
public class GroupView extends ListActivity implements AvatarCallback {

	private String mGroup, mDate;
	private ArrayList<MListItem> mList;
	private Intent mIndividualIntent;
	private JSONObject mData;
	private MListAdapter mAdapter;

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
			mIndividualIntent.putExtra("Group", mGroup); 
			mIndividualIntent.putExtra("Date", mDate); 
			mIndividualIntent.putExtra("ID", ((ListItemView)v).getmId()); 
			startActivity(mIndividualIntent);
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
		if (mData != null)
			fillList();
	}

	/**
	 * Receives data from the previous activity and from the server
	 * @see Connection
	 */
	private void receiveData() {

		/** Header informations */
		mGroup = getIntent().getStringExtra("Group"); //TODO: ia nume grup de la server
		TextView groupText = (TextView) findViewById(R.id.classHeader);
		groupText.setText(mGroup);
		mDate = getIntent().getStringExtra("Date");
		TextView dateText = (TextView) findViewById(R.id.dateHeader);
		dateText.setText(mDate);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String course = preferences.getString("course", null);
		TextView cv = (TextView) findViewById(R.id.courseName);
		cv.setText(course);

		/** Data from the server */
		String id = preferences.getString("userId", null);
		String code = preferences.getString("loginCode", null);
		if (getIntent().getBooleanExtra("Current", false) == true)
			mData = new Connection(this).getCurrentGroup(id, code);
		else
			mData = new Connection(this).getGroup(id, code, mGroup);

	}

	/**
	 * Fills list with students
	 */
	private void fillList() {
		JSONArray students;
		//mListView = (ListView)findViewById(R.id.studentsList);
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

		mAdapter = new MListAdapter(this, mList, this, onItemClick);
		setListAdapter(mAdapter);
	}

}




