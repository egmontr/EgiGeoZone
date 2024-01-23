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

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

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


public class TrackingLocationService extends Service {

	private final Logger log = Logger.getLogger(TrackingLocationService.class);
	private final String TAG = "TrackingLocationService";
	private LocationCallback locationCallback;
	private FusedLocationProviderClient mLocationClient;

	@Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
			// Disconnecting the client invalidates it.
			mLocationClient.removeLocationUpdates(locationCallback);
		}catch (Exception e) {
			// Nichts tun
			String a = e.getMessage();
			Log.e("LocService", "Error: " + a);
			log.error("Error in destroy: " + a, e);
		}
    }

    @SuppressLint("UnspecifiedImmutableFlag")
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		DbGlobalsHelper dbGlobalsHelper = new DbGlobalsHelper(this);
		int trackIntervall = Integer.parseInt(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCINTERVAL) == null
				|| dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCINTERVAL).isEmpty()
				? "5" : dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCINTERVAL));

		int locPrio = Priority.PRIORITY_BALANCED_POWER_ACCURACY;
		try {
			locPrio = Integer.parseInt(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCPRIORITY));
		} catch (Exception e) {
			// do nothing
		}
		int trackPrio = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCPRIORITY) == null
				? Priority.PRIORITY_BALANCED_POWER_ACCURACY : locPrio;

		// Begin polling for new location updates.
		// Note that this can be NULL if last location isn't already known.
		LocationRequest mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(trackPrio);
		mLocationRequest.setInterval(60L * 1000 * trackIntervall);
//		mLocationRequest.setFastestInterval()

		Log.d(TAG, "Location connected with intervall: " + trackIntervall);
		Log.d(TAG, "Location connected with priority: " + trackPrio);

		// Get last known recent location.
		mLocationClient = LocationServices.getFusedLocationProviderClient(this);
			try {
							locationCallback = new LocationCallback() {
								@Override
								public void onLocationResult(@NonNull LocationResult locationResult) {
									if (locationResult.getLastLocation() == null) {
										return;
									}
									Location location = locationResult.getLastLocation();

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

									SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(getApplicationContext());
									sharedPrefsUtil.setLocationPref(location_lat, Double.valueOf(location.getLatitude()).toString());
									sharedPrefsUtil.setLocationPref(location_lng, Double.valueOf(location.getLongitude()).toString());
									sharedPrefsUtil.setLocationPref(location_utc_time, nowAsISO);
									sharedPrefsUtil.setLocationPref(location_local_time, nowAsLocal);
									sharedPrefsUtil.setLocationPref(location_accuracy, Float.valueOf(location.getAccuracy()).toString());
								}
							};

				mLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());

			} catch (SecurityException se) {
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

			Intent notificationIntent = new Intent(this, MainEgiGeoZone.class);
			PendingIntent pendingIntent;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
				pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
			} else {
				pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			}
			this.startForeground(6666, NotificationUtil.prepareNotification(this, R.drawable.footsteps, this.getString(R.string.text_tracking_notification), pendingIntent));

			return super.onStartCommand(intent, flags, startId);
		}
	}

















