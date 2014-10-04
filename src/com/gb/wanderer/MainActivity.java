package com.gb.wanderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.Toast;

import com.gb.pro.R;
import com.gb.pro.db.LocationDataSource;
import com.gb.pro.nLocation.DataAnalyser;
import com.gb.pro.nLocation.Loc_Tag;
import com.gb.pro.nLocation.NextLocation;
import com.gb.pro.nLocation.POI;
import com.gb.pro.nLocation.Suggester;
import com.gb.pro.nLocation.Tag;
import com.gb.pro.nearBy.NearBy;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements LocationListener {
	
	GoogleMap googleMap;
	PendingIntent pendingIntent;
	SharedPreferences sharedPreferences;
	
	int locationCount = 0;

	// Location Variables
	Location loc;
	LocationManager locationManager;

	double mLatitude;
	double mLongitude;

	//GPS
	public boolean isGPSEnabled = false;
	// flag for network status
	boolean isNetworkEnabled = false;
	// flag for GPS status
	boolean canGetLocation = false;
	Location location; // location
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute

	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_TEXTSEARCH = "/textsearch";
	private static final String TYPE_AUTOCOMPLETE = "/queryautocomplete";
    private static final String OUT_JSON = "/json";

	private static final String API_KEY = "AIzaSyCMo1i-g0XsRBYg5to8sr7KRNESsdAqH4I";
	//private static final String API_KEY = "AIzaSyAn42sELbHn9N0mVU_mSGGwRkJgalgH9pU";
	//private static final String API_KEY = "AIzaSyD8YOWRXdD6nRtM17GkdYaIbCC6-P6TqUo";
	
	private static final String LOG_TAG = "WANDERER";

	
	//Drawer
	private DrawerLayout mDrawerLayout;
    //private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    
    
	AutoCompleteTextView autoCompView;
	NearBy nb;
	NextLocation nl;
	
	Button nSearch, nLocation;
	
	AlertDialog.Builder builder;
	LatLng tag_tmp;
	ProgressDialog pDialog;
	
	LocationDataSource lds;
	POI poi;
	DataAnalyser da;
	Suggester st;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Getting Google Play availability status
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

		// Showing status
		if (status != ConnectionResult.SUCCESS) { // Google Play Services are
													// not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
			dialog.show();
		} else { // Google Play Services are available
			initializeDrawer();
			
			Intent intent = new Intent(this, LocationService.class);
			startService(intent);
			
			autoCompView = (AutoCompleteTextView) findViewById(R.id.autoComplete);
	        autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
	        
	        initializeGMap();
	        initializeSearch();

			initializeDB();
			
	        new LoadData().execute();
			
		}
	}
	
	
	
	
	
	
	private void initializeDrawer() {
		// enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);		
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
    
    
    
    

	private void loadData() {
		/*Loc_Tag t;
		t = new Loc_Tag(25, 81);
		lds.addLoc(t);
		t.gid = "BH4";
		lds.addPOI(t);
		if(!lds.checkContainPOI(t))
			Toast.makeText(this, "Problem1", Toast.LENGTH_LONG).show();
		
		t = new Loc_Tag(24, 81);
		lds.addLoc(t);
		t.gid = "BH2";
		lds.addPOI(t);
		if(!lds.checkContainPOI(t))
			Toast.makeText(this, "Problem2", Toast.LENGTH_LONG).show();
		*/
		/*lds.addLoc(new Loc_Tag(25.426998, 81.771787));
		lds.addLoc(new Loc_Tag(25.427313, 81.770934));
		lds.addLoc(new Loc_Tag(25.428127, 81.771218));
		lds.addLoc(new Loc_Tag(25.426848, 81.771733));
		lds.addLoc(new Loc_Tag(25.426848, 81.771733));
		lds.addLoc(new Loc_Tag(25.427318, 81.770955));
		lds.addLoc(new Loc_Tag(25.426848, 81.771733));
		lds.addLoc(new Loc_Tag(25.427788, 81.771942));
		lds.addLoc(new Loc_Tag(25.426848, 81.771733));
		lds.addLoc(new Loc_Tag(25.428146, 81.771207));
		lds.addLoc(new Loc_Tag(25.426964, 81.771819));
		lds.addLoc(new Loc_Tag(25.426848, 81.771733));
		lds.addLoc(new Loc_Tag(25.426843, 81.771620));
		lds.addLoc(new Loc_Tag(25.426998, 81.771787));*/
		
		ArrayList<Loc_Tag> loc_d = lds.getAllLoc_Tag();
		
		lds.removeAllPOI();
		
		ArrayList<Set<Integer>> clu = new ArrayList<Set<Integer>>();
	    HashMap<String , Integer> h = new HashMap<String, Integer>();
	    HashMap<Integer , Integer> ds = new HashMap<Integer, Integer>();
	    
	    for(int k = 0; k < loc_d.size(); k++) {
            Loc_Tag p = loc_d.get(k);
            Set<Integer> tmp = new HashSet<Integer>();
            
            for(int i = 0; i < loc_d.size(); i++) {
                Loc_Tag pt = loc_d.get(i);
                if(pt.isSame(p, 20)) {
                    String key = pt.lat + "," + pt.lng;
                    tmp.add(i);
                    if(h.containsKey(key)) {
                        ds.put(k, h.get(key));
                     } else {
                        h.put(key, k);
                    }
                }
            }
            clu.add(tmp);
        }
        
        for(int i = clu.size() - 1; i >= 0; i--) {
            if(ds.containsKey(i)) {
            	int j = ds.get(i);
                
            	if(i != j) {
            		Set<Integer> t = clu.remove(i);
                	clu.get(ds.get(i)).addAll(t);
                }
            }
        }
        
        for(Set<Integer> t : clu) {
        	double x = 0, y = 0, z = 0;
        	if(t.size() > 3) {
	            for(Integer j : t) {
	                Loc_Tag pt = loc_d.get(j);
	                //count += pt.count;
	                x += Math.cos(pt.lat * Math.PI / 180.0) * Math.cos(pt.lng * Math.PI / 180.0);
	                y += Math.cos(pt.lat * Math.PI / 180.0) * Math.sin(pt.lng * Math.PI / 180.0);
	                z += Math.sin(pt.lat * Math.PI / 180.0);
	                //System.out.println(j + " " + pt.lat + ", " + pt.lng);
	            }
	            
            	x /= 365.25 * t.size();
            	y /= 365.25 * t.size();
            	z /= 365.25 * t.size();
            	
            	double lon = Math.atan2(y, x);
            	double hyp = Math.sqrt(x * x + y * y);
            	double lat = Math.atan2(z, hyp);
            	lat *= 180 / Math.PI;
            	lon *= 180 / Math.PI;
            	//Toast.makeText(this, "POI: " + lat + "," + lon, Toast.LENGTH_SHORT).show();
            	lds.addPOI(new Loc_Tag(lat, lon));
            }
        }
		
		poi = new POI(lds.getAllPOI());
		//Toast.makeText(MainActivity.this, "" + lds.getAllPOI().size(), Toast.LENGTH_LONG).show();
		
		//Toast.makeText(this, "COUNT: "+ lds.getCount() + "", Toast.LENGTH_LONG).show();
		
		ArrayList<Loc_Tag> usd = new ArrayList<Loc_Tag>();
		
		for(Loc_Tag tmp : loc_d) {
			int i = poi.nearestPOIIndex(tmp);
			if(i >= 0) {
				tmp.index = i;
				usd.add(tmp);
			}
		}
		
		da = new DataAnalyser();
		da.addLoc_Tag(usd);
		
		st = new Suggester(da, poi);
	}


	private class LoadData extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... params) {
			loadData();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			Toast.makeText(MainActivity.this, "Data Loading Complete" , Toast.LENGTH_LONG).show();
			
			Toast.makeText(MainActivity.this, "Count : " + lds.getCount() , Toast.LENGTH_LONG).show();
			
			nb = new NearBy(MainActivity.this, lds, poi, googleMap);
			nl = new NextLocation(MainActivity.this, st, googleMap);

			nLocation = (Button) findViewById(R.id.nLocation);
			nSearch = (Button) findViewById(R.id.nSearch);
			
			nSearch.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Time t = new Time();
					t.setToNow();
					nb.query(mLatitude, mLongitude, true);
					mDrawerLayout.closeDrawers();
				}
			});
			
			nLocation.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					nl.nextLoc(mLatitude, mLongitude);
					mDrawerLayout.closeDrawers();
				}
			});
		}
	}

	
	
	private void initializeDB() {
		lds = new LocationDataSource(this);
		lds.open();
		
		/*
		Loc_Tag l;
		lds.add(new Loc_Tag(25.0, 81.0));
		
		l = lds.getLastEntry();
		if(l != null)Toast.makeText(this, l.date+ "" , Toast.LENGTH_LONG).show();
		else Toast.makeText(this, "1 null" , Toast.LENGTH_LONG).show();
		
		lds.add(new Loc_Tag(25.0, 25.0));
		lds.add(new Loc_Tag(81.0, 81.0));
		
		Toast.makeText(this, lds.count() + "" , Toast.LENGTH_LONG).show();
		
		l = lds.getLastEntry();
		if(l != null)Toast.makeText(this, l.date + "" , Toast.LENGTH_LONG).show();
		else Toast.makeText(this, "1 null" , Toast.LENGTH_LONG).show();*/
	}




	private void initializeGMap() {
		// Getting reference to the SupportMapFragment of activity_main.xml
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
	
		// Getting GoogleMap object from the fragment
		googleMap = fm.getMap();
	
		// Enabling MyLocation Layer of Google Map
		googleMap.setMyLocationEnabled(true);
	
		googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng arg0) {
				tag_tmp = arg0;
				//DIALOG BOX TO ADD YOUR OWN TAGS
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Enter Tag for the place");
				final EditText input = new EditText(MainActivity.this);
				input.setInputType(InputType.TYPE_CLASS_TEXT);
				builder.setView(input);
				builder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String m_text = input.getText().toString();
							
							lds.addTag(new Tag(tag_tmp.latitude, tag_tmp.longitude, m_text));
	
							drawMarker(tag_tmp, BitmapDescriptorFactory.HUE_GREEN, m_text);
							tag_tmp = null;
							
							Toast.makeText(MainActivity.this, "Success :  Tag Added", Toast.LENGTH_SHORT).show();
						}
					});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
				builder.show();
			}
		});
		
		googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker arg0) {
				tag_tmp = arg0.getPosition();
				// TODO Auto-generated method stub
				builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Add Proximity Alert");
				builder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// proximity part
						// This intent will call the activity ProximityActivity
						Intent proximityIntent = new Intent("locp.activity.proximity");
	
						// how to pass the intent
						// Passing latitude to the PendingActivity
						proximityIntent.putExtra("lat", tag_tmp.latitude);
	
						// Passing longitude to the PendingActivity
						proximityIntent.putExtra("lng", tag_tmp.longitude);
	
						// Creating a pending intent which will be invoked by
						// LocationManager when the specified region is
						// entered or exited
						pendingIntent = PendingIntent.getActivity(getBaseContext(), 0,
								proximityIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
	
						// Setting proximity alert
						// The pending intent will be invoked when the device enters or
						// exits the region 20 meters
						// away from the marked point
						// The -1 indicates that, the monitor will not be expired
						locationManager.addProximityAlert(tag_tmp.latitude,
								tag_tmp.longitude, 4, -1, pendingIntent);
						Toast.makeText(MainActivity.this, "Successfully Added", Toast.LENGTH_SHORT).show();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
				
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
				    public void run() {
				    	builder.show();
				    }}, 1000);
				return false;
			}
		});
	}




	private void initializeSearch() {
		ImageButton btnFind = (ImageButton) findViewById(R.id.btn_find);

		btnFind.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				LatLng cView = googleMap.getCameraPosition().target;

				StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_TEXTSEARCH + OUT_JSON);
				sb.append("?location=" + cView.latitude + "," + cView.longitude);
				sb.append("&radius=1000");
				try {
					sb.append("&query=" + URLEncoder.encode(autoCompView.getText().toString(), "utf8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				sb.append("&key=" + API_KEY);

				// Creating a new non-ui thread task to download Google
				// place JSON data
				PlacesTask placesTask = new PlacesTask();

				// Invokes the "doInBackground()" method of the class
				// PlaceTask
				String[] url = new String[1];
				url[0] = sb.toString();
				placesTask.execute(url);

				//Toast.makeText(getApplicationContext(),"Geting Data from Srver Addr: " + sb.toString(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	
	
	
	
	
	/** A method to download JSON data from URL */
	/** A class, to download Google Places */
	private class PlacesTask extends AsyncTask<String, Void, String> {
		String er = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Searching...");
			pDialog.setCancelable(false);
			pDialog.show();
		}
		
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
				parserTask.execute(result);

				//Toast.makeText(getApplicationContext(), "under parserTask.execute(result) which start parser doInBackground", Toast.LENGTH_SHORT).show();
			} else {
				if (pDialog.isShowing())
					pDialog.dismiss();
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage(er);
				builder.setTitle("Error");
				builder.show();
			}
		}
	}

	
	
	
	
	
	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
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
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
			
			if (pDialog.isShowing())
				pDialog.dismiss();
			
			// Clears all the existing markers
			googleMap.clear();

			if(list.size() > 0) {
				//Toast.makeText(context, str, duration).show();
				sharedPreferences = getSharedPreferences("location", 0);
	
				SharedPreferences.Editor editor = sharedPreferences.edit();
	
				//float avg_lat = 0, avg_lng = 0;
				double c_lat = mLatitude, c_lng = mLongitude;
				for (int i = 0; i < list.size(); i++) {
					// Getting a place from the places list
					HashMap<String, String> hmPlace = list.get(i);
	
					// Getting latitude of the place
					double lat = Double.parseDouble(hmPlace.get("lat"));
					//avg_lat += lat;
					// Getting longitude of the place
					double lng = Double.parseDouble(hmPlace.get("lng"));
					//avg_lng += lng;
					if(i == 0) {
						c_lat = lat;
						c_lng = lng;
					}
	
					// Getting name
					String name = hmPlace.get("place_name");
	
					//String gid = hmPlace.get("gid");
					
					// Toast.makeText(context, "place name", duration).show();
	
					// Getting vicinity
					String vicinity = hmPlace.get("vicinity");
	
					//Loc_Tag t = new Loc_Tag(lat, lng);
					//t.gid = gid;
					//lds.addPOI(t);
					
					LatLng latLng = new LatLng(lat, lng);
	
					// Creating Marker
					drawMarker(latLng, BitmapDescriptorFactory.HUE_RED, name + " : " + vicinity);
					// Drawing circle on the map
					drawCircle(latLng);
	
					Log.e(LOG_TAG, "Search: "+name+" "+vicinity);
	
					/** Opening the editor object to write data to sharedPreferences */
					// Storing the details for the i-th location
					editor.putString("lat" + i, Double.toString(latLng.latitude));
					editor.putString("lng" + i, Double.toString(latLng.longitude));
					editor.putString("name" + i, name);
	
					// Toast.makeText(getBaseContext(),
					// "Proximity Alert is added",Toast.LENGTH_SHORT).show();
	
				}
				//avg_lat /= list.size();
				//avg_lng /= list.size();
				
				googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(c_lat, c_lng)));
				//editor.putString("c_lat", Double.toString(c_lat));
				//editor.putString("c_lng", Double.toString(c_lng));
				
				// Storing the count of locations or marker count
				editor.putInt("locationCount", list.size());
	
				/** Storing the zoom level to the shared preferences */
				editor.putString("zoom", Float.toString(googleMap.getCameraPosition().zoom));
				/** Saving the values stored in the shared preferences */
				editor.commit();
			}
			// this one is end of postexecute
		}
	}

	
	
	
	//Location Change Operations
	@Override
	public void onLocationChanged(Location location) {
		mLatitude = location.getLatitude();
		mLongitude = location.getLongitude();
		
		Loc_Tag l = new Loc_Tag(mLatitude, mLongitude);
		lds.addLoc(l);
		
		//LatLng latLng = new LatLng(mLatitude, mLongitude);
		//nb.query(mLatitude, mLongitude, false);
		//googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		//googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	
	
	
	
	//Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
    	
        switch(item.getItemId()) {
        case R.id.settings:
        	Intent st = new Intent(getApplicationContext(), Preferences.class);
    		startActivity(st);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	

    
    
    private void drawCircle(LatLng point) {
		// Instantiating CircleOptions to draw a circle around the marker
		CircleOptions circleOptions = new CircleOptions();

		// Specifying the center of the circle
		circleOptions.center(point);

		// Radius of the circle
		circleOptions.radius(5);

		// Border color of the circle
		circleOptions.strokeColor(Color.BLACK);

		// Fill color of the circle
		circleOptions.fillColor(0x30ff0000);

		// Border width of the circle
		circleOptions.strokeWidth(2);

		// Adding the circle to the GoogleMap
		googleMap.addCircle(circleOptions);
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
		googleMap.addMarker(markerOptions);
	}

	
	
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		lds.close();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		lds.open();
		
		/*sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(sharedPreferences.getBoolean("nSearch", true)) {
			nSearch.setVisibility(View.VISIBLE);
		} else {
			nSearch.setVisibility(View.INVISIBLE);
		}*/
		
		// Opening the sharedPreferences object
		sharedPreferences = getSharedPreferences("location", 0);

		// Getting number of locations already stored
		locationCount = sharedPreferences.getInt("locationCount", 0);

		// Getting stored zoom level if exists else return 0
		String zoom = sharedPreferences.getString("zoom", "0");

		// If locations are already saved
		if (locationCount != 0) {
			String lat = "";
			String lng = "";
			String name = "";

			LatLng c = null;
			// Iterating through all the locations stored
			for (int i = 0; i < locationCount; i++) {
				// Getting the latitude of the i-th location
				lat = sharedPreferences.getString("lat" + i, "0");
				
				// Getting the longitude of the i-th location
				lng = sharedPreferences.getString("lng" + i, "0");
				
				name = sharedPreferences.getString("name" + i, "none");

				LatLng p = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
				if(i == 0) {
					c = p;
				}
				// Drawing marker on the map
				drawMarker(p, BitmapDescriptorFactory.HUE_RED, name);
				// Drawing circle on the map
				drawCircle(p);
			}
			//double c_lat = Double.parseDouble(sharedPreferences.getString("c_lat", Double.toString(mLatitude)));
			//double c_lng = Double.parseDouble(sharedPreferences.getString("c_lat", Double.toString(mLongitude)));
			
			// Moving CameraPosition to last clicked position
			if(c != null) {
				googleMap.moveCamera(CameraUpdateFactory.newLatLng(c));
			}
			// Setting the zoom level in the map on last position is clicked
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(Float.parseFloat(zoom)));
		}
		
		
		// If locations are already saved
		List<Tag> tags = lds.getAllTag();
		
		if(tags != null && tags.size() > 0) {
			// Iterating through all the locations stored
			for (Tag t : tags) {
				// Drawing marker on the map
				drawMarker(new LatLng(t.lat, t.lng),
						BitmapDescriptorFactory.HUE_GREEN, t.name);
				// Drawing circle on the map
				drawCircle(new LatLng(t.lat, t.lng));
			}
		}

	}

	
	
	
	
	
	
	
	@Override
	protected void onStart() {
		super.onStart();
		// Getting LocationManager object from System Service
		// LOCATION_SERVICE
		getLocation();
	}
	
	//Checking For Location access and Updating variables onResume
	public Location getLocation() {
		Toast.makeText(this, "Getting Location", Toast.LENGTH_SHORT).show();
		try {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (isGPSEnabled == false && isNetworkEnabled == false) {
				// no network provider is enabled
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("No Location Provider Enabled");
				builder.setTitle("Error");
				builder.show();
				// AlertDialog dialog = builder.create();
				// dialog.show();
				// Toast.makeText(this, "No Location Provider Enables",
				// Toast.LENGTH_LONG).show();
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					location = null;
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							mLatitude = location.getLatitude();
							mLongitude = location.getLongitude();
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					location = null;
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS Enabled", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								mLatitude = location.getLatitude();
								mLongitude = location.getLongitude();
							}
						}
					}
				}
				
				LatLng latLng = new LatLng(mLatitude, mLongitude);

				googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
				googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	
	
	
	
	
	
	
	private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&location=25.427002,81.771731");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }
    
    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }
    }
}