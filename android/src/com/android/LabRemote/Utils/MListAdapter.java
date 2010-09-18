/**
 * MListAdapter.java
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

package com.android.LabRemote.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;

import com.android.LabRemote.UI.AttendanceItemView;
import com.android.LabRemote.UI.GroupItemView;
import com.android.LabRemote.UI.ListItemView;

/**
 *  Adapter class for the list's content 
 *  Manages the item in the list
 *  @see ListItemView
 *  @see MListItem
 */
public class MListAdapter extends BaseAdapter {

	/** Array filled with list's elements */
	private ArrayList<MListItem> mItems = new ArrayList<MListItem>();
	/** Callback when a new avatar is downloaded and parsed */
	private AvatarCallback mAvatarCallback;
	/** Called when a list item is clicked */
	private OnClickListener mOnItemClick;
	private boolean attendance = false;
	private Context mContext;

	public MListAdapter(Context context, ArrayList<MListItem> items, 
			AvatarCallback avatarCallback, OnClickListener onItemClick) {
		mAvatarCallback = avatarCallback;
		mOnItemClick = onItemClick;
		mContext = context;
		mItems = items;
	}
	
	public MListAdapter(Context context, ArrayList<MListItem> items) {
		mContext = context;
		mItems = items;
		attendance = true;
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
		ListItemView item;
		MListItem it = mItems.get(index);
		
		if (attendance == false) {
			item = new GroupItemView(mContext, mItems.get(index));
			if (it.getAvatar() == null)
				new DownloadAvatar(it.getImgUrl(), mAvatarCallback, item);
			item.setClickable(true);
			item.setOnClickListener(mOnItemClick);
		} else
			item = new AttendanceItemView(mContext, mItems.get(index));
		
		return item;
	}

	/**
	 * Downloads an avatar from the given url and fires the callback
	 * function that displays the new photo
	 */
	private class DownloadAvatar extends Thread {
		private String mUrl;
		private AvatarCallback mAvatarCallback;
		private ListItemView mItem;

		public DownloadAvatar(String url, AvatarCallback avatarCallback, ListItemView item) {
			mUrl = url;
			mAvatarCallback= avatarCallback;
			mItem = item;
			start();
		}

		public void run() {
			try
			{
				HttpURLConnection con = (HttpURLConnection)(new URL(mUrl)).openConnection();
				con.connect();
				Bitmap b = BitmapFactory.decodeStream(con.getInputStream());
				mItem.getItem().setAvatar(b);
				ShowAvatar displayer = new ShowAvatar(mItem, b);
				mAvatarCallback.onImageReceived(displayer);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
}
