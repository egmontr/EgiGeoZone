package de.egi.geofence.geozone.geofence;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;

import com.google.android.gms.location.Geofence;
import com.pathsense.android.sdk.location.PathsenseGeofenceEvent;

import org.apache.log4j.Logger;

/**
 * Created by egmontr on 21.06.2016.
 */
public class PathsenseGeofenceEventReceiver extends BroadcastReceiver
{
    private final Logger log = Logger.getLogger(PathsenseGeofenceEventReceiver.class);
    @Override
    public void onReceive(Context context, Intent intent)
    {
        log.debug("onReceive");
//        PathsenseGeofenceEventReceiverService.enqueueWork(context, intent);
        int kJobId = 573;
        try {
            ComponentName serviceComponent = new ComponentName(context, PathsenseGeofenceEventReceiverService.class);
            JobInfo.Builder builder = new JobInfo.Builder(kJobId, serviceComponent);
            PersistableBundle extras = new PersistableBundle();

            PathsenseGeofenceEvent geofenceEvent = PathsenseGeofenceEvent.fromIntent(intent);
            if (geofenceEvent != null) {
                if (geofenceEvent.isIngress()) {
                    // ingress
                    extras.putInt("trans", Geofence.GEOFENCE_TRANSITION_ENTER);
                } else if (geofenceEvent.isEgress()) {
                    // egress
                    extras.putInt("trans", Geofence.GEOFENCE_TRANSITION_EXIT);
                }
                long accuracy = -1;
                if (geofenceEvent.getLocation().hasAccuracy()) {
                    extras.putLong("acc", (long) geofenceEvent.getLocation().getAccuracy());
                }
                extras.putString("geofenceId", geofenceEvent.getGeofenceId());
                extras.putDouble("lat", geofenceEvent.getLocation().getLatitude());
                extras.putDouble("lng", geofenceEvent.getLocation().getLongitude());
                extras.putString("prov", geofenceEvent.getLocation().getProvider());
                extras.putLong("time", geofenceEvent.getLocation().getTime());
            }

//            builder.setMinimumLatency(5 * 1000);
            builder.setExtras(extras);
            JobInfo jobInfo = builder.build();
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int result = jobScheduler.schedule(jobInfo);
            log.debug("############ PathsenseGeofenceEventReceiverService: result = " + result);
            if (result == JobScheduler.RESULT_SUCCESS) {
                log.info("PathsenseGeofenceEventReceiverService scheduled successfully!");
            }
        } catch (Exception e) {
            log.error("Error Starting PathsenseGeofenceEventReceiverService with JobScheduler: " + e);
        }


    }
}
