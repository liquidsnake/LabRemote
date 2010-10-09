/**
 * Login.java
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
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.android.LabRemote.R;
import com.android.LabRemote.Server.Connection;
import com.android.LabRemote.Server.ServerResponse;

/** 
 * Application's login activity <br>
 * Checks authentification
 */
public class Login extends Activity {

	/** Reads login code and host from the application's private data */
	private SharedPreferences mPreferences;
	/** Displayed when login failed */
	private AlertDialog mInvalidLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		/** Initialize invalid login dialog */
		mInvalidLogin = new AlertDialog.Builder(this).create();
		DialogInterface.OnClickListener lis = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent mIntent = new Intent(getApplicationContext(), Settings.class);
				startActivity(mIntent);
			}
		};
		mInvalidLogin.setButton(AlertDialog.BUTTON_POSITIVE, "OK", lis);

		/** Check host and code */
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor ed = mPreferences.edit();
		ed.putString("loginCode", "code");
		ed.commit();
		String code = mPreferences.getString("loginCode", null);
		String host = mPreferences.getString("host", null);

		if (code == null || host == null) { /** Start Settings activity */
			Intent mIntent = new Intent(this, Settings.class);
			startActivity(mIntent);
			finish();
		} else { /** Check login */
			ServerResponse res = new Connection(this).login();
			if (res.getError() == null) { 
				Intent mIntent = new Intent(this, Main.class);
				startActivity(mIntent);
				finish();
			} else { 
				mInvalidLogin.setMessage(res.getError());
				mInvalidLogin.show();		
			}
		}
	}
}
