/**
 * Settings.java
 *     
 * Version 1.0
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

package com.android.LabRemote.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/** 
 * Application's settings activity. <br>
 * Allows the users to set the middleware's host, load the login code
 * and switch between courses.
 */
public class Settings extends Activity {

	/** Shows if login was checked or not */
	private boolean mLogin;
	/** Reads application's private data: host, login code and course */
	private SharedPreferences mPreferences;
	/** Writes application's private data: host, login code and course */
	private SharedPreferences.Editor mEditor;
	/** Default value for select course button when no course is selected */
	private static String sDefSelect;

	/** Edit text box where user inserts the server's address */
	private EditText mHost;
	/** When this layout is clicked, the Barcode Scanner application is loaded */
	private LinearLayout mLoadCode;
	/** Displays the current course and allows the user to change it on click */
	private TextView mSelectCourse;
	/** Checked if there is a login code stored in the application's data */
	private CheckBox mCodeChecked;
	/** On click, checks login and loads the main menu activity */
	private Button mDone;
	/** Dialog that allows the user to select a course */
	private AlertDialog.Builder mSelCourseDialog;
	/** Dialog that displays an error when login fails */
	private AlertDialog.Builder mLoginFailedDialog;
	/** Background layout where the user can load the login code manual */
	private RelativeLayout manual;
	/** Allows the user to insert a new login code */
	private EditText editCode;

	private static final String INTENT_SCAN = "com.google.zxing.client.android.SCAN";
	private static final String SCAN_MODE = "SCAN_MODE";
	private static final String QR_CODE_MODE = "QR_CODE_MODE";
	private static final String SCAN_RESULT = "SCAN_RESULT";
	private static final int REQCODE_SCAN = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		invalidateLogin();
		super.onResume();
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.settings);

		sDefSelect = (String) getResources().getString(R.string.change_course);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mEditor = mPreferences.edit(); 
		mLogin = false;

		/** Server address */
		mHost = (EditText)findViewById(R.id.serverHost);
		String host = mPreferences.getString("host", null);
		mHost.setText((host == null) ? "" : host);
		mHost.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String address = parseAddress(mHost.getText().toString());
				mEditor.putString("host", address); 
				mEditor.commit();
				invalidateLogin();
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void afterTextChanged(Editable s){}
		});

		/** Login code */
		mCodeChecked = (CheckBox)findViewById(R.id.codeChecked);
		mLoadCode = (LinearLayout)findViewById(R.id.loadCode);
		mCodeChecked.setChecked(mPreferences.getString("loginCode", null) != null);
		mLoadCode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (checkBarcodeScanner() == false)
					Toast.makeText(v.getContext(), 
							"You must install Barcode scanner app first", 1).show();
				else {
					Intent intent = new Intent(INTENT_SCAN);
					intent.putExtra(SCAN_MODE, QR_CODE_MODE);
					startActivityForResult(intent, REQCODE_SCAN);
				}
			}
		});	

		/** Load login code manual */
		Button manualLoad = (Button)findViewById(R.id.loadManual);
		manual = (RelativeLayout)findViewById(R.id.loadCodeManual);
		manualLoad.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mLoadCode.setVisibility(View.GONE);
				manual.setVisibility(View.VISIBLE);				
			}
		});
		editCode = (EditText)findViewById(R.id.editCode);
		Button loadEditedCode = (Button)findViewById(R.id.loadEditedCode);
		loadEditedCode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mCodeChecked.setChecked(true);
				storeCode(editCode.getText().toString());
				invalidateLogin();	
				mLoadCode.setVisibility(View.VISIBLE);
				manual.setVisibility(View.GONE);	
			}
		});

		/** Select course */
		mSelectCourse = (TextView)findViewById(R.id.selectCourse);
		String course = mPreferences.getString("course", null);
		mSelectCourse.setText((course != null) ? course : sDefSelect);
		mSelectCourse.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ServerResponse result = checkLogin();
				if (result.getError() == null) {
					ArrayList<Map<String, String>> courses = 
						(ArrayList<Map<String, String>>)result.getRespone();
					String[] names = new String[courses.size()];
					String[] ids = new String[courses.size()];
					for (int i = 0; i < courses.size(); i++) {
						names[i] = courses.get(i).get("name");
						ids[i] = courses.get(i).get("id");
					}

					showSelect(names, ids);
					mLogin = true;
				} else 
					showLoginFailed(result.getError());		
			}
		});

		/** Dialogs */
		mSelCourseDialog = new AlertDialog.Builder(this);
		mSelCourseDialog.setTitle("Select a course");
		mLoginFailedDialog = new AlertDialog.Builder(this);
		mLoginFailedDialog.setTitle("Login failed");
		mLoginFailedDialog.setCancelable(true);

		/** Done button */
		mDone = (Button)findViewById(R.id.doneButton);
		mDone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mLogin == false) {
					ServerResponse result = checkLogin();
					if (result.getError() != null) {
						showLoginFailed(result.getError());
						return;
					} 
				}
				Intent mIntent = new Intent(getApplicationContext(), Main.class);
				startActivity(mIntent);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQCODE_SCAN)
		{
			/** Store new login code */
			String result = null;
			if (data != null) {
				result = data.getStringExtra(SCAN_RESULT);
				mCodeChecked.setChecked(true);
				storeCode(result);
				invalidateLogin();
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private ServerResponse checkLogin() {
		return new Connection(this).login();
	}

	/**
	 * Shows select courses dialog
	 */
	private void showSelect(String[] courses, String[] courseIds) {
		final String[] names = courses;
		final String[] ids = courseIds;
		mSelCourseDialog.setItems(courses, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mEditor.putString("course", names[item]);
				mEditor.putString("courseId", ids[item]);
				mSelectCourse.setText(names[item]);
			}
		});
		mSelCourseDialog.create().show();		
	}

	private void showLoginFailed(String error) {
		mLoginFailedDialog.setMessage(error);
		mLoginFailedDialog.show();		
	}

	/**
	 * Checks if Barcode Scanner application is installed
	 */
	private boolean checkBarcodeScanner() {
		final PackageManager packageManager = this.getPackageManager();
		final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	/**
	 * Stores new code in the application's private data
	 */
	private void storeCode(String code) {
		if (code != null) {
			StringTokenizer st = new StringTokenizer(code, "|"); 
			if (st.countTokens() == 2) {
				mEditor.putString("userId", st.nextToken());
				mEditor.putString("loginCode", st.nextToken()); 
				mEditor.commit();
				mCodeChecked.setChecked(true);
			}
		}
	}

	/**
	 * Invalidates login data when the user 
	 * changes host or login code
	 */
	private void invalidateLogin() {
		mSelectCourse.setText(sDefSelect);
		mEditor.putString("course", null);
		mEditor.commit();
		mLogin = false;
	}

	/**
	 * Manages server address
	 */
	private String parseAddress(String host) {
		String result = host.trim();
		return (result.startsWith("http://") ? result : "http://" + result);
	}

}
