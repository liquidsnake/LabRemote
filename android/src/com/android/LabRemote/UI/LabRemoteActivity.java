package com.android.LabRemote.UI;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;
import com.android.LabRemote.Utils.CustomDate;
import com.android.LabRemote.Utils.GroupID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class LabRemoteActivity extends Activity {
	public final static int REQUEST_FROM_CURRENT = 1;
	public final static int REQUEST_FROM_GROUP = 2;
	public final static int REQUEST_FROM_STUDENT = 3;
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
			in.putExtra("Date", CustomDate.getCurrentDate());
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
	 * 
	 */
	private void getGroups() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Select a group");
		ServerResponse response = new Connection(this).getGroups();
		if (response.getError() != null)
			Toast.makeText(this, response.getError(), 1).show();
		else {
			JSONObject resp = (JSONObject) response.getRespone();
			final Hashtable<String, GroupID> res = new Hashtable<String, GroupID>();
			try {
				JSONArray data = resp.getJSONArray("activities");
				for (int i = 0; i < data.length(); i++) {
					JSONObject group = data.getJSONObject(i);
					res.put(group.getString("name"), new GroupID
							(group.getString("group"), group.getString("activity_id")));
				}
				Vector<String> list = new Vector<String>(res.keySet());
				Collections.sort(list);
				final String[] ll = new String[list.size()];
				for (int i = 0; i < list.size(); i++)
					ll[i] = list.get(i);				
				
				alert.setItems(ll, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						Intent groups = new Intent(getApplicationContext(), GroupView.class);
						groups.putExtra("Group", ((GroupID)res.get(ll[item])).getName());
						groups.putExtra("AID", ((GroupID)res.get(ll[item])).getActivity());
						startActivityForResult(groups, GroupView.REQUEST_FROM_SERVER);
					}
				});
				alert.create().show();
			} catch (JSONException e) {
				
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
