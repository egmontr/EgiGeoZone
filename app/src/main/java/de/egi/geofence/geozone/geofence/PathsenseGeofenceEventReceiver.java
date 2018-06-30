package de.egi.geofence.geozone.geofence;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.apache.log4j.Logger;

/**
 * Created by egmontr on 21.06.2016.
 */
public class PathsenseGeofenceEventReceiver extends WakefulBroadcastReceiver
{
    private final Logger log = Logger.getLogger(PathsenseGeofenceEventReceiver.class);
    @Override
    public void onReceive(Context context, Intent intent)
    {
        log.debug("onReceive");

        // Explicitly specify that PathsenseGeofenceEventReceiverService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), PathsenseGeofenceEventReceiverService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        if (isOrderedBroadcast()) setResultCode(Activity.RESULT_OK);
    }
}
