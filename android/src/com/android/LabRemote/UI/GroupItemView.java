/** 
 * View that gives the layout of a groupView item
 * @see GroupView
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.UI;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.LabRemote.R;
import com.android.LabRemote.Utils.GroupItem;

public class GroupItemView extends LinearLayout {
	
	private GroupItem mItem; 
	private ImageView mImg;
	private TextView mName;
	private EditText mGrade;
	private GestureDetector mGestureDetector;

	public GroupItemView(Context context, GroupItem item) {
		super(context);
		mItem = item;
		
		LayoutInflater layoutInflater = (LayoutInflater) 
		getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.group_item, this, true);
		
		/** Image */
		mImg = (ImageView) findViewById(R.id.groupPhoto);
		mImg.setBackgroundResource(mItem.getImg());
		
		/** Name */
		mName = (TextView) findViewById(R.id.groupName);
		mName.setText(mItem.getName());
		
		/** Grade */
		mGrade = (EditText) findViewById(R.id.groupGrade);
		mGrade.setText(mItem.getGrade());
		
		/** Fling gesture detector for increase/decrease grade */
		mGestureDetector = new GestureDetector(new MyGestureDetector());
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
                 if (mGestureDetector.onTouchEvent(event)) {
                     return true;
                 }
                 return false;
             }
         };
         setOnTouchListener(gestureListener);
	}
	
	
	/**
	 * Gesture detector class that handles finger gestures
	 */
	class MyGestureDetector extends SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, 
				float velocityX, float velocityY) {
			
			int newGrade = Integer.parseInt(mGrade.getText().toString());

			if (e2.getX() > e1.getX()) {
				newGrade++;
				setGrade(new String(newGrade + ""));
			} else {
				newGrade--;
				setGrade(new String(newGrade + ""));
			}
			return true;
		}

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

	public ImageView getImg() {
		return mImg;
	}

	public EditText getGrade() {
		return mGrade;
	}

	public TextView getName() {
		return mName;
	}


}
