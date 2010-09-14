/**
 * ListItemView.java
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.android.LabRemote.Utils.MListItem;

/**
 * View that gives the layout for an item in a list of students
 * @see MListItem
 */
public class ListItemView extends LinearLayout {

	private MListItem mItem; 
	private ImageView mImg;
	private TextView mName;
	private EditText mGrade;
	private GestureDetector mGestureDetector;

	/**
	 * Initializes the view with the data provided by item
	 * @param item Contains strings that defines the view's image, name and/or grade
	 */
	public ListItemView(Context context, MListItem item) {
		super(context);
		mItem = item;

		LayoutInflater layoutInflater = (LayoutInflater) 
		getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.group_item, this, true);

		/** Image */
		mImg = (ImageView) findViewById(R.id.groupPhoto);
		mImg.setBackgroundResource(R.drawable.frame);
		Bitmap avatar = mItem.getAvatar();
		setImage(avatar);

		/** Name */
		mName = (TextView) findViewById(R.id.groupName);
		mName.setText(mItem.getName());

		/** Grade */ 
		if (mItem.getGrade() != null) {
			mGrade = (EditText) findViewById(R.id.groupGrade);
			mGrade.setText(mItem.getGrade());
			mGrade.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					mItem.setGrade(mGrade.getText().toString());
				}
				public void afterTextChanged(Editable s) {}
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {}
			});
		}

		/** Fling gesture detector for increase/decrease grade */
		if (mItem.getGrade() != null) {
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
	}


	/**
	 * Gesture detector class that handles finger gestures
	 * Used to detect fling gesture that increases/decreases a grade
	 */
	class MyGestureDetector extends SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, 
				float velocityX, float velocityY) {

			float newGrade = Float.parseFloat(mGrade.getText().toString());
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

	public void setEmptyImage() {
		Bitmap b = BitmapFactory.decodeResource
		(getResources(), R.drawable.empty);
		mImg.setImageBitmap(b);
	}

	public void setImage(Bitmap photo) {
		if (photo == null) 
			setEmptyImage();
		else
			mImg.setImageBitmap(photo);
	}

	public void setName(String name) {
		mName.setText(name);
	}

	public void setGrade(String grade) {
		mGrade.setText(grade);	
		mItem.setGrade(grade);
	}

	public MListItem getItem() {
		return mItem;
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

	public String getmId() {
		return mItem.getID();
	}

}
