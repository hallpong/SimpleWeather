package com.simpleweather.app.activity;

import com.simpleweather.app.R;
import com.simpleweather.app.util.HttpCallbackListener;
import com.simpleweather.app.util.HttpUtil;
import com.simpleweather.app.util.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity {

	private LinearLayout llWeatherInfo;
	private TextView tvArea;
	private TextView tvPublishDate;
	private TextView tvCurrentDate;
	private TextView tvLowTemp;
	private TextView tvHighTemp;
	private TextView tvWeatherDesp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);

		llWeatherInfo = (LinearLayout) findViewById(R.id.ll_weatherInfo);
		tvArea = (TextView) findViewById(R.id.tv_area);
		tvPublishDate = (TextView) findViewById(R.id.tv_publishTime);
		tvCurrentDate = (TextView) findViewById(R.id.current_date);
		tvLowTemp = (TextView) findViewById(R.id.tv_lowTemp);
		tvHighTemp = (TextView) findViewById(R.id.tv_highTemp);
		tvWeatherDesp = (TextView) findViewById(R.id.tv_weatherDesp);

		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			tvPublishDate.setText("同步中...");
			llWeatherInfo.setVisibility(View.INVISIBLE);
			tvArea.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			showWeather();
		}
	}

	/**
	 * 没有县级代号就直接显示本地天气
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		tvArea.setText(prefs.getString("city_name", ""));
		tvPublishDate
				.setText("今天" + prefs.getString("publish_time", "") + "发布");
		tvCurrentDate.setText(prefs.getString("current_date", ""));
		tvLowTemp.setText(prefs.getString("temp1", ""));
		tvHighTemp.setText(prefs.getString("temp2", ""));
		tvWeatherDesp.setText(prefs.getString("weather_desp", ""));
		llWeatherInfo.setVisibility(View.VISIBLE);
		tvArea.setVisibility(View.VISIBLE);
	}

	/**
	 * 根据County的代号查询天气
	 * 
	 * @param countyCode
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	/**
	 * 从服务器获取天气代号或天气信息
	 * 
	 * @param address
	 * @param type
	 */
	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						tvPublishDate.setText("同步失败...");

					}
				});
			}

			@Override
			public void OnFinish(String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String array[] = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							showWeather();
						}
					});

				}

			}
		});
	}

	/**
	 * 根据天气代号获取天气信息；
	 * 
	 * @param weatherCode
	 */
	protected void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFromServer(address, "weatherCode");

	}
}
