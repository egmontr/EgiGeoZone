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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.SharedPrefsUtil;

public class TrackingUtils {

	public static void startTracking(Context context, String zone, int trackIntervallZone, boolean trackToFile, boolean trackUrl, boolean trackToMail){
    	
		int reqId = exists(context, zone);
    	// Prüfen, ob ein Tracking zu dieser Zone schon läuft und dann nicht mehr einen neuen track starten
    	if (reqId == 0){
	    	reqId = getNewReqId(context);
	    	// Save TrackingRule to SharedPrefs
	        checkAndSaveTrackingRule(context, zone, reqId);
    	}
    	
        // Schauen, ob Service läuft. Wenn nicht starten
		if (!isMyServiceRunning(TrackingLocationService.class, context)){
			Intent i = new Intent(context, TrackingLocationService.class);
			context.startService(i);

			Log.i("TrackingUtils","Service stopped");
		}
		
		// Starten eines Location Tracker Services 
		Intent myIntent = new Intent(context, TrackingReceiverWorker.class);
        myIntent.putExtra("zone", zone);
		myIntent.putExtra("mins", trackIntervallZone);
        myIntent.putExtra("trackUrl", trackUrl);
        myIntent.putExtra("trackToFile", trackToFile);
        myIntent.putExtra("trackToMail", trackToMail);
        myIntent.putExtra("reqId", Integer.toString(reqId));
        TrackingReceiverWorkerService.startAlarm(context, myIntent, reqId, trackIntervallZone);
		// Set permanent notification, to see that we track
//        NotificationUtil.sendPermanentNotification(context, R.drawable.footsteps, context.getString(R.string.text_tracking_notification), 6666);

        Log.i("TrackingUtils","Alarm started for: " + reqId);
    }
    

    public static void stopTracking(Context context, String zone){
    	
    	int reqId = getReqId(context, zone);
		
    	removeTrackingRule(context, zone, reqId);

    	// Alarm stoppen
		Intent myIntent = new Intent(context, TrackingReceiverWorker.class);
        myIntent.putExtra("zone", zone);
        myIntent.putExtra("reqId", Integer.toString(reqId));
        TrackingReceiverWorkerService.cancelAlarm(context, myIntent, reqId);
        
        // Schauen, ob Service nicht noch von anderen Trackings benötigt wird. Wenn nicht, dann löschen.
		if (isMyServiceRunning(TrackingLocationService.class, context)){
			// Hier prüfen
			if (!noTrackings(context)){
				// Service stoppen
				Intent i = new Intent(context, TrackingLocationService.class);
				context.stopService(i);
				// Delete permanent notification, that shown that we tracked
//                NotificationUtil.cancelPermanentNotification(context, 6666);
				Log.i("TrackingUtils","Service stopped");
			}
		}
    }

    public static void stopAllTrackings(Context context){
    	
    	List<String> reqIds = removeAllTrackingRules(context);

		for (String key : reqIds) {
			int reqId;

			int unterstrich = key.lastIndexOf("_");
			if (isNumeric(key.substring(unterstrich + 1))) {
				reqId = Integer.parseInt(key.substring(unterstrich + 1));
			} else {
				continue;
			}

			// Alarm stoppen
			Intent myIntent = new Intent(context, TrackingReceiverWorker.class);
			//        myIntent.putExtra("zone", zone);
			myIntent.putExtra("reqId", Integer.toString(reqId));
			TrackingReceiverWorkerService.cancelAlarm(context, myIntent, reqId);
			Log.i("TrackingUtils", "Alarm stopped for: " + reqId);
		}
    	
        // Schauen, ob Service nicht noch von anderen Trackings ben�tigt wird. Wenn nicht, dann l�schen.
		if (isMyServiceRunning(TrackingLocationService.class, context)){
			// Service stoppen
			Intent i = new Intent(context, TrackingLocationService.class);
			context.stopService(i);
			// Delete permanent notification, that shown that we tracked
            NotificationUtil.cancelPermanentNotification(context, 6666);
			Log.i("TrackingUtils","Service stopped");
		}
    }

    /**
     * Delete TrackingRule in SharedPrefs
     */
    private static void removeTrackingRule(Context context, String zone, int reqId) {
    	String key = zone + "_" + reqId;
    	SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
    	sharedPrefsUtil.removeTrackingPref(key);
    }

