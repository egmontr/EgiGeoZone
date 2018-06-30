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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;

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
public class GeofenceRequester implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    // Storage for a reference to the calling client
    private final Context context;

    // Stores the PendingIntent used to send geofence transitions back to the app
    private PendingIntent mGeofencePendingIntent;

    // Stores the current list of geofences
    private ArrayList<Geofence> mCurrentGeofences;

    // Stores the current instantiation of the location client
    private GoogleApiClient mLocationClient;
	private final Logger log = Logger.getLogger(GeofenceRequester.class);
    /*
     * Flag that indicates whether an add or remove request is underway. Check this
     * flag before attempting to start a new request.
     */
    private boolean mInProgress;

    public GeofenceRequester(Context activityContext) {
        // Save the context
        context = activityContext;

        // Initialize the globals to null
        mGeofencePendingIntent = null;
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
        log.debug("setInProgressFlag");
    // Set the "In Progress" flag.
        mInProgress = flag;
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
        mCurrentGeofences = (ArrayList<Geofence>) geofences;

        // If a request is not already in progress
        if (!mInProgress) {
            log.info("addGeofence: not in progress");
            // Toggle the flag and continue
            mInProgress = true;

            // Request a connection to Location Services
            requestConnection();

        // If a request is in progress
        } else {
            log.error("addGeofence: in progress. Stop the request");
            // Throw an exception and stop the request
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void requestConnection() {
        log.debug("requestConnection");
        getLocationClient().connect();
    }

    /**
     * Get the current location client, or create a new one if necessary.
     *
     * @return A LocationClient object
     */
    private GoogleApiClient getLocationClient() {
        if (mLocationClient == null) {
            mLocationClient = new GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)         
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build(); 
        }
        return mLocationClient;

    }
    /**
     * Once the connection is available, send a request to add the Geofences
     */
    private void continueAddGeofences() {
        log.debug("continueAddGeofences");
        // Get a PendingIntent that Location Services issues when a geofence transition occurs
        mGeofencePendingIntent = createRequestPendingIntent();
        // Send a request to add the current geofences
        PendingResult<Status> result;
        try{
//            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder().addGeofence(mCurrentGeofences).setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).build();
            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder().addGeofences(mCurrentGeofences).build();
            result = LocationServices.GeofencingApi.addGeofences(mLocationClient, geofencingRequest, mGeofencePendingIntent);
        }catch(SecurityException se){
            String message = "SecurityException: " + se.getLocalizedMessage();
            Log.e(Constants.APPTAG, message);
            log.error(message);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            NotificationUtil.showError(context, "SecurityException", se.getLocalizedMessage() + " - Set App permissions! (see also log file)");
            return;
        }
        result.setResultCallback(this);
    }

    /**
     * Get a location client and disconnect from Location Services
     */
    private void requestDisconnection() {
        log.debug("requestDisconnection");
        // A request is no longer in progress
        mInProgress = false;
        getLocationClient().disconnect();
    }

    /*
     * Called by Location Services once the location client is connected.
     *
     * Continue by adding the requested geofences.
     */
    @Override
    public void onConnected(Bundle arg0) {
        // If debugging, log the connection
        Log.d(Constants.APPTAG, context.getString(R.string.connected));
        log.debug("onConnected");
        // Continue adding the geofences
        continueAddGeofences();
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
//            Intent intent = new Intent(context, ReceiveTransitionsIntentService.class);
        	Intent intent = new Intent("de.egi.geofence.geozone.ACTION_RECEIVE_GEOFENCE");
            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
//            return PendingIntent.getService(
//                    context,
//                    0,
//                    intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT);
        	
        	return PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        	
        }
    }

    /*
     * Implementation of OnConnectionFailedListener.onConnectionFailed
     * If a connection or disconnection request fails, report the error
     * connectionResult is passed in from Location Services
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        log.debug("onConnectionFailed");
        // Turn off the request flag
        mInProgress = false;
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (!connectionResult.hasResolution()) {

            Intent errorBroadcastIntent = new Intent(Constants.ACTION_CONNECTION_ERROR);
            errorBroadcastIntent.addCategory(Constants.CATEGORY_LOCATION_SERVICES)
                                .putExtra(Constants.EXTRA_CONNECTION_ERROR_CODE,
                                        connectionResult.getErrorCode());
            LocalBroadcastManager.getInstance(context).sendBroadcast(errorBroadcastIntent);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    	Toast.makeText(context, "GoogleApiClient Connection Suspended", Toast.LENGTH_SHORT).show();
    }

	@Override
	public void onResult(@NonNull Status status) {
        log.debug("onResult");
        String statusMessage;
        switch (status.getStatusCode()){
            case 1000:
                statusMessage = context.getString(R.string.geofence_not_available) + " (" + GeofenceStatusCodes.getStatusCodeString(1000) + ")";
                break;
            case 1001:
                statusMessage = context.getString(R.string.geofence_too_many_geofences) + " (" + GeofenceStatusCodes.getStatusCodeString(1001) + ")";
                break;
            case 1002:
                statusMessage = context.getString(R.string.geofence_too_many_pending_intents)  + " (" + GeofenceStatusCodes.getStatusCodeString(1002) + ")";
                break;
            default:
                statusMessage = CommonStatusCodes.getStatusCodeString(status.getStatusCode())  + " (" + GeofenceStatusCodes.getStatusCodeString(status.getStatusCode()) + ")";
        }
		String toastMessage;
		if (status.isSuccess()) {
            toastMessage = context.getString(R.string.add_geofences_result_success) + " StatusCode: " + status.getStatusCode() + " StatusMessage: " + statusMessage;
            Log.d(Constants.APPTAG, toastMessage);

		} else {
            toastMessage = context.getString(R.string.add_geofences_result_failure) + " StatusCode: " + status.getStatusCode() + " StatusMessage: " + statusMessage;
            Log.e(Constants.APPTAG, toastMessage);
            log.error(toastMessage);
        }
		Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
		requestDisconnection();
	}
}
