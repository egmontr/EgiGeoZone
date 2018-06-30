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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    // --Commented out by Inspection (23.12.2015 15:26):private SQLiteDatabase db;
    private static DbHelper sInstance;
    public static synchronized DbHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DbHelper(Context context) {
        super(context, DbContract.DATABASE_NAME, null, DbContract.DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbContract.ServerEntry.CREATE_TABLE);
        db.execSQL(DbContract.SmsEntry.CREATE_TABLE);
        db.execSQL(DbContract.MailEntry.CREATE_TABLE);
        db.execSQL(DbContract.MoreEntry.CREATE_TABLE);
        db.execSQL(DbContract.RequirementsEntry.CREATE_TABLE);
        db.execSQL(DbContract.ZoneEntry.CREATE_TABLE);
        db.execSQL(DbContract.GlobalsEntry.CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Neue Felder hinzufügen

        try {
            db.execSQL(DbContract.MoreEntry.ALTER_TABLE_ADD_ENTER_SOUND_MM); // in 270 eingeführt
        }catch(Exception ignored){}
        try {
            db.execSQL(DbContract.MoreEntry.ALTER_TABLE_ADD_EXIT_SOUND_MM); // in 270 eingeführt
        }catch(Exception ignored){}

        try {
            db.execSQL(DbContract.MailEntry.ALTER_TABLE_ADD_STARTTLS); // in 215 eingeführt
        }catch(Exception ignored){}


            try {
                db.execSQL(DbContract.ZoneEntry.ALTER_TABLE_ADD_ACCURACY); // 204 und in 205 entfernt und in 206 wieder eingeführt
            }catch(Exception ignored){}
            try {
                db.execSQL(DbContract.ZoneEntry.ALTER_TABLE_ADD_TYPE); // 220
                // Initialize Type to G, when NULL
                ContentValues values = new ContentValues();
                values.put(DbContract.ZoneEntry.CN_TYPE, "G");
                db.update(DbContract.ZoneEntry.TN, values, DbContract.ZoneEntry.CN_TYPE + " IS NULL", null);
            }catch(Exception ignored){}
            try {
                db.execSQL(DbContract.ZoneEntry.ALTER_TABLE_ADD_BEACON); // 220
            }catch(Exception ignored){}
            try {
                db.execSQL(DbContract.ServerEntry.ALTER_TABLE_ADD_FALLBACK); // 205
            }catch(Exception ignored){}
            try {
                db.execSQL(DbContract.ZoneEntry.ALTER_TABLE_ADD_ALIAS); // 223 Beta
            }catch(Exception ignored){}
            try {
                db.execSQL(DbContract.ServerEntry.ALTER_TABLE_ADD_URL_TRACKING); // 233 Beta
            }catch(Exception ignored){}
        }

    public void dropAndCreate(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbContract.ZoneEntry.DELETE_TABLE);
        db.execSQL(DbContract.ServerEntry.DELETE_TABLE);
        db.execSQL(DbContract.SmsEntry.DELETE_TABLE);
        db.execSQL(DbContract.MailEntry.DELETE_TABLE);
        db.execSQL(DbContract.MoreEntry.DELETE_TABLE);
        db.execSQL(DbContract.RequirementsEntry.DELETE_TABLE);
        db.execSQL(DbContract.GlobalsEntry.DELETE_TABLE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    /**
     * @return the db
     */
//    public SQLiteDatabase getDb() {
//        return db;
//    }
}