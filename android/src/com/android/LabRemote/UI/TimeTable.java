/**
 * TimeTable.java
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;
import com.android.LabRemote.Utils.CustomDate;
import com.android.LabRemote.Utils.GroupID;

/** 
 * Select a class group based on a specific day and a time interval
 */
public class TimeTable extends LabRemoteActivity {

	/** Starts group view activity */
	private Intent mGroupIntent;
	/** Expandable list view of week days */
	private ExpandableListView mDays;
	/** Unparsed JSON data received from server */
	private JSONObject mData;
	/** A matrix with timetable's items. Each week day has a list of sorted 
	 * intervals, each interval has a group and its activity id */
	private ArrayList<ArrayList<HashMap<String, String>>> mListItems;
	/** Requests that a child activity returns with error message 
	 * if there was a server communication error during its initialization */
	public static final int REQUEST_FROM_SERVER = 2;
	/** Array with string representations of the timetable's week days */
	private static final String[] week_day = { "Monday", "Tuesday", 
		"Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		setContentView(R.layout.timetable);

		setSingleExpandable();
		mGroupIntent = new Intent(this, GroupView.class);

		mDays.setOnChildClickListener(new OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				startGroupView(mListItems.get(groupPosition).get(childPosition).get("group"), 
						mListItems.get(groupPosition).get(childPosition).get("aid"), 
						CustomDate.getDate(groupPosition+1));
				return false;

			}
		});

		/** Header informations */
		String mDate = CustomDate.getCurrentDate();
		TextView dateText = (TextView) findViewById(R.id.dateHeader);
		dateText.setText(mDate); 
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String course = preferences.getString("course", null);
		TextView cv = (TextView) findViewById(R.id.courseName);
		cv.setText(course);

		/** Server request */
		ServerResponse result = new Connection(this).getTimetable(); 
		mData = (JSONObject)result.getRespone();
		if (mData != null)
			fillTable();
		else
			exitServerError(result.getError());
	}

	/**
	 * Fills the timetable with data
	 */
	public void fillTable() {
		ArrayList<Hashtable<String, List<GroupID>>> mParsedData;
		SimpleExpandableListAdapter expListAdapter;
		ArrayList<HashMap<String, String>> parents;
		ArrayList<HashMap<String, String>> subList;
		HashMap<String, String> parentMap;
		HashMap<String, String> childrenMap;

		/** Parse data from server */
		mParsedData = parseTimetable(); 

		/** Week days list */
		parents = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < mParsedData.size(); ++i) {
			parentMap = new HashMap<String, String>();
			parentMap.put("day", week_day[i]);
			parents.add(parentMap);
		}

		/** Groups list sorted by interval */
		mListItems = new ArrayList<ArrayList<HashMap<String, String>>>();
		for (int i = 0; i < mParsedData.size(); ++i) { 
			subList = new ArrayList<HashMap<String, String>>();

			Hashtable<String, List<GroupID>> h = mParsedData.get(i);
			Vector<String> v = new Vector<String>(h.keySet());
			Collections.sort(v);
			Iterator<String> it = v.iterator();
			while (it.hasNext()) { 
				/** for each interval */
				String interval =  (String)it.next();
				for (int k = 0; k < h.get(interval).size(); k++) { 
					/** for each group in interval */
					childrenMap = new HashMap<String, String>();
					childrenMap.put("group", h.get(interval).get(k).getName());
					childrenMap.put("interval", interval);
					childrenMap.put("aid", h.get(interval).get(k).getActivity());
					subList.add(childrenMap);
				}
			}
			mListItems.add(subList);
		}

		expListAdapter = new SimpleExpandableListAdapter(this, parents,
				R.layout.exp_item, new String[] {"day"}, new int[] {R.id.exp_name}, 
				mListItems, R.layout.exp_child_item, new String[]{"group", "interval"}, 
				new int[]{R.id.exp_child_group, R.id.exp_child_interval});
		mDays.setAdapter(expListAdapter);
	}

	/**
	 * Gets timetable from the server and parses the result
	 * @return A list of week days, represented as hashtables
	 * @see getWeekDay
	 */
	private ArrayList<Hashtable<String, List<GroupID>>> parseTimetable() {
		ArrayList<Hashtable<String, List<GroupID>>> timetable = 
			new ArrayList<Hashtable<String, List<GroupID>>>();
		JSONObject table;

		try {
			table = mData.getJSONObject("timetable"); 
			timetable.add(getWeekDay(table, "monday")); 
			timetable.add(getWeekDay(table, "tuesday"));
			timetable.add(getWeekDay(table, "wednesday"));
			timetable.add(getWeekDay(table, "thursday"));
			timetable.add(getWeekDay(table, "friday"));
			timetable.add(getWeekDay(table, "saturday"));
			timetable.add(getWeekDay(table, "sunday"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return timetable;
	}

	/**
	 * Parses a week day from a JSON object
	 * @param from The unparsed JSON object
	 * @param day The requested week day
	 * @return A hashtable where an interval is the key for a list of groups 
	 */
	private Hashtable<String, List<GroupID>> getWeekDay(JSONObject from, String day) {
		Hashtable<String, List<GroupID>> wDay = 
			new Hashtable<String, List<GroupID>>();

		try {
			JSONObject jDay = from.getJSONObject(day);
			Iterator<?> rez = jDay.keys();

			while (rez.hasNext()) {
				String key = (String) rez.next();
				JSONArray val = (JSONArray) jDay.get(key);
				ArrayList<GroupID> vals = new ArrayList<GroupID>();
				for (int i = 0; i < val.length(); i++) {
					JSONObject in = val.getJSONObject(i);
					vals.add(new GroupID(in.getString("name"), in.getString("id")));
				}
				wDay.put(key, vals);
			}
		} catch (JSONException e) {
			e.printStackTrace(); 
		}
		return wDay;
	}

	/**
	 * Only one item can be expanded at a time 
	 */
	private void setSingleExpandable() {
		mDays = (ExpandableListView) findViewById(R.id.exp_list);
		mDays.setOnGroupExpandListener(new OnGroupExpandListener() {
			public void onGroupExpand(int groupPosition) {
				for (int i = 0; i < 7; i++)
					if (i != groupPosition)
						mDays.collapseGroup(i);
			}
		});
	}

	/**
	 * Starts a group view for the selected class
	 * @param group class group index
	 * @param date scheduled lab date 
	 * @see GroupView
	 */
	private void startGroupView(String group, String aid, String date) { //TODO: scot date
		mGroupIntent.putExtra("Group", group);
		mGroupIntent.putExtra("Date", date);
		mGroupIntent.putExtra("AID", aid);
		startActivityForResult(mGroupIntent, REQUEST_FROM_SERVER);
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
