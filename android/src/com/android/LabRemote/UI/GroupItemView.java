/**
 * GroupItemView.java
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
import android.graphics.drawable.StateListDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;

import com.android.LabRemote.R;
import com.android.LabRemote.Utils.GroupItem;

/**
 * View that gives the layout for an item in a list of students
 * @see GroupView
 */
public class GroupItemView extends LinearLayout {

	/** The view's dataset with informations about the student */
	private GroupItem mItem; 
	/** Popup grade editor's window */
	private PopupWindow popupGrade;
	/** Popup grade editor */
	private EditText popupEdit;
	/** Student's avatar */
	private ImageView mImg;
	/** Student's name */
	private TextView mName;
	/** Student's grade */
	private TextView mGrade;
	/** Detects fling gesture that increases/decreases grade */
	private GestureDetector mGestureDetector;
	private Context mContext;

	/**
	 * Initializes the view with the data provided by item
	 * @param item Contains strings that defines the view's image, name and grade
	 */
	public GroupItemView(Context context, GroupItem item) {
		super(context);
		mContext = context;
		mItem = item;
		LayoutInflater layoutInflater = (LayoutInflater) 
		getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.group_item, this, true);
		FrameLayout fr = (FrameLayout)findViewById(R.id.imageframe);
		StateListDrawable st = new StateListDrawable();
	    st.addState(new int[] {-android.R.attr.state_selected}, 
	    		getResources().getDrawable(R.drawable.att));
	    st.addState(new int[] {android.R.attr.state_selected}, 
	    		getResources().getDrawable(R.color.darkgrey));
        st.addState(new int[] {android.R.attr.state_pressed}, 
        		getResources().getDrawable(R.color.darkgrey));
		fr.setBackgroundDrawable(st);
		initImage(R.id.groupPhoto);
		initName(R.id.groupName);
		initGrade(R.id.groupGrade);
	}

	/**
	 * Initializes avatar
	 */
	private void initImage(int res) {
		mImg = (ImageView) findViewById(res);
		Bitmap avatar = mItem.getAvatar();
		setImage(avatar);
	}

	/**
	 * Initializes student's name
	 */
	private void initName(int res) {
		mName = (TextView) findViewById(res);
		mName.setText(mItem.getName());
	}

	/**
	 * Initializes student's grade
	 */
	private void initGrade(int res) {

		/** Grade */ 
		if (mItem.getGrade() != null) {
			mGrade = (TextView) findViewById(res);
			String grade = mItem.getGrade();
			if (grade.equals("0"))
				mGrade.setText("--");	
			else
				mGrade.setText(grade);
			mGrade.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					int[] location 	= new int[2];
					v.getLocationOnScreen(location);
					initPopupEdit();
					initPopupWindow();
					popupGrade.showAtLocation(v, 
							Gravity.NO_GRAVITY, location[0], location[1]);
				}
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
	 * Initializes popup editor 
	 */
	private void initPopupEdit() {
		int grade;

		popupEdit = new EditText(mContext);
		popupEdit.setTextColor(R.color.black);
		popupEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
		try {
			grade = Integer.parseInt(mGrade.getText().toString());
			popupEdit.setText(grade+"");
		} catch (NumberFormatException e) {
			popupEdit.setText("0");
		}

		popupEdit.setLayoutParams(
				new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		popupEdit.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				popupGrade.update(ViewGroup.LayoutParams.WRAP_CONTENT, 
						ViewGroup.LayoutParams.WRAP_CONTENT);
				try {
					int grade = Integer.parseInt(popupEdit.getText().toString());
					setGrade(grade+"");
				} catch (NumberFormatException e) {
					setGrade("0"); 
				}
			}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		});
		popupEdit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				popupGrade.dismiss();
			}
		});
	}

	/**
	 * Initializes popup editor's window
	 */
	private void initPopupWindow() {
		popupGrade = new PopupWindow(popupEdit, ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT);
		popupGrade.setFocusable(true);
		popupGrade.setTouchable(true);
		popupGrade.setOutsideTouchable(true);
		popupGrade.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		popupGrade.setTouchInterceptor(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				popupGrade.dismiss();
				return true;
			}
		});
		popupGrade.setOnDismissListener(new OnDismissListener() {
			public void onDismiss() {
				String rez = popupEdit.getText().toString().
				replaceAll("^\\s+", "").replaceAll("\\s+$", "");
				setGrade((rez == "") ? "0" : rez);
			}
		});
	}


	/**
	 * Gesture detector class that handles finger gestures
	 * Used to detect fling gesture that increases/decreases a grade
	 */
	private class MyGestureDetector extends SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, 
				float velocityX, float velocityY) {
			int newGrade;

			try {
				newGrade = Integer.parseInt(mGrade.getText().toString());
			} catch (NumberFormatException e) {
				newGrade = 0;
			}
			
			if (e2.getX() > e1.getX()) 
				newGrade++;
			else if (newGrade > 0)
				newGrade--;
			setGrade(newGrade+"");
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
		if (grade.equals("0"))
			mGrade.setText("--");	
		else
			mGrade.setText(grade);
		mItem.setGrade(grade);
	}

	public GroupItem getItem() {
		return mItem;
	}

	public ImageView getImg() {
		return mImg;
	}

	public TextView getGrade() {
		return mGrade;
	}

	public TextView getName() {
		return mName;
	}

	public String getmId() {
		return mItem.getID();
	}

}
