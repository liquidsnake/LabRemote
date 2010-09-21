package com.android.LabRemote.Server;

public class ServerResponse {
	private Object mResult; //if null print error
	private String mError;
	
	public ServerResponse(Object result, String error) {
		this.mResult = result;
		this.mError = error;
	}
	
	public Object getRespone() {
		return mResult;
	}
	
	public String getError() {
		return mError;
	}
}
