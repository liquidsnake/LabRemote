/**
 * StudentProvider.java
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

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides data for the search manager
 */
public class StudentProvider extends ContentProvider {
	public static String AUTHORITY = "com.android.LabRemote.Utils.StudentProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/student");
	private static final int SEARCH_STUDENT = 0;
	private static final int SEARCH_SUGGEST = 1;
	private static final UriMatcher sURIMatcher = buildUriMatcher();

	/**
	 * Creates an uri matcher, used by the provider to identify its data set <br />
	 * Can refer search results or suggestions 
	 */ 
	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, "student", SEARCH_STUDENT);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
		return matcher;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (sURIMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			if (selectionArgs == null) {
				throw new IllegalArgumentException(
						"selectionArgs must be provided for the Uri: " + uri);
			}
			return getSuggestions(selectionArgs[0]);
		case SEARCH_STUDENT:
			if (selectionArgs == null) {
				throw new IllegalArgumentException(
						"selectionArgs must be provided for the Uri: " + uri);
			}
			return search(selectionArgs[0]); 
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	/**
	 * Loads search suggestions from server 
	 */
	private Cursor getSuggestions(String query) {
		String[] columns = new String[] {
				BaseColumns._ID,
				SearchManager.SUGGEST_COLUMN_TEXT_1,
				SearchManager.SUGGEST_COLUMN_INTENT_DATA};
		MatrixCursor res = new MatrixCursor(columns);
		if (query == null)
			return res;

		query = query.toLowerCase();
		ServerResponse result = new Connection(getContext()).getSearch(query); 
		JSONObject mData = (JSONObject)result.getRespone();
		if (mData != null)
		try {
			JSONArray ar = mData.getJSONArray("students"); 
			for(int i = 0; i < ar.length(); i++) {
				JSONObject student = ar.getJSONObject(i);
				res.addRow(new String[]{student.getString("id"), 
						student.getString("name"), student.getString("id")});
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} 

		return res;
	}

	/**
	 * Loads search results from server
	 */
	private Cursor search(String query) {
		String[] columns = new String[] {
				BaseColumns._ID,
				SearchManager.SUGGEST_COLUMN_TEXT_1}; 
		MatrixCursor res = new MatrixCursor(columns);
		if (query == null)
			return null;
		
		query = query.toLowerCase();
		ServerResponse result = new Connection(getContext()).getSearch(query); 
		JSONObject mData = (JSONObject)result.getRespone();
		if (mData != null)
		try {
			JSONArray ar = mData.getJSONArray("students");
			for(int i = 0; i < ar.length(); i++) {
				JSONObject student = ar.getJSONObject(i);
				res.addRow(new String[]{student.getString("id"), student.getString("name")});
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return res;
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}
}
