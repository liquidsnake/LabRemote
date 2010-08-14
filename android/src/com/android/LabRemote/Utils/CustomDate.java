/** 
 * Custom Date 
 * Custom date format for the application header
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
 */

package com.android.LabRemote.Utils;

import java.util.Calendar;

public class CustomDate {
	public static final String[] week_day = { "Monday", "Tuesday", "Wednesday",
		"Thursday", "Friday", "Saturday", "Sunday" };
	public static final String[] short_week = { "Mon", "Tue", "Wed", "Thu",
		"Fri", "Sat", "Sun" };
	public static final String[] month = { "January", "February", "March",
		"April", "May", "June", "July", "August", "September", "October",
		"November", "December" };

	public static String getDate(int day) {
		String result;

		final Calendar c = Calendar.getInstance();
		int mDay = c.get(Calendar.DAY_OF_MONTH) + day - c.get(Calendar.DAY_OF_WEEK) + 2;
		result = short_week[day] + ", " + mDay + " " + month[c.get(Calendar.MONTH)];

		return result;		
	}

	public static String getCurrentDate() {
		String result;

		final Calendar c = Calendar.getInstance();
		int mDay = c.get(Calendar.DAY_OF_MONTH);
		result = short_week[c.get(Calendar.DAY_OF_WEEK)-2] + ", " + mDay + " " + month[c.get(Calendar.MONTH)];

		return result;	
	}

}
