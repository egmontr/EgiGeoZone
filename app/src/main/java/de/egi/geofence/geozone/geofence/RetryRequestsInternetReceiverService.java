package de.egi.geofence.geozone.geofence;

import android.app.IntentService;
import android.content.Intent;

import org.apache.log4j.Logger;

import de.egi.geofence.geozone.utils.Utils;

/**
 * Created by egmont on 05.10.2016.
 */

public class RetryRequestsInternetReceiverService extends IntentService {
    private final Logger log = Logger.getLogger(RetryRequestsInternetReceiverService.class.getSimpleName());

    public RetryRequestsInternetReceiverService() {
        super("RetryRequestsInternetReceiverService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try{
            Utils.doRetry(this, log);
        }finally {
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            log.debug("Release the wake lock");
            RetryRequestsInternetReceiver.completeWakefulIntent(intent);
        }
    }
}
