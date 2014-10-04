package com.gb.pro.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {

	private static final String LOGTAG = "WANDERER";

	private static final String DATABASE_NAME = "location";
	private static final int DATABASE_VERSION = 1;
	
	public static final String LOC_RAW_ID = "id";
	//public static final String LOC_ID = "id";
	public static final String LAT = "latitude";
	public static final String LNG = "longitude";
	//public static final String GID = "gid";
	public static final String TIME = "date";
	//public static final String ETIME = "etime";
	//public static final String COUNT = "cnt";
	
	public static final String TAG_ID = "id";
	public static final String LAT_T = "latitude";
	public static final String LNG_T = "longitude";
	public static final String NAME = "name";
	
	public static final String POI_ID = "id";
	public static final String LAT_P = "latitude";
	public static final String LNG_P = "longitude";
	//public static final String GID_P = "gid";
	
	//public static final String TABLE_LOC = "nlocation";
	public static final String TABLE_LOC_RAW = "nlocation_raw";
	public static final String TABLE_TAG = "tag";
	public static final String TABLE_POI = "poi";
	
	private static final String TABLE_CREATE_LOC_RAW = 
			"CREATE TABLE " + TABLE_LOC_RAW + " (" +
			LOC_RAW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			LAT + " REAL, " +
			LNG + " REAL, " +
			TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP " +
			")";
	
	/*private static final String TABLE_CREATE_LOC = 
			"CREATE TABLE " + TABLE_LOC + " (" +
			LOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			LAT + " REAL, " +
			LNG + " REAL, " +
			COUNT + "REAL, " +
			TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP " +
			")";*/
	
	private static final String TABLE_CREATE_TAG = 
			"CREATE TABLE " + TABLE_TAG + " (" +
			TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			LAT_T + " REAL, " +
			LNG_T + " REAL, " +
			NAME + " TEXT " +
			")";
	
	private static final String TABLE_CREATE_POI = 
			"CREATE TABLE " + TABLE_POI + " (" +
			POI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			LAT_P + " REAL, " +
			LNG_P + " REAL " +
			//GID_P + " TEXT " +
			")";
	
	public DBOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//db.execSQL(TABLE_CREATE_LOC);
		db.execSQL(TABLE_CREATE_LOC_RAW);
		db.execSQL(TABLE_CREATE_TAG);
		db.execSQL(TABLE_CREATE_POI);
		Log.i(LOGTAG, "Table has been created");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOC);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOC_RAW);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_POI);
		onCreate(db);
	}

}
