package com.simpleweather.app.service;

import com.simpleweather.app.receiver.AutoUpdateReceiver;
import com.simpleweather.app.util.HttpCallbackListener;
import com.simpleweather.app.util.HttpUtil;
import com.simpleweather.app.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				updateWeather();

			}
		}).start();
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHoure = 8 * 60 * 60 * 1000;
		long triggerTime = SystemClock.elapsedRealtime() + anHoure;
		Intent i = new Intent(this, AutoUpdateReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * update weather information automatically
	 */
	protected void updateWeather() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String weatherCode = prefs.getString("weather_code", "");
		if (!TextUtils.isEmpty(weatherCode)) {
			String address = "http://www.weather.com.cn/data/cityinfo/"
					+ weatherCode + ".html";
			HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

				@Override
				public void onError(Exception e) {
					e.printStackTrace();
				}

				@Override
				public void OnFinish(String response) {
					Utility.handleWeatherResponse(AutoUpdateService.this,
							response);
				}
			});
		}
	}

}
