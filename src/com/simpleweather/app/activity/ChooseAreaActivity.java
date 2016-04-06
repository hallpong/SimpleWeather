package com.simpleweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.simpleweather.app.R;
import com.simpleweather.app.db.SimpleWeatherDB;
import com.simpleweather.app.model.City;
import com.simpleweather.app.model.County;
import com.simpleweather.app.model.Province;
import com.simpleweather.app.util.HttpCallbackListener;
import com.simpleweather.app.util.HttpUtil;
import com.simpleweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {

	private static final int LEVEL_PROVINCE = 0;
	private static final int LEVEL_CITY = 1;
	private static final int LEVEL_COUNTY = 2;

	private ListView listView;
	private TextView tvTitle;
	private List<String> dataList = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private SimpleWeatherDB simpleWeatherDB;

	private int currentLevel;

	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;

	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;

	private ProgressDialog progressDialog;
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);

		isShowWeather();

		listView = (ListView) findViewById(R.id.list_view);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		simpleWeatherDB = SimpleWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					selectedCounty = countyList.get(position);
					String countyCode = selectedCounty.getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,
							WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();
	}

	/**
	 * 是否跳转到天气展示界面
	 */
	private void isShowWeather() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		isFromWeatherActivity = getIntent().getBooleanExtra(
				"from_weather_activity", false);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
	}

	/**
	 * 获取省份列表信息
	 */
	protected void queryProvinces() {
		provinceList = simpleWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			tvTitle.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	/**
	 * 获取城市列表；
	 */
	protected void queryCities() {
		cityList = simpleWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			tvTitle.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	/**
	 * 获取County列表
	 */
	protected void queryCounties() {
		countyList = simpleWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			tvTitle.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}

	/**
	 * 根据传入的code和typ从服务器获取省市县数据；
	 * 
	 * @param code
	 * @param type
	 */
	private void queryFromServer(String code, final String type) {
		String address;
		if (TextUtils.isEmpty(code)) {
			// 要获取的是省份列表
			address = "http://www.weather.com.cn/data/list3/city.xml";
		} else {
			// 获取的是市县列表
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onError(Exception e) {
				// 无法从服务器获取数据
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败",
								Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void OnFinish(String response) {
				// 从服务器获取到数据；
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(simpleWeatherDB,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCityResponse(simpleWeatherDB,
							response, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountyResponse(simpleWeatherDB,
							response, selectedCity.getId());
				}
				// 如果返回的结果不为null
				if (result) {
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}
		});
	}

	/**
	 * 表示正在从服务器获取数据；
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	/**
	 * 从服务器获取数据结束，关闭Dialog
	 */
	protected void closeProgressDialog() {
		if (progressDialog != null)
			progressDialog.dismiss();
	}

	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
