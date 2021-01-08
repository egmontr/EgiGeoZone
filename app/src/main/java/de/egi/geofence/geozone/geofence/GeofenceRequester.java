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

package de.egi.geofence.geozone.geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.utils.Constants;

/**
 * Class for connecting to Location Services and requesting geofences.
 * <b>
 * Note: Clients must ensure that Google Play services is available before requesting geofences.
 * </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to check.
 *
 *
 * To use a GeofenceRequester, instantiate it and call AddGeofence(). Everything else is done
 * automatically.
 *
 */

// @@https://github.com/googlesamples/android-play-location/tree/master/Geofencing/app/src/main/java/com/google/android/gms/location/sample/geofencing

public class GeofenceRequester implements OnCompleteListener<Void> {

    // Storage for a reference to the calling client
    private final Context context;

    private GeofencingClient mGeofencingClient;

    // Stores the PendingIntent used to send geofence transitions back to the app
    private PendingIntent mGeofencePendingIntent;

	private final Logger log = Logger.getLogger(GeofenceRequester.class);

    public GeofenceRequester(Context activityContext) {
        // Save the context
        context = activityContext;

        // Initialize the globals to null
        mGeofencePendingIntent = null;

        mGeofencingClient = LocationServices.getGeofencingClient(context);
    }

    /**
     * Returns the current PendingIntent to the caller.
     *
     * @return The PendingIntent used to create the current set of geofences
     */
    public PendingIntent getRequestPendingIntent() {
        return createRequestPendingIntent();
    }

    /**
     * Start adding geofences. Save the geofences, then start adding them by requesting a
     * connection
     *
     * @param geofences A List of one or more geofences to add
     */
    public void addGeofences(List<Geofence> geofences) throws UnsupportedOperationException {
        log.debug("addGeofence");
        /*
         * Save the geofences so that they can be sent to Location Services once the
         * connection is available.
         */
        // Stores the current list of geofences
        ArrayList<Geofence> mCurrentGeofences = (ArrayList<Geofence>) geofences;
        GeofencingRequest gr = getGeofencingRequest(mCurrentGeofences);
        mGeofencePendingIntent = getRequestPendingIntent();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            log.debug("addGeofence: No Permission access fine location");
            return;
        }
        mGeofencingClient.addGeofences(gr, mGeofencePendingIntent).addOnCompleteListener(this);
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest(ArrayList<Geofence> mCurrentGeofences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mCurrentGeofences);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Get a PendingIntent to send with the request to add Geofences. Location Services issues
     * the Intent inside this PendingIntent whenever a geofence transition occurs for the current
     * list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent createRequestPendingIntent() {
        log.debug("createRequestPendingIntent");
        // If the PendingIntent already exists
        if (null != mGeofencePendingIntent) {
            // Return the existing intent
            return mGeofencePendingIntent;

        // If no PendingIntent exists
        } else {
            // Create an Intent pointing to the IntentService
        	Intent intent = new Intent(context, GeofenceReceiverService.class);
            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
        	
        	return PendingIntent.getService(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        	
        }
    }

    /**
     * @param task the resulting Task, containing either a result or error.
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {
		String toastMessage;
        String statusMessage = "";
        if (task.isSuccessful()) {
            toastMessage = context.getString(R.string.add_geofences_result_success);
            Log.d(Constants.APPTAG, toastMessage);
		} else {
            // Get the status code for the error and log it using a user-friendly message.
            if (task.getException().getMessage().contains("1000:")){
                statusMessage = "(1000) " + context.getString(R.string.geofence_not_available) + " (" + GeofenceStatusCodes.getStatusCodeString(1000) + ")";
            }
            if (task.getException().getMessage().contains("1001:")){
                statusMessage = "(1001 )" + context.getString(R.string.geofence_too_many_geofences) + " (" + GeofenceStatusCodes.getStatusCodeString(1001) + ")";
            }
            if (task.getException().getMessage().contains("1002:")){
                statusMessage = "(1002) " + context.getString(R.string.geofence_too_many_pending_intents)  + " (" + GeofenceStatusCodes.getStatusCodeString(1002) + ")";
            }
            toastMessage = context.getString(R.string.add_geofences_result_failure) + " StatusMessage: " + statusMessage;
            Log.e(Constants.APPTAG, toastMessage);
            log.error(toastMessage);
        }
		Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
	}
}
