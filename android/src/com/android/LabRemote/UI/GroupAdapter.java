/* 
 * Adapter that manages items in the group list
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.UI;

import java.util.ArrayList;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.LabRemote.Utils.GroupItem;

public class GroupAdapter extends BaseAdapter {
	
	private ArrayList<GroupItem> mItems = new ArrayList<GroupItem>();
	private Context mContext;

	public GroupAdapter(Context context, ArrayList<GroupItem> items) {
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
		 GroupItemView item;
		 
		 if (convertView == null) {
			 item = new GroupItemView(mContext, mItems.get(index));
	     } else {
	    	 item = (GroupItemView) convertView;
	          String name = mItems.get(index).getName();
	          item.setName(name);
	          String grade = mItems.get(index).getGrade();
	          item.setGrade(grade);
	          int photo = mItems.get(index).getImg();
	          item.setImage(photo);
	     }

		 return item;
	}
}