	/**
     * Save TrackingRule to SharedPrefs
     */
    private static void checkAndSaveTrackingRule(Context context, String zone, int reqId) {
    	String key = zone + "_" + reqId;
    	SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
    	String tracking = sharedPrefsUtil.getTrackingPref(key);
    	if (tracking == null || tracking.equalsIgnoreCase("")){
            Log.i("TrackingReceiverWorker","Saving key: " + key);
    		sharedPrefsUtil.setTrackingPref(key, zone);
    	}
    }

	/**
     * Generate new ReqId
     */
    private static int getNewReqId(Context context){
    	int resp = 0;
    	SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
    	Map<String, ?> map = sharedPrefsUtil.getAllPref();
    	Set<String> keys = map.keySet();
    	
//    	Beispiel: de.egi.geofence.geozone.KEY_##TRACKING##_##_zuHause_4711
		for (String key : keys) {
			if (key.startsWith(Constants.KEY_PREFIX + "_" + "##TRACKING##")) {
				int unterstrich = key.lastIndexOf("_");
				if (isNumeric(key.substring(unterstrich + 1))) {
					if (Integer.parseInt(key.substring(unterstrich + 1)) > resp) {
						resp = Integer.parseInt(key.substring(unterstrich + 1)) + 1;
					}
				}
			}
		}
    	return resp == 0 ? 1 : resp;
    }

	/**
     * Search for all ReqIds, remove them and give the list of reqids back
     */
    private static List<String> removeAllTrackingRules(Context context){
    	String resp;
    	List<String> reqIds = new ArrayList<>();
    	SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
    	Map<String, ?> map = sharedPrefsUtil.getAllPref();
    	Set<String> keys = map.keySet();
    	
//    	Beispiel: de.egi.geofence.geozone.KEY_##TRACKING##_##_zuHause_4711
		for (String key : keys) {
			if (key.startsWith(Constants.KEY_PREFIX + "_" + "##TRACKING##" + "_##_")) {
				int unterstrich = key.lastIndexOf("_");
				if (isNumeric(key.substring(unterstrich + 1))) {
					resp = key.substring(unterstrich + 1);
					reqIds.add(resp);
					Log.i("TrackingUtils", "Removing key: " + key);
					sharedPrefsUtil.removePref(key);

				}
			}
		}
    	return reqIds;
    }

	/**
     * Search for ReqId with zone
     */
    private static int getReqId(Context context, String zone){
    	int resp = 0;
    	SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
    	Map<String, ?> map = sharedPrefsUtil.getAllPref();
    	Set<String> keys = map.keySet();
    	
//    	Beispiel: de.egi.geofence.geozone.KEY_##TRACKING##_##_zuHause_4711
		for (String key : keys) {
			if (key.startsWith(Constants.KEY_PREFIX + "_" + "##TRACKING##" + "_##_" + zone + "_")) {
				int unterstrich = key.lastIndexOf("_");
				if (isNumeric(key.substring(unterstrich + 1))) {
					resp = Integer.parseInt(key.substring(unterstrich + 1));
					return resp;
				}
			}
		}
    	return resp;
    }

	/**
     * Check for existing key
     */
    public static int exists(Context context, String zone){
    	int resp = 0;
    	SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
    	Map<String, ?> map = sharedPrefsUtil.getAllPref();
    	Set<String> keys = map.keySet();
    	
//    	Beispiel: de.egi.geofence.geozone.KEY_##TRACKING##_##_zuHause_4711
		for (String key : keys) {
			if (key.startsWith(Constants.KEY_PREFIX + "_" + "##TRACKING##" + "_##_" + zone + "_")) {
				int unterstrich = key.lastIndexOf("_");
				if (isNumeric(key.substring(unterstrich + 1))) {
					resp = Integer.parseInt(key.substring(unterstrich + 1));
					Log.i("TrackingUtils", "Key exists: " + key);
					return resp;
				}
			}
		}
    	return resp;
    }


    /**
     * Check for Tracking to stop service
     */
    private static boolean noTrackings(Context context){
    	SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
    	Map<String, ?> map = sharedPrefsUtil.getAllPref();
    	Set<String> keys = map.keySet();
		for (String key : keys) {
			if (key.startsWith(Constants.KEY_PREFIX + "_" + "##TRACKING##")) {
				Log.i("TrackingUtils", "Found track for key : " + key);
				return true;
			}
		}
    	return false;
    }
    
    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager)context. getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("TrackingUtils","Service already running");
                return true;
            }
        }
        Log.i("TrackingUtils", "Service not running");
        return false;
    }
    
	private static boolean isNumeric(String str) {
		try {
			@SuppressWarnings({"unused", "UnusedAssignment"})
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
