/**
 * Login.java
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

import com.android.LabRemote.R;
import com.android.LabRemote.Data.LoginData;
import com.android.LabRemote.Server.Connection;

/** 
 * Application's login activity 
 * Checks authentification
 */
public class Login extends Activity {

	private SharedPreferences mPreferences;
	private AlertDialog mInvalidLogin;
	private Intent mIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login);
		
		/** Initialize invalid login dialog */
		mInvalidLogin = new AlertDialog.Builder(this).create();
		DialogInterface.OnClickListener lis = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mIntent = new Intent(getApplicationContext(), Settings.class);
				startActivity(mIntent);
			}
		};
		mInvalidLogin.setButton(AlertDialog.BUTTON_POSITIVE, "OK", lis);
		
		/** Check host and code */
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String code = mPreferences.getString("loginCode", null);
		String host = mPreferences.getString("host", null);

		if (code == null || host == null) { /** Start Settings activity */
			Intent mIntent = new Intent(this, Settings.class);
			startActivity(mIntent);
		} else { /** Check login */
			LoginData res = new Connection(this).login();
			if (res.getError() == null) { 
				mIntent = new Intent(this, Main.class);
				startActivity(mIntent);
				finish();
			} else { 
				mInvalidLogin.setMessage(res.getError());
				mInvalidLogin.show();		
			}
		}
	}
}
