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

public class DbMailHelper {
	private final DbHelper dbHelper;
	private SQLiteDatabase db;

	public DbMailHelper(Context context) {
		dbHelper = DbHelper.getInstance(context.getApplicationContext());
	}
	
    // Get all 
    public Cursor getCursorAllMail() {
    	db = dbHelper.getReadableDatabase();
        return db.query(DbContract.MailEntry.TN, DbContract.MailEntry.allColumns, null, null, null, null, DbContract.MailEntry.CN_NAME);
	}

    // Get all sorted
    public Cursor getCursorAllMailSorted() {
        db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DbContract.MailEntry.TN + " ORDER BY " + DbContract.MailEntry.CN_NAME + " COLLATE NOCASE", null);
    }

    // Get by Id 
    public MailEntity getCursorMailById(int ind) {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.MailEntry.TN, DbContract.MailEntry.allColumns, DbContract.MailEntry._ID + " = " + ind, null, null, null, null);
        cursor.moveToFirst();
        MailEntity me = cursorToMail(cursor);
        cursor.close();
        return me;

	}

    // Get by name 
    public MailEntity getCursorMailByName(String name) {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.MailEntry.TN, DbContract.MailEntry.allColumns, DbContract.MailEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        MailEntity me = cursorToMail(cursor);
        cursor.close();
        return me;

	}

    // Create new entry
    public void createMail(MailEntity mailEntity) {
    	db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.MailEntry.CN_FROM, mailEntity.getFrom());
        values.put(DbContract.MailEntry.CN_NAME, mailEntity.getName());
        values.put(DbContract.MailEntry.CN_SMTP_PORT, mailEntity.getSmtp_port());
        values.put(DbContract.MailEntry.CN_SMTP_PW, mailEntity.getSmtp_pw());
        values.put(DbContract.MailEntry.CN_SMTP_SERVER, mailEntity.getSmtp_server());
        values.put(DbContract.MailEntry.CN_SMTP_USER, mailEntity.getSmtp_user());
        values.put(DbContract.MailEntry.CN_SSL, mailEntity.isSsl() ? 1 : 0);
        values.put(DbContract.MailEntry.CN_SUBJECT, mailEntity.getSubject());
        values.put(DbContract.MailEntry.CN_BODY, mailEntity.getBody());
        values.put(DbContract.MailEntry.CN_TO, mailEntity.getTo());
        values.put(DbContract.MailEntry.CN_ENTER, mailEntity.isEnter() ? 1 : 0);
        values.put(DbContract.MailEntry.CN_EXIT, mailEntity.isExit() ? 1 : 0);
        values.put(DbContract.MailEntry.CN_STARTTLS, mailEntity.isStarttls() ? 1 : 0);

        db.insert(DbContract.MailEntry.TN, null, values);
      }

    private MailEntity cursorToMail(Cursor cursor) {
    	MailEntity me = new MailEntity();
        me.setId(cursor.getInt(0));
        me.setName(cursor.getString(1));
        me.setSmtp_user(cursor.getString(2));
        me.setSmtp_pw(cursor.getString(3));
        me.setSmtp_server(cursor.getString(4));
        me.setSmtp_port(cursor.getString(5));
        me.setFrom(cursor.getString(6));
        me.setTo(cursor.getString(7));
        me.setSubject(cursor.getString(8));
        me.setBody(cursor.getString(9));
        me.setSsl(cursor.getInt(10) == 1);
        me.setEnter(cursor.getInt(11) == 1);
        me.setExit(cursor.getInt(12) == 1);
        me.setStarttls(cursor.getInt(13) == 1);

        return me;
      }
    
    // Löschen mit ID
    public void deleteMail(String ind) {
    	db = dbHelper.getWritableDatabase();
	    db.delete(DbContract.MailEntry.TN, DbContract.MailEntry._ID + " = " + ind, null);
      }

    // Update
    private void updateMail(MailEntity mailEntity) {
    	db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.MailEntry.CN_BODY, mailEntity.getBody());
        values.put(DbContract.MailEntry.CN_FROM, mailEntity.getFrom());
        values.put(DbContract.MailEntry.CN_NAME, mailEntity.getName());
        values.put(DbContract.MailEntry.CN_SMTP_PORT, mailEntity.getSmtp_port());
        values.put(DbContract.MailEntry.CN_SMTP_PW, mailEntity.getSmtp_pw());
        values.put(DbContract.MailEntry.CN_SMTP_SERVER, mailEntity.getSmtp_server());
        values.put(DbContract.MailEntry.CN_SMTP_USER, mailEntity.getSmtp_user());
        values.put(DbContract.MailEntry.CN_SSL, mailEntity.isSsl() ? 1 : 0);
        values.put(DbContract.MailEntry.CN_SUBJECT, mailEntity.getSubject());
        values.put(DbContract.MailEntry.CN_TO, mailEntity.getTo());
        values.put(DbContract.MailEntry.CN_ENTER, mailEntity.isEnter() ? 1 : 0);
        values.put(DbContract.MailEntry.CN_EXIT, mailEntity.isExit() ? 1 : 0);
        values.put(DbContract.MailEntry.CN_STARTTLS, mailEntity.isStarttls() ? 1 : 0);

        db.update(DbContract.MailEntry.TN, values, DbContract.MailEntry._ID + " = " + mailEntity.getId(), null);
      }

    // Check for double profile 
    private boolean mailProfileExists(String name) {
    	db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DbContract.MailEntry.TN, DbContract.MailEntry.allColumns, DbContract.MailEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0 || cursor.getString(1) == null){
        	cursor.close();
        	return false;
        } 
        cursor.close();
        return true;
	}

    // Store: Wenn vorhanden ersetzen, ansonsten anlegen.
    public void storeMail(MailEntity mailEntity) {
    	boolean exists = mailProfileExists(mailEntity.getName());
    	if (exists){
    		updateMail(mailEntity);
    	}else{
    		mailEntity.setId(0);
    		createMail(mailEntity);
    	}
    }
}