package de.egi.geofence.geozone.geofence;

import android.content.Context;

import com.google.android.gms.location.Geofence;
import com.pathsense.android.sdk.location.PathsenseGeofenceEventEnum;
import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;
import com.pathsense.android.sdk.location.PathsenseMonitoringGeofenceCallback;

import org.apache.log4j.Logger;

/**
 * Created by egmontr on 22.06.2016.
 */
public class PathsenseGeofence implements PathsenseMonitoringGeofenceCallback{
    // Storage for a reference to the calling client
    private final Context context;
    private final Logger log = Logger.getLogger(PathsenseGeofence.class);

    public PathsenseGeofence(Context context){
        // Save the context
        this.context = context;
    }

    /**
     * Start adding geofences. Save the geofences, then start adding them by requesting a
     * connection
     *
     */
    public void addGeofence(SimpleGeofence geofence) {
        log.debug("addGeofence");
        final PathsenseLocationProviderApi api = PathsenseLocationProviderApi.getInstance(context);

        // first remove
        api.removeGeofence(geofence.getId());

        // then add geofence
        PathsenseGeofenceEventEnum pse = PathsenseGeofenceEventEnum.INGRESS_EGRESS;
        switch (geofence.getTransitionType()){
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                pse = PathsenseGeofenceEventEnum.INGRESS;
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                pse = PathsenseGeofenceEventEnum.EGRESS;
                break;
            default:
                pse = PathsenseGeofenceEventEnum.INGRESS_EGRESS;
        }

        api.addGeofence(geofence.getId(),
                Double.valueOf(geofence.getLatitude()),
                Double.valueOf(geofence.getLongitude()),
                Integer.valueOf(geofence.getRadius()),
                pse,
                PathsenseGeofenceEventReceiver.class);

        api.monitoringGeofence(geofence.getId(), this);
    }

    public void removeGeofence(String zone) {
        log.debug("removeGeofence");
        final PathsenseLocationProviderApi api = PathsenseLocationProviderApi.getInstance(context);
        api.removeGeofence(zone);
    }

    public void removeGeofences() {
        log.debug("removeGeofences");
        final PathsenseLocationProviderApi api = PathsenseLocationProviderApi.getInstance(context);
        api.removeGeofences();
    }

    @Override
    public void onMonitoringGeofenceCallback(String s, boolean b) {
        log.debug("onMonitoringGeofenceCallback");
    }
}
