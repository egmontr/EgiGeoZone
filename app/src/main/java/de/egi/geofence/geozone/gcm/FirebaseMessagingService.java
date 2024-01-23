/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.egi.geofence.geozone.gcm;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import de.egi.geofence.geozone.MainEgiGeoZone;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";
    private static DbGlobalsHelper datasource;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Nur wenn GCM auf true
        DbGlobalsHelper datasource = new DbGlobalsHelper(this);
        String doGcm = datasource.getCursorGlobalsByKey(Constants.DB_KEY_GCM);
        if (doGcm == null || doGcm.equalsIgnoreCase("false")){
            return;
        }

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "GCM from: " + remoteMessage.getFrom());

        // Message data payload: {source=gcmsend_fhem, contentText=ThermostatGarage '$tempUnten', gcmDeviceName=gcm, type=message, vibrate=false, contentTitle=ThermostatGarage, notifyId=1, tickerText=Temperaturkontrolle}

        // source = gcmsend_fhem
        // from = 1111111
        // type = notify oder message (notify = fhem-notify und message = benachrichtigung
        // vibrate = false
        // changes = sensor_value:21.9
        // deviceName = thHeizung
        // android.support.content.wakelockid = 5
        // collapse_key = do_not_collapse

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Intent rmIntent = remoteMessage.toIntent();
            Bundle extras = rmIntent.getExtras();

            if (!extras.isEmpty()) {
                String type = extras.getString("type");
                // normale Benachrichtigungen
                if ("message".equalsIgnoreCase(type)) {
                    handleMessage(extras);
                } else if ("notify".equalsIgnoreCase(type) || (type == null || type.trim().equals(""))) {
                    // Spezielle FHEM-Benachrichtigungen über Geräte-Status-Änderungen
                    handleNotify(extras);
                } else {
                    handleOthers(extras);
                }
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    @SuppressLint("UnspecifiedImmutableFlag")
    private void handleMessage(Bundle extras) {
        int notifyId = 100;
        try {
            if (extras.containsKey("notifyId")) {
                notifyId = Integer.parseInt(extras.getString("notifyId"));
            }
        } catch (Exception e) {
            Log.e(TAG, "invalid notify id: " + extras.getString("notifyId"));
        }
        Intent openIntent = new Intent(this, MainEgiGeoZone.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, notifyId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, notifyId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationUtil.notify(this, notifyId, pendingIntent,
                extras.getString("contentTitle"),
                extras.getString("contentText"),
                extras.getString("tickerText"), shouldVibrate(extras), shouldPlaySound(extras), R.drawable.cloud_email);
    }

    private boolean shouldVibrate(@NonNull Bundle extras) {
        return extras.containsKey("vibrate") && "true".equalsIgnoreCase(extras.getString("vibrate"));
    }

    private boolean shouldPlaySound(@NonNull Bundle extras) {
        return extras.containsKey("playSound") && "true".equalsIgnoreCase(extras.getString("playSound"));
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void handleNotify(Bundle extras) {
        int notifyId = 101;
        Intent openIntent = new Intent(this, MainEgiGeoZone.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, notifyId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, notifyId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationUtil.notify(this, notifyId, pendingIntent,
                "EgiFCM notify message",
                "From " + extras.getString("source") + "(" + extras.getString("from") + ")\n" + extras.getString("deviceName") + "-->" + extras.getString("changes"),
                extras.getString("deviceName") + "-->" + extras.getString("changes"),
                shouldVibrate(extras), shouldPlaySound(extras), R.drawable.cloud_email);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void handleOthers(Bundle extras) {
        int notifyId = 102;
        Intent openIntent = new Intent(this, MainEgiGeoZone.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, notifyId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, notifyId, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationUtil.notify(this, notifyId, pendingIntent,
                "EgiFCM other message",
                extras.toString(), "",
                shouldVibrate(extras), shouldPlaySound(extras), R.drawable.cloud_email);
    }


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed GCM token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(token);

        datasource = new DbGlobalsHelper(this);

//        String merkToken = getRegistrationId(this);

        datasource.storeGlobals(Constants.DB_KEY_GCM_REG_ID, token);
        String appVersion = getAppVersion(this);
        datasource.storeGlobals(Constants.DB_KEY_LASTINSTALLEDAPPLICATIONVERSION, appVersion);

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

    // [END on_new_token]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainEgiGeoZone.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.cloud_email)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        NotificationChannel channel = new NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
