package com.android.LabRemote.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;

public class StudentProvider extends ContentProvider {

	public static String AUTHORITY = "com.android.LabRemote.Utils.StudentProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/student");

	private static final int SEARCH_WORDS = 0;
	private static final int SEARCH_SUGGEST = 2;
	private static final int REFRESH_SHORTCUT = 3;
	private static final UriMatcher sURIMatcher = buildUriMatcher();

	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);

		matcher.addURI(AUTHORITY, "student", SEARCH_WORDS);

		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

		return matcher;
	}

	@Override
	public boolean onCreate() {
		return true;
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
		case SEARCH_WORDS:
			if (selectionArgs == null) {
				throw new IllegalArgumentException(
						"selectionArgs must be provided for the Uri: " + uri);
			}
			return search(selectionArgs[0]);
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	private Cursor getSuggestions(String query) {//TODO:
		query = query.toLowerCase();
		String[] columns = new String[] {
				BaseColumns._ID,
				SearchManager.SUGGEST_COLUMN_TEXT_1,
				SearchManager.SUGGEST_COLUMN_INTENT_DATA};

		MatrixCursor res = new MatrixCursor(columns);
		ServerResponse result = new Connection(getContext()).getSearch(query); 
		JSONObject mData = (JSONObject)result.getRespone();
		try {
			JSONArray ar = mData.getJSONArray("students");
			for(int i = 0; i < ar.length(); i++) {
				JSONObject student = ar.getJSONObject(i);
				res.addRow(new String[]{student.getString("id"), student.getString("name"), student.getString("id")});
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} 

		return res;
	}

	private Cursor search(String query) {
		query = query.toLowerCase();
		String[] columns = new String[] {
				BaseColumns._ID,
				SearchManager.SUGGEST_COLUMN_TEXT_1}; // este nume student

		MatrixCursor res = new MatrixCursor(columns);
		ServerResponse result = new Connection(getContext()).getSearch(query); 
		JSONObject mData = (JSONObject)result.getRespone();
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
		case REFRESH_SHORTCUT:
			return SearchManager.SHORTCUT_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
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
