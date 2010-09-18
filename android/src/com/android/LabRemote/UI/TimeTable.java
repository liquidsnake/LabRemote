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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Utils.CustomDate;


/** 
 * Select a class group based on a specific day and a time interval
 */
public class TimeTable extends Activity {

	private Intent groupIntent;
	private ExpandableListView days;
	private ArrayList<ArrayList<HashMap<String, String>>> children;
	private ArrayList<Hashtable<String, List<String>>> mData;
	private String[] week_day = { "Monday", "Tuesday", 
			"Wednesday", "Thursday", "Friday" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		setContentView(R.layout.day_list);

		/** Server request */
		mData = new Connection(this).getTimetable("1", "code"); 
		//TODO: check null

		setSingleExpandable();
		groupIntent = new Intent(this, GroupView.class);

		/** Init list */
		SimpleExpandableListAdapter expListAdapter = fillList();
		days.setAdapter(expListAdapter);

		days.setOnChildClickListener(new OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				startGroupView(children.get(groupPosition).get(childPosition).
						get("group"), CustomDate.getDate(groupPosition));
				return false;

			}
		});
	}

	/**
	 * Fills the timetable with data
	 * @return The adapter that controls the list's content
	 */
	public SimpleExpandableListAdapter fillList() {

		SimpleExpandableListAdapter expListAdapter;
		ArrayList<HashMap<String, String>> parents;
		ArrayList<HashMap<String, String>> subList;
		HashMap<String, String> parentMap;
		HashMap<String, String> childrenMap;

		parents = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < mData.size(); ++i) {
			parentMap = new HashMap<String, String>();
			parentMap.put("day", week_day[i]);
			parents.add(parentMap);
		}

		children = new ArrayList<ArrayList<HashMap<String, String>>>();
		for (int i = 0; i < mData.size(); ++i) { //for each day TODO: poate nu-s toate zilele
			subList = new ArrayList<HashMap<String, String>>();
			
			Hashtable<String, List<String>> h = mData.get(i);
			Vector v = new Vector(h.keySet());
			Collections.sort(v);
			Iterator<String> it = v.iterator();
			while (it.hasNext()) { //for each interval
				String interval =  (String)it.next();
				for (int k = 0; k < h.get(interval).size(); k++) { //for each group in interval
					childrenMap = new HashMap<String, String>();
					childrenMap.put("group", h.get(interval).get(k));
					childrenMap.put("interval", interval);
					subList.add(childrenMap);
				}
			}
			children.add(subList);
		}

		expListAdapter = new SimpleExpandableListAdapter(this, parents,
				R.layout.exp_item, new String[] {"day"},
				new int[] { R.id.exp_name }, children, R.layout.exp_child_item,
				new String[]{"group", "interval" }, new int[]{R.id.exp_child_group, R.id.exp_child_interval});

		return expListAdapter;
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
	private void startGroupView(String group, String date) {
		groupIntent.putExtra("Group", group);
		groupIntent.putExtra("Date", date);
		startActivity(groupIntent);
	}

}
