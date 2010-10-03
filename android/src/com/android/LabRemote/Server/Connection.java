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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

//TODO: return null la catch
/**
 * Handles communication with the server: API posts and request
 */
public class Connection {
	/** Edit and store application's private data */
	private SharedPreferences mPreferences;
	/** Login code */
	private String mCode;
	/** Middleware's address */
	private String mHost;
	/** Selected course */
	private String mCourse;
	/** Assistant's id */
	private String mID;
	/** Http client used for queries */
	private HttpClient mHttpClient;
	
	/** API Queries */
	private static final String logQuery = "/api/login/";
	private static final String searchQuery = "/api/search/";
	private static final String individualQuery = "/api/student/";
	private static final String groupQuery = "/api/group/";
	private static final String timeQuery = "/api/timetable/";
	private static final String currentQuery = "/api/current_group/";
	
	/** Error messages */
	private static final String invalidResponse = "Invalid response from the server";
	private static final String serverError = "There was a problem with the server or the request";
	
	public Connection(Context context) {
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.mCode = mPreferences.getString("loginCode", null);
		this.mHost = mPreferences.getString("host", null);
		this.mCourse = mPreferences.getString("course", null);
		this.mID = mPreferences.getString("userId", null);
		this.mHttpClient = new DefaultHttpClient();
	}

	/**
	 * Generic http request
	 * @param url Server address
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
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Handles login queries and saves informations received from 
	 * the server in the application's private data
	 * @return {@link ServerResponse}
	 */
	public ServerResponse login() {
		ServerResponse res;
		JSONObject jObject = null;
		String[] courses;	
		
		/** Server request */
		String request = mHost + logQuery + mCode;
		res = get(request);
		jObject = (JSONObject) res.getRespone();
		if (jObject == null)
			return res;

		/** Parse response */
		try {
			SharedPreferences.Editor editor = mPreferences.edit();
			String user_id = jObject.getString("user");
			JSONArray c = jObject.getJSONArray("courses");
			courses = new String[c.length()];
			for (int i = 0; i < c.length(); i++)
				courses[i] = c.getString(i);

			if (c.length() > 0)
				editor.putString("course", courses[0]); 
			editor.putString("userId", user_id); 
			editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
			return new ServerResponse(null, invalidResponse);
		}

		/** Successful login */
		return new ServerResponse(courses, null);
	}

	/**
	 * Generic data request
	 * @param request
	 * @return {@link ServerResponse}
	 */
	public ServerResponse get(String request) {
		JSONObject jObject = null;
		
		/** HTTP error (server or client) */
		String result = httpReq(request);
		if (result == null) 
			return new ServerResponse(null, serverError); 

		/** Invalid response from server or error */  
		try {
			jObject = new JSONObject(result);
			if (jObject.getString("status").equals("failed"))
				return new ServerResponse(null, jObject.getString("error")); 
		} catch (JSONException e) {
			e.printStackTrace();
			return new ServerResponse(null, invalidResponse); 
		}

		return new ServerResponse(jObject, null);
	}

	public ServerResponse getGroup(String group, String aid) {
		String request = mHost + groupQuery + mCourse + "/" + mID + "/" 
				+ mCode + "/" + group + "/" + aid;
		return get(request);
	}
	
	public ServerResponse getCurrentGroup() {
		String request = mHost + currentQuery + mCourse + "/" + mID + "/" + mCode;
		return get(request);
	}

	public ServerResponse getSearch(String query) {
		String request = mHost + searchQuery + mCourse + "/" + mID + "/" + mCode + "/" + query;
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
	
	/**
	 * Posts data on the server
	 * @param data content for post
	 * @param type Post's type (e.g.: group)
	 * @return a {@link ServerResponse} that shows if the post succedeed or not
	 */
	public ServerResponse post(JSONObject data, String type) {
		String url = mHost + "/api/post/";
		HttpPost httpost = new HttpPost(url);
		HttpResponse res = null;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);  
        
        nameValuePairs.add(new BasicNameValuePair("user", mID));  
        nameValuePairs.add(new BasicNameValuePair("course", mCourse));  
        nameValuePairs.add(new BasicNameValuePair("session_key", mCode));  
        nameValuePairs.add(new BasicNameValuePair("type", type));
        nameValuePairs.add(new BasicNameValuePair("contents", data.toString()));  
        
        /** Send post */
		try {
	        httpost.setEntity(new UrlEncodedFormEntity(nameValuePairs)); 
			res = mHttpClient.execute(httpost);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return new ServerResponse("failed", "invalid post");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return new ServerResponse("failed", "invalid post");
		} catch (IOException e) {
			e.printStackTrace();
			return new ServerResponse("failed", "invalid post");
		}
		
		/** Read post result */
		HttpEntity entity = res.getEntity();
		if (entity != null) {
			InputStream instream;
			try {
				instream = entity.getContent();
				String rez = convertStreamToString(instream);
				instream.close();
				JSONObject result = new JSONObject(rez);
				if (result.getString("status").equals("success"))
					return new ServerResponse("success", null);
				else
					return new ServerResponse("failed", result.getString("error"));
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return new ServerResponse("failed", "invalid post");
	}

	/**
	 * Used for parsing http data 
	 */
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
			return null;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return sb.toString();
	}
}
