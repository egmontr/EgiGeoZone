package de.egi.geofence.geozone.tracker;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Calendar;

import de.egi.geofence.geozone.SendMail;
import de.egi.geofence.geozone.db.DbZoneHelper;
import de.egi.geofence.geozone.db.MailEntity;
import de.egi.geofence.geozone.db.ServerEntity;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.utils.Api;
import de.egi.geofence.geozone.utils.AuthenticationParameters;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.IOUtil;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.SharedPrefsUtil;
import de.egi.geofence.geozone.utils.Utils;

/**
 * Created by egmont on 05.10.2016.
 */

public class TrackingReceiverWorkerService extends IntentService {
    private static final String TAG = "TrackingRWS";
    //    final static LogConfigurator logConfigurator = new LogConfigurator();
    private final Logger log = Logger.getLogger(TrackingReceiverWorkerService.class);
    private Api geoApi;

    public TrackingReceiverWorkerService() {
        super("TrackingReceiverWorkerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String zone = intent.getStringExtra("zone");
            String reqId = intent.getStringExtra("reqId");
            boolean trackToFile = intent.getBooleanExtra("trackToFile", false);
            boolean trackToMail = intent.getBooleanExtra("trackToMail", false);
            boolean trackToUrl = intent.getBooleanExtra("trackUrl", false);
            int trackIntervallZone = intent.getIntExtra("mins", 5);

            TrackingUtils.startTracking(this, zone, trackIntervallZone, trackToFile, trackToUrl, trackToMail);

            String key_lat = "location_lat";
            String key_lng = "location_lng";
            String key_time_utc = "location_utc_time";
            String key_time_local = "location_local_time";
            String key_accuracy = "location_accuracy";

            SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(this);
            String location_lat = sharedPrefsUtil.getLocationPref(key_lat);
            String location_lng = sharedPrefsUtil.getLocationPref(key_lng);
            String location_local_time = sharedPrefsUtil.getLocationPref(key_time_local);
            String location_utc_time = sharedPrefsUtil.getLocationPref(key_time_utc);
            String location_accuracy = sharedPrefsUtil.getLocationPref(key_accuracy);

            Log.i(TAG, "Location: " + zone + "/" + reqId + " " + location_lat + ", " + location_lng + " " + location_local_time + " accuracy: " + location_accuracy);
            log.debug("Location: " + zone + "/" + reqId + " " + location_lat + ", " + location_lng + " " + location_local_time + " accuracy: " + location_accuracy);
            if (!TrackingUtils.isMyServiceRunning(TrackingLocationService.class, this)) {
                Log.i(TAG, "************** Location Service NOT RUNNING **************");
                Log.i(TAG, "************** RESTARTING Location Service **************");
                Intent i = new Intent(this, TrackingLocationService.class);
                this.startService(i);
            }

            // Write to file
            if (trackToFile) {
                writeToFile(zone, location_lat, location_lng, location_local_time, location_accuracy);
            }

            // Write to EMail
            if (trackToMail) {
                DbZoneHelper dbZoneHelper = new DbZoneHelper(this);
                ZoneEntity ze = dbZoneHelper.getCursorZoneByName(zone);
                MailEntity me = ze.getTrackMailEntity();

                String mailUser = me.getSmtp_user();
                String mailUserPw = me.getSmtp_pw();
                String mailSmtpHost = me.getSmtp_server();
                String mailSmtpPort = me.getSmtp_port();
                String mailSender = me.getFrom();
                String mailEmpf = me.getTo();
                String mailText = me.getBody();
                String mailSubject = me.getSubject();
                boolean mailSsl = me.isSsl();
                boolean mailStarttls = me.isStarttls();
                if (TextUtils.isEmpty(mailUser) || TextUtils.isEmpty(mailUserPw) || TextUtils.isEmpty(mailSmtpHost)
                        || TextUtils.isEmpty(mailSmtpPort) || TextUtils.isEmpty(mailSender) || TextUtils.isEmpty(mailEmpf)) {
                    // Configure Mail-Settings properly
                    NotificationUtil.showError(this, zone + ": Error sending tracking mail", "Tracking: configure mail properly");
                }

                try {
                    // Mail senden
                    SendMail smail = new SendMail(this, mailUser, mailUserPw, mailSmtpHost, mailSmtpPort, mailSender, mailEmpf, mailSsl, mailStarttls);
                    // Subject
                    mailSubject = Utils.replaceAllTracking(this, mailSubject, zone, ze.getAlias(), location_lat, location_lng, location_local_time, location_utc_time, location_accuracy);
                    // Body
                    mailText = Utils.replaceAllTracking(this, mailText, zone, ze.getAlias(), location_lat, location_lng, location_local_time, location_utc_time, location_accuracy);
                    // Send mail
                    smail.sendMail(mailSubject, mailText, false);

                } catch (Exception ex) {
                    Log.e(Constants.APPTAG, "error sending mail", ex);
                    NotificationUtil.showError(this, zone + ": Error sending tracking mail", ex.toString());
                }
            }

            // Call Server
            // Beispiel: yyyy.xxx.eu/tracking/track.php?latitude=48.2160&longitude=11.3213&&date=yyyy-MM-dd'T'HH:mm:ssZ&device=xxxxx&zone=home
            if (trackToUrl) {
                try {
                    DbZoneHelper dbZoneHelper = new DbZoneHelper(this);
                    ZoneEntity ze = dbZoneHelper.getCursorZoneByName(zone);
                    ServerEntity se = ze.getTrackServerEntity();

                    String trackUrl = se.getUrl_tracking();

                    // No Url set --> return
                    if (trackUrl.isEmpty()) return;

                    executeCall(this, zone, ze.getAlias(), location_lat, location_lng, location_local_time, location_utc_time, location_accuracy, trackUrl, se.getTimeout(), se.getCert(),
                            se.getCert_password(), se.getCa_cert(), se.getUser(), se.getUser_pw(), false);

                } catch (Exception e) {
                    // nothing to do
                }
            }
        }finally {
            // Release the wake lock provided by the WakefulBroadcastReceiver.
            TrackingReceiverWorker.completeWakefulIntent(intent);
        }
    }

// https://plus.google.com/+AndroidDevelopers/posts/GdNrQciPwqo

// if(Build.VERSION.SDK_INT < 23){
//    setExact(...)
// }
// else{
//    setExactAndAllowWhileIdle(...)
// }

