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

public class DbZoneHelper{

    private final Context context;

    private final DbHelper dbHelper;
    private SQLiteDatabase db;
    public DbZoneHelper(Context context) {
        dbHelper = DbHelper.getInstance(context.getApplicationContext());
        this.context = context;
    }

    // Get all by type
    public Cursor getCursorAllZone(String type) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor;
        if (type == null  || type.equals("")){
            cursor = db.query(DbContract.ZoneEntry.TN, DbContract.ZoneEntry.allColumns, null, null, null, null, null);
        }else{
            cursor = db.query(DbContract.ZoneEntry.TN, DbContract.ZoneEntry.allColumns, DbContract.ZoneEntry.CN_TYPE + " = '" + type + "'", null, null, null, null);
        }
        return cursor;
    }

    // Get all zone names ordered by name
    public Cursor getCursorAllZoneNames() {
        db = dbHelper.getReadableDatabase();
        Cursor cursor;
        cursor = db.query(DbContract.ZoneEntry.TN, DbContract.ZoneEntry.zoneNames, null, null, null, null, DbContract.ZoneEntry.CN_NAME);
        return cursor;
    }

    // Get by name
    public ZoneEntity getCursorZoneByName(String name) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbContract.ZoneEntry.TN, DbContract.ZoneEntry.allColumns, DbContract.ZoneEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0 || cursor.getString(1) == null){
            cursor.close();
            return null;
        }else {
            ZoneEntity zo = cursorToZone(cursor);
            cursor.close();
            return zo;
        }

    }

// --Commented out by Inspection START (17.08.2016 08:23):
//    // Get by Id
//    public ZoneEntity getCursorZoneById(int ind) {
//        db = dbHelper.getReadableDatabase();
//        Cursor cursor = db.query(DbContract.ZoneEntry.TN, DbContract.ZoneEntry.allColumns, DbContract.ZoneEntry._ID + " = " + ind, null, null, null, null);
//        cursor.moveToFirst();
//        ZoneEntity zo = cursorToZone(cursor);
//        cursor.close();
//        return zo;
//
//    }
// --Commented out by Inspection STOP (17.08.2016 08:23)

    // Create new entry
    public void createZone(ZoneEntity zoneEntity) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
