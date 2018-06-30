package de.egi.geofence.geozone.geofence;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import org.apache.log4j.Logger;

import de.egi.geofence.geozone.utils.Utils;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RetryJobSchedulerService extends JobService {
    private final Logger log = Logger.getLogger(RetryJobSchedulerService.class.getSimpleName());
    private Context context;
    private Handler mJobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage( Message msg ) {
            Utils.doRetry(context, log);
            jobFinished((JobParameters) msg.obj, false);
            return true;
        }
    } );
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        context = this;
        // Start action in own thread
        mJobHandler.sendMessage(Message.obtain( mJobHandler, 1, jobParameters ));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        // Do not restart
        return false;
    }
}
