package com.simpleweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.simpleweather.app.db.SimpleWeatherDB;
import com.simpleweather.app.model.City;
import com.simpleweather.app.model.County;
import com.simpleweather.app.model.Province;

public class Utility {

	private Utility() {

	}

	/**
	 * 解析服务器返回的天气信息，并存储到本地；
	 * 
	 * @param context
	 * @param response
	 *            服务器返回的数据；
	 */
	public static void handleWeatherResponse(Context context, String response) {
		try {
			JSONObject json = new JSONObject(response);
			JSONObject weatherInfo = json.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,
					weatherDesp, publishTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 将服务器返回的所有信息存储到SharedPreferences中
	 */
	private static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日",
				Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", dateFormat.format(new Date()));
		editor.putBoolean("city_selected", true);
		editor.commit();
	}

	/**
	 * 解析和处理服务器返回的省级数据；
	 */
	public synchronized static boolean handleProvincesResponse(
			SimpleWeatherDB simpleWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceName(array[1]);
					province.setProvinceCode(array[0]);
					simpleWeatherDB.saveProvince(province);
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * 解析和处理服务器返回的City数据
	 */
	public synchronized static boolean handleCityResponse(
			SimpleWeatherDB simpleWeatherDB, String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String p : allCities) {
					String[] array = p.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					simpleWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 解析和处理服务器返回的County数据
	 */
	public synchronized static boolean handleCountyResponse(
			SimpleWeatherDB simpleWeatherDB, String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String p : allCounties) {
					String[] array = p.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					simpleWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}
