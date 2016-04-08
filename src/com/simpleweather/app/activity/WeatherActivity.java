package com.simpleweather.app.activity;

import com.simpleweather.app.R;
import com.simpleweather.app.service.AutoUpdateService;
import com.simpleweather.app.util.HttpCallbackListener;
import com.simpleweather.app.util.HttpUtil;
import com.simpleweather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener {

	private LinearLayout llWeatherInfo;
	private TextView tvArea;
	private TextView tvPublishDate;
	private TextView tvCurrentDate;
	private TextView tvLowTemp;
	private TextView tvHighTemp;
	private TextView tvWeatherDesp;
	private Button btHome;
	private Button btRefresh;
	private SharedPreferences prefs;
	private RelativeLayout rlContent;

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
		btHome = (Button) findViewById(R.id.bt_home);
		btRefresh = (Button) findViewById(R.id.bt_refresh);
		rlContent = (RelativeLayout) findViewById(R.id.rl_content);

		btHome.setOnClickListener(this);
		btRefresh.setOnClickListener(this);
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			tvPublishDate.setText("ͬ����...");
			llWeatherInfo.setVisibility(View.INVISIBLE);
			tvArea.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			showWeather();
		}
	}

	/**
	 * û���ؼ����ž�ֱ����ʾ��������
	 */
	private void showWeather() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		tvArea.setText(prefs.getString("city_name", ""));
		tvPublishDate
				.setText("����" + prefs.getString("publish_time", "") + "����");
		tvCurrentDate.setText(prefs.getString("current_date", ""));
		tvLowTemp.setText(prefs.getString("temp1", ""));
		tvHighTemp.setText(prefs.getString("temp2", ""));
		String weatherDesp = prefs.getString("weather_desp", "");
		tvWeatherDesp.setText(weatherDesp);
		if (weatherDesp.contains("ѩ")) {
			rlContent.setBackgroundResource(R.drawable.bg_snow);
		} else if (weatherDesp.contains("��")) {
			rlContent.setBackgroundResource(R.drawable.bg_rain);
		} else if (weatherDesp.contains("��")) {
			rlContent.setBackgroundResource(R.drawable.bg_fine_day);
		}else if(weatherDesp.contains("����")){
			rlContent.setBackgroundResource(R.drawable.bg_cloudy_day);
		}else if(weatherDesp.contains("��")){
			rlContent.setBackgroundResource(R.drawable.bg_fog);
		}
		llWeatherInfo.setVisibility(View.VISIBLE);
		tvArea.setVisibility(View.VISIBLE);

		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}

	/**
	 * ����County�Ĵ��Ų�ѯ����
	 * 
	 * @param countyCode
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	/**
	 * �ӷ�������ȡ�������Ż�������Ϣ
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
						tvPublishDate.setText("ͬ��ʧ��...");

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
	 * �����������Ż�ȡ������Ϣ��
	 * 
	 * @param weatherCode
	 */
	protected void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFromServer(address, "weatherCode");

	}

	/**
	 * ��������¼�
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_home:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.bt_refresh:
			tvPublishDate.setText("ͬ����...");
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode))
				queryWeatherInfo(weatherCode);
			break;
		default:
			break;
		}
	}
}