    public static void startAlarm(Context context, Intent alarmIntent, int alarmId, int delayMs) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent recurringAlarm = PendingIntent.getBroadcast(context, alarmId, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar updateTime = Calendar.getInstance();
        alarm.cancel(recurringAlarm);
        if (Build.VERSION.SDK_INT >= 23) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis() + (1000 * 60 * delayMs), recurringAlarm);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarm.setExact(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis() + (1000 * 60 * delayMs), recurringAlarm);
        } else {
            alarm.set(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis() + (1000 * 60 * delayMs), recurringAlarm);
        }
    }

// http://stackoverflow.com/questions/36064701/android-alarm-setexactandallowwhileidle-unexpected-behavior-on-samsung

//	public static void startAlarm(Context context, Intent intent, int reqId, int min) {
//		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//		PendingIntent pi = PendingIntent.getBroadcast(context, reqId, intent,  PendingIntent.FLAG_UPDATE_CURRENT);
//		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * min, pi); // Millisec * Second * Minuten -  Minuten
//	}

    public static void cancelAlarm(Context context, Intent alarmIntent, int reqId) {
        PendingIntent alarm = PendingIntent.getBroadcast(context, reqId, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarm);
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeToFile(String zone, String location_lat, String location_lng, String location_time, String location_accuracy){
        try {
            String trackerLogDir = Environment.getExternalStorageDirectory() + File.separator + "egigeozone";
            File trackerDir = new File(trackerLogDir);
            if(!trackerDir.exists()){
                trackerDir.mkdir();
            }

            String trackerLogFile = Environment.getExternalStorageDirectory() + File.separator + "egigeozone" + File.separator + "locationtracker_" + zone + ".txt";
            File trackerFile = new File(trackerLogFile);
            //if file doesnt exists, then create it
            if(!trackerFile.exists()){
                trackerFile.createNewFile();
            }

            FileWriter fw = new FileWriter(trackerFile, true);
            BufferedWriter bufferWritter = new BufferedWriter(fw);
            bufferWritter.write(location_time + " " + location_lat + ", " + location_lng + ", " + location_accuracy + "\n");
            bufferWritter.close();
        } catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
    private File getClientCertFile(String clientCertificateName) {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        return new File(externalStorageDir + File.separator + "egigeozone", clientCertificateName);
    }

    private String readCaCert(String caCertificateName) throws Exception {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File caCert = new File(externalStorageDir + File.separator + "egigeozone", caCertificateName);
        InputStream inputStream = new FileInputStream(caCert);
        return IOUtil.readFully(inputStream);
    }


    public void executeCall(final Context context, final String zone, final String alias, String location_lat, String location_lng, String location_time,
                            String location_time_utc, String location_accuracy, String trackUrl, String timeout, String cert, String cert_password,
                            String ca_cert, String user, String user_pw, final boolean test){
        try{
//            String androidIdInhalt = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//            String deviceIdInhalt = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

            // Tracking URL
            trackUrl = Utils.replaceAllTracking(context, trackUrl, zone, alias, location_lat, location_lng, location_time, location_time_utc, location_accuracy);

            AuthenticationParameters authParams = new AuthenticationParameters();
            authParams.setUrl(trackUrl);

            // No Url set --> return
            if (authParams.getUrl().isEmpty()) return;


            authParams.setClientCertificate(TextUtils.isEmpty(cert) ? null : getClientCertFile(cert));
            authParams.setClientCertificatePassword(TextUtils.isEmpty(cert_password) ? null : cert_password);
            authParams.setCaCertificate(TextUtils.isEmpty(ca_cert) ? null : readCaCert(ca_cert));
            authParams.setUser(TextUtils.isEmpty(user) ? null : user);
            authParams.setUserPasswd(TextUtils.isEmpty(user_pw) ? null : user_pw);

            log.debug("tracking zone: " + zone);
            log.debug("tracking latitude: " + location_lat);
            log.debug("tracking longitude: " + location_lng);
            log.debug("tracking urlTracking: " + trackUrl);
            log.debug("tracking  user: " + user);
            log.debug("tracking  date: " + location_time);
            log.debug("tracking client_cert: " + cert);
            log.debug("tracking ca_cert: " + ca_cert);
            log.debug("tracking timeout: " + timeout);

            geoApi = new Api(authParams, timeout);

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {

                    try {
                        geoApi.doGet();

                        int responseCode = geoApi.getLastResponseCode();
                        if (responseCode == 200) {
                            if (test){
                                // Broadcast damit der Test-Dialog angezeigt wird
                                Intent intent = new Intent();
                                intent.setAction(Constants.ACTION_TEST_STATUS_OK);
                                intent.putExtra("TestType", "Tracking");
                                context.sendBroadcast(intent);
                            }
                            log.debug("Tracking Response code after get: "  + responseCode);
                        } else {
                            if (test){
                                // Broadcast damit der Test-Dialog angezeigt wird
                                Intent intent = new Intent();
                                intent.setAction(Constants.ACTION_TEST_STATUS_NOK);
                                intent.putExtra("TestResult", "Error (GR01) in get of the server response. Response Code: " + responseCode);
                                intent.putExtra("TestType", "Tracking");
                                context.sendBroadcast(intent);
                            }
                            log.error("Tracking Response code after get: "  + responseCode);
                        }
                    } catch (Throwable ex) {
                        log.error(zone + ": Error (GR02) in get of the server response", ex);
                        if (test){
                            // Broadcast damit der Test-Dialog angezeigt wird
                            Intent intent = new Intent();
                            intent.setAction(Constants.ACTION_TEST_STATUS_NOK);
                            intent.putExtra("TestResult", "Error (GR02) in get of the server response: " + ex.toString());
                            intent.putExtra("TestType", "Tracking");
                            context.sendBroadcast(intent);
                        }

                    }
                    return null;
                }
            }.execute();

        } catch (Exception ex) {
            Log.e(Constants.APPTAG, "error sending server request", ex);
            log.error(zone + ": Error (GR03) sending server request", ex);
        }
    }

}
