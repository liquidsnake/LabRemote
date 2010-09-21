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

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;
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
	private JSONObject mData;
	public static final int REQUEST_FROM_SERVER = 4;

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
			startActivityForResult(individualIntent, REQUEST_FROM_SERVER);
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
		ServerResponse result = new Connection(this).getSearch(query); 
		JSONObject mData = (JSONObject)result.getRespone();
		
		if (mData != null) 
			fillList();
		else
			exitServerError(result.getError());
	}
	
	private void fillList() {
		mList = new ArrayList<MListItem>();

		try {
			JSONArray ar = mData.getJSONArray("students");
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
