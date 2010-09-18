/**
 * Connection.java
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

package com.android.LabRemote.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.LabRemote.Data.LoginData;

//TODO: Treat errors and exceptions
//TODO: No more session keys.. le ia direct din prefs

public class Connection {
	private static int ARRAY = 1;
	private static int OBJECT = 0;
	private SharedPreferences mPreferences;
	private String mCode;
	private String mHost;
	private HttpClient mHttpClient;
	
	/** API Queries */
	private static final String logQuery = "/api/login/";
	private static final String searchQuery = "/api/search/";
	private static final String individualQuery = "/api/student/";
	private static final String groupQuery = "/api/group/";
	private static final String timeQuery = "/api/timetable/";
	private static final String currentQuery = "/api/current_group/";
	
	/** Error messages */
	private static final String serverError = 
		"There was a problem with the server. Would you like to try another host address?";
	private static final String invalidLogin = 
		"Invalid login.Would you like to change the host or load another code?";
	private static final String invalidResponse = "Invalid response from the server";

	public Connection(Context context) {
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.mCode = mPreferences.getString("loginCode", null);
		this.mHost = mPreferences.getString("host", null);
		this.mHttpClient = new DefaultHttpClient();
	}

	/**
	 * Generic http request
	 * @param url
	 * @return
	 */
	public String httpReq(String url) {
		HttpGet httpget = new HttpGet(url); 
		HttpResponse response;
		String result = null;

		try {
			response = mHttpClient.execute(httpget);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				result = convertStreamToString(instream);
				instream.close();
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		return result;
	}

	/**
	 * 
	 * @param code
	 * @param context
	 * @return
	 */
	public LoginData login() {
		LoginData res;
		JSONObject jObject = null;
		String[] courses;	
		
		/** Http request */
		String request = mHost + logQuery + mCode;
		String result = httpReq(request);
		if (result == null) 
			return new LoginData(serverError);

		/** Parse response */
		try {
			jObject = new JSONObject(result);
			SharedPreferences.Editor editor = mPreferences.edit();

			String log = jObject.getString("login");
			if (log == "invalid")
				return new LoginData(invalidLogin);

			String user_id = jObject.getString("user");
			JSONArray c = jObject.getJSONArray("courses");
			courses = new String[c.length()];
			for (int i = 0; i < c.length(); i++)
				courses[i] = c.getString(i);

			editor.putString("userId", user_id); 
			if (c.length() > 0)
				editor.putString("course", courses[0]); 
			editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
			return new LoginData(invalidResponse);
		}

		/** Successful login */
		res = new LoginData(null);
		res.addCourses(courses);
		return res;
	}

	/**
	 * Generic data request
	 * @param request
	 * @param type
	 * @return
	 */
	public Object get(String request, int type) {
		Object jObject = null;
		String result = httpReq(request);
		if (result == null)
			return null;

		try {
			if (type == ARRAY)
				jObject = new JSONArray(result);
			else if (type == OBJECT)
				jObject = new JSONObject(result);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return jObject;
	}

	public JSONObject getGroup(String id, String session_key, String group) {
		String request = mHost + groupQuery + id + "/" + session_key + "/" + group;
		return (JSONObject) get(request, OBJECT);
	}
	
	public JSONObject getCurrentGroup(String id, String session_key) {
		String request = mHost + currentQuery + id + "/" + session_key;
		return (JSONObject) get(request, OBJECT);
	}

	public JSONArray getSearch(String id, String session_key, String query) {
		String request = mHost + searchQuery + id + "/" + session_key + "/" + query;
		return (JSONArray) get(request, ARRAY);
	}

	public JSONObject getStudent(String id, String session_key, String course, String studId) {
		System.out.println("cu " + course + id); 
		String request = mHost + individualQuery + id + "/" + session_key + "/" + course + "/" + studId;
		return (JSONObject) get(request, OBJECT);
	}

	public ArrayList<Hashtable<String, List<String>>> getTimetable(String id, String session_key) {
		String request = mHost + timeQuery + id + "/" + session_key;
		ArrayList<Hashtable<String, List<String>>> timetable = new ArrayList<Hashtable<String, List<String>>>();
		JSONObject table;

		try {
			table = ((JSONObject)get(request, OBJECT)).getJSONObject("timetable"); //TODO: check null
			timetable.add(getWeekDay(table, "monday")); //TODO: check null inainte de add
			timetable.add(getWeekDay(table, "tuesday"));
			timetable.add(getWeekDay(table, "wednesday"));
			timetable.add(getWeekDay(table, "thursday"));
			timetable.add(getWeekDay(table, "friday"));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return timetable;
	}

	private Hashtable<String, List<String>> getWeekDay(JSONObject from, String when) {
		Hashtable<String, List<String>> day = new Hashtable<String, List<String>>();
		try {
			JSONObject jDay = from.getJSONObject(when);
			Iterator rez = jDay.keys();

			while (rez.hasNext()) {
				String key = (String) rez.next();
				JSONArray val = (JSONArray) jDay.get(key);
				ArrayList<String> vals = new ArrayList<String>();
				for (int i = 0; i < val.length(); i++)
					vals.add(val.getString(i));
				day.put(key, vals);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return day;
	}


	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}
