/**
 * Settings.java
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

package com.android.LabRemote.UI;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.LabRemote.R;
import com.android.LabRemote.Data.LoginData;
import com.android.LabRemote.Server.Connection;

/** 
 * Application's settings activity
 * Allows the users to set the middleware's host, load the login code
 * and switch between courses.
 */
public class Settings extends Activity {
	private boolean login;
	private SharedPreferences mPreferences;
	private SharedPreferences.Editor mEditor;
	private static String defServer = "Insert server address";
	private static String defSelect = "Click here to select course";

	
	/** UI content */
	private EditText mHost;
	private TextView mLoadCode, mSelectCourse;
	private CheckBox mCodeChecked;
	private Button mDone;
	private Intent mMain;
	private AlertDialog.Builder mSelCourseDialog;
	private Dialog mLoginFailedDialog;

	/** Barcode Scanner */
	private static final String INTENT_SCAN = "com.google.zxing.client.android.SCAN";
	private static final String SCAN_MODE = "SCAN_MODE";
	private static final String QR_CODE_MODE = "QR_CODE_MODE";
	private static final String SCAN_RESULT = "SCAN_RESULT";
	private static final int REQCODE_SCAN = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.settings);
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mMain = new Intent(getApplicationContext(), Main.class);
		mEditor = mPreferences.edit(); 
		login = false;
		
		/** Server address */
		mHost = (EditText)findViewById(R.id.serverHost);
		String host = mPreferences.getString("host", null);
		mHost.setText((host == null) ? defServer : host);
		mHost.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mEditor.putString("host", mHost.getText().toString()); 
				mEditor.commit();
				invalidateLogin();
			}
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			public void afterTextChanged(Editable s) {}
		});
		
		/** Login code */
		mCodeChecked = (CheckBox)findViewById(R.id.codeChecked);
		mLoadCode = (TextView)findViewById(R.id.loadCode);
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
		
		/** Select course */
		mSelectCourse = (TextView)findViewById(R.id.selectCourse);
		String course = mPreferences.getString("course", null);
		mSelectCourse.setText((course != null) ? course : defSelect);
		mSelectCourse.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				LoginData result = checkLogin();
				if (result.getError() == null) {
					showSelect(result.getCourses());
					login = true;
				} else 
					showLoginFailed(result.getError());		
			}
		});
		
		/** Dialogs */
		mSelCourseDialog = new AlertDialog.Builder(this);
		mSelCourseDialog.setTitle("Select a course");
		mLoginFailedDialog = new Dialog(this);
		mLoginFailedDialog.setCanceledOnTouchOutside(true);
		
		/** Done button */
		mDone = (Button)findViewById(R.id.doneButton);
		mDone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (login == false) {
					LoginData result = checkLogin();
					if (result.getError() != null) {
						showLoginFailed(result.getError());
					} else 
						startActivity(mMain);
				} else 
					startActivity(mMain);
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
	
	private LoginData checkLogin() {
		return new Connection(this).login();
	}
	
	/**
	 * Shows select courses dialog
	 */
	private void showSelect(String[] courses) {
		final String[] values = courses;
		mSelCourseDialog.setItems(courses, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	mEditor.putString("course", values[item]);
		    	mSelectCourse.setText(values[item]);
		    }
		});
		mSelCourseDialog.create().show();		
	}
	
	private void showLoginFailed(String error) {
		mLoginFailedDialog.setTitle(error);
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
			mEditor.putString("loginCode", code); 
			mEditor.commit();
			mCodeChecked.setChecked(true);
		}
	}
	
	/**
	 * Invalidates login data when the user 
	 * changes host or login code
	 */
	private void invalidateLogin() {
		mSelectCourse.setText("Click here to select course");
		mEditor.putString("course", null);
		mEditor.commit();
		login = false;
	}

}
