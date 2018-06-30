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
package de.egi.geofence.geozone.gcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.utils.Constants;

public class GcmRegistrationIntentService extends IntentService {

    private static final String TAG = "RegistIntentService";
    private static DbGlobalsHelper datasource;

    public GcmRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            datasource = new DbGlobalsHelper(this);
            String sender_id = datasource.getCursorGlobalsByKey(Constants.DB_KEY_GCM_SENDERID);

            if (TextUtils.isEmpty(sender_id)) return;

            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(sender_id, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "GCM Registration Token: " + token);

            saveRegistration(token);

            // Subscribe to topic channels
//            subscribeTopics(token);

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void saveRegistration(String token) {
        // Add custom implementation, as needed.
        // Persist the regID - no need to register again.

        String merkToken = getRegistrationId(this);

        datasource.storeGlobals(Constants.DB_KEY_GCM_REG_ID, token);
        String appVersion = getAppVersion(this);
        datasource.storeGlobals(Constants.DB_KEY_LASTINSTALLEDAPPLICATIONVERSION, appVersion);

        if (!merkToken.equals(token)) {
            // Reg-Id nach Zwischenablage
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("RegId to clipboard", token);
            clipboard.setPrimaryClip(clip);

            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, GcmTokenDialog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("de.egi.geofence.geozone.gcm.token", token);
            startActivity(intent);
        }
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static String getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public static String getRegistrationId(Context context) {
        datasource = new DbGlobalsHelper(context);
        String registrationId = datasource.getCursorGlobalsByKey(Constants.DB_KEY_GCM_REG_ID);

        if (TextUtils.isEmpty(registrationId)) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        String registeredVersion = datasource.getCursorGlobalsByKey(Constants.DB_KEY_LASTINSTALLEDAPPLICATIONVERSION) ;
        String currentVersion = getAppVersion(context);
        if (!registeredVersion.equals(currentVersion)) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
}
