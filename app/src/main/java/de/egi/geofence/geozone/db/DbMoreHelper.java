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

public class DbMoreHelper{

	private final DbHelper dbHelper;
	private SQLiteDatabase db;
	public DbMoreHelper(Context context) {
		dbHelper = DbHelper.getInstance(context.getApplicationContext());
	}
	
    // Get all 
    public Cursor getCursorAllMore() {
    	db = dbHelper.getReadableDatabase();
        return db.query(DbContract.MoreEntry.TN, DbContract.MoreEntry.allColumns, null, null, null, null, DbContract.MoreEntry.CN_NAME);
	}

    // Get all sorted
    public Cursor getCursorAllMoreSorted() {
        db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DbContract.MoreEntry.TN + " ORDER BY " + DbContract.MoreEntry.CN_NAME + " COLLATE NOCASE", null);
    }

    // Get by Id 
    public MoreEntity getCursorMoreById(int ind) {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.MoreEntry.TN, DbContract.MoreEntry.allColumns, DbContract.MoreEntry._ID + " = " + ind, null, null, null, null);
        cursor.moveToFirst();
        MoreEntity me = cursorToMore(cursor);
        cursor.close();
        return me;

	}

    // Get by name 
    public MoreEntity getCursorMoreByName(String name) {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.MoreEntry.TN, DbContract.MoreEntry.allColumns, DbContract.MoreEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        MoreEntity me = cursorToMore(cursor);
        cursor.close();
        return me;

	}

    // Create new entry
    public void createMore(MoreEntity moreEntity) {
    	db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.MoreEntry.CN_NAME, moreEntity.getName());
        values.put(DbContract.MoreEntry.CN_ENTER_TASK, moreEntity.getEnter_task());
        values.put(DbContract.MoreEntry.CN_EXIT_TASK, moreEntity.getExit_task());

        values.put(DbContract.MoreEntry.CN_ENTER_WIFI, moreEntity.getEnter_wifi() == null ? 3 : moreEntity.getEnter_wifi());
        values.put(DbContract.MoreEntry.CN_EXIT_WIFI, moreEntity.getExit_wifi() == null ? 3 : moreEntity.getExit_wifi());
        values.put(DbContract.MoreEntry.CN_ENTER_BT, moreEntity.getEnter_bt() == null ? 3 : moreEntity.getEnter_bt());
        values.put(DbContract.MoreEntry.CN_EXIT_BT, moreEntity.getExit_bt() == null ? 3 : moreEntity.getExit_bt());
        values.put(DbContract.MoreEntry.CN_ENTER_SOUND, moreEntity.getEnter_sound() == null ? 4 : moreEntity.getEnter_sound());
        values.put(DbContract.MoreEntry.CN_EXIT_SOUND, moreEntity.getExit_sound() == null ? 4 : moreEntity.getExit_sound());
        values.put(DbContract.MoreEntry.CN_ENTER_SOUND_MM, moreEntity.getEnter_soundMM() == null ? 2 : moreEntity.getEnter_soundMM());
        values.put(DbContract.MoreEntry.CN_EXIT_SOUND_MM, moreEntity.getExit_soundMM() == null ? 2 : moreEntity.getExit_soundMM());

        db.insert(DbContract.MoreEntry.TN, null, values);
      }

    private MoreEntity cursorToMore(Cursor cursor) {
    	MoreEntity more = new MoreEntity();
        more.setId(cursor.getInt(0));
        more.setName(cursor.getString(1));
        more.setEnter_task(cursor.getString(2));
        more.setEnter_wifi(cursor.getInt(3));
        more.setEnter_sound(cursor.getInt(4));
        more.setEnter_bt(cursor.getInt(5));
        more.setExit_task(cursor.getString(6));
        more.setExit_wifi(cursor.getInt(7));
        more.setExit_sound(cursor.getInt(8));
        more.setExit_bt(cursor.getInt(9));
        more.setEnter_soundMM(cursor.getInt(10));
        more.setExit_soundMM(cursor.getInt(11));

        return more;
      }
    
    // Löschen mit ID
    public void deleteMore(String ind) {
        db = dbHelper.getWritableDatabase();
        db.delete(DbContract.MoreEntry.TN, DbContract.MoreEntry._ID + " = " + ind, null);
      }

    // Update
    private void updateMore(MoreEntity moreEntity) {
    	db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.MoreEntry.CN_NAME, moreEntity.getName());
        values.put(DbContract.MoreEntry.CN_ENTER_TASK, moreEntity.getEnter_task());
        values.put(DbContract.MoreEntry.CN_EXIT_TASK, moreEntity.getExit_task());
        
        values.put(DbContract.MoreEntry.CN_ENTER_WIFI, moreEntity.getEnter_wifi() == null ? 3 : moreEntity.getEnter_wifi());
        values.put(DbContract.MoreEntry.CN_EXIT_WIFI, moreEntity.getExit_wifi() == null ? 3 : moreEntity.getExit_wifi());
        values.put(DbContract.MoreEntry.CN_ENTER_BT, moreEntity.getEnter_bt() == null ? 3 : moreEntity.getEnter_bt());
        values.put(DbContract.MoreEntry.CN_EXIT_BT, moreEntity.getExit_bt() == null ? 3 : moreEntity.getExit_bt());
        values.put(DbContract.MoreEntry.CN_ENTER_SOUND, moreEntity.getEnter_sound() == null ? 4 : moreEntity.getEnter_sound());
        values.put(DbContract.MoreEntry.CN_EXIT_SOUND, moreEntity.getExit_sound() == null ? 4 : moreEntity.getExit_sound());
        values.put(DbContract.MoreEntry.CN_ENTER_SOUND_MM, moreEntity.getEnter_soundMM() == null ? 2 : moreEntity.getEnter_soundMM());
        values.put(DbContract.MoreEntry.CN_EXIT_SOUND_MM, moreEntity.getExit_soundMM() == null ? 2 : moreEntity.getExit_soundMM());

        db.update(DbContract.MoreEntry.TN, values, DbContract.MoreEntry._ID + " = " + moreEntity.getId(), null);
      }

    // Check for double profile 
    private boolean moreProfileExists(String name) {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.MoreEntry.TN, DbContract.MoreEntry.allColumns, DbContract.MoreEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0 || cursor.getString(1) == null){
        	cursor.close();
        	return false;
        } 
        cursor.close();
        return true;
	}

    // Store: Wenn vorhanden ersetzen, ansonsten anlegen.
    public void storeMore(MoreEntity moreEntity) {
    	boolean exists = moreProfileExists(moreEntity.getName());
    	if (exists){
    		updateMore(moreEntity);
    	}else{
    		moreEntity.setId(0);
    		createMore(moreEntity);
    	}
    }


}