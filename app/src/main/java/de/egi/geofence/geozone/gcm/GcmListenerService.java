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

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import de.egi.geofence.geozone.GcmLog;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.utils.NotificationUtil;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    private static final String TAG = "EgiGeoZone GCM";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param extras Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle extras) {
        String message = extras.getString("message");

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

//        if (from.startsWith("/topics/")) {
//            // message received from some topic.
//        } else {
//            // normal downstream message.
//        }

        Log.i(TAG, "Received: " + extras.toString());
        if (!extras.containsKey("type")	|| !extras.containsKey("source")) {
            Log.i(TAG, "received GCM message, but doesn't fit required fields");
            return;
        }
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

        // source = gcmsend_fhem
        // from = 1111111
        // type = notify oder message (notify = fhem-notify und message = benachrichtigung
        // vibrate = false
        // changes = sensor_value:21.9
        // deviceName = thHeizung
        // android.support.content.wakelockid = 5
        // collapse_key = do_not_collapse

    }

    @Override
    public void onDeletedMessages() {
        systemMessage("Deleted messages on server");
    }

    @Override
    public void onSendError(String msgId, String error) {
        systemMessage("Send error: " + error);
    }


    private void systemMessage(String extras) {
        int notifyId = 99;
        Intent openIntent = new Intent(this, GcmLog.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notifyId,	openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationUtil.notify(this, notifyId, pendingIntent,
                "EgiGeoZone GCM message",
                extras,
                "EgiGeoZone GCM message",
                true, true, R.drawable.cloud_email);
    }
    private void handleMessage(Bundle extras) {
        int notifyId = 100;
        try {
            if (extras.containsKey("notifyId")) {
                notifyId = Integer.valueOf(extras.getString("notifyId"));
            }
        } catch (Exception e) {
            Log.e(TAG, "invalid notify id: " + extras.getString("notifyId"));
        }
        Intent openIntent = new Intent(this, GcmLog.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notifyId,	openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationUtil.notify(this, notifyId, pendingIntent,
                extras.getString("contentTitle"),
                extras.getString("contentText"),
                extras.getString("tickerText"), shouldVibrate(extras), shouldPlaySound(extras), R.drawable.cloud_email);
    }

    private boolean shouldVibrate(Bundle extras) {
        return extras.containsKey("vibrate") && "true".equalsIgnoreCase(extras.getString("vibrate"));
    }

    private boolean shouldPlaySound(Bundle extras) {
        return extras.containsKey("playSound") && "true".equalsIgnoreCase(extras.getString("playSound"));
    }

    private void handleNotify(Bundle extras) {
        int notifyId = 101;
        Intent openIntent = new Intent(this, GcmLog.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notifyId,	openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationUtil.notify(this, notifyId, pendingIntent,
                "EgiGeoZone GCM notify message",
                "From " + extras.getString("source") + "(" + extras.getString("from") + ")\n" + extras.getString("deviceName") + "-->" + extras.getString("changes"),
                extras.getString("deviceName") + "-->" + extras.getString("changes"),
                shouldVibrate(extras), shouldPlaySound(extras), R.drawable.cloud_email);
    }

    private void handleOthers(Bundle extras) {
        int notifyId = 102;
        Intent openIntent = new Intent(this, GcmLog.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notifyId,	openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationUtil.notify(this, notifyId, pendingIntent,
                "EgiGeoZone other GCM message",
                extras.toString(), "",
                shouldVibrate(extras), shouldPlaySound(extras), R.drawable.cloud_email);
    }
}
