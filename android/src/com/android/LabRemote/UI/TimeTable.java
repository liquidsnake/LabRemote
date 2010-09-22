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
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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


/** 
 * Select a class group based on a specific day and a time interval
 */
public class TimeTable extends Activity {

	private Intent groupIntent;
	private ExpandableListView days;
	private ArrayList<ArrayList<HashMap<String, String>>> children;
	private JSONObject mData;
	private String mDate;
	public static final int REQUEST_FROM_SERVER = 2;
	private ArrayList<Hashtable<String, List<Pair<String, String>>>> mParsedData;
	private String[] week_day = { "Monday", "Tuesday", 
			"Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		setContentView(R.layout.day_list);
		
		setSingleExpandable();
		groupIntent = new Intent(this, GroupView.class);

		days.setOnChildClickListener(new OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				startGroupView(children.get(groupPosition).get(childPosition).get("group"), 
						children.get(groupPosition).get(childPosition).get("aid"), 
						CustomDate.getDate(groupPosition));
				return false;

			}
		});
		

		/** Header informations */
		mDate = CustomDate.getCurrentDate();
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
	 * @return The adapter that controls the list's content
	 */
	public void fillTable() {
		SimpleExpandableListAdapter expListAdapter;
		ArrayList<HashMap<String, String>> parents;
		ArrayList<HashMap<String, String>> subList;
		HashMap<String, String> parentMap;
		HashMap<String, String> childrenMap;
		
		/** Parse data from server */
		mParsedData = getTimetable(); 
		
		parents = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < mParsedData.size(); ++i) {
			parentMap = new HashMap<String, String>();
			parentMap.put("day", week_day[i]);
			parents.add(parentMap);
		}

		children = new ArrayList<ArrayList<HashMap<String, String>>>();
		for (int i = 0; i < mParsedData.size(); ++i) { 
			subList = new ArrayList<HashMap<String, String>>();
			
			Hashtable<String, List<Pair<String, String>>> h = mParsedData.get(i);
			Vector<String> v = new Vector<String>(h.keySet());
			Collections.sort(v);
			Iterator<String> it = v.iterator();
			while (it.hasNext()) { //for each interval
				String interval =  (String)it.next();
				for (int k = 0; k < h.get(interval).size(); k++) { //for each group in interval
					childrenMap = new HashMap<String, String>();
					childrenMap.put("group", h.get(interval).get(k).first);
					childrenMap.put("interval", interval);
					childrenMap.put("aid", h.get(interval).get(k).second);
					subList.add(childrenMap);
				}
			}
			children.add(subList);
		}

		expListAdapter = new SimpleExpandableListAdapter(this, parents,
				R.layout.exp_item, new String[] {"day"},
				new int[] { R.id.exp_name }, children, R.layout.exp_child_item,
				new String[]{"group", "interval" }, new int[]{R.id.exp_child_group, R.id.exp_child_interval});

		days.setAdapter(expListAdapter);
	}
	
	/**
	 * Gets timetable from the server and parses the result
	 * @return
	 */
	private ArrayList<Hashtable<String, List<Pair<String, String>>>> getTimetable() {
		ArrayList<Hashtable<String, List<Pair<String, String>>>> timetable = new ArrayList<Hashtable<String, List<Pair<String, String>>>>();
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
	 * Empty day if tag wasn't present
	 * @param from
	 * @param when
	 * @return
	 */
	private Hashtable<String, List<Pair<String, String>>> getWeekDay(JSONObject from, String when) {
		Hashtable<String, List<Pair<String, String>>> day = new Hashtable<String, List<Pair<String, String>>>();
		try {
			JSONObject jDay = from.getJSONObject(when);
			Iterator rez = (Iterator)jDay.keys();

			while (rez.hasNext()) {
				String key = (String) rez.next();
				JSONArray val = (JSONArray) jDay.get(key);
				ArrayList<Pair<String, String>> vals = new ArrayList<Pair<String, String>>();
				for (int i = 0; i < val.length(); i++) {
					JSONObject in = val.getJSONObject(i);
					vals.add(new Pair<String, String>(in.getString("name"), in.getString("id")));
				}
				day.put(key, vals);
			}
		} catch (JSONException e) {
			e.printStackTrace(); 
		}
		return day;
	}

	/**
	 * Only one item can be expanded at a time 
	 */
	private void setSingleExpandable() {

		days = (ExpandableListView) findViewById(R.id.exp_list);
		days.setOnGroupExpandListener(new OnGroupExpandListener() {
			public void onGroupExpand(int groupPosition) {
				for (int i = 0; i < 5; i++)
					if (i != groupPosition)
						days.collapseGroup(i);
			}
		});
	}

	/**
	 * Starts a group view for the selected class
	 * @param group class group index
	 * @param date scheduled lab date 
	 * @see GroupView
	 */
	private void startGroupView(String group, String aid, String date) {
		groupIntent.putExtra("Group", group);
		groupIntent.putExtra("Date", date);
		groupIntent.putExtra("AID", aid);
		startActivityForResult(groupIntent, REQUEST_FROM_SERVER);
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
