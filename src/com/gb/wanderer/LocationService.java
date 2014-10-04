package com.gb.wanderer;

import com.gb.pro.db.LocationDataSource;
import com.gb.pro.nLocation.Loc_Tag;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service implements LocationListener {

	// Location Variables
		//Location loc;
	LocationManager locationManager;
		
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
	
	LocationDataSource lds;
	double mLatitude, mLongitude;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Toast.makeText(this, "Location Service Started", Toast.LENGTH_SHORT).show();
		lds = new LocationDataSource(this);
		lds.open();
		
		getLocation();
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {        
		return Service.START_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLocationChanged(Location location) {
		mLatitude = location.getLatitude();
		mLongitude = location.getLongitude();
		
		//Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
		Loc_Tag l = new Loc_Tag(mLatitude, mLongitude);
		lds.addLoc(l);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	//Checking For Location access and Updating variables onResume
	public Location getLocation() {
		//Toast.makeText(this, "Getting Location", Toast.LENGTH_SHORT).show();
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
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}
}
