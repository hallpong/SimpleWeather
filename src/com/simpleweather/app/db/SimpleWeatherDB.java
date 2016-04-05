package com.simpleweather.app.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.simpleweather.app.model.City;
import com.simpleweather.app.model.County;
import com.simpleweather.app.model.Province;

public class SimpleWeatherDB {

	public static final String DB_NAME = "simple_weather.db";

	public static final int VERSION = 1;

	private static SimpleWeatherDB simpleWeatherDB;

	private SQLiteDatabase db;

	private SimpleWeatherDB(Context context) {
		SimpleWeatherOpenHelper helper = new SimpleWeatherOpenHelper(context,
				DB_NAME, null, VERSION);
		db = helper.getWritableDatabase();
	}

	/**
	 * 获取SimpleWeatherDB的实例
	 */
	public static SimpleWeatherDB getInstance(Context context) {
		if (simpleWeatherDB == null)
			simpleWeatherDB = new SimpleWeatherDB(context);
		return simpleWeatherDB;
	}

	/**
	 * 将Province实例存储到数据库
	 */
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}

	/**
	 * 将City实例存储到数据库
	 */
	public void saveCity(City city) {

		if (city != null) {

			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());

			db.insert("City", null, values);
		}
	}

	/**
	 * 将County实例存储到数据库
	 */
	public void saveCounty(County county) {

		if (county != null) {

			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());

			db.insert("County", null, values);
		}
	}

	/**
	 * 从数据库读取所有的省份信息；
	 */
	public List<Province> loadProvinces() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db
				.query("Province", null, null, null, null, null, null);

		if (cursor != null) {
			while (cursor.moveToNext()) {
				String provinceName = cursor.getString(cursor
						.getColumnIndex("province_name"));
				String provinceCode = cursor.getString(cursor
						.getColumnIndex("province_code"));
				int id = cursor.getInt(cursor.getColumnIndex("id"));
				Province province = new Province(id, provinceName, provinceCode);
				list.add(province);
			}
		}

		return list;
	}

	/**
	 * 从数据库中读取某省下的所有城市信息；
	 */
	public List<City> loadCities(int provinceId) {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id=?",
				new String[] { String.valueOf(provinceId) }, null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String cityName = cursor.getString(cursor
						.getColumnIndex("city_name"));
				String cityCode = cursor.getString(cursor
						.getColumnIndex("city_code"));
				int id = cursor.getInt(cursor.getColumnIndex("id"));
				City city = new City(id, cityName, cityCode, provinceId);
				list.add(city);
			}
		}
		return list;
	}

	/**
	 * 从数据库读取某市下的所有县的信息
	 */
	public List<County> loadCounties(int cityId) {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id=?",
				new String[] { String.valueOf(cityId) }, null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String countyName = cursor.getString(cursor
						.getColumnIndex("county_name"));
				String countyCode = cursor.getString(cursor
						.getColumnIndex("county_code"));
				int id = cursor.getInt(cursor.getColumnIndex("id"));
				County county = new County(id, countyName, countyCode, cityId);
				list.add(county);
			}
		}
		return list;
	}
}