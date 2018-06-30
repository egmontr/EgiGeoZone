package de.egi.geofence.geozone.utils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.egi.geofence.geozone.db.DbZoneHelper;

/**
 * Created by RitterE on 09.03.2017.
 */

public class ZonesContentProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse("content://de.egi.geofence.geozone.zonesContentProvider/zoneNames");

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] fields, @Nullable String where, @Nullable String[] whereArgs, @Nullable String sortOrder) {
        DbZoneHelper zoneHelper = new DbZoneHelper(getContext());
        // Get all zone names ordered by name
        Cursor c = zoneHelper.getCursorAllZoneNames();
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
