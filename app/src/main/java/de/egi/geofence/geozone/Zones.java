package de.egi.geofence.geozone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.geofence.SimpleGeofence;
import de.egi.geofence.geozone.geofence.SimpleGeofenceStore;
import de.egi.geofence.geozone.utils.Constants;

public class Zones extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DbGlobalsHelper dbGlobalsHelper = new DbGlobalsHelper(this);
        // Instantiate a new geofence storage area
        SimpleGeofenceStore geofenceStore = new SimpleGeofenceStore(this);
        List<SimpleGeofence> geofences = geofenceStore.getGeofences();

        ArrayList<String> mCurrentGeofenceNames = new ArrayList<>();

        for (SimpleGeofence simpleGeofence : geofences) {
            mCurrentGeofenceNames.add(simpleGeofence.getId());
        }

        // Create intent to deliver the result data
        Intent result = new Intent();
        result.putStringArrayListExtra("allZoneNames", mCurrentGeofenceNames);

        setResult(Activity.RESULT_OK, result);
        finish();
    }
}
