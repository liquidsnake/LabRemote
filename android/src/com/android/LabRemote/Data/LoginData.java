/**
 * LoginData.java
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

package com.android.LabRemote.Data;

public class LoginData {
	private String mErrorMessage;
	private String[] mCourses;
	
	public LoginData(String error) {
		mErrorMessage = error;
	}
	
	public void addCourses(String[] course) {
		mCourses = course;
	}
	
	public String[] getCourses() {
		return mCourses;
	}
	
	public String getError() {
		return mErrorMessage;
	}
}
