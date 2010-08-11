/* 
 * View that gives the layout of a groupView item
 * @see GroupView
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.LabRemote.R;
import com.android.LabRemote.Utils.GroupItem;

public class GroupItemView extends LinearLayout {
	
	private GroupItem mItem; 
	private ImageView mImg;
	private TextView mGrade, mName;

	public GroupItemView(Context context, GroupItem item) {
		super(context);
		mItem = item;
		
		LayoutInflater layoutInflater = (LayoutInflater) 
		getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.group_item, this, true);
		
		/* Image */
		mImg = (ImageView) findViewById(R.id.groupPhoto);
		mImg.setBackgroundResource(mItem.getImg());
		
		/* Name */
		mName = (TextView) findViewById(R.id.groupName);
		mName.setText(mItem.getName());
		
		/* Grade */
		mGrade = (Button) findViewById(R.id.groupGrade);
		mGrade.setText(mItem.getGrade());		
	}
	
	public void setImage(int img) {
		mImg.setBackgroundResource(img);
	}
	
	public void setName(String name) {
		mName.setText(name);
	}
	
	public void setGrade(String grade) {
		mGrade.setText(grade);	
	}

}
