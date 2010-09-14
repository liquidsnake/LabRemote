/**
 * SearchActivity.java
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
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Utils.AvatarCallback;
import com.android.LabRemote.Utils.MListAdapter;
import com.android.LabRemote.Utils.MListItem;
import com.android.LabRemote.Utils.ShowAvatar;


/** 
 * Activity that handles search query and lists the results
 * @see ListItemView
 * @see MListAdapter
 * @see MListItem
 */
public class SearchActivity extends ListActivity implements AvatarCallback {
	private ListView mListView;
	private ArrayList<MListItem> mList;

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
			Intent individualIntent = new Intent(getApplicationContext(), StudentView.class);
			individualIntent.putExtra("Name", ((ListItemView)v).getName().getText());
			startActivity(individualIntent);
		}
	};

	@Override
	public void onNewIntent(final Intent newIntent) {
		super.onNewIntent(newIntent);

		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			doSearch("onNewIntent()");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.search);

		mListView = (ListView) findViewById(android.R.id.list);	    	    
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doSearch(query);
		}
	}

	/**
	 * Sends search query to the server and lists the resulting students
	 * @param query Search query typed by the user
	 */
	public void doSearch(String query) {
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String id = preferences.getString("userId", null);
		String code = preferences.getString("loginCode", null);
		JSONArray ar = new Connection(this).getSearch(id, code, query); 
		mList = new ArrayList<MListItem>();

		try {
			for(int i = 0; i < ar.length(); i++) {
				JSONObject student = ar.getJSONObject(i);
				mList.add(new MListItem(student.getString("avatar"), 
						student.getString("name"), student.getString("id")));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} 

		MListAdapter students = new MListAdapter(this, mList, this, onItemClick);
		mListView.setAdapter(students);
	}

}
