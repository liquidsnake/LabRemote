/* 
 * Data structure that maintains a group view item
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.Utils;

public class GroupItem {
	private int mImg; //TODO: change resource image with the image received from server
	private String mName;
	private String mGrade; 
	
	public GroupItem(int img, String name, String grade) {
		mImg = img;
		mName = name;
		mGrade = grade;
	}
	
	public int getImg() {
		return mImg;
	}
	
	public void setImg(int mImg) {
		this.mImg = mImg;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String mName) {
		this.mName = mName;
	}
	
	public String getGrade() {
		return mGrade;
	}
	
	public void setGrade(String mGrade) {
		this.mGrade = mGrade;
	}
	
}