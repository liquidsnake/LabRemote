/** 
 * Search Activity
 * Activity that handles search query
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.UI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.LabRemote.R;

public class SearchActivity extends ListActivity {
	private ListView mList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
	    setContentView(R.layout.search);
	    
	    /* Handle result */
	    mList = (ListView) findViewById(android.R.id.list);	    	    
	    Intent intent = getIntent();

	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      doSearch(query);
	    }
	    
	}
	
	@Override
	public void onNewIntent(final Intent newIntent) {
		super.onNewIntent(newIntent);
	        
		final Intent queryIntent = getIntent();
	    final String queryAction = queryIntent.getAction();
	    if (Intent.ACTION_SEARCH.equals(queryAction)) {
	    	doSearch("onNewIntent()");
	    }

	}
	
	public void doSearch(String query) {
	      String[] from = new String[] { "Name" };
	      int[] to = new int[] { R.id.result_name };
	        
	      List<HashMap<String, String>> contentMap = new ArrayList<HashMap<String, String>>();
	      for(int i = 0; i < 4; i++){
	    	  HashMap<String, String> map = new HashMap<String, String>();
	          map.put("Name", " Luana " + i);
	          contentMap.add(map);
	      }

	        SimpleAdapter students = new SimpleAdapter(this, contentMap, R.layout.result, from, to);
	        mList.setAdapter(students);

	        mList.setOnItemClickListener(new OnItemClickListener() {
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                Intent individualIntent = new Intent(getApplicationContext(), IndividualView.class);
	                individualIntent.putExtra("Name", ((TextView)((LinearLayout) view).getChildAt(0)).getText());
	                startActivity(individualIntent);
	            }
	        });		
	}
}
