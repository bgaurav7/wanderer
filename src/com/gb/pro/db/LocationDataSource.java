package com.gb.pro.db;

import java.util.ArrayList;
import java.util.List;

import com.gb.pro.nLocation.Loc_Tag;
import com.gb.pro.nLocation.Tag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocationDataSource {

	public static final String LOGTAG = "WANDERER";
	
	SQLiteOpenHelper dbhelper;
	SQLiteDatabase database;
	
	public static final String[] allColTag = {
		DBOpenHelper.TAG_ID,
		DBOpenHelper.LAT_T,
		DBOpenHelper.LNG_T,
		DBOpenHelper.NAME
	};
	
	public static final String[] allColLoc = {
		DBOpenHelper.LOC_RAW_ID,
		DBOpenHelper.LAT,
		DBOpenHelper.LNG,
		DBOpenHelper.TIME
		//LocationDBOpenHelper.ETIME,
		//LocationDBOpenHelper.COUNT
	};
	
	public LocationDataSource(Context context) {
		dbhelper = new DBOpenHelper(context);
	}
	
	public void open() {
		Log.i(LOGTAG, "Database opened");
		database = dbhelper.getWritableDatabase();
	}

	public void close() {
		Log.i(LOGTAG, "Database closed");		
		dbhelper.close();
	}
	
	/*public boolean checkContainPOI(Loc_Tag t) {
		Cursor c = database.query(DBOpenHelper.TABLE_POI, new String[] {}, DBOpenHelper.GID_P + " = ?", new String[] {t.gid},
				null, null, null, null);
		return c.getCount() > 0;
	}*/
	
	public void addPOI(Loc_Tag t) {
		//if(!checkContainPOI(t)) {
			ContentValues values = new ContentValues();
			values.put(DBOpenHelper.LAT_P, t.lat);
			values.put(DBOpenHelper.LNG_P, t.lng);
			long insertid = database.insert(DBOpenHelper.TABLE_POI, null, values);
		//}
	}
	
	public void removeAllPOI() {
		database.delete(DBOpenHelper.TABLE_POI, null, null);
	}
	
	public ArrayList<Loc_Tag> getAllPOI() {
		ArrayList<Loc_Tag> l = new ArrayList<Loc_Tag>();
		
		Cursor c = database.rawQuery("SELECT * FROM " + DBOpenHelper.TABLE_POI , null);
		
		if (c.moveToFirst()) {
		     do {
		    	 Loc_Tag t = new Loc_Tag(c.getDouble(c.getColumnIndex(DBOpenHelper.LAT_P)),
							c.getDouble(c.getColumnIndex(DBOpenHelper.LNG_P)));
		    	 //t.gid = c.getString(c.getColumnIndex(DBOpenHelper.GID_P));
							
		    	 l.add(t); 
		     } while (c.moveToNext());
		}
	    c.close();
	    
		return l;
	}
	
	//Tag Table
	public void addTag(Tag t) {
		ContentValues values = new ContentValues();
		values.put(DBOpenHelper.LAT_T, t.lat);
		values.put(DBOpenHelper.LNG_T, t.lng);
		values.put(DBOpenHelper.NAME, t.name);
		long insertid = database.insert(DBOpenHelper.TABLE_TAG, null, values);
		Log.i(LOGTAG, "Tag Entry: " + t.name + " " + t.lat + "," + t.lng);
	}
	
	public List<Tag> getAllTag() {
		List<Tag> l = new ArrayList<Tag>();
		
		Cursor c = database.rawQuery("SELECT * FROM " + DBOpenHelper.TABLE_TAG , null);
		
		if (c.moveToFirst()) {
		     do {
		    	 l.add(new Tag(c.getDouble(c.getColumnIndex(DBOpenHelper.LAT_T)),
							c.getDouble(c.getColumnIndex(DBOpenHelper.LNG_T)),
							c.getString(c.getColumnIndex(DBOpenHelper.NAME))
							)); 

		     } while (c.moveToNext());
		}
	    c.close();
	    
		return l;
	}
	
	//Location Table
	private void createEntryLocation(Loc_Tag lg) {
		ContentValues values = new ContentValues();
		values.put(DBOpenHelper.LAT, lg.lat);
		values.put(DBOpenHelper.LNG, lg.lng);
		//values.put(DBOpenHelper.COUNT, 1);
		long insertid = database.insert(DBOpenHelper.TABLE_LOC_RAW , null, values);
		Log.i(LOGTAG, "Location Entry: " + lg.lat + "," + lg.lng);
	}
	
	/*private void updateEntryLocation(long id, int count) {
		count++;
		//Cursor c = sqliteDB.rawQuery("UPDATE "+ MyConstants.TABLE_NAME + " SET "+ MyConstants.ISFAV + " = "+fav+ " WHERE " + MyConstants.WORD_NAME + " = \""+word_name+"\"", null);
		String query = "UPDATE " + DBOpenHelper.TABLE_LOC_ + " SET " + DBOpenHelper.COUNT + " = " + count 
				+ " WHERE " + DBOpenHelper.LOC_ID + " = \"" + DBOpenHelper.LOC_ID + "\"";
		
		database.rawQuery(query, null);
	}*/
	
	private void createEntryRAW(Loc_Tag lg) {
		ContentValues values = new ContentValues();
		values.put(DBOpenHelper.LAT, lg.lat);
		values.put(DBOpenHelper.LNG, lg.lng);
		long insertid = database.insert(DBOpenHelper.TABLE_LOC_RAW, null, values);
		Log.i(LOGTAG, "Location Entry: " + lg.lat + "," + lg.lng);
		
	}
	
	public void addLoc(Loc_Tag lg) {
		Loc_Tag last = getLastEntry();
		
		createEntryRAW(lg);
		/*if(last == null || !lg.isSame(last, 1)) {
			createEntryLocation(lg);
		} else {
			updateEntryLocation(last.id, last.count);
		}*/
	}

	public Loc_Tag getLastEntry() {
		String query = "SELECT * from " + DBOpenHelper.TABLE_LOC_RAW + 
				" order by " + DBOpenHelper.LOC_RAW_ID + " DESC limit 1";
		Cursor c = database.rawQuery(query, null);
		
		if (c != null && c.moveToFirst()) {
		    Loc_Tag t = new Loc_Tag(c.getDouble(c.getColumnIndex(DBOpenHelper.LAT)), c.getDouble(c.getColumnIndex(DBOpenHelper.LNG)));
		    t.id = c.getLong(c.getColumnIndex(DBOpenHelper.LOC_RAW_ID));
		    t.time = c.getString(c.getColumnIndex(DBOpenHelper.TIME));
		    //t.count = c.getInt(c.getColumnIndex(DBOpenHelper.COUNT));
		    return t;
		}
		return null;
	}
	
	public int getCount() {
		Cursor c = database.rawQuery("SELECT * FROM " + DBOpenHelper.TABLE_LOC_RAW , null);
		return c.getCount();
	}
	
	public ArrayList<Loc_Tag> getAllLoc_Tag() {
		ArrayList<Loc_Tag> l = new ArrayList<Loc_Tag>();
		
		Cursor c = database.rawQuery("SELECT * FROM " + DBOpenHelper.TABLE_LOC_RAW , null);
		
		if (c.moveToFirst()) {
		     do {
		    	 Loc_Tag t = new Loc_Tag(c.getDouble(c.getColumnIndex(DBOpenHelper.LAT)),
							c.getDouble(c.getColumnIndex(DBOpenHelper.LNG)));
		    	 t.index = c.getInt(c.getColumnIndex(DBOpenHelper.LOC_RAW_ID));
		    	 //t.count = c.getInt(c.getColumnIndex(DBOpenHelper.COUNT));
		    	 l.add(t); 

		     } while (c.moveToNext());
		}
	    c.close();
	    
		return l;
	}
}
