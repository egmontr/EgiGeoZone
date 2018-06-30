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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.Utils;

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
public class GeofenceRemover implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    // Storage for a context from the calling client
    private final Context mContext;

    // Stores the current list of geofences
    private List<String> mCurrentGeofenceIds;

    // Stores the current instantiation of the location client
    private GoogleApiClient mLocationClient;

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

    /**
     * Construct a GeofenceRemover for the current Context
     *
     * @param context A valid Context
     */
    public GeofenceRemover(Context context) {
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

        // If a removal request is in progress, throw an exception
        } else {

            throw new UnsupportedOperationException();
        }
    }

    /**
     * Once the connection is available, send a request to remove the Geofences. The method
     * signature used depends on which type of remove request was originally received.
     */
    private void continueRemoveGeofences() {
    	PendingResult<Status> result = null; 
    	switch (mRequestType) {
            // If removeGeofencesByIntent was called
            case INTENT :
            	result = LocationServices.GeofencingApi.removeGeofences(mLocationClient, mCurrentIntent);
//            	mLocationClient.removeGeofences(mCurrentIntent, this);
                break;

            // If removeGeofencesById was called
            case LIST :
            	result = LocationServices.GeofencingApi.removeGeofences(mLocationClient, mCurrentGeofenceIds);
//                mLocationClient.removeGeofences(mCurrentGeofenceIds, this);
                break;
        }
        result.setResultCallback(this);
    }

    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    private void requestConnection() {
        getLocationClient().connect();
    }

    /**
     * Get the current location client, or create a new one if necessary.
     *
     * @return A LocationClient object
     */
    private GoogleApiClient getLocationClient() {
        if (mLocationClient == null) {

            mLocationClient = new GoogleApiClient.Builder(mContext)
            .addApi(LocationServices.API)         
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build(); 
        }
        return mLocationClient;
    }

    /**
     * When the request to remove geofences by PendingIntent returns, handle the result.
     *
     * @param statusCode the code returned by Location Services
     * @param requestIntent The Intent used to request the removal.
     */
