/**
 * MListItem.java
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

import android.graphics.Bitmap;

/**
 * Data structure that maintains an item from a list of students
 */
public class MListItem {

	private String mImgUrl; 
	private String mName;
	private String mGrade; 
	private Bitmap mAvatar;
	private String mID;	

	public MListItem(String img, String name, String grade, String id) {
		mImgUrl = img;
		mName = name;
		mGrade = grade;
		mAvatar = null;
		mID = id;
	}

	public MListItem(String img, String name, String id) {
		mImgUrl = img;
		mName = name;
		mGrade = null;
		mAvatar = null;
		mID = id;
	}

	public Bitmap getAvatar() {
		return mAvatar;
	}

	public void setAvatar(Bitmap avatar) {
		mAvatar = avatar;
	}

	public String getID() {
		return mID;
	}

	public void setID(String ID) {
		mID = ID;
	}

	public String getImgUrl() {
		return mImgUrl;
	}

	public void setImg(String imgUrl) {
		mImgUrl = imgUrl;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getGrade() {
		return mGrade;
	}

	public void setGrade(String grade) {
		mGrade = grade;
	}

}