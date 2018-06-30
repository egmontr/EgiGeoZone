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

public class DbServerHelper{
    private final DbHelper dbHelper;
    private SQLiteDatabase db;

    public DbServerHelper(Context context) {
        dbHelper = DbHelper.getInstance(context.getApplicationContext());
    }


    // Get all
    public Cursor getCursorAllServer() {
        db = dbHelper.getReadableDatabase();
        return db.query(DbContract.ServerEntry.TN, DbContract.ServerEntry.allColumns, null, null, null, null, DbContract.ServerEntry.CN_NAME);
    }

    // Get all sorted
    public Cursor getCursorAllServerSorted() {
        db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DbContract.ServerEntry.TN + " ORDER BY " + DbContract.ServerEntry.CN_NAME + " COLLATE NOCASE", null);
    }

    // Get by Id
    public ServerEntity getCursorServerById(int ind) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbContract.ServerEntry.TN, DbContract.ServerEntry.allColumns, DbContract.ServerEntry._ID + " = " + ind, null, null, null, null);
        cursor.moveToFirst();
        ServerEntity me = cursorToServer(cursor);
        cursor.close();
        return me;
    }

    // Get by name
    public ServerEntity getCursorServerByName(String name) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbContract.ServerEntry.TN, DbContract.ServerEntry.allColumns, DbContract.ServerEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        ServerEntity me = cursorToServer(cursor);
        cursor.close();
        return me;
    }

    // Create new entry
    public void createServer(ServerEntity serverEntity) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.ServerEntry.CN_NAME, serverEntity.getName());
        values.put(DbContract.ServerEntry.CN_CA_CERT, serverEntity.getCa_cert());
        values.put(DbContract.ServerEntry.CN_CERT, serverEntity.getCert());
        values.put(DbContract.ServerEntry.CN_CERT_PASSWORD, serverEntity.getCert_password());
        values.put(DbContract.ServerEntry.CN_TIMEOUT, serverEntity.getTimeout());
        values.put(DbContract.ServerEntry.CN_URL_ENTER, serverEntity.getUrl_enter());
        values.put(DbContract.ServerEntry.CN_URL_EXIT, serverEntity.getUrl_exit());
        values.put(DbContract.ServerEntry.CN_URL_FHEM, serverEntity.getUrl_fhem());
        values.put(DbContract.ServerEntry.CN_URL_TRACKING, serverEntity.getUrl_tracking());
        values.put(DbContract.ServerEntry.CN_USER, serverEntity.getUser());
        values.put(DbContract.ServerEntry.CN_USER_PW, serverEntity.getUser_pw());
        values.put(DbContract.ServerEntry.CN_ID_FALLBACK, serverEntity.getId_fallback());

        db.insert(DbContract.ServerEntry.TN, null, values);
    }

    private ServerEntity cursorToServer(Cursor cursor) {
        if(cursor.getCount() == 0) return null;
        ServerEntity server = new ServerEntity();
        server.setId(cursor.getInt(0));
        server.setName(cursor.getString(1));
        server.setUrl_fhem(cursor.getString(2));
        server.setUrl_enter(cursor.getString(3));
        server.setUrl_exit(cursor.getString(4));
        server.setUser(cursor.getString(5));
        server.setUser_pw(cursor.getString(6));
        server.setCert(cursor.getString(7));
        server.setCert_password(cursor.getString(8));
        server.setCa_cert(cursor.getString(9));
        server.setTimeout(cursor.getString(10));
        server.setId_fallback(cursor.getString(11));
        server.setUrl_tracking(cursor.getString(12));

        return server;
    }

    // Löschen mit ID
    public void deleteServer(String ind) {
        db = dbHelper.getWritableDatabase();
        db.delete(DbContract.ServerEntry.TN, DbContract.ServerEntry._ID + " = " + ind, null);
    }

    // Update
    private void updateServer(ServerEntity serverEntity) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.ServerEntry.CN_NAME, serverEntity.getName());
        values.put(DbContract.ServerEntry.CN_CA_CERT, serverEntity.getCa_cert());
        values.put(DbContract.ServerEntry.CN_CERT, serverEntity.getCert());
        values.put(DbContract.ServerEntry.CN_CERT_PASSWORD, serverEntity.getCert_password());
        values.put(DbContract.ServerEntry.CN_TIMEOUT, serverEntity.getTimeout());
        values.put(DbContract.ServerEntry.CN_URL_ENTER, serverEntity.getUrl_enter());
        values.put(DbContract.ServerEntry.CN_URL_EXIT, serverEntity.getUrl_exit());
        values.put(DbContract.ServerEntry.CN_URL_FHEM, serverEntity.getUrl_fhem());
        values.put(DbContract.ServerEntry.CN_URL_TRACKING, serverEntity.getUrl_tracking());
        values.put(DbContract.ServerEntry.CN_USER, serverEntity.getUser());
        values.put(DbContract.ServerEntry.CN_USER_PW, serverEntity.getUser_pw());
        values.put(DbContract.ServerEntry.CN_ID_FALLBACK, serverEntity.getId_fallback());

        db.update(DbContract.ServerEntry.TN, values, DbContract.ServerEntry._ID + " = " + serverEntity.getId(), null);
    }

    // Check for double profile
    private boolean serverProfileExists(String name) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbContract.ServerEntry.TN, DbContract.ServerEntry.allColumns, DbContract.ServerEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0 || cursor.getString(1) == null){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    // Store: Wenn vorhanden ersetzen, ansonsten anlegen.
    public void storeServer(ServerEntity serverEntity) {
        boolean exists = serverProfileExists(serverEntity.getName());
        if (exists){
            updateServer(serverEntity);
        }else{
            serverEntity.setId(0);
            createServer(serverEntity);
        }
    }

}