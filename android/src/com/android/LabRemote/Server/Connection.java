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

//TODO: Treat errors and exceptions

public class Connection {
	private static final String logQuery = "/api/login/";
	private static final String searchQuery = "/api/search/";
	private static final String individualQuery = "/api/student/";
	private static final String groupQuery = "/api/group/";
	private static final String timeQuery = "/api/timetable/";
	private static int ARRAY = 1;
	private static int OBJECT = 0;
	//private static final String HOST = "http://10.0.2.2:8000";
	private static final String HOST = "http://lr.korect.ro";
	private String baseURL;
	private HttpClient mHttpClient;

	public Connection(Context context) {
		this.baseURL = HOST; //TODO: gets host from shared pref
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
	public boolean login(String code, Context context) {
		// Prepare a request object
		String request = baseURL + logQuery + code;
		String result = httpReq(request);
		if (result == null)
			return false;
		JSONObject jObject = null;

		try {
			jObject = new JSONObject(result);
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = preferences.edit();

			String log = jObject.getString("login");
			if (log == "invalid")
				return false;

			String user_id = jObject.getString("user");
			String name = jObject.getString("name");
			String course = jObject.getJSONArray("courses").getString(0);

			editor.putString("userId", user_id); 
			editor.putString("userName", name); 
			editor.putString("course", course); 
			editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
			//return false;
		}

		return true;
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
		String request = baseURL + groupQuery + id + "/" + session_key + "/" + group;
		return (JSONObject) get(request, OBJECT);
	}

	public JSONArray getSearch(String id, String session_key, String query) {
		String request = baseURL + searchQuery + id + "/" + session_key + "/" + query;
		return (JSONArray) get(request, ARRAY);
	}

	public JSONObject getStudent(String id, String session_key, String course, String studId) {
		String request = baseURL + individualQuery + id + "/" + session_key + "/" + course + "/" + studId;
		return (JSONObject) get(request, OBJECT);
	}

	//intoarce timetable array/json
	///timetable/“user_id”/“session_key”/
	public JSONObject getTimetable(String id, String session_key) {
		String request = baseURL + timeQuery + id + "/" + session_key;
		ArrayList<Hashtable<String, String>> timetable = new ArrayList<Hashtable<String, String>>();
		JSONObject table;

		try {
			table = ((JSONObject)get(request, OBJECT)).getJSONObject("timetable"); //TODO: check null
			timetable.add(getWeekDay(table, "monday"));
			timetable.add(getWeekDay(table, "tuesday"));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return table;
	}

	//{ "8-10" : ["gr1", "gr2", "gr4"], .... }
	//=> vector cu 7 jsonobj (per zile)
	//in timetab, pt fiecare zi din vector => getKeys
	//=>hashtable sort dupa chei
	//pt fiecare noua cheie => afisez cheie + val
	private Hashtable<String, String> getWeekDay(JSONObject from, String when) {
		Hashtable<String, String> day = new Hashtable<String, String>();
		try {
			JSONObject jDay = from.getJSONObject(when);
			Iterator rez = jDay.keys();

			while (rez.hasNext()) {
				String key = (String) rez.next();
				String val = (String) from.get(key);
				day.put(key, val);
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
