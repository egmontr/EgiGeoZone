/*
* Copyright 2014 - 2015 Egmont R. (egmontr@gmail.com)
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/		
package de.egi.geofence.geozone.tracker;

import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.egi.geofence.geozone.MainEgiGeoZone;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.SharedPrefsUtil;
import de.egi.geofence.geozone.utils.Utils;


public class TrackingLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener  {
	private GoogleApiClient mLocationClient;

	private final Logger log = Logger.getLogger(TrackingLocationService.class);
	private final String TAG = "TrackingLocationService";
	private int trackIntervall = 0;
	private int trackPrio = 0;


	@Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
			// Disconnecting the client invalidates it.
			LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);

			// only stop if it's connected, otherwise we crashif (mLocationClient != null) {
			if (mLocationClient != null) {
				mLocationClient.disconnect();
			}
		}catch (Exception e) {
			// Nichts tun
			String a = e.getMessage();
			Log.e("LocService", "Error: " + a);
			log.error("Error in destroy: " + a, e);
		}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		DbGlobalsHelper dbGlobalsHelper = new DbGlobalsHelper(this);
    	this.trackIntervall = Integer.parseInt(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCINTERVAL) == null
                || dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCINTERVAL).isEmpty()
				 ? "5" : dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCINTERVAL));

		int locPrio = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
		try{
			locPrio = Integer.parseInt(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCPRIORITY));
		}catch(Exception e){
			// do nothing
		}
    	this.trackPrio = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCPRIORITY) == null
				? LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY : locPrio;
    	
		mLocationClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
		mLocationClient.connect();

		Intent notificationIntent = new Intent(this, MainEgiGeoZone.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		this.startForeground(6666, NotificationUtil.prepareNotification(this, R.drawable.footsteps, this.getString(R.string.text_tracking_notification), pendingIntent));

		return super.onStartCommand(intent, flags, startId);
    }

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result) {
		Log.i(TAG, "GoogleApiClient connection has failed");
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {

		// Begin polling for new location updates.
		// Note that this can be NULL if last location isn't already known.
		LocationRequest mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(trackPrio);
		mLocationRequest.setInterval(60 * 1000 * trackIntervall);
//		mLocationRequest.setFastestInterval()

		Log.d(TAG, "Location connected with intervall: " + trackIntervall);
		Log.d(TAG, "Location connected with priority: " + trackPrio);

		try {
			// Get last known recent location.
			Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
			if (mCurrentLocation != null) {
				// Print current location if not null
				Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
//				LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
			}

			LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

		}catch (SecurityException se){
			// Display UI and wait for user interaction
			AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
			alertDialogBuilder.setMessage(this.getString(R.string.alertPermissions));
			alertDialogBuilder.setTitle(this.getString(R.string.titleAlertPermissions));

			alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();

		}

	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i(TAG, "GoogleApiClient connection has been suspend");
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Location: " + location.toString());

		String location_lat = "location_lat";
    	String location_lng = "location_lng";
		String location_utc_time = "location_utc_time";
		String location_local_time = "location_local_time"; // device time. Not UTC
		String location_accuracy = "location_accuracy";

		// Device time
		TimeZone tz = TimeZone.getDefault();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		df.setTimeZone(tz);
		String nowAsLocal = df.format(new Date(location.getTime()));
		Log.d(TAG, "Local location time: " + nowAsLocal);

		// UTC time as ISO
		TimeZone tz1 = TimeZone.getTimeZone("UTC");
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
		df1.setTimeZone(tz1);
		String nowAsISO = df1.format(new Date(location.getTime()));

		SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(this);
   		sharedPrefsUtil.setLocationPref(location_lat, Double.valueOf(location.getLatitude()).toString());
   		sharedPrefsUtil.setLocationPref(location_lng, Double.valueOf(location.getLongitude()).toString());
		sharedPrefsUtil.setLocationPref(location_utc_time, nowAsISO);
		sharedPrefsUtil.setLocationPref(location_local_time, nowAsLocal);
		sharedPrefsUtil.setLocationPref(location_accuracy, Float.valueOf(location.getAccuracy()).toString());
	}
}

















