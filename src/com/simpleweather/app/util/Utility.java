package com.simpleweather.app.util;

import android.text.TextUtils;
import android.util.Log;

import com.simpleweather.app.db.SimpleWeatherDB;
import com.simpleweather.app.model.City;
import com.simpleweather.app.model.County;
import com.simpleweather.app.model.Province;

public class Utility {

	private Utility() {

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
