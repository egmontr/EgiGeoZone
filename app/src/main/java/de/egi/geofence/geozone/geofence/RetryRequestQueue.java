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
package de.egi.geofence.geozone.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Map;

import de.egi.geofence.geozone.MainEgiGeoZone;

public class RetryRequestQueue {
	// The name of the resulting SharedPreferences
    private static SharedPreferences mPrefs;
    private static final String SHARED_PREFERENCE_NAME = MainEgiGeoZone.class.getSimpleName() + "_RetryRequests";
	
	public static void setRequest(Context context, String zone, int transition, String realLat, String realLng, String realTime, int retrys){
        setPref(context, zone, Integer.toString(transition));
        setPref(context, zone + "_lat##", realLat == null ? "" : realLat);
        setPref(context, zone + "_lng##", realLng == null ? "" : realLng);
        setPref(context, zone + "_time##", realTime == null ? "" : realTime);
        setPrefInt(context, zone + "_retrys##", retrys);
	}
	
    private static void setPref(Context context, String key, String value){
		setSharedPrefs(context);
        Editor editor = mPrefs.edit();
        editor.putString(key, value);
        // Commit the changes
        editor.apply();
    }

    private static void setPrefInt(Context context, String key, int value){
        setSharedPrefs(context);
        Editor editor = mPrefs.edit();
        editor.putInt(key, value);
        // Commit the changes
        editor.apply();
    }

    public static void removePref(Context context, String key){
		setSharedPrefs(context);
        Editor editor = mPrefs.edit();
        editor.remove(key);
        editor.remove(key + "_lat##");
        editor.remove(key + "_lng##");
        editor.remove(key + "_time##");
        editor.remove(key + "_retrys##");
        // Commit the changes
        editor.apply();
    }

    public static Map<String, ?> getAllPref(Context context){
        setSharedPrefs(context);
        return mPrefs.getAll();
    }

    public static String getPref(Context context, String key){
        setSharedPrefs(context);
        return mPrefs.getString(key, null);
    }

    public static int getPrefInt(Context context, String key){
        setSharedPrefs(context);
        return mPrefs.getInt(key, 0);
    }

    private static void setSharedPrefs(Context context){
	    mPrefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }
}
