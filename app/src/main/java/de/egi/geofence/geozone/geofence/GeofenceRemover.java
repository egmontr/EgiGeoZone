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

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.log4j.Logger;

import java.util.List;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.utils.Constants;

/**
 * Class for connecting to Location Services and removing geofences.
 * <p>
 * <b>
 * Note: Clients must ensure that Google Play services is available before removing geofences.
 * </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to check.
 * <p>
 * To use a GeofenceRemover, instantiate it, then call either RemoveGeofencesById() or
 * RemoveGeofencesByIntent(). Everything else is done automatically.
 *
 */
public class GeofenceRemover{

    // Storage for a context from the calling client
    private final Context mContext;

    // Stores the current list of geofences
    private List<String> mCurrentGeofenceIds;

    // Stores the current instantiation of the location client
    private GeofencingClient mLocationClient;

    // The PendingIntent sent in removeGeofencesByIntent
    private PendingIntent mCurrentIntent;

    /*
     *  Record the type of removal. This allows continueRemoveGeofences to call the appropriate
     *  removal request method.
     */
    private Constants.REMOVE_TYPE mRequestType;

    /*
     * Flag that indicates whether an add or remove request is underway. Check this
     * flag before attempting to start a new request.
     */
    private boolean mInProgress;

    private final Logger log = Logger.getLogger(GeofenceRemover.class);


    /**
     * Construct a GeofenceRemover for the current Context
     *
     * @param context A valid Context
     */
    public GeofenceRemover(Context context) {
        log.debug("removeGeofence");
        // Save the context
        mContext = context;

        // Initialize the globals to null
        mCurrentGeofenceIds = null;
        mLocationClient = null;
        mInProgress = false;
    }

    /**
     * Set the "in progress" flag from a caller. This allows callers to re-set a
     * request that failed but was later fixed.
     *
     * @param flag Turn the in progress flag on or off.
     */
    public void setInProgressFlag(boolean flag) {
        // Set the "In Progress" flag.
        mInProgress = flag;
    }

    /**
     * Remove the geofences in a list of geofence IDs. To remove all current geofences associated
     * with a request, you can also call removeGeofencesByIntent.
     * <p>
     * <b>Note: The List must contain at least one ID, otherwise an Exception is thrown</b>
     *
     * @param geofenceIds A List of geofence IDs
     */
    public void removeGeofencesById(List<String> geofenceIds) throws
        IllegalArgumentException, UnsupportedOperationException {
        // If the List is empty or null, throw an error immediately
        if ((null == geofenceIds) || (geofenceIds.size() == 0)) {
            throw new IllegalArgumentException();

        // Set the request type, store the List, and request a location client connection.
        } else {

            // If a removal request is not already in progress, continue
            if (!mInProgress) {
                mRequestType = Constants.REMOVE_TYPE.LIST;
                mCurrentGeofenceIds = geofenceIds;
                requestConnection();
                removeGeofences();
            // If a removal request is in progress, throw an exception
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    /**
     * Remove the geofences associated with a PendIntent. The PendingIntent is the one used
     * in the request to add the geofences; all geofences in that request are removed. To remove
     * a subset of those geofences, call removeGeofencesById().
     *
     * @param requestIntent The PendingIntent used to request the geofences
     */
    public void removeGeofencesByIntent(PendingIntent requestIntent) {

        // If a removal request is not in progress, continue
        if (!mInProgress) {
            // Set the request type, store the List, and request a location client connection.
            mRequestType = Constants.REMOVE_TYPE.INTENT;
            mCurrentIntent = requestIntent;
            requestConnection();
            removeGeofences();
        // If a removal request is in progress, throw an exception
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Once the connection is available, send a request to remove the Geofences. The method
     * signature used depends on which type of remove request was originally received.
     */
    private void removeGeofences() {
        final String[] toastMessage = {""};
    	switch (mRequestType) {
            // If removeGeofencesByIntent was called
            case INTENT :
            	mLocationClient.removeGeofences(mCurrentIntent)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Geofences removed
                                toastMessage[0] = mContext.getString(R.string.remove_geofences_intent_success);
                                Log.d(Constants.APPTAG, "OnSuccess Intent: " + toastMessage[0]);
                                log.debug("OnSuccess Intent: " + toastMessage[0]);
                                Toast.makeText(mContext, toastMessage[0], Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to remove geofences
                                toastMessage[0] = mContext.getString(R.string.remove_geofences_intent_failure) + " StatusMessage: " + e.getMessage();
                                Log.e(Constants.APPTAG, "OnFailure Intent: " + toastMessage[0]);
                                log.error("OnFailure Intent: " + toastMessage[0]);
                                Toast.makeText(mContext, toastMessage[0], Toast.LENGTH_SHORT).show();
                            }
                        });
                break;

            // If removeGeofencesById was called
            case LIST :
            	mLocationClient.removeGeofences(mCurrentGeofenceIds)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Geofences removed
                            toastMessage[0] = mContext.getString(R.string.remove_geofences_id_success);
                            Log.d(Constants.APPTAG, "OnSuccess IdList: " + toastMessage[0]);
                            log.debug ("OnSuccess IdList: " + toastMessage[0]);
                            Toast.makeText(mContext, toastMessage[0], Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to remove geofences
                            toastMessage[0] = mContext.getString(R.string.remove_geofences_id_failure) + " StatusMessage: " + e.getMessage();
                            Log.e(Constants.APPTAG, "OnFailure IdList: " + toastMessage[0]);
                            log.error("OnFailure IdList: " + toastMessage[0]);
                            Toast.makeText(mContext, toastMessage[0], Toast.LENGTH_SHORT).show();
                        }
                    });
                break;
        }
    }

    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void requestConnection() {
        getLocationClient();
    }

    /**
     * Get the current location client, or create a new one if necessary.
     *
     */
    private void getLocationClient() {
        if (mLocationClient == null) {
            mLocationClient = LocationServices.getGeofencingClient(mContext);
        }
    }
}
