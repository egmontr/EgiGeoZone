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

package de.egi.geofence.geozone.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.egi.geofence.geozone.GlobalSingleton;
import de.egi.geofence.geozone.MainEgiGeoZone;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.db.DbGlobalsHelper;

public class NotificationUtil {
	private static DbGlobalsHelper datasource;
	private final static Logger log = Logger.getLogger(NotificationUtil.class);

	public static void notify(Context context, int notifyId, PendingIntent pendingIntent, String contentTitle,
			String contentText, String tickerText, boolean vibrate, boolean playSound, int icon) {
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		// Get an instance of the Notification manager
		NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		String channelId = "channel-gcm";
		String channelName = "EgiGeoZone GCM";

		NotificationChannel mChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
		mChannel.setShowBadge(true);
		mChannel.shouldShowLights();
		if (vibrate) {
			mChannel.enableVibration(true);
			mChannel.setVibrationPattern(new long[] { 100, 400 });
		}

		notificationManager.createNotificationChannel(mChannel);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setTicker(tickerText)
				.setSmallIcon(icon)
				.setLights(0xff00960b, 1000, 5000)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icon))
				.setWhen(System.currentTimeMillis()).setAutoCancel(true)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
				.setContentIntent(pendingIntent);
		if (vibrate) {
			notificationBuilder.setVibrate(new long[] { 100, 400 });
		}
		if (playSound) {
			notificationBuilder.setSound(alarmSound);
		}
		Notification notification = notificationBuilder.build();
		notificationManager.notify(notifyId, notification);
		
        datasource = new DbGlobalsHelper(context);
        boolean gcmLogging = Utils.isBoolean(datasource.getCursorGlobalsByKey(Constants.DB_KEY_GCM_LOGGING));
        
		// Alle GCM-Benachrichtigungen protokollieren
		if (gcmLogging){
			try {
				String gcmLogFile = context.getFilesDir() + File.separator + "egigeozone" + File.separator + "gcmnotifications.txt";
				File gcmFile = new File(gcmLogFile);
	    		//if file doesnt exists, then create it
	    		if(!gcmFile.exists()){
					//noinspection ResultOfMethodCallIgnored
					gcmFile.createNewFile();
	    		}
				
	    		Date date = new Date();
	    		String d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss - ", Locale.getDefault()).format(date);
	    		
				FileWriter fw = new FileWriter(gcmFile, true);
				BufferedWriter bufferWritter = new BufferedWriter(fw);
    	        bufferWritter.write(d + tickerText + "\n");
    	        bufferWritter.write(d + contentTitle  + "\n");
    	        String replContentText = contentText.replaceAll("\n", " ");
    	        bufferWritter.write(d + replContentText  + "\n");
    	        bufferWritter.write("-------------------------------\n");
    	        bufferWritter.close();

			} catch (Exception e) {
				Log.e("Exception", "File write failed: " + e);
			}
		}
	}
	
    /**
     * Fehlerdialog anzeigen
     */
    public static void showError(Context context, String title, String error){
    	Log.d(Constants.APPTAG, error);
    	sendErrorNotification(context, title, error);
    }
    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     *
     */
    private static void sendErrorNotification(Context context, String title, String error) {
        datasource = new DbGlobalsHelper(context);
        boolean doNotification = Utils.isBoolean(datasource.getCursorGlobalsByKey(Constants.DB_KEY_ERROR_NOTIFICATION));

        if (!doNotification){
       		return;
        }
        // Create an explicit content Intent that starts the NotificationError
        Intent notificationIntent = new Intent(context,NotificationError.class);
        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the main Activity to the task stack as the parent
        stackBuilder.addParentStack(MainEgiGeoZone.class);
        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.notification);
		remoteView.setTextViewText(R.id.notification_titel, title);
		remoteView.setTextViewText(R.id.notification_text, error);

		GlobalSingleton.getInstance().setNotificationTitel(title);
		GlobalSingleton.getInstance().setNotificationText(error);

        // Get a PendingIntent containing the entire back stack
		PendingIntent notificationPendingIntent;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
		}else{
			notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		// Get an instance of the Notification manager
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		String channelId = "channel-error";
		String channelName = "EgiGeoZone Error";

		NotificationChannel mChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
		mChannel.setShowBadge(true);
		mChannel.shouldShowLights();
		mChannel.setShowBadge(true);
		mNotificationManager.createNotificationChannel(mChannel);

		// Get a notification builder that's compatible with platform versions >= 4
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);

        // Set the notification contents
        builder.setSmallIcon(R.drawable.location_pointer_error).setContentTitle(title)
               .setContentText(error).setContentIntent(notificationPendingIntent).setSubText("See also log file").setWhen(System.currentTimeMillis())
               .setDefaults(Notification.DEFAULT_ALL).setContent(remoteView);

        // Issue the notification
        mNotificationManager.notify(1, builder.build());
    }
    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     * @param transitionType The type of transition that occurred.
     *
     */
    public static void sendNotification(Context context, String transitionType, String ids, String origin) {
        datasource = new DbGlobalsHelper(context);
        boolean doNotification = Utils.isBoolean(datasource.getCursorGlobalsByKey(Constants.DB_KEY_NOTIFICATION));

        if (!doNotification){
       		return;
        }
        // Create an explicit content Intent that starts the main Activity
        Intent notificationIntent = new Intent(context,MainEgiGeoZone.class);

        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the main Activity to the task stack as the parent
        stackBuilder.addParentStack(MainEgiGeoZone.class);

        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack
		PendingIntent notificationPendingIntent;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
		}else{
			notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		// Get an instance of the Notification manager
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		String channelId = "channel-main";
		String channelName = "EgiGeoZone Main";

		NotificationChannel mChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
		mChannel.setShowBadge(true);
		mChannel.shouldShowLights();
		mNotificationManager.createNotificationChannel(mChannel);

		// Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);

		if (log.isDebugEnabled()) {
			// Set debug notification contents
			builder.setSmallIcon(R.drawable.location_pointer).setContentTitle(origin + ": " + context.getString(R.string.geofence_transition_notification_title, transitionType, ids))
					.setContentText(context.getString(R.string.geofence_transition_notification_text)).setContentIntent(notificationPendingIntent);
		}else {
			// Set the notification contents
			builder.setSmallIcon(R.drawable.location_pointer).setContentTitle(context.getString(R.string.geofence_transition_notification_title, transitionType, ids))
					.setContentText(context.getString(R.string.geofence_transition_notification_text)).setContentIntent(notificationPendingIntent);
		}

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

	/**
	 * Posts a notification in the notification bar when live tracking is running.
	 * If the user clicks the notification, control goes to the main Activity.
	 *
	 */
	public static void sendPermanentNotification(Context context, int icon, String notificationText, int id) {
		// Create an explicit content Intent that starts the main Activity
		Intent notificationIntent = new Intent(context,MainEgiGeoZone.class);

		// Construct a task stack
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

		// Adds the main Activity to the task stack as the parent
		stackBuilder.addParentStack(MainEgiGeoZone.class);

		// Push the content Intent onto the stack
		stackBuilder.addNextIntent(notificationIntent);

		// Get a PendingIntent containing the entire back stack
		PendingIntent notificationPendingIntent;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
		}else{
			notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		// Get an instance of the Notification manager
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		String channelId = "channel-perm";
		String channelName = "EgiGeoZone";

		NotificationChannel mChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
		mNotificationManager.createNotificationChannel(mChannel);

		// Get a notification builder that's compatible with platform versions >= 4
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
		// Set the notification contents
		builder.setSmallIcon(icon).setContentTitle("EgiGeoZone")
				.setContentText(notificationText).setContentIntent(notificationPendingIntent).setOngoing(true);

		// Issue the notification
		mNotificationManager.notify(id, builder.build());
	}
	/**
	 * Cancel a notification in the notification bar when live tracking is running.
	 * If the user clicks the notification, control goes to the main Activity.
	 *
	 */
	public static void cancelPermanentNotification(Context context, int id) {
		// Create an explicit content Intent that starts the main Activity
		Intent notificationIntent = new Intent(context,MainEgiGeoZone.class);
		// Construct a task stack
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the main Activity to the task stack as the parent
		stackBuilder.addParentStack(MainEgiGeoZone.class);
		// Push the content Intent onto the stack
		stackBuilder.addNextIntent(notificationIntent);
		// Get an instance of the Notification manager
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// Issue the notification
		mNotificationManager.cancel(id);
	}

	public static void cancelNotification(Context ctx, int notifyId) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
		nMgr.cancel(notifyId);
	}


	public static Notification prepareNotification(Context context, int icon, String notificationText, PendingIntent pendingIntent){
		// Get an instance of the Notification manager
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		String channelId = "channel-tracker";
		String channelName = "EgiGeoZone tracker";

		NotificationChannel mChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
		mNotificationManager.createNotificationChannel(mChannel);

		// Get a notification builder that's compatible with platform versions >= 4
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
		builder.setContentTitle("EgiGeoZone").setContentText(notificationText).setSmallIcon(icon)
				.setContentIntent(pendingIntent);
		return builder.build();
	}

	/**
	 * Posts a notification in the notification bar when a transition is detected.
	 * If the user clicks the notification, control goes to the main Activity.
	 *
	 */
	@SuppressLint("UnspecifiedImmutableFlag")
	public static void sendErrorNotificationWithButtons(Context context, String title, String error) {
		datasource = new DbGlobalsHelper(context);
		boolean doNotification = Utils.isBoolean(datasource.getCursorGlobalsByKey(Constants.DB_KEY_ERROR_NOTIFICATION));

		if (!doNotification){
			return;
		}
		// Create an explicit content Intent that starts the NotificationError
		Intent notificationIntentTest = new Intent();
		notificationIntentTest.setAction(Constants.ACTION_DONOTDISTURB_OK);

		// Get a PendingIntent containing the entire back stack
		PendingIntent notificationPendingIntentTesta;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			notificationPendingIntentTesta = PendingIntent.getBroadcast(context, 1234, notificationIntentTest, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
		}else{
			notificationPendingIntentTesta = PendingIntent.getBroadcast(context, 1234, notificationIntentTest, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		Intent notificationIntentHuber = new Intent();
		notificationIntentHuber.setAction(Constants.ACTION_DONOTDISTURB_NOK);

		// Get a PendingIntent containing the entire back stack
		PendingIntent notificationPendingIntentHuber;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			notificationPendingIntentHuber = PendingIntent.getBroadcast(context, 1234, notificationIntentHuber, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
		}else{
			notificationPendingIntentHuber = PendingIntent.getBroadcast(context, 1234, notificationIntentHuber, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		// Get an instance of the Notification manager
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		String channelId = "channel-buttons";
		String channelName = "EgiGeoZone";

		NotificationChannel mChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
		mNotificationManager.createNotificationChannel(mChannel);

		// Get a notification builder that's compatible with platform versions >= 4
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);

		// Set the notification contents
		builder.setSmallIcon(R.drawable.location_pointer_error).setContentTitle(title)
				.setContentText(error).setWhen(System.currentTimeMillis())
				.setDefaults(Notification.DEFAULT_ALL).setStyle(new NotificationCompat.BigTextStyle().bigText(error))
				.setAutoCancel(true)
				.addAction(R.drawable.ic_check_black_24dp, context.getString(R.string.action_yes), notificationPendingIntentTesta)
				.addAction(R.drawable.ic_close_black_24dp, context.getString(R.string.action_no), notificationPendingIntentHuber);

		// Issue the notification
		mNotificationManager.notify(222, builder.build());
	}

}