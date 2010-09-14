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
import java.util.HashMap;

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
	private String[] week_day = { "Monday", "Tuesday", 
			"Wednesday", "Thursday", "Friday" };

	/** Test data */
	private String[][] extendeData = {
		{ "334 CB S1 8:10", "335 CA S2 12:14", "332 CC S2 14:16",
			"333 CB S1 16:18", "331 CA S2 10:12" },
		{ "334 CB S1 8:10", "335 CA S2 12:14", "332 CC S2 14:16",
			"333 CB S1 16:18", "331 CA S2 10:12" },
		{ "334 CB S1 8:10", "335 CA S2 12:14", "332 CC S2 14:16",
			"333 CB S1 16:18", "331 CA S2 10:12" },
		{ "334 CB S1 8:10", "335 CA S2 12:14", "332 CC S2 14:16",
			"333 CB S1 16:18", "331 CA S2 10:12" },
		{ "334 CB S1 8:10", "335 CA S2 12:14", "332 CC S2 14:16",
			"333 CB S1 16:18", "331 CA S2 10:12" } };

	private String[][] data = {
			{ "334 CB", "335 CA", "332 CC", "333 CB", "331 CA" },
			{ "334 CB", "335 CA", "332 CC", "333 CB", "331 CA" },
			{ "334 CB", "335 CA", "332 CC", "333 CB", "331 CA" },
			{ "334 CB", "335 CA", "332 CC", "333 CB", "331 CA" },
			{ "334 CB", "335 CA", "332 CC", "333 CB", "331 CA" } };

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		setContentView(R.layout.day_list);

		/** Server request */
		new Connection(this).getTimetable("1", "code"); 

		setSingleExpandable();
		groupIntent = new Intent(this, GroupView.class);

		/** Init list */
		SimpleExpandableListAdapter expListAdapter = fillList();
		days.setAdapter( expListAdapter );

		days.setOnChildClickListener(new OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				startGroupView(data[groupPosition][childPosition], 
						CustomDate.getDate(groupPosition));
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
		ArrayList<ArrayList<HashMap<String, String>>> children;
		ArrayList<HashMap<String, String>> subList;
		HashMap<String, String> parentMap;
		HashMap<String, String> childrenMap;

		parents = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < 5; ++i) {
			parentMap = new HashMap<String, String>();
			parentMap.put("day", week_day[i]);
			parents.add(parentMap);
		}

		children = new ArrayList<ArrayList<HashMap<String, String>>>();
		for (int i = 0; i < 5; ++i) {
			subList = new ArrayList<HashMap<String, String>>();
			for (int j = 0; j < 5; ++j) {
				childrenMap = new HashMap<String, String>();
				childrenMap.put("group", extendeData[i][j]);
				subList.add(childrenMap);
			}
			children.add(subList);
		}

		expListAdapter = new SimpleExpandableListAdapter(this, parents,
				R.layout.exp_item, new String[] { "day" },
				new int[] { R.id.exp_name }, children, R.layout.exp_child_item,
				new String[] { "group" }, new int[] { R.id.exp_child_name });

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
