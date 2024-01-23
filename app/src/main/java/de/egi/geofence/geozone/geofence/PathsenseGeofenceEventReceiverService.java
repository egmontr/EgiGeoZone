package de.egi.geofence.geozone.geofence;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import org.apache.log4j.Logger;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.Worker;
import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.Utils;

/**
 * Created by egmont on 05.01.2023.
 */

public class PathsenseGeofenceEventReceiverService extends JobService {
    private final Logger log = Logger.getLogger(PathsenseGeofenceEventReceiverService.class);
    private Context context;
    private int trans;
    private Location loc;
    private String geofenceId;

    private final Handler mJobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage( Message msg ) {
            int transition;
            transition = trans;
            String transitionType = getTransitionString(transition);
            DbGlobalsHelper dbGlobalsHelper = new DbGlobalsHelper(context);
            boolean reboot = Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_REBOOT));
            if (reboot) {
                Log.i(Constants.APPTAG, "Do not call events after reboot or at update");
                log.error("Do not call events after reboot or at update");
                log.error("Reboot: " + true);
                dbGlobalsHelper.storeGlobals(Constants.DB_KEY_REBOOT, "false");
            } else {
                float accuracy = 0;
                if (loc.hasAccuracy()) {
                    accuracy = loc.getAccuracy();
                }
                // Requests ausführen
                // Aktionen ausführen
                Worker worker = new Worker(context);
                worker.handleTransition(transition, geofenceId, Constants.GEOZONE, accuracy, loc, Constants.FROM_PATHSENSE);
                // Log the transition type and a message
                Log.d(Constants.APPTAG, getString(R.string.geofence_transition_notification_title, transitionType, geofenceId));
                Log.d(Constants.APPTAG, getString(R.string.geofence_transition_notification_text));
                log.info("after handleGeofenceTransition: " + getString(R.string.geofence_transition_notification_title, transitionType, geofenceId));
                log.debug("after handleGeofenceTransition: " + getString(R.string.geofence_transition_notification_text));
            }
            jobFinished((JobParameters) msg.obj, false);
            return true;
        }
    } );

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        context = this;

        long acc = jobParameters.getExtras().getLong("acc");
        trans = jobParameters.getExtras().getInt("trans");
        geofenceId = jobParameters.getExtras().getString("geofenceId");

        loc = new Location(jobParameters.getExtras().getString("prov"));
        loc.setLongitude(jobParameters.getExtras().getDouble("lng"));
        loc.setLatitude(jobParameters.getExtras().getDouble("lat"));
        loc.setAccuracy(acc);
        loc.setTime(jobParameters.getExtras().getLong("time"));

        // Start action in own thread
        mJobHandler.sendMessage(Message.obtain( mJobHandler, 1, jobParameters ));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        // Do not restart
        return false;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return this.getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return this.getString(R.string.geofence_transition_exited);

            default:
                return this.getString(R.string.geofence_transition_unknown);
        }
    }

}
