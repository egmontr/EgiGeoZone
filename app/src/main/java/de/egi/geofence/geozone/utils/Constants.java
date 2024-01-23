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

package de.egi.geofence.geozone.utils;

/**
 * This class defines constants used by location sample apps.
 */
public final class Constants {

    public static final String ZONE 		= "${zone}";
    public static final String TRANSITION 	= "${transition}";
    public static final String TRANSITIONTYPE = "${transitionType}";
    public static final String LAT 			= "${latitude}";
    public static final String LNG          = "${longitude}";
    public static final String REALLAT 		= "${realLatitude}";
    public static final String REALLGN 		= "${realLongitude}";
    public static final String RADIUS		= "${radius}";
    public static final String DEVICEID	 	= "${deviceId}";
    public static final String ANDROIDID	= "${androidId}";
    public static final String ACCURACY 	= "${accuracy}";

    public static final String DATE 		= "${date}"; // Date as ISO
    public static final String LOCALDATE    = "${localDate}"; // local device date
    public static final String LOCATIONDATE = "${locationDate}"; // location date as ISO
    public static final String LOCALLOCATIONDATE = "${localLocationDate}"; // local location date from device

    public static final String NA 	= "N/A";

    public static final String GEOZONE = "G";
    public static final String BEACON = "B";

    public static final String FROM_GOOGLE = "G";
    public static final String FROM_PATHSENSE = "P";

    public static final int IGNORE_BATTERY_OPTIMIZATIONS_MIN_BUILD = 23;

    public static final String DB_KEY_THEME = "theme";
    public static final String DB_KEY_NEW_API = "newApi";
    public static final String DB_KEY_FALSE_POSITIVES = "falsePositives";
    public static final String DB_KEY_NOTIFICATION = "notification";
    public static final String DB_KEY_ERROR_NOTIFICATION = "errorNotification";
    public static final String DB_KEY_STICKY_NOTIFICATION = "stickyNotification";
    public static final String DB_KEY_BROADCAST = "broadcast";
    public static final String DB_KEY_GCM = "gcm";
    public static final String DB_KEY_GCM_LOGGING = "gcmLogging";
    public static final String DB_KEY_GCM_SENDERID = "senderId";
    public static final String DB_KEY_GCM_REG_ID = "gcmRegId";
    public static final String DB_KEY_LOCINTERVAL = "locInterval";
    public static final String DB_KEY_LOCPRIORITY = "locPriority";
    public static final String DB_KEY_LOG_LEVEL = "logLevel";
    public static final String DB_KEY_REBOOT = "reboot";
    public static final String DB_KEY_LASTINSTALLEDAPPLICATIONVERSION = "lastInstalledApplicationVersion";
    public static final String DB_KEY_BEACON_SCAN = "beacon_scan";
    public static final String DB_KEY_GUID = "guid";

    public static final String DB_KEY_MIGRATED_TO_DB = "migratedToDb";

    public static final String TEST_ZONE = "TestGeoZone";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // Used to track what type of geofence removal request was made.
    public enum REMOVE_TYPE {INTENT, LIST}

    // Used to track what type of request is in process
    public enum REQUEST_TYPE {ADD, REMOVE}

    /*
     * A log tag for the application
     */
    public static final String APPTAG = "EgiGeoZone";

    // Intent actions
    public static final String ACTION_STATUS_CHANGED = "de.egi.geofence.geozone.STATUS";
    public static final String ACTION_EGIGEOZONE_GETPLUGINS = "de.egi.geofence.geozone.GETPLUGINS";
    public static final String ACTION_EGIGEOZONE_PLUGIN_EVENT = "de.egi.geofence.geozone.plugin.EVENT";
    public static final String ACTION_DONOTDISTURB_OK = "de.egi.geofence.geozone.DONOTDISTURB.OK";
    public static final String ACTION_DONOTDISTURB_NOK = "de.egi.geofence.geozone.DONOTDISTURB.NOK";

    public static final String ACTION_TEST_STATUS_OK =
            "de.egi.geofence.geozone.TEST_STATUS_OK";

    public static final String ACTION_TEST_STATUS_NOK =
            "de.egi.geofence.geozone.TEST_STATUS_NOK";

    public static final String ACTION_CONNECTION_ERROR =
            "de.egi.geofence.geozone.ACTION_CONNECTION_ERROR";

    // The Intent category used by all Location Services sample apps
    public static final String CATEGORY_LOCATION_SERVICES =
            "de.egi.geofence.geozone.CATEGORY_LOCATION_SERVICES";

    // Keys for extended data in Intents
    public static final String EXTRA_CONNECTION_ERROR_CODE =
            "de.egi.geofence.geozone.EXTRA_CONNECTION_ERROR_CODE";
    // The prefix for flattened geofence keys
    public static final String KEY_PREFIX = "de.egi.geofence.geozone.KEY";
    /*
     * Constants used in verifying the correctness of input values
     */
    public static final double MAX_LATITUDE = 90.d;

    public static final double MIN_LATITUDE = -90.d;

    public static final double MAX_LONGITUDE = 180.d;

    public static final double MIN_LONGITUDE = -180.d;

    public static final float MIN_RADIUS = 1f;

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // A string of length 0, used to clear out input fields
    public static final String EMPTY_STRING = "";

    public static final CharSequence GEOFENCE_ID_DELIMITER = ",";

}
