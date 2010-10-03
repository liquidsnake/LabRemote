/**
 * CustomDate.java
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

import java.util.Calendar;

/** 
 * Custom date format for the application header
 * TODO: testat bine
 */
public class CustomDate {
	public static final String[] short_week = { "Sun", "Mon", "Tue", "Wed", "Thu",
		"Fri", "Sat"};
	public static final String[] month = { "January", "February", "March",
		"April", "May", "June", "July", "August", "September", "October",
		"November", "December" };

	public static String getDate(int day) { 
		String result;
		day = day%7 + 1;
		final Calendar c = Calendar.getInstance();
		int dif = day - c.get(Calendar.DAY_OF_WEEK); 
		c.add(Calendar.DATE, dif);
		result = short_week[day-1] + ", " + c.get(Calendar.DAY_OF_MONTH) + 
				" " + month[c.get(Calendar.MONTH)];
		return result;		
	}

	public static String getCurrentDate() {
		String result;

		final Calendar c = Calendar.getInstance();
		int mDay = c.get(Calendar.DAY_OF_MONTH);
		result = short_week[c.get(Calendar.DAY_OF_WEEK)-1] + ", " + mDay + 
				" " + month[c.get(Calendar.MONTH)];
		return result;	
	}

}
