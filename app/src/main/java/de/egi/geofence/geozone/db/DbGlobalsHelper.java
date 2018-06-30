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

package de.egi.geofence.geozone.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.location.LocationRequest;

import java.util.Properties;

import de.egi.geofence.geozone.utils.Constants;

public class DbGlobalsHelper  {
	private final DbHelper dbHelper;
	private SQLiteDatabase db;
	public DbGlobalsHelper(Context context) {
		dbHelper = DbHelper.getInstance(context.getApplicationContext());
//		db = dbHelper.getDb();
	}

    // Get all as props
    public Properties getCursorAllGlobals() {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.GlobalsEntry.TN, DbContract.GlobalsEntry.allColumns, null, null, null, null, null);
	    
	    Properties properties = new Properties();
		// Werte aus DB lesen
	    while (cursor.moveToNext()) {
			properties.put(cursor.getString(1), cursor.getString(2) == null ? "" : cursor.getString(2));
        }
		
	    // Prüfen, ob vorhanden. Wenn nicht, mit Standardwerten vorbelegen
	    if (!properties.containsKey(Constants.DB_KEY_NOTIFICATION)){
	    	properties.put(Constants.DB_KEY_NOTIFICATION, "true");
	    }
	    if (!properties.containsKey(Constants.DB_KEY_ERROR_NOTIFICATION)){
	    	properties.put(Constants.DB_KEY_ERROR_NOTIFICATION, "true");
	    }
	    if (!properties.containsKey(Constants.DB_KEY_GCM_SENDERID)){
	    	properties.put(Constants.DB_KEY_GCM_SENDERID, "");
	    }
	    if (!properties.containsKey(Constants.DB_KEY_GCM_REG_ID)){
	    	properties.put(Constants.DB_KEY_GCM_REG_ID, "");
	    }
	    if (!properties.containsKey(Constants.DB_KEY_GCM)){
	    	properties.put(Constants.DB_KEY_GCM, "false");
	    }
	    if (!properties.containsKey(Constants.DB_KEY_GCM_LOGGING)){
	    	properties.put(Constants.DB_KEY_GCM_LOGGING, "false");
	    }
	    if (!properties.containsKey(Constants.DB_KEY_LOCINTERVAL)){
	    	properties.put(Constants.DB_KEY_LOCINTERVAL, "5");
	    }
	    if (!properties.containsKey(Constants.DB_KEY_LOCPRIORITY)){
	    	properties.put(Constants.DB_KEY_LOCPRIORITY, Integer.valueOf(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).toString());
	    }
	    if (!properties.containsKey(Constants.DB_KEY_LOG_LEVEL)){
	    	properties.put(Constants.DB_KEY_LOG_LEVEL, "ERROR");
	    }
		if (!properties.containsKey(Constants.DB_KEY_MIGRATED_TO_DB)){
			properties.put(Constants.DB_KEY_MIGRATED_TO_DB, "false");
		}
		if (!properties.containsKey(Constants.DB_KEY_NEW_API)){
			properties.put(Constants.DB_KEY_NEW_API, "false");
		}
	    cursor.close();
	    return properties;
	}

// --Commented out by Inspection START (23.12.2015 15:25):
//    // Get by Id
//    public GlobalsEntity getCursorGlobalsById(int ind) {
//    	db = dbHelper.getReadableDatabase();
//	    Cursor cursor = db.query(DbContract.GlobalsEntry.TN, DbContract.GlobalsEntry.allColumns, DbContract.GlobalsEntry._ID + " = " + ind, null, null, null, null);
//        cursor.moveToFirst();
//        GlobalsEntity me = cursorToGlobals(cursor);
//        cursor.close();
////	    db.close();
//        return me;
//
//	}
// --Commented out by Inspection STOP (23.12.2015 15:25)

    // Get by key 
    public String getCursorGlobalsByKey(String key) {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.GlobalsEntry.TN, DbContract.GlobalsEntry.allColumns, DbContract.GlobalsEntry.CN_KEY + " = '" + key + "'", null, null, null, null);
	    if (cursor.getCount() == 0){
	    	cursor.close();
	    	return null;
	    }
	    
//	    if (BuildConfig.DEBUG) {
//		    while (cursor.moveToNext()) {
//		    	Log.d("", cursor.getInt(0) + ":" + cursor.getString(1) + ":" + cursor.getString(2));
//		    }
//	    }
	    
	    cursor.moveToFirst();
        GlobalsEntity me = cursorToGlobals(cursor);
        cursor.close();
//        db.close();
        return me.getValue();

	}

    // Create new entry
    public void createGlobals(String key, String value) {
    	db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.GlobalsEntry.CN_KEY, key);
        values.put(DbContract.GlobalsEntry.CN_VALUE, value);
        db.insert(DbContract.GlobalsEntry.TN, null, values);
//        db.close();
      }

    private GlobalsEntity cursorToGlobals(Cursor cursor) {
    	GlobalsEntity Globals = new GlobalsEntity();
        Globals.setId(cursor.getInt(0));
        Globals.setKey(cursor.getString(1));
        Globals.setValue(cursor.getString(2));
        
        return Globals;
      }
    
// --Commented out by Inspection START (23.12.2015 15:25):
//    // Löschen mit ID
//    public void deleteGlobals(String ind) {
//    	db = dbHelper.getWritableDatabase();
//	    db.delete(DbContract.GlobalsEntry.TN, DbContract.GlobalsEntry._ID + " = " + ind, null);
////	    db.close();
//      }
// --Commented out by Inspection STOP (23.12.2015 15:25)

    // Update
	private void updateGlobals(String key, String value) {
    	db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.GlobalsEntry.CN_KEY, key);
        values.put(DbContract.GlobalsEntry.CN_VALUE, value);
        db.update(DbContract.GlobalsEntry.TN, values, DbContract.GlobalsEntry.CN_KEY + " = '" + key + "'", null);
//        db.close();
      }
    
    // Check for row 
	private boolean globalExists(String key) {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.GlobalsEntry.TN, DbContract.GlobalsEntry.allColumns, DbContract.GlobalsEntry.CN_KEY + " = '" + key + "'", null, null, null, null);

//	    if (BuildConfig.DEBUG) {
//		    while (cursor.moveToNext()) {
//		    	Log.d("", cursor.getInt(0) + ":" + cursor.getString(1) + ":" + cursor.getString(2));
//		    }
//	    }

        cursor.moveToFirst();
        if (cursor.getCount() == 0){
        	cursor.close();
//        	db.close();
        	return false;
        } 
    	cursor.close();
//    	db.close();
        return true;
	}


    // Store: Wenn vorhanden ersetzen. Ansonsten anlegen.
    public void storeGlobals(String key, String value) {
    	boolean exists = globalExists(key);
    	if (!exists){
    		createGlobals(key, value);
    	}else{
    		updateGlobals(key, value);
    	}
    }
}