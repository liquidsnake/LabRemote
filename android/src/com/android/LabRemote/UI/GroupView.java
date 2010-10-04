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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;
import com.android.LabRemote.Utils.AvatarCallback;
import com.android.LabRemote.Utils.GroupAdapter;
import com.android.LabRemote.Utils.GroupItem;
import com.android.LabRemote.Utils.ShowAvatar;
//TODO: current week chenar, selector + vacante + post
/** 
 * Lists the students of a specific group
 * @see GroupItemView
 * @see GroupAdapter
 * @see GroupItem
 */
public class GroupView extends ListActivity implements AvatarCallback {
 
	/** Group's name */
	private String mGroup;
	/** Activity id associated to the request */
	private String mAID;
	/** Informations about the students in the group */
	private ArrayList<GroupItem> mList;
	/** JSON data received from the server */
	private JSONObject mData;
	/** Adapter that manages the group's items */
	private GroupAdapter mAdapter;
	/** Requests that a child activity returns with error message 
	 * if there was a server communication error during its initialization */
	public static final int REQUEST_FROM_SERVER = 3;
	private ArrayList<String> mInactiveWeeks;
	private String mCurrentWeek;
	private AvatarCallback ava;

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
			Intent mIndividualIntent = new Intent(getApplicationContext(), StudentView.class);
			mIndividualIntent.putExtra("Name", ((GroupItemView)v).getName().getText()); 
			mIndividualIntent.putExtra("ID", ((GroupItemView)v).getmId()); 
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
		
		/** Test schimbare saptamana */
		ava = this; /*
		TableLayout he = (TableLayout) findViewById(R.id.header);
		he.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ArrayList<GroupItem> nou = new ArrayList<GroupItem>();
				nou.add(mList.get(0));
				nou.add(mList.get(1));
				nou.add(mList.get(2));
				nou.add(mList.get(3));
				mAdapter = new GroupAdapter(getApplicationContext(), nou, ava, onItemClick);
				setListAdapter(mAdapter);				
			}
		}); */
		
		/** Test popup week */
		TableLayout he = (TableLayout) findViewById(R.id.header);
		he.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String[] from = new String[] {"sapt"};
				int[] to = new int[] {R.id.resultName};
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();

				for (int i = 0; i < 18; i++) {//TODO: max weeks
					Map<String, String> map = new HashMap<String, String>();
					map.put("sapt", i+"");
					list.add(map);
				}
				SimpleAdapter words = new SimpleAdapter(getApplicationContext(), list, 
						R.layout.search_result_item, from, to);
				final Gallery content = new Gallery(getApplicationContext());
				content.setSpacing(5);
				content.setBackgroundColor(R.color.lav);
				content.setAdapter(words);
				content.setSelection(Integer.parseInt(mCurrentWeek), true);
				content.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						if (mInactiveWeeks.contains(arg2+""))
							Toast.makeText(getApplicationContext(), "vacanta", 1).show();
						else
							getWeek(arg2); //in alt thread + dc e sapt curent nu mai cer
					}
				});
				
				PopupWindow week = new PopupWindow(content, ViewGroup.LayoutParams.FILL_PARENT, 
						ViewGroup.LayoutParams.WRAP_CONTENT);
				week.setBackgroundDrawable(getResources().getDrawable(R.color.lav));
				week.setFocusable(true);
				week.setTouchable(true);
				week.setOutsideTouchable(true);
				week.showAsDropDown(v);
			}
		});

		receiveData();
	}

	private class Week extends AsyncTask<String, String, String> {
	     protected void onProgressUpdate(Integer... progress) {
	     }

	     protected void onPostExecute(String result) {
	    	 if (mData != null) 
	 			fillList();
	    }

		@Override
		protected String doInBackground(String... params) {
			ServerResponse response = new Connection(getApplicationContext()).
			getGroup(params[0], params[1], params[2]); //stiu oricand group si mAID ?
			mData = (JSONObject) response.getRespone();
			return "";
		}
	 }
	 
	private void getWeek(int id) {
		new Week().execute(mGroup, mAID, id+"");
	}
	
	/**
	 * Receives data from the previous activity and from the server
	 * @see Connection
	 */
	private void receiveData() {

		/** Header informations */
		String mDate = getIntent().getStringExtra("Date");
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
		if (getIntent().getBooleanExtra("Current", false) == true) 
			response = new Connection(this).getCurrentGroup();
		else
			response = new Connection(this).getGroup(mGroup, mAID, null);
		
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
		mList = new ArrayList<GroupItem>();
		
		try {
			students = mData.getJSONArray("students"); 
			for (int i = 0; i < students.length(); i++) {
				JSONObject stud = students.getJSONObject(i);
				mList.add(new GroupItem(stud.getString("avatar"), stud.getString("name"), 
						stud.getString("grade"), stud.getString("id"))); 
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
			
		try {
			mGroup = mData.getString("name");
			TextView groupText = (TextView) findViewById(R.id.classHeader);
			groupText.setText(mGroup);
			JSONArray inactive = mData.getJSONArray("inactive_weeks");
			mInactiveWeeks = new ArrayList<String>();
			for (int i = 0; i < inactive.length(); i++)
				mInactiveWeeks.add(i, inactive.getString(i));
			mAID = mData.getString("activity_id");
			mCurrentWeek = mData.getString("week");
		} catch (JSONException e) {
			e.printStackTrace();
		}
			
		mAdapter = new GroupAdapter(this, mList, this, onItemClick);
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
				stud.put("grade", mList.get(i).getGrade());
				students.put(stud);
			}			
			result.put("students", students);
			new Connection(this).post(result, "group"); //TODO: check response
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




