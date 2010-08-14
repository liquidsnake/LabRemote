/** 
 * Listview - the students of a specific class
 * @see GroupViewItem
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.UI;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.LabRemote.R;
import com.android.LabRemote.Utils.GroupItem;

public class GroupView extends Activity implements OnGesturePerformedListener {

	private String mGroup, mDate;
	private ListView mListView;
	private ArrayList<GroupItem> mList;
	private Intent mIndividualIntent;
	private GroupAdapter mAdapter;
	private GestureLibrary mLibrary;

	/**
	 * On click on a list item, open individual activity for the selected student
	 */
	private OnClickListener onItemClick = new OnClickListener() {
		public void onClick(View v) {
			mIndividualIntent = new Intent(getApplicationContext(), IndividualView.class);
			mIndividualIntent.putExtra("Name", ((GroupItemView)v).getName().getText()); 
			mIndividualIntent.putExtra("Group", mGroup); 
			mIndividualIntent.putExtra("Date", mDate); 
			startActivity(mIndividualIntent);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.group_view);

		receiveData();

		fillList();
		mListView.setAdapter(mAdapter);

		/** Gestures Test */
		mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!mLibrary.load()) {
			finish();
		}
		GestureOverlayView gestures = (GestureOverlayView)findViewById(R.id.gestures);
		gestures.addOnGesturePerformedListener(this);		
	}

	/**
	 * Receive data from the previous activity
	 */
	private void receiveData() {

		mGroup = getIntent().getStringExtra("Group");
		TextView groupText = (TextView) findViewById(R.id.classHeader);
		groupText.setText(mGroup);
		mDate = getIntent().getStringExtra("Date");
		TextView dateText = (TextView) findViewById(R.id.dateHeader);
		dateText.setText(mDate);

	}

	/**
	 * Fill list with students
	 */
	private void fillList() {

		mListView = (ListView) findViewById(R.id.studentsList);
		mList = new ArrayList<GroupItem>();

		/** Test Data for the list view */
		String names[] = {"Popescu Anca", "Ionescu Ana Andreea", 
				"Bobocescu Alina", "Popescu Ana Cristiana"};
		for (int i = 0; i < 10; i++) {
			mList.add(new GroupItem(R.drawable.mi, names[i%4], i + ""));
		}

		mAdapter = new GroupAdapter(this, mList);
	}

	/**
	 *  Adapter class for the list's content 
	 */
	private class GroupAdapter extends BaseAdapter {

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
				item.setClickable(true);
				item.setOnClickListener(onItemClick);
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

	/**
	 * Gestures test
	 */
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList predictions = mLibrary.recognize(gesture);

		if (predictions.size() > 0) {
			Prediction prediction = (Prediction) predictions.get(0);
			if (prediction.score > 1.0) {
				Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT).show();
			}
		}
	}

}