//        values.put(DbContract.ZoneEntry.CN_ACTIV, ZoneEntity.isActiv() ? 1 : 0);
        values.put(DbContract.ZoneEntry.CN_NAME, zoneEntity.getName());
        values.put(DbContract.ZoneEntry.CN_LATITUDE, zoneEntity.getLatitude());
        values.put(DbContract.ZoneEntry.CN_LONGITUDE, zoneEntity.getLongitude());
        values.put(DbContract.ZoneEntry.CN_RADIUS, zoneEntity.getRadius());
        values.put(DbContract.ZoneEntry.CN_ID_EMAIL, zoneEntity.getId_email());
        values.put(DbContract.ZoneEntry.CN_ID_MORE_ACTIONS, zoneEntity.getId_more_actions());
        values.put(DbContract.ZoneEntry.CN_ID_REQUIREMENTS, zoneEntity.getId_requirements());
        values.put(DbContract.ZoneEntry.CN_LOCAL_TRACKING_INTERVAL, zoneEntity.getLocal_tracking_interval());
        values.put(DbContract.ZoneEntry.CN_TRACK_TO_FILE, zoneEntity.isTrack_to_file() ? 1 : 0);
        values.put(DbContract.ZoneEntry.CN_TRACK_ID_EMAIL, zoneEntity.getTrack_id_email());
        values.put(DbContract.ZoneEntry.CN_TRACK_TO_SERVER, zoneEntity.getTrack_url());
        values.put(DbContract.ZoneEntry.CN_ENTER_TRACKER, zoneEntity.isEnter_tracker() ? 1 : 0);
        values.put(DbContract.ZoneEntry.CN_EXIT_TRACKER, zoneEntity.isExit_tracker() ? 1 : 0);
        values.put(DbContract.ZoneEntry.CN_ID_SERVER, zoneEntity.getId_server());
        values.put(DbContract.ZoneEntry.CN_ID_SMS, zoneEntity.getId_sms());
        values.put(DbContract.ZoneEntry.CN_STATUS, zoneEntity.isStatus() ? 1 : 0);
        values.put(DbContract.ZoneEntry.CN_ACCURACY, zoneEntity.getAccuracy());
        values.put(DbContract.ZoneEntry.CN_TYPE, zoneEntity.getType());
        values.put(DbContract.ZoneEntry.CN_BEACON, zoneEntity.getBeacon());
        values.put(DbContract.ZoneEntry.CN_ALIAS, zoneEntity.getAlias());

        db.insert(DbContract.ZoneEntry.TN, null, values);
    }

    private ZoneEntity cursorToZone(Cursor cursor) {
        ZoneEntity zone = new ZoneEntity();
        zone.setId(cursor.getInt(0));
        zone.setName(cursor.getString(1));
        zone.setLatitude(cursor.getString(2));
        zone.setLongitude(cursor.getString(3));
        zone.setRadius(cursor.getInt(4));
        zone.setId_server(cursor.getString(5));
        zone.setId_sms(cursor.getString(6));
        zone.setId_email(cursor.getString(7));
        zone.setId_more_actions(cursor.getString(8));
        zone.setId_requirements(cursor.getString(9));
        zone.setLocal_tracking_interval(cursor.getInt(10));
        zone.setTrack_to_file(cursor.getInt(11) == 1);
        zone.setTrack_id_email(cursor.getString(12));
        zone.setTrack_url(cursor.getString(13));
        zone.setEnter_tracker(cursor.getInt(14) == 1);
        zone.setExit_tracker(cursor.getInt(15) == 1);
        zone.setStatus(cursor.getInt(16) == 1);
        zone.setAccuracy(cursor.getInt(17));
        zone.setType(cursor.getString(18));
        zone.setBeacon(cursor.getString(19));
        zone.setAlias(cursor.getString(20));

        ServerEntity serverEntity = null;
        SmsEntity smsEntity = null;
        MailEntity mailEntity = null;
        MoreEntity moreEntity = null;
        RequirementsEntity requirementsEntity = null;
        MailEntity mailTrackEntity = null;
        ServerEntity serverTrackEntity = null;

        DbServerHelper dbServerHelper = new DbServerHelper(context);
        DbSmsHelper dbSmsHelper = new DbSmsHelper(context);
        DbMailHelper dbMailHelper = new DbMailHelper(context);
        DbMoreHelper dbMoreHelper = new DbMoreHelper(context);
        DbRequirementsHelper dbRequirementsHelper = new DbRequirementsHelper(context);

        if (cursor.getString(5) != null) serverEntity = dbServerHelper.getCursorServerByName(cursor.getString(5));
        if (cursor.getString(6) != null) smsEntity = dbSmsHelper.getCursorSmsByName(cursor.getString(6));
        if (cursor.getString(7) != null) mailEntity = dbMailHelper.getCursorMailByName(cursor.getString(7));
        if (cursor.getString(8) != null) moreEntity = dbMoreHelper.getCursorMoreByName(cursor.getString(8));
        if (cursor.getString(9) != null) requirementsEntity = dbRequirementsHelper.getCursorRequirementsByName(cursor.getString(9));
        if (cursor.getString(12) != null) mailTrackEntity = dbMailHelper.getCursorMailByName(cursor.getString(12));
        if (cursor.getString(13) != null) serverTrackEntity = dbServerHelper.getCursorServerByName(cursor.getString(13));

        zone.setMailEntity(mailEntity);
        zone.setMoreEntity(moreEntity);
        zone.setRequirementsEntity(requirementsEntity);
        zone.setServerEntity(serverEntity);
        zone.setSmsEntity(smsEntity);
        zone.setTrackMailEntity(mailTrackEntity);
        zone.setTrackServerEntity(serverTrackEntity);

        return zone;
    }

    // Löschen mit Name
    public void deleteZone(String name) {
        db = dbHelper.getWritableDatabase();
        db.delete(DbContract.ZoneEntry.TN, DbContract.ZoneEntry.CN_NAME + " = '" + name + "'", null);
    }

    // Update
    public void updateZone(ZoneEntity zoneEntity) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.ZoneEntry.CN_NAME, zoneEntity.getName());
        values.put(DbContract.ZoneEntry.CN_LATITUDE, zoneEntity.getLatitude());
        values.put(DbContract.ZoneEntry.CN_LONGITUDE, zoneEntity.getLongitude());
        values.put(DbContract.ZoneEntry.CN_RADIUS, zoneEntity.getRadius());
        values.put(DbContract.ZoneEntry.CN_ID_EMAIL, zoneEntity.getId_email());
        values.put(DbContract.ZoneEntry.CN_ID_MORE_ACTIONS, zoneEntity.getId_more_actions());
        values.put(DbContract.ZoneEntry.CN_ID_REQUIREMENTS, zoneEntity.getId_requirements());
        values.put(DbContract.ZoneEntry.CN_LOCAL_TRACKING_INTERVAL, zoneEntity.getLocal_tracking_interval());
        values.put(DbContract.ZoneEntry.CN_TRACK_TO_FILE, zoneEntity.isTrack_to_file());
        values.put(DbContract.ZoneEntry.CN_TRACK_ID_EMAIL, zoneEntity.getTrack_id_email());
        values.put(DbContract.ZoneEntry.CN_TRACK_TO_SERVER, zoneEntity.getTrack_url());
        values.put(DbContract.ZoneEntry.CN_ENTER_TRACKER, zoneEntity.isEnter_tracker());
        values.put(DbContract.ZoneEntry.CN_EXIT_TRACKER, zoneEntity.isExit_tracker());
        values.put(DbContract.ZoneEntry.CN_ID_SERVER, zoneEntity.getId_server());
        values.put(DbContract.ZoneEntry.CN_ID_SMS, zoneEntity.getId_sms());
        values.put(DbContract.ZoneEntry.CN_ACCURACY, zoneEntity.getAccuracy());
        values.put(DbContract.ZoneEntry.CN_TYPE, zoneEntity.getType());
        values.put(DbContract.ZoneEntry.CN_BEACON, zoneEntity.getBeacon());
        values.put(DbContract.ZoneEntry.CN_ALIAS, zoneEntity.getAlias());

        // Soll hier nicht upgedatet werden, da der Status extra behandelt wird.