//    @Override
//    public void onRemoveGeofencesByPendingIntentResult(int statusCode,
//            PendingIntent requestIntent) {
//
//        // Create a broadcast Intent that notifies other components of success or failure
//        Intent broadcastIntent = new Intent();
//
//        // If removing the geofences was successful
//        if (statusCode == LocationStatusCodes.SUCCESS) {
//
//            // In debug mode, log the result
//            Log.d(GeofenceUtils.APPTAG,
//                    mContext.getString(R.string.remove_geofences_intent_success));
//
//            // Set the action and add the result message
//            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);
//            broadcastIntent.putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS,
//                    mContext.getString(R.string.remove_geofences_intent_success));
//
//        // If removing the geocodes failed
//        } else {
//
//            // Always log the error
//            Log.e(GeofenceUtils.APPTAG,
//                    mContext.getString(R.string.remove_geofences_intent_failure, statusCode));
//
//            // Set the action and add the result message
//            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
//            broadcastIntent.putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS,
//                    mContext.getString(R.string.remove_geofences_intent_failure, statusCode));
//        }
//
//        // Broadcast the Intent to all components in this app
//        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);
//
//        // Disconnect the location client
//        requestDisconnection();
//    }
//
//    /**
//     * When the request to remove geofences by IDs returns, handle the result.
//     *
//     * @param statusCode The code returned by Location Services
//     * @param geofenceRequestIds The IDs removed
//     */
//    @Override
//    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
//
//        // Create a broadcast Intent that notifies other components of success or failure
//        Intent broadcastIntent = new Intent();
//
//        // Temp storage for messages
//        String msg;
//
//        // If removing the geocodes was successful
//        if (LocationStatusCodes.SUCCESS == statusCode) {
//
//            // Create a message containing all the geofence IDs removed.
//            msg = mContext.getString(R.string.remove_geofences_id_success,
//                    Arrays.toString(geofenceRequestIds));
//
//            // In debug mode, log the result
//            Log.d(GeofenceUtils.APPTAG, msg);
//
//            // Create an Intent to broadcast to the app
//            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED)
//                           .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
//                           .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
//
//        } else {
//        // If removing the geocodes failed
//
//            /*
//             * Create a message containing the error code and the list
//             * of geofence IDs you tried to remove
//             */
//            msg = mContext.getString(
//                    R.string.remove_geofences_id_failure,
//                    statusCode,
//                    Arrays.toString(geofenceRequestIds)
//            );
//
//            // Log an error
//            Log.e(GeofenceUtils.APPTAG, msg);
//
//            // Create an Intent to broadcast to the app
//            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR)
//                           .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
//                           .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
//        }
//
//        // Broadcast whichever result occurred
//        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);
//
//        // Disconnect the location client
//        requestDisconnection();
//    }

    /**
     * Get a location client and disconnect from Location Services
     */
    private void requestDisconnection() {

        // A request is no longer in progress
        mInProgress = false;

        getLocationClient().disconnect();
        /*
         * If the request was done by PendingIntent, cancel the Intent. This prevents problems if
         * the client gets disconnected before the disconnection request finishes; the location
         * updates will still be cancelled.
         */
        if (mRequestType == Constants.REMOVE_TYPE.INTENT) {
            mCurrentIntent.cancel();
        }

    }

    /*
     * Called by Location Services once the location client is connected.
     *
     * Continue by removing the requested geofences.
     */
    @Override
    public void onConnected(Bundle arg0) {
        // If debugging, log the connection
        Log.d(Constants.APPTAG, mContext.getString(R.string.connected));

        // Continue the request to remove the geofences
        continueRemoveGeofences();
    }

    /*
     * Implementation of OnConnectionFailedListener.onConnectionFailed
     * If a connection or disconnection request fails, report the error
     * connectionResult is passed in from Location Services
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // A request is no longer in progress
        mInProgress = false;

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {

            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult((Activity) mContext,
                    Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
            } catch (SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }

        /*
         * If no resolution is available, put the error code in
         * an error Intent and broadcast it back to the main Activity.
         * The Activity then displays an error dialog.
         * is out of date.
         */
        } else {

            Intent errorBroadcastIntent = new Intent(Constants.ACTION_CONNECTION_ERROR);
            errorBroadcastIntent.addCategory(Constants.CATEGORY_LOCATION_SERVICES)
                                .putExtra(Constants.EXTRA_CONNECTION_ERROR_CODE,
                                        connectionResult.getErrorCode());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(errorBroadcastIntent);
        }
    }

	@Override
	public void onResult(Status status) {
//        log.debug("onResult");
		
		String toastMessage = "";
        String statusMessage;
        switch (status.getStatusCode()){
            case 1000:
                statusMessage = mContext.getString(R.string.geofence_not_available) + " (" + GeofenceStatusCodes.getStatusCodeString(1000) + ")";
                break;
            case 1001:
                statusMessage = mContext.getString(R.string.geofence_too_many_geofences) + " (" + GeofenceStatusCodes.getStatusCodeString(1001) + ")";
                break;
            case 1002:
                statusMessage = mContext.getString(R.string.geofence_too_many_pending_intents)  + " (" + GeofenceStatusCodes.getStatusCodeString(1002) + ")";
                break;
            default:
                statusMessage = CommonStatusCodes.getStatusCodeString(status.getStatusCode())  + " (" + GeofenceStatusCodes.getStatusCodeString(status.getStatusCode()) + ")";
        }

    	switch (mRequestType) {
        // If removeGeofencesByIntent was called
        case INTENT :
    		if (status.isSuccess()) {
//                toastMessage = mContext.getString(R.string.remove_geofences_intent_success) + " StatusCode: " + status.getStatusCode() + " StatusMessage: " + (status.getStatusMessage() != null ? status.getStatusMessage() : "");
                toastMessage = mContext.getString(R.string.remove_geofences_intent_success) + " StatusCode: " + statusMessage;
                Log.d(Constants.APPTAG, toastMessage);
    		} else {
//                toastMessage = mContext.getString(R.string.remove_geofences_intent_failure) + " StatusCode: " + status.getStatusCode() + " StatusMessage: " + (status.getStatusMessage() != null ? status.getStatusMessage() : "");
                toastMessage = mContext.getString(R.string.remove_geofences_intent_failure) + " StatusCode: " + statusMessage;
                // Log an error
                Log.e(Constants.APPTAG, toastMessage);

                // Display UI and wait for user interaction
                AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(mContext);
                alertDialogBuilder.setMessage(statusMessage);
                alertDialogBuilder.setTitle("Geofence Error");

                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                });

                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
            break;

        // If removeGeofencesById was called
        case LIST :
    		if (status.isSuccess()) {
//                toastMessage = mContext.getString(R.string.remove_geofences_id_success) + " StatusCode: " + status.getStatusCode() + " StatusMessage: " + (status.getStatusMessage() != null ? status.getStatusMessage() : "");
                toastMessage = mContext.getString(R.string.remove_geofences_id_success) + " StatusCode: " + status.getStatusCode() + " StatusMessage: " + statusMessage;
                Log.d(Constants.APPTAG, toastMessage);
    		} else {
//                toastMessage = mContext.getString(R.string.remove_geofences_id_failure) + " StatusCode: " + status.getStatusCode() + " StatusMessage: " + (status.getStatusMessage() != null ? status.getStatusMessage() : "");
                toastMessage = mContext.getString(R.string.remove_geofences_id_failure) + " StatusCode: " + status.getStatusCode() + " StatusMessage: " + statusMessage;
                // Log an error
                Log.e(Constants.APPTAG, toastMessage);

                // Display UI and wait for user interaction
                AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(mContext);
                alertDialogBuilder.setMessage(statusMessage);
                alertDialogBuilder.setTitle("Geofence Error");

                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                });

                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
            break;
    	}
		
		Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();
		
		requestDisconnection();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Toast.makeText(mContext, "GoogleApiClient Connection Suspended", Toast.LENGTH_SHORT).show();
	}
}
