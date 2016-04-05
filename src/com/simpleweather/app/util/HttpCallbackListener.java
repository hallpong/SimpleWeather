package com.simpleweather.app.util;

public interface HttpCallbackListener {

	void OnFinish(String response);
	
	void onError(Exception e);
}
