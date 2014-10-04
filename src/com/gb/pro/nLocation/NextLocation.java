package com.gb.pro.nLocation;

import java.text.Format;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.text.format.Time;

public class NextLocation {
	
	Context main;
	private Cursor mCursor = null;
	private static final String[] COLS = new String[] { CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART};
	
	GoogleMap gMap;
	Suggester st;
	Format df, tf;
	Loc_Tag t;
	
	public NextLocation(Context m, Suggester st, GoogleMap g) {
		main = m;
		
		this.st =  st;
		this.gMap = g;
		df = DateFormat.getDateFormat(main);
		tf = DateFormat.getTimeFormat(main);
	}
	
	public void nextLoc(double lat, double lng) {
		if(!calendarEvent()) {
			t = st.sugg(lat, lng);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(main);
			if(t != null) {
				builder.setMessage("" + t.lat + "," + t.lng);
			} else {
				builder.setMessage("No Suggestion");
			}
			builder.setTitle("Next Location");
			builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setPositiveButton("View On Map", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					gMap.clear();
					drawMarker(new LatLng(t.lat, t.lng), BitmapDescriptorFactory.HUE_RED, "Next Preferred Location");
					
				}
			});
			
			builder.show();
		}
	}
	
	private void drawMarker(LatLng point, float color, String name) {
		// Creating an instance of MarkerOptions
		MarkerOptions markerOptions = new MarkerOptions();

		// Setting latitude and longitude for the marker
		markerOptions.position(point);

		markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));

		// Adding InfoWindow title
		markerOptions.title(name);

		// Adding InfoWindow contents
		markerOptions.snippet(Double.toString(point.latitude) + ","
				+ Double.toString(point.longitude));

		// Adding marker on the Google Map
		gMap.addMarker(markerOptions);
		gMap.moveCamera(CameraUpdateFactory.newLatLng(point));
	}
	
	public boolean calendarEvent() {
		String selection = "((" + CalendarContract.Events.DTSTART + " >= ?) AND (" + CalendarContract.Events.DTSTART + " <= ?))";
        Time t = new Time();
        t.setToNow();
        String dtStart = Long.toString(t.toMillis(false));
        t.minute = t.minute + 10;
        String dtEnd = Long.toString(t.toMillis(false));
        String[] selectionArgs = new String[] { dtStart, dtEnd };
        
        mCursor = main.getContentResolver().query(CalendarContract.Events.CONTENT_URI, COLS, selection, selectionArgs, null);
        mCursor.moveToFirst();
        
        if(mCursor.getCount() > 0) {
        	AlertDialog.Builder builder = new AlertDialog.Builder(main);
        	
            try {
            	String title = mCursor.getString(0);
            	long start = mCursor.getLong(1);
            	builder.setMessage(title+" on "+ df.format(start)+" at "+ tf.format(start));
            	builder.setTitle("Next Location");
    			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    				
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.dismiss();
    				}
    			});
    			builder.show();
            } catch (Exception e) {
            	//ignore
            }
        	return true;
        }
		return false;
	}
}

