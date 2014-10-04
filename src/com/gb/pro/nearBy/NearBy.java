package com.gb.pro.nearBy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.gb.pro.db.LocationDataSource;
import com.gb.pro.nLocation.Loc_Tag;
import com.gb.pro.nLocation.POI;
import com.gb.wanderer.PlaceJSONParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class NearBy {
	Context main;
	LocationDataSource lds;
	POI poi;
	SharedPreferences sharedPreferences;
	GoogleMap gMap;
	
	double mLat, mLon;
	
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_NEARSEARCH = "/nearbysearch";
	private static final String OUT_JSON = "/json";

	private static final String API_KEY = "AIzaSyCMo1i-g0XsRBYg5to8sr7KRNESsdAqH4I";
	//private static final String API_KEY = "AIzaSyAn42sELbHn9N0mVU_mSGGwRkJgalgH9pU";
	//private static final String API_KEY = "AIzaSyD8YOWRXdD6nRtM17GkdYaIbCC6-P6TqUo";
	
	private static final String LOG_TAG = "Wanderer";
	static boolean d;
	
	Loc_Tag cur; 
	
	public NearBy(Context main, LocationDataSource lds, POI p, GoogleMap g) {
		this.main = main;
		this.lds = lds;
		this.poi = p;
		this.gMap = g;
	}
	
	public void query(double mLatitude, double mLongitude, boolean d) {
		this.mLat = mLatitude;
		this.mLon = mLongitude;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main.getApplicationContext());
		
		cur = new Loc_Tag(mLat, mLon);
		
		int index = poi.nearestPOIIndex(cur);
		if(index >= 0) {
			Loc_Tag t = poi.keys.get(index);
			if(t.isSame(cur, 10)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(main);
				builder.setMessage("Nearby: "+cur.lat+","+cur.lng + " \nDistance:(A) Less than 5m");
				// A = when place is outside and the person is inside/around 5 meters
				
				builder.setTitle("NearBy");
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
						drawMarker(new LatLng(cur.lat, cur.lng), BitmapDescriptorFactory.HUE_RED, "NearBy");
						
					}
				});
				builder.show();
				return;
			}
		}
		
		StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_NEARSEARCH + OUT_JSON);
		sb.append("?location=" + mLatitude + "," + mLongitude);
		sb.append("&rankby=distance");
		sb.append("&types=" + sharedPreferences.getString("place", "food"));
		sb.append("&key=" + API_KEY);
		
		// Get Nearest Location of Place
		NearPlacesTask nPlacesTask = new NearPlacesTask();
		this.d = d;
		
		// Invokes the "doInBackground()" method of the class
		// NearPlaceTask
		String[] url = new String[1];
		url[0] = sb.toString();
		nPlacesTask.execute(url);
	}
	
	/** A method to download JSON data from URL */
	/** A class, to download Google Places */
	private class NearPlacesTask extends AsyncTask<String, Void, String> {
		String er = null;

		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			try {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(urls[0]);

				HttpResponse execute = client.execute(httpGet);
				InputStream content = execute.getEntity().getContent();

				BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
				String s = "";
				while ((s = buffer.readLine()) != null) {
					response += s;
				}
				response = response.trim();
			} catch (Exception e) {
				response = null;
				e.printStackTrace();
				er = e.toString();
			}

			return response;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result) {
			ParserTask parserTask = new ParserTask();
			
			//Toast.makeText(getApplicationContext(), "Places Retriever. Data : " + result, Toast.LENGTH_SHORT).show();

			// Start parsing the Google places in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			if (result != null && result.compareTo("") != 0) {
				//Toast.makeText(main, result, Toast.LENGTH_LONG).show();
				parserTask.execute(result);

				//Toast.makeText(getApplicationContext(), "under parserTask.execute(result) which start parser doInBackground", Toast.LENGTH_SHORT).show();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(main);
				builder.setMessage(er);
				builder.setTitle("Error");
				builder.show();
			}
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

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends AsyncTask<String, Void, List<HashMap<String, String>>> {
		JSONObject jObject;
		
		// Invoked by execute() method of this object
		@Override
		protected List<HashMap<String, String>> doInBackground(String... jsonData) {
			List<HashMap<String, String>> places = null;

			try {
				PlaceJSONParser placeJsonParser = new PlaceJSONParser();

				jObject = new JSONObject(jsonData[0]);

				/** Getting the parsed data as a List construct */
				places = placeJsonParser.parse(jObject);
			} catch (Exception e) {
				Log.d("Exception", e.toString());
				AlertDialog.Builder builder = new AlertDialog.Builder(main);
				builder.setMessage(e.toString());
				builder.setTitle("Error");
				builder.show();
			}

			return places;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(List<HashMap<String, String>> list) {
			//Toast.makeText(context, "from post execute of parser task markers should start from this", duration).show();
			
			if(list.size() > 0) {
				display(list.get(0));
				/*int i = -1;
				int n = 0;
				int visit = -1;
				
				sharedPreferences = main.getSharedPreferences("visit", 0);
				
				SharedPreferences.Editor editor = sharedPreferences.edit();
				
				for(HashMap<String, String> p : list) {
					Loc_Tag t = new Loc_Tag(Double.parseDouble(p.get("lat")), Double.parseDouble(p.get("lng")));
					//t.gid = p.get("gid");
					//lds.addPOI(t);
					
					i++;
					if(sharedPreferences.getInt(p.get("id"), 0) > visit) {
						n = i;
						visit = sharedPreferences.getInt(p.get("id"), 0);
					}
				}
				
				editor.putInt(list.get(n).get("id"), visit + 1);
				editor.commit();
				
				if(NearBy.d == true) {
					display(list.get(n));
					d = false;
				}*/
			} else {
				display();
			}
		}
		
		public void display() {
			AlertDialog.Builder builder = new AlertDialog.Builder(main);
			builder.setTitle("NearBy");
			builder.setMessage("No Suggestion Available");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show();
		}
		
		public void display(HashMap<String, String> hmPlace) {
			
			// Getting latitude of the place
			double lat = Double.parseDouble(hmPlace.get("lat"));
			
			// Getting longitude of the place
			double lng = Double.parseDouble(hmPlace.get("lng"));
			
			cur = new Loc_Tag(lat, lng);
			// Getting name
			String name = hmPlace.get("place_name");
			cur.name = name;
			float result[] = new float[5];
			Location.distanceBetween(mLat, mLon, lat, lng, result);
			float distance = -1;
			if(result.length > 0)
				distance = result[0]; 
			// Getting vicinity
			String vicinity = hmPlace.get("vicinity");
			
			AlertDialog.Builder builder = new AlertDialog.Builder(main);
			// A = when place is outside and the person is inside/around 5 meters
			if(distance < 5)
				builder.setMessage("Nearby: "+name+" "+vicinity + " \nDistance:(A) Less than 5m");
			// B = when place is outside and the person is inside/around 10 meters
			else if (distance < 10)
				builder.setMessage("Nearby: "+name+" "+vicinity + " \nDistance:(B) Less than 10m");
			// C = when place is outside and the person is inside/around 20 meters
			else if (distance < 20)
				builder.setMessage("Nearby: "+name+" "+vicinity + " \nDistance:(C) Less than 20m");
			else
				builder.setMessage("Nearby: "+name+" "+vicinity + " \nDistance:(D) Greater than 20m");
			
			builder.setTitle("NearBy");
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
					drawMarker(new LatLng(cur.lat, cur.lng), BitmapDescriptorFactory.HUE_RED, cur.name);
				}
			});
			builder.show();
			
			Log.e(LOG_TAG, "Search: "+name+" "+vicinity+ " ");
		}
	}
}