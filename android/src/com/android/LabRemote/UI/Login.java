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
import com.android.LabRemote.Server.Connection;

/** 
 * Application's login activity 
 * Checks authentification
 */
public class Login extends Activity {

	private static final String INTENT_SCAN = "com.google.zxing.client.android.SCAN";
	private static final String SCAN_MODE = "SCAN_MODE";
	private static final String QR_CODE_MODE = "QR_CODE_MODE";
	private static final String SCAN_RESULT = "SCAN_RESULT";
	private static final int REQCODE_SCAN = 0;
	private SharedPreferences mPreferences;
	private AlertDialog getCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login);

		/** Init get code dialog */
		getCode = new AlertDialog.Builder(this).create();
		getCode.setMessage("Invalid code. Do you want to load a valid one ?");
		DialogInterface.OnClickListener lis = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				/** Load scanner application */
				Intent intent = new Intent(INTENT_SCAN);
				intent.putExtra(SCAN_MODE, QR_CODE_MODE);
				startActivityForResult(intent, REQCODE_SCAN);
			}
		};
		getCode.setButton(AlertDialog.BUTTON_POSITIVE, "OK", lis);

		/** Check log in */
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String code = mPreferences.getString("loginCode", null);
		if (checkServerAuth(code)) {
			Intent mIntent = new Intent(this, Main.class);
			startActivity(mIntent);
		}
		else 
			getCode.show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQCODE_SCAN)
		{
			/** Check and store login code */
			String result = null;
			if (data != null)
				result = data.getStringExtra(SCAN_RESULT);
			if (checkServerAuth(result)) {
				storeCode(result);
				Intent mIntent = new Intent(this, Main.class);
				startActivity(mIntent);
			}
			else {
				getCode.show();
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void storeCode(String code) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString("loginCode", code); 
		editor.commit();
	}

	private boolean checkServerAuth(String code) {
		//uc if (code == null)
		//uc return false;
		Connection con = new Connection(this);
		return con.login("code", getApplicationContext()); //co
		//uc return con.login(code, getApplicationContext());

	}

}
