package de.egi.geofence.geozone.geofence;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.apache.log4j.Logger;

import java.util.List;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.Worker;
import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.Utils;

/**
 * Created by egmont on 05.10.2016.
 */

public class GeofenceReceiverService extends IntentService {
    private final Logger log = Logger.getLogger(GeofenceReceiver.class);

    public GeofenceReceiverService() {
        super("GeofenceReceiverService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        log.debug("onHandleIntent");
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        try {
            if (event != null) {
                if (event.hasError()) {
                    handleError(event);
                } else {
                    handleEnterExit(event);
                }
            }
        }finally {
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            log.debug("Release the wake lock");
            GeofenceReceiver.completeWakefulIntent(intent);
        }
    }

    private void handleError(GeofencingEvent event){
        log.debug("handleError");
        // Get the error code
        int errorCode = event.getErrorCode();
        // Get the error message
        String errorMessage = LocationServiceErrorMessages.getErrorString(this, errorCode);
        // Log the error
        Log.e(Constants.APPTAG, this.getString(R.string.geofence_transition_error_detail, errorCode, errorMessage));
        log.error(Constants.APPTAG + ": " + errorCode + " " + errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }


    private void handleEnterExit(GeofencingEvent event) {
        log.debug("handleEnterExit");
        // Get the type of transition (entry or exit)
        int transition = event.getGeofenceTransition();

        // Genauigkeit des Standortes einschränken
        Location location = event.getTriggeringLocation();
        log.debug("Location accuracy: " + location.toString());
        float accuracy = -1;
        if (location.hasAccuracy()) {
            accuracy = location.getAccuracy();
        }

        // Test that a valid transition was reported
        if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)){
            // Post a notification
            List<Geofence> geofences = event.getTriggeringGeofences();
            String[] geofenceIds = new String[geofences.size()];
            for (int index = 0; index < geofences.size() ; index++) {
                geofenceIds[index] = geofences.get(index).getRequestId();
            }
            String ids = TextUtils.join(Constants.GEOFENCE_ID_DELIMITER, geofenceIds);

            String transitionType = getTransitionString(transition);
            // Notification senden
            DbGlobalsHelper dbGlobalsHelper = new DbGlobalsHelper(this);
            boolean reboot = Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_REBOOT));

            if (reboot){
                Log.i(Constants.APPTAG, "Do not call events after reboot or at update");
                log.error("Do not call events after reboot or at update");
                log.error("Reboot: " + true);
                dbGlobalsHelper.storeGlobals(Constants.DB_KEY_REBOOT, "false");
                return;
//            }else{
//                NotificationUtil.sendNotification(this, transitionType, ids, Constants.FROM_GOOGLE);
            }

            // Requests ausführen
            // Aktionen ausführen
            Worker worker = new Worker(this.getApplicationContext());
            worker.handleTransition(transition, ids, Constants.GEOZONE, accuracy, location, Constants.FROM_GOOGLE);

            // Log the transition type and a message
            Log.d(Constants.APPTAG, this.getString(R.string.geofence_transition_notification_title, transitionType, ids));
            Log.d(Constants.APPTAG,	this.getString(R.string.geofence_transition_notification_text));
            log.debug("after handleGeofenceTransition: " + this.getString(R.string.geofence_transition_notification_title, transitionType, ids));
            log.debug("after handleGeofenceTransition: " + this.getString(R.string.geofence_transition_notification_text));
            // An invalid transition was reported
        } else {
            // Always log as an error
            Log.e(Constants.APPTAG, this.getString(R.string.geofence_transition_invalid_type, transition, ""));
            log.error(this.getString(R.string.geofence_transition_invalid_type, transition, ""));
        }
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
