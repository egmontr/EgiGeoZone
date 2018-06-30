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

public class DbSmsHelper{

	private final DbHelper dbHelper;
	private SQLiteDatabase db;
	public DbSmsHelper(Context context) {
		dbHelper = DbHelper.getInstance(context.getApplicationContext());
	}

    // Get all 
    public Cursor getCursorAllSms() {
    	db = dbHelper.getReadableDatabase();
        return db.query(DbContract.SmsEntry.TN, DbContract.SmsEntry.allColumns, null, null, null, null, DbContract.SmsEntry.CN_NAME);
	}

    // Get all sorted
    public Cursor getCursorAllSmsSorted() {
        db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DbContract.SmsEntry.TN + " ORDER BY " + DbContract.SmsEntry.CN_NAME + " COLLATE NOCASE", null);
    }


    // Get by Id 
    public SmsEntity getCursorSmsById(int ind) {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.SmsEntry.TN, DbContract.SmsEntry.allColumns, DbContract.SmsEntry._ID + " = " + ind, null, null, null, null);
        cursor.moveToFirst();
        SmsEntity me = cursorToSms(cursor);
        cursor.close();
        return me;
	}

    // Get by name 
    public SmsEntity getCursorSmsByName(String name) {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.SmsEntry.TN, DbContract.SmsEntry.allColumns, DbContract.SmsEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        SmsEntity me = cursorToSms(cursor);
        cursor.close();
        return me;
	}

    // Create new entry
    public void createSms(SmsEntity smsEntity) {
    	db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.SmsEntry.CN_NAME, smsEntity.getName());
        values.put(DbContract.SmsEntry.CN_NUMBER, smsEntity.getNumber());
        values.put(DbContract.SmsEntry.CN_TEXT, smsEntity.getText());
        values.put(DbContract.MailEntry.CN_ENTER, smsEntity.isEnter() ? 1 : 0);
        values.put(DbContract.MailEntry.CN_EXIT, smsEntity.isExit() ? 1 : 0);

        db.insert(DbContract.SmsEntry.TN, null, values);
      }

    private SmsEntity cursorToSms(Cursor cursor) {
    	SmsEntity sms = new SmsEntity();
        sms.setId(cursor.getInt(0));
        sms.setName(cursor.getString(1));
        sms.setNumber(cursor.getString(2));
        sms.setText(cursor.getString(3));
        sms.setEnter(cursor.getInt(4) == 1);
        sms.setExit(cursor.getInt(5) == 1);

        return sms;
      }
    
    // Löschen mit ID
    public void deleteSms(String ind) {
    	db = dbHelper.getWritableDatabase();
	    db.delete(DbContract.SmsEntry.TN, DbContract.SmsEntry._ID + " = " + ind, null);
      }

    // Update
    private void updateSms(SmsEntity smsEntity) {
    	db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.SmsEntry.CN_NAME, smsEntity.getName());
        values.put(DbContract.SmsEntry.CN_NUMBER, smsEntity.getNumber());
        values.put(DbContract.SmsEntry.CN_TEXT, smsEntity.getText());
        values.put(DbContract.MailEntry.CN_ENTER, smsEntity.isEnter() ? 1 : 0);
        values.put(DbContract.MailEntry.CN_EXIT, smsEntity.isExit() ? 1 : 0);

        db.update(DbContract.SmsEntry.TN, values, DbContract.SmsEntry._ID + " = " + smsEntity.getId(), null);
      }

    // Check for double profile 
    private boolean smsProfileExists(String name) {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.SmsEntry.TN, DbContract.SmsEntry.allColumns, DbContract.SmsEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0 || cursor.getString(1) == null){
        	cursor.close();
        	return false;
        } 
        cursor.close();
        return true;
	}

    // Store: Wenn vorhanden ersetzen, ansonsten anlegen.
    public void storeSms(SmsEntity smsEntity) {
    	boolean exists = smsProfileExists(smsEntity.getName());
    	if (exists){
    		updateSms(smsEntity);
    	}else{
    		smsEntity.setId(0);
    		createSms(smsEntity);
    	}
    }
}