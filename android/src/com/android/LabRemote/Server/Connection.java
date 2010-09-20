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
//TODO: pun status in rasp 
//TODO: object la search in loc de array

public class Connection {
	private SharedPreferences mPreferences;
	private Context mContext;
	private String mCode;
	private String mHost;
	private String mCourse;
	private String mID;
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
		"There was a problem with the server or the request";
	private static final String invalidLogin = 
		"Invalid login. Would you like to change the host or load another code?";
	private static final String invalidResponse = "Invalid response from the server";
	
	public Connection(Context context) {
		mContext = context;
		mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		this.mCode = mPreferences.getString("loginCode", null);
		this.mHost = mPreferences.getString("host", null);
		this.mCourse = mPreferences.getString("course", null);
		this.mID = mPreferences.getString("userId", null);
		this.mHttpClient = new DefaultHttpClient();
	}

	/**
	 * Generic http request
	 * @param url
	 * @return Response string or null if there was a problem 
	 * with the server or the request
	 */
	public String httpReq(String url) {
		HttpGet httpget = new HttpGet(url); 
		HttpResponse response;
		String result = null;

		try {
			response = mHttpClient.execute(httpget);
			int code = response.getStatusLine().getStatusCode();
			if (code < 200 || code > 299) 
				return null;			
			
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
			if (log == "invalid") {
				return new LoginData(invalidLogin);
			}

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
	 * TODO: daca imi pot trimite numai json object
	 */
	public ServerResponse get(String request) {
		JSONObject jObject = null;
		
		/** HTTP error (server or client) */
		String result = httpReq(request);
		if (result == null) 
			return new ServerResponse(null, serverError); 

		/** Invalid response from server */ //serverul nu stie api 
		try {
			jObject = new JSONObject(result);
		} catch (JSONException e) {
			e.printStackTrace();
			return new ServerResponse(null, invalidResponse); 
		}
		
		//TODO: daca am status o sa-l verific aici inainte sa extrag err
		/** Invalid request from client */
		try {
			String err = jObject.getString("error");
			if (err != null) 
				return new ServerResponse(null, err);
		} catch (JSONException e) {
			e.printStackTrace(); 
		}

		return new ServerResponse(jObject, null);
	}

	public ServerResponse getGroup(String group) {
		String request = mHost + groupQuery + mCourse + "/" + mID + "/" + mCode + "/" + group;
		return get(request);
	}
	
	public ServerResponse getCurrentGroup() {
		String request = mHost + currentQuery + mCourse + "/" + mID + "/" + mCode;
		return get(request);
	}

	public ServerResponse getSearch(String query) {
		String request = mHost + searchQuery + mID + "/" + mCode + "/" + query;
		return get(request);
	}

	public ServerResponse getStudent(String id) {
		String request = mHost + individualQuery + mCourse + "/" + mID + "/" + mCode + "/" + id;
		return get(request);
	}

	public ServerResponse getTimetable() {
		String request = mHost + timeQuery + mCourse + "/" + mID + "/" + mCode;
		return get(request);
	}

	//TODO: si de aici ies pe probl de rasp de la server
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
