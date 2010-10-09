/**
 * LabRemoteActivity.java
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;
import com.android.LabRemote.Utils.GroupID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Generic LabRemote activity that implements common methods between this application's views
 */
public class LabRemoteActivity extends Activity {

	/** Requests that current group activity returns with error message 
	 * if there was a server communication error during its initialization */
	public final static int REQUEST_FROM_CURRENT = 1;
	/** Requests that group activity returns with error message 
	 * if there was a server communication error during its initialization */
	public final static int REQUEST_FROM_GROUP = 2;
	/** Requests that student activity returns with error message 
	 * if there was a server communication error during its initialization */
	public final static int REQUEST_FROM_STUDENT = 3;
	/** Requests that timetable activity returns with error message 
	 * if there was a server communication error during its initialization */
	public final static int REQUEST_FROM_TIMETABLE = 4;	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent in;
		switch (item.getItemId()) {
		case R.id.cgroup:
			in = new Intent(this, GroupView.class);
			in.putExtra("Group", "");
			in.putExtra("Current", true); 
			startActivityForResult(in, REQUEST_FROM_CURRENT);
			return true;
		case R.id.timetable:
			in = new Intent(this, TimeTable.class);
			startActivityForResult(in, REQUEST_FROM_TIMETABLE);
			return true;
		case R.id.search:
			onSearchRequested();
			return true;
		case R.id.settings:
			in = new Intent(this, Settings.class);
			startActivity(in);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_CANCELED) {
			if (requestCode == GroupView.REQUEST_FROM_CURRENT) 
				getGroups();
			else if (data != null)
				if (data.getStringExtra("serverError") != null)
					Toast.makeText(this, data.getStringExtra("serverError"), 1).show();
		}
	}

	/**
	 * If a current group request fails, the user gets a list of the groups
	 * where he/she is an assistant
	 */
	private void getGroups() {
		final Hashtable<String, GroupID> res = new Hashtable<String, GroupID>();
		AlertDialog.Builder selectGroup = new AlertDialog.Builder(this);
		selectGroup.setTitle("Select a group");

		ServerResponse response = new Connection(this).getGroups();
		if (response.getError() != null)
			Toast.makeText(this, response.getError(), 1).show();
		else { /** Parse server response */
			JSONObject resp = (JSONObject) response.getRespone();
			try {
				JSONArray data = resp.getJSONArray("activities");
				for (int i = 0; i < data.length(); i++) {
					JSONObject group = data.getJSONObject(i);
					res.put(group.getString("name"), new GroupID
							(group.getString("group"), group.getString("activity_id")));
				}
				/** Sort resulted list by group name */
				Vector<String> list = new Vector<String>(res.keySet());
				Collections.sort(list);
				final String[] groups = new String[list.size()];
				for (int i = 0; i < list.size(); i++)
					groups[i] = list.get(i);				

				selectGroup.setItems(groups, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						Intent groupIntent = new Intent(getApplicationContext(), GroupView.class);
						groupIntent.putExtra("Group", ((GroupID)res.get(groups[item])).getName());
						groupIntent.putExtra("AID", ((GroupID)res.get(groups[item])).getActivity());
						startActivityForResult(groupIntent, REQUEST_FROM_GROUP);
					}
				});
				selectGroup.create().show();
			} catch (JSONException e) {
				Toast.makeText(this, "Invalid server response", 1).show();
			}
		}
	}

	/** 
	 * Passes search data to the searchable activity
	 * @see android.app.Activity#onSearchRequested()
	 */
	public boolean onSearchRequested() {
		startSearch(null, false, null, false); 
		return true;
	}

}
