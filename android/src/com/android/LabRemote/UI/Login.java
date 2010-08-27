/** 
 * Application login activity 
 * Checks authentification
 * 
 * Version: 1.0
 * 
 * Copyright (c) 2010 LabRemote team
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
import android.widget.Toast;

import com.android.LabRemote.R;

public class Login extends Activity {
	
    private static final String INTENT_SCAN = "com.google.zxing.client.android.SCAN";
    private static final String SCAN_MODE = "SCAN_MODE";
    private static final String QR_CODE_MODE = "QR_CODE_MODE";
    private static final String SCAN_RESULT = "SCAN_RESULT";
    private static final int REQCODE_SCAN = 0;
    private AlertDialog getCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login);
		
		/* Init get code dialog */
		getCode = new AlertDialog.Builder(this).create();
		getCode.setMessage("Invalid code. Do you want to load a valid one ?");
		DialogInterface.OnClickListener lis = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// load scanner application
				Intent intent = new Intent(INTENT_SCAN);
		        intent.putExtra(SCAN_MODE, QR_CODE_MODE);
		        startActivityForResult(intent, REQCODE_SCAN);
			}
		};
		getCode.setButton(AlertDialog.BUTTON_POSITIVE, "OK", lis);

		/* Check log in */
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String code = preferences.getString("loginCode", null);
		if (checkServerAuth(code)) {
			//TODO: tot aici primesc si primele informatii de la server
			Intent mIntent = new Intent(this, Main.class);
			startActivity(mIntent);
		}
		else {
			getCode.show();
		}
				
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQCODE_SCAN)
        {
        	// check and store login code
        	String result = null;
        	if (data != null)
        		result = data.getStringExtra(SCAN_RESULT);
        	if (checkServerAuth(result)) {
    			//TODO: tot aici primesc si primele informatii de la server
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
		Toast.makeText(this, code, 1).show();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("loginCode", code); 
		editor.commit();
	}
	
	private boolean checkServerAuth(String code) {
		if (code == null)
			return false;
		//TODO: send code to server + check response
		return true;		
	}
	
	
}
