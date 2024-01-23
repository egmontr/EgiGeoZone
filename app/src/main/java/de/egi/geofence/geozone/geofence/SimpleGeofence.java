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

import com.google.android.gms.location.Geofence;

/**
 * A single Geofence object, defined by its center (latitude and longitude position) and radius.
 */
public class SimpleGeofence {
    // Instance variables
    private final String mId;
    private final String mLatitude;
    private final String mLongitude;
    private final String mRadius;
    private final String mAccuracy;
    private final long mExpirationDuration;
    private final int mTransitionType;
    private boolean status;
    private final String mAlias;

    public String getmAccuracy() {
        return mAccuracy;
    }

	/**
     * @param geofenceId The Geofence's request ID
     * @param latitude Latitude of the Geofence's center. The value is not checked for validity.
     * @param longitude Longitude of the Geofence's center. The value is not checked for validity.
     * @param radius Radius of the geofence circle. The value is not checked for validity
     * @param expiration Geofence expiration duration in milliseconds The value is not checked for
     * validity.
     * @param transition Type of Geofence transition. The value is not checked for validity.
     */
    public SimpleGeofence(
            String geofenceId,
            String latitude,
            String longitude,
            String radius,
            String accurracy,
            long expiration,
            int transition,
            boolean status,
            String alias
            ) {
        // Set the instance fields from the constructor
        // An identifier for the geofence
        this.mId = geofenceId;
        // Center of the geofence
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        // Radius of the geofence, in meters
        this.mRadius = radius;
        // Expiration time in milliseconds
        this.mExpirationDuration = expiration;
        // Transition type
        this.mTransitionType = transition;
        this.status = status;
        this.mAccuracy = accurracy;
        this.mAlias = alias;
    }
    

    // Instance field getters

    /**
     * Get the geofence ID
     * @return A SimpleGeofence ID
     */
    public String getId() {
        return mId;
    }

    public String getmAlias() {
        return mAlias;
    }

    /**
     * Get the geofence latitude
     * @return A latitude value
     */
    public String getLatitude() {
        return mLatitude;
    }

    /**
     * Get the geofence longitude
     * @return A longitude value
     */
    public String getLongitude() {
        return mLongitude;
    }

    /**
     * Get the geofence radius
     * @return A radius value
     */
    public String getRadius() {
        return mRadius;
    }

    /**
     * Get the geofence transition type
     * @return Transition type (see Geofence)
     */
    public int getTransitionType() {
        return mTransitionType;
    }

    /**
     * Creates a Location Services Geofence object from a
     * SimpleGeofence.
     *
     * @return A Geofence object
     */
    public Geofence toGeofence() {
        // Build a new Geofence object
        return new Geofence.Builder()
                       .setRequestId(getId())
                       .setTransitionTypes(mTransitionType)
                       .setCircularRegion(Double.parseDouble(getLatitude()), Double.parseDouble(getLongitude()), Float.parseFloat(getRadius()))
                       .setExpirationDuration(mExpirationDuration)
                       .build();
    }

	/**
	 * @return the status
	 */
	public boolean isStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}
}
