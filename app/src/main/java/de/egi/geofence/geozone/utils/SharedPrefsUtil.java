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

package de.egi.geofence.geozone.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Map;

import de.egi.geofence.geozone.MainEgiGeoZone;

public class SharedPrefsUtil {
    // The SharedPreferences object in which geofences are stored
    private final SharedPreferences mPrefs;
    private final String TRACKING = "##TRACKING##";
    private final String LOCATION = "##LOCATION##";

    // The name of the resulting SharedPreferences
    private static final String SHARED_PREFERENCE_NAME = MainEgiGeoZone.class.getSimpleName();

    // Create the SharedPreferences storage with private access only
    public SharedPrefsUtil(Context context) {
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void setLocationPref(String key, String value){
        setPref(LOCATION, key, value);
    }
    
    public void setTrackingPref(String key, String value){
        setPref(TRACKING, key, value);
    }
    
    public void removeTrackingPref(String key){
        Editor editor = mPrefs.edit();
        editor.remove(getGeofenceFieldKey(TRACKING, key));
        // Commit the changes
        editor.apply();
    }
    
    public void removePref(String key){
        Editor editor = mPrefs.edit();
        editor.remove(key);
        // Commit the changes
        editor.apply();
    }

    private void setPref(String id, String key, String value){
        Editor editor = mPrefs.edit();
        editor.putString(getGeofenceFieldKey(id, key), value);
        // Commit the changes
        editor.apply();
    }

    public void setPref(String key, String value){
        Editor editor = mPrefs.edit();
        editor.putString(key, value);
        // Commit the changes
        editor.apply();
    }

    public String getTrackingPref(String key){
    	return getPref(getGeofenceFieldKey(TRACKING, key));
    }

    public String getLocationPref(String key){
    	return getPref(getGeofenceFieldKey(LOCATION, key));
    }

    public String getPref(String key){
    	return mPrefs.getString(key, null);
    }

    public Map<String, ?> getAllPref(){
    	return mPrefs.getAll();
    }
    
    /**
     * return the key name of the
     * object's values in SharedPreferences.
     *
     * @param id The ID of a Geofence object
     * @param fieldName The field represented by the key
     * @return The full key name of a value in SharedPreferences
     */
    private String getGeofenceFieldKey(String id, String fieldName) {
        String TRENNER = "_##_";
        return Constants.KEY_PREFIX + "_" + id + TRENNER + fieldName;
    }
}
