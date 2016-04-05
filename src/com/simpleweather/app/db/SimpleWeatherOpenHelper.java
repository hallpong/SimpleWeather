package com.simpleweather.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SimpleWeatherOpenHelper extends SQLiteOpenHelper {

	public static final String CREATE_TABLE_PROVINCE = "create table Province(id integer"
			+ " primary key autoincrement, province_name text, province_code text)";

	public static final String CREATE_TABLE_CITY = "create table City(id integer primary key"
			+ " autoincrement, city_name text, city_code text, province_id integer)";

	public static final String CREATE_TABLE_COUNTY = "create table County(id integer primary key"
			+ " autoincrement, county_name text, county_code text, city_id integer)";

	public SimpleWeatherOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_PROVINCE);
		db.execSQL(CREATE_TABLE_CITY);
		db.execSQL(CREATE_TABLE_COUNTY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		
	}

}
