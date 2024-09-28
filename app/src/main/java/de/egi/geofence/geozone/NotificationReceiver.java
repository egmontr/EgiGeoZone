package de.egi.geofence.geozone;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.egi.geofence.geozone.utils.Constants;

public class NotificationReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action_name = intent.getAction();
        if (action_name.equals(Constants.ACTION_DONOTDISTURB_OK)) {
            context.startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        }
        // Get an instance of the Notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(222);
//        context.unregisterReceiver(myReceiver);

    }
}
