/**
 * GroupView.java
 *     
 * Version 1.0
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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

/** 
 * Lists the students of a specific group
 * @see GroupItemView
 * @see GroupAdapter
 * @see GroupItem
 */
public class GroupView extends LabRemoteActivity implements AvatarCallback {

	/** Group's name */
	private String mGroup;
	/** ListView that displays the group's students */
	private ListView mListView;
	/** Activity id associated to the request */
	private String mAID;
	/** Informations about the students in the group */
	private ArrayList<GroupItem> mList;
	/** JSON data received from the server */
	private JSONObject mData;
	/** Adapter that manages the group's items */
	private GroupAdapter mAdapter;
	/** A list of the invalid weeks for the current activity (e.g. vacation) */
	private ArrayList<String> mInactiveWeeks;
	/** The number of weeks associated with this activity */
	private int mMaxWeeks = -1;
	/** Week id for the current attendance */
	private String mCurrentWeek = "-1";
	/** Dropbox bar that allows the user to select a new week */
	private PopupWindow mWeeksBar;

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
			startActivityForResult(mIndividualIntent, LabRemoteActivity.REQUEST_FROM_STUDENT);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.group_view);

		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setEmptyView(findViewById(android.R.id.empty));

		/** Dropbox week switcher */
		RelativeLayout header = (RelativeLayout) findViewById(R.id.header);
		header.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showWeeksBar(v);
			}
		});

		/** Gets content data */
		mInactiveWeeks = new ArrayList<String>();
		receiveData();

		/** Invalid activity */
		if (mInactiveWeeks.contains(mCurrentWeek)) {
			TextView t = (TextView)findViewById(android.R.id.empty);
			t.setText((String) getResources().getString(R.string.invalid_activity));
		}
	}

	/**
	 * Receives data from the previous activity and from the server
	 * @see Connection
	 */
	private void receiveData() {

		/** Header informations */
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

		/** Students */
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

		/** Other data from server */
		try {
			mGroup = mData.getString("name");
			TextView groupText = (TextView) findViewById(R.id.classHeader);
			groupText.setText(mGroup);
			mCurrentWeek = mData.getString("week"); 
			String mDate = mData.getString("date"); 
			TextView dateText = (TextView) findViewById(R.id.dateHeader);
			dateText.setText(mDate + " [" + mCurrentWeek + "]"); 
			JSONArray inactive = mData.getJSONArray("inactive_weeks");
			mMaxWeeks = mData.getInt("max_weeks");
			for (int i = 0; i < inactive.length(); i++)
				mInactiveWeeks.add(i, inactive.getString(i));
			mAID = mData.getString("activity_id");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mAdapter = new GroupAdapter(this, mList, this, onItemClick);
		mListView.setAdapter(mAdapter);
	}

	@Override
	protected void onPause() {
		if (!mInactiveWeeks.contains(mCurrentWeek))
			post();
		super.onPause();
	}

	/**
	 * Sends activity data to the server
	 */
	private void post() {
		JSONObject result = new JSONObject();
		try {
			result.put("name", mGroup);
			result.put("activity_id", mAID);
			result.put("week", mCurrentWeek);
			JSONArray students = new JSONArray();
			for (int i = 0; i < mList.size(); i++) {
				JSONObject stud = new JSONObject();
				stud.put("id", mList.get(i).getID());
				stud.put("grade", mList.get(i).getGrade());
				students.put(stud);
			}			
			result.put("students", students);

			ServerResponse res = new Connection(this).post(result, "group"); 
			if (res.getError() != null)
				Toast.makeText(getApplicationContext(), 
						"Post Error: " + res.getError(), 1).show();

		} catch (JSONException e) {
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

	/******************************** Switch week ********************************/
	/*****************************************************************************/

	/**
	 * Initializes select week bar and shows it as a dropbox 
	 */
	private void showWeeksBar(View v) {
		final WeekAdapter adapter;
		final Gallery weeksList;

		ArrayList<String> wee = new ArrayList<String>();
		for (int i = 1; i <= mMaxWeeks; i++) 
			wee.add(i+"");

		LinearLayout layout = new LinearLayout(getApplicationContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		TextView title = new TextView(getApplicationContext());
		title.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		title.setText("Select week:");
		layout.addView(title);

		weeksList = new Gallery(getApplicationContext());
		weeksList.setSpacing(5);
		adapter = new WeekAdapter(getApplicationContext(), 
				wee, null, mInactiveWeeks, mCurrentWeek);
		weeksList.setAdapter(adapter);
		weeksList.setSelection(Integer.parseInt(mCurrentWeek), true);
		weeksList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				arg2++;
				if (mInactiveWeeks.contains(arg2+""))
					Toast.makeText(getApplicationContext(), "Inactive activity", 1).show();
				else {
					TextView sel = (TextView)arg1.findViewById(R.id.weekId);
					sel.setTypeface(Typeface.DEFAULT_BOLD);
					if (arg2 != Integer.parseInt(mCurrentWeek)) {
						getWeek(arg2); 
					}
					mWeeksBar.dismiss();
				}
			}
		});
		layout.addView(weeksList);

		mWeeksBar = new PopupWindow(layout, ViewGroup.LayoutParams.FILL_PARENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mWeeksBar.setBackgroundDrawable(getResources().
				getDrawable(android.R.drawable.spinner_dropdown_background));
		mWeeksBar.setFocusable(true);
		mWeeksBar.setTouchable(true);
		mWeeksBar.setOutsideTouchable(true);
		mWeeksBar.showAsDropDown(v);
	}

	/**
	 * Saves the current week and get's the newly selected week from the server
	 * @param id Week's identifier
	 */
	private void getWeek(int id) {
		if (!mInactiveWeeks.contains(mCurrentWeek))
			post();		
		new Week().execute(mGroup, mAID, id+"");
	}

	/**
	 * Handles a group server request in the background and
	 * updates the list with the resulted activity
	 */
	private class Week extends AsyncTask<String, String, String> {
		private ProgressDialog dialog;

		protected void onPreExecute() {
			dialog =  ProgressDialog.show(GroupView.this, "Switch week" , 
					" Loading. Please wait ... ", true);
		}

		protected void onPostExecute(String result) {
			if (mData != null) 
				fillList();
			dialog.dismiss();
		}

		@Override
		protected String doInBackground(String... params) {
			publishProgress("");
			ServerResponse response = new Connection(getApplicationContext()).
			getGroup(params[0], params[1], params[2]); 
			mData = (JSONObject) response.getRespone();
			return "";
		}

	}

	/**
	 * Adapter that handles the list of weeks from the dropbox bar
	 */
	private class WeekAdapter extends BaseAdapter {

		/** Array filled with list's elements */
		private ArrayList<String> mItems = new ArrayList<String>();
		private ArrayList<String> mInvalid;
		private Context mContext;
		private String mWeek;

		public WeekAdapter(Context context, ArrayList<String> items, 
				OnClickListener onItemClick, ArrayList<String> invalid, String week) {
			mWeek = week;
			mInvalid = invalid;
			mContext = context;
			mItems = items;
		}

		public int getCount() {
			return mItems.size();
		}

		public Object getItem(int index) {
			return mItems.get(index);
		}

		public long getItemId(int index) {
			return index;
		}

		public View getView(int index, View convertView, ViewGroup parent) {
			View item;
			LayoutInflater layoutInflater = (LayoutInflater)
			mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			item = layoutInflater.inflate(R.layout.week_number, null, false);

			String it = mItems.get(index);
			TextView text = (TextView)item.findViewById(R.id.weekId);
			text.setText(it);

			index++;
			/** Selected week */
			if (mWeek.equals(index+"")) 
				text.setTypeface(Typeface.DEFAULT_BOLD);

			/** Invalid week */
			if (mInvalid.contains(index+""))
				text.setTextColor(R.color.snow);

			return item;
		}
	}

}




