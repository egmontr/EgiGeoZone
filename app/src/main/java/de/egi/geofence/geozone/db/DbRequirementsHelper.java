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

public class DbRequirementsHelper {

    private final DbHelper dbHelper;
    private SQLiteDatabase db;

    public DbRequirementsHelper(Context context) {
        dbHelper = DbHelper.getInstance(context.getApplicationContext());
    }


    // Get all 
    public Cursor getCursorAllRequirements() {
        db = dbHelper.getReadableDatabase();
        return db.query(DbContract.RequirementsEntry.TN, DbContract.RequirementsEntry.allColumns, null, null, null, null, DbContract.RequirementsEntry.CN_NAME);
    }

    // Get all sorted
    public Cursor getCursorAllRequirementsSorted() {
        db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DbContract.RequirementsEntry.TN + " ORDER BY " + DbContract.RequirementsEntry.CN_NAME + " COLLATE NOCASE", null);
    }

    // Get by Id
    public RequirementsEntity getCursorRequirementsById(int ind) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbContract.RequirementsEntry.TN, DbContract.RequirementsEntry.allColumns, DbContract.RequirementsEntry._ID + " = " + ind, null, null, null, null);
        cursor.moveToFirst();
        RequirementsEntity re = cursorToRequirements(cursor);
        cursor.close();
        return re;
    }

    // Get by name
    public RequirementsEntity getCursorRequirementsByName(String name) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbContract.RequirementsEntry.TN, DbContract.RequirementsEntry.allColumns, DbContract.RequirementsEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        RequirementsEntity re = cursorToRequirements(cursor);
        cursor.close();
        return re;
    }

    // Create new entry
    public void createRequirements(RequirementsEntity requirementsEntity) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.RequirementsEntry.CN_NAME, requirementsEntity.getName());
        values.put(DbContract.RequirementsEntry.CN_ENTER_BT, requirementsEntity.getEnter_bt());
        values.put(DbContract.RequirementsEntry.CN_EXIT_BT, requirementsEntity.getExit_bt());
        values.put(DbContract.RequirementsEntry.CN_MON, requirementsEntity.isMon());
        values.put(DbContract.RequirementsEntry.CN_TUE, requirementsEntity.isTue());
        values.put(DbContract.RequirementsEntry.CN_WED, requirementsEntity.isWed());
        values.put(DbContract.RequirementsEntry.CN_THU, requirementsEntity.isThu());
        values.put(DbContract.RequirementsEntry.CN_FRI, requirementsEntity.isFri());
        values.put(DbContract.RequirementsEntry.CN_SAT, requirementsEntity.isSat());
        values.put(DbContract.RequirementsEntry.CN_SUN, requirementsEntity.isSun());

        db.insert(DbContract.RequirementsEntry.TN, null, values);
    }

    private RequirementsEntity cursorToRequirements(Cursor cursor) {
        RequirementsEntity req = new RequirementsEntity();
        req.setId(cursor.getInt(0));
        req.setName(cursor.getString(1));
        req.setEnter_bt(cursor.getString(2));
        req.setExit_bt(cursor.getString(3));
        req.setMon(cursor.getInt(4) == 1);
        req.setTue(cursor.getInt(5) == 1);
        req.setWed(cursor.getInt(6) == 1);
        req.setThu(cursor.getInt(7) == 1);
        req.setFri(cursor.getInt(8) == 1);
        req.setSat(cursor.getInt(9) == 1);
        req.setSun(cursor.getInt(10) == 1);

        return req;
    }

    // Löschen mit ID
    public void deleteRequirements(String ind) {
        db = dbHelper.getWritableDatabase();
        db.delete(DbContract.RequirementsEntry.TN, DbContract.RequirementsEntry._ID + " = " + ind, null);
    }

    // Update
    private void updateRequirements(RequirementsEntity requirementsEntity) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.RequirementsEntry.CN_NAME, requirementsEntity.getName());
        values.put(DbContract.RequirementsEntry.CN_ENTER_BT, requirementsEntity.getEnter_bt());
        values.put(DbContract.RequirementsEntry.CN_EXIT_BT, requirementsEntity.getExit_bt());
        values.put(DbContract.RequirementsEntry.CN_MON, requirementsEntity.isMon());
        values.put(DbContract.RequirementsEntry.CN_TUE, requirementsEntity.isTue());
        values.put(DbContract.RequirementsEntry.CN_WED, requirementsEntity.isWed());
        values.put(DbContract.RequirementsEntry.CN_THU, requirementsEntity.isThu());
        values.put(DbContract.RequirementsEntry.CN_FRI, requirementsEntity.isFri());
        values.put(DbContract.RequirementsEntry.CN_SAT, requirementsEntity.isSat());
        values.put(DbContract.RequirementsEntry.CN_SUN, requirementsEntity.isSun());

        db.update(DbContract.RequirementsEntry.TN, values, DbContract.RequirementsEntry._ID + " = " + requirementsEntity.getId(), null);

    }

    // Check for double profile 
    private boolean requirementsProfileExists(String name) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DbContract.RequirementsEntry.TN, DbContract.RequirementsEntry.allColumns, DbContract.RequirementsEntry.CN_NAME + " = '" + name + "'", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0 || cursor.getString(1) == null) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    // Store: Wenn vorhanden ersetzen, ansonsten anlegen.
    public void storeRequirements(RequirementsEntity requirementsEntity) {
        boolean exists = requirementsProfileExists(requirementsEntity.getName());
        if (exists) {
            updateRequirements(requirementsEntity);
        } else {
            requirementsEntity.setId(0);
            createRequirements(requirementsEntity);
        }
    }


}