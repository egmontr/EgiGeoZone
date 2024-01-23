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

package de.egi.geofence.geozone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.geofence.GeofenceRequester;
import de.egi.geofence.geozone.geofence.PathsenseGeofence;
import de.egi.geofence.geozone.geofence.SimpleGeofence;
import de.egi.geofence.geozone.geofence.SimpleGeofenceStore;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.Utils;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class StartupReceiver extends BroadcastReceiver {

    private final static LogConfigurator logConfigurator = new LogConfigurator();
    private final Logger log = Logger.getLogger(StartupReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {

        logConfigurator.setFileName(context.getFilesDir() + File.separator + "egigeozone" + File.separator + "egigeozone.log");
        logConfigurator.setUseFileAppender(true);
        logConfigurator.setRootLevel(Level.ERROR);
        // Set log level of a specific logger
        logConfigurator.setLevel("de.egi.geofence.geozone", Level.INFO);
        try {
            logConfigurator.configure();
            log.error("Logger set!");
            Log.i("", "Logger set!");
        } catch (Exception ignored) {
        }

        log.error("EgiGeoZone gestartet");
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)){
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                context.getApplicationContext().registerReceiver(this, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
            } else {
                // We are good, continue with adding geofences!
                log.error("call registerGeofencesAfterRebootOrUpdate: " + intent.getAction());
                registerGeofencesAfterRebootOrUpdate(context);
            }
        }

        if(intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)){
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                context.unregisterReceiver(this);
                // We got our GPS stuff up, add our geofences!
                log.error("call registerGeofencesAfterRebootOrUpdate: " + intent.getAction());
                registerGeofencesAfterRebootOrUpdate(context);
            }
        }
    }

    private void registerGeofencesAfterRebootOrUpdate(Context context) {
        log.error("in registerGeofencesAfterRebootOrUpdate");

        DbGlobalsHelper dbGlobalsHelper = new DbGlobalsHelper(context);
        dbGlobalsHelper.storeGlobals(Constants.DB_KEY_REBOOT, "true");

        // Instantiate a new geofence storage area
        SimpleGeofenceStore geofenceStore = new SimpleGeofenceStore(context);
        List<SimpleGeofence> geofences = geofenceStore.getGeofences();

        // Pathsense registrieren
        if (Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
            log.error("registerPathsenseAfterRebootOrUpdate");
            PathsenseGeofence pathsenseGeofence = new PathsenseGeofence(context);
            for (SimpleGeofence simpleGeofence : geofences) {
                pathsenseGeofence.addGeofence(simpleGeofence);
            }
        } else {
            // Geofences registrieren
            // Instantiate the current List of geofences
            List<Geofence> mCurrentGeofences = new ArrayList<>();
            // Instantiate a Geofence requester
            GeofenceRequester mGeofenceRequester = new GeofenceRequester(context);
            log.error("registerGeofencesAfterRebootOrUpdate - created mGeofenceRequester");

            for (SimpleGeofence simpleGeofence : geofences) {
                mCurrentGeofences.add(simpleGeofence.toGeofence());
                log.error("registerGeofencesAfterRebootOrUpdate - added geofence " + simpleGeofence.getId());
            }
            // Register all again onRestart/ReBoot
            try {
                // Try to add geofences
                if (mCurrentGeofences.size() > 0) {
                    mGeofenceRequester.addGeofences(mCurrentGeofences);
                    log.error("Geofences registered after reboot/update");
                }
            } catch (UnsupportedOperationException e) {
                // Notify user that previous request hasn't finished.
                Toast.makeText(context, R.string.add_geofences_already_requested_error, Toast.LENGTH_LONG).show();
                log.error("Error registering Geofence", e);
//            showError("Error registering Geofence", e.toString());
            }
        }
    }
}