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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	/** Selected course's id */
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
	private static final String groupsQuery = "/api/groups/";
	private static final String timeQuery = "/api/timetable/";
	private static final String currentQuery = "/api/current_group/";
	
	/** Error messages */
	private static final String invalidResponse = "Invalid response from the server";
	private static final String serverError = "There was a problem with the server or the request";
	
	public Connection(Context context) {
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.mCode = mPreferences.getString("loginCode", null);
		this.mHost = mPreferences.getString("host", null);
		this.mCourse = mPreferences.getString("courseId", null);
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
		
		/** Server request */
		String request = mHost + logQuery + mID + "/";
		String signature = md5("login" + mID + mCode);
		res = get(request+signature+"/");
		jObject = (JSONObject) res.getRespone();
		if (jObject == null)
			return res;

		/** Parse response */
		ArrayList<Map<String, String>> courses = new ArrayList<Map<String, String>>();
		try {
			SharedPreferences.Editor editor = mPreferences.edit();
			//String user_id = jObject.getString("user");
			JSONArray jsonCourses = jObject.getJSONArray("courses");
			
			for (int i = 0; i < jsonCourses.length(); i++) {
				Map<String, String> course = new HashMap<String, String>(); 
				course.put("id", jsonCourses.getJSONObject(i).getString("id"));
				course.put("name", jsonCourses.getJSONObject(i).getString("abbr"));
				courses.add(course);
			}

			if (!courses.isEmpty()) {
				editor.putString("course", courses.get(0).get("name"));
				editor.putString("courseId", courses.get(0).get("id"));
			}
			//editor.putString("userId", user_id); 
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

	public ServerResponse getGroup(String group, String aid, String week) {
		String request = mHost + groupQuery + mCourse + "/" + mID + "/" 
				+ group + "/" + aid + "/";
		String signature = "group" + mCourse + mID + group + aid;
		if (week != null) {
			request += week + "/";
			signature += week;
		}			
		signature = md5(signature+mCode);
		return get(request+signature+"/");
	}
	
	public ServerResponse getGroups() {
		String request = mHost + groupsQuery + mCourse + "/" + mID + "/";
		String signature = md5("groups" + mCourse + mID + mCode);
		
		return get(request+signature+"/");
	}
	
	public ServerResponse getCurrentGroup() {
		String request = mHost + currentQuery + mCourse + "/" + mID + "/";
		String signature = md5("current_group" + mCourse + mID + mCode); 
		return get(request+signature+"/");
	}

	public ServerResponse getSearch(String query) {
		String request = mHost + searchQuery + mCourse + "/" + mID + "/" + query + "/";
		String signature = md5("search" + mCourse + mID + query + mCode);
		return get(request+signature+"/");
	}

	public ServerResponse getStudent(String id) {
		String request = mHost + individualQuery + mCourse + "/" + mID + "/" + id + "/";
		String signature = md5("student" + mCourse + mID + id + mCode);
		return get(request+signature+"/");
	}

	public ServerResponse getTimetable() {
		String request = mHost + timeQuery + mCourse + "/" + mID + "/";
		String signature = md5("timetable" + mCourse + mID + mCode);
		return get(request+signature+"/");
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
		String signature = new String();
		HttpResponse res = null;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);  
        nameValuePairs.add(new BasicNameValuePair("user", mID));  
        signature += mID;
        nameValuePairs.add(new BasicNameValuePair("course", mCourse));  
        signature += mCourse;
        nameValuePairs.add(new BasicNameValuePair("type", type));
        signature += type;
        nameValuePairs.add(new BasicNameValuePair("contents", data.toString()));  
        signature += data.toString();
        signature += mCode;
        nameValuePairs.add(new BasicNameValuePair("hash", md5(signature)));  
    
        
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
	
	public String md5(String input) {  
	    try     {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1,messageDigest);
            String md5 = number.toString(16);
       
            while (md5.length() < 32)
                    md5 = "0" + md5;
       
            return md5;
	    } catch(NoSuchAlgorithmException e) {
            return null;
	    }
	}  
}