//        values.put(DbContract.ZoneEntry.CN_STATUS, zoneEntity.isStatus() ? 1 : 0);

        db.update(DbContract.ZoneEntry.TN, values, DbContract.ZoneEntry.CN_NAME+ " = '" + zoneEntity.getName() + "'", null);

    }

// --Commented out by Inspection START (23.12.2015 15:46):
//    // Update String field
//    public void updateZoneField(String zone, String field, String value ) {
//        db = dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(field, value);
//
//        db.update(DbContract.ZoneEntry.TN, values, DbContract.ZoneEntry.CN_NAME+ " = '" + zone + "'", null);
//
//    }
// --Commented out by Inspection STOP (23.12.2015 15:46)

// --Commented out by Inspection START (23.12.2015 15:46):
//    // Update Long/Integer field
//    public void updateZoneField(String zone, String field, long value ) {
//        db = dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(field, value);
//
//        int res = db.update(DbContract.ZoneEntry.TN, values, DbContract.ZoneEntry.CN_NAME+ " = '" + zone + "'", null);
//    }
// --Commented out by Inspection STOP (23.12.2015 15:46)

    // Update boolean field
    public void updateZoneField(String zone, String field, boolean value ) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(field, value  ? 1 : 0);

        db.update(DbContract.ZoneEntry.TN, values, DbContract.ZoneEntry.CN_NAME+ " = '" + zone + "'", null);

    }

    // Store: Wenn vorhanden ersetzen, ansonsten anlegen.
    public void storeZone(ZoneEntity zoneEntity) {
        boolean exists = zoneExists(zoneEntity.getName());
        if (exists){
            updateZone(zoneEntity);
        }else{
            zoneEntity.setId(0);
            createZone(zoneEntity);
        }
    }

    // Check for double zone
    private boolean zoneExists(String name) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbContract.ZoneEntry.TN, DbContract.ZoneEntry.allColumns, DbContract.ZoneEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0 || cursor.getString(1) == null){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }


}






















