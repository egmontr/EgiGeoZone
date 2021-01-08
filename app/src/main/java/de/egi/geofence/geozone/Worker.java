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

package de.egi.geofence.geozone;

import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;

import de.egi.geofence.geozone.db.DbContract;
import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.db.DbServerHelper;
import de.egi.geofence.geozone.db.DbZoneHelper;
import de.egi.geofence.geozone.db.ServerEntity;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.geofence.GeofenceRequester;
import de.egi.geofence.geozone.geofence.PathsenseGeofence;
import de.egi.geofence.geozone.geofence.RetryJobSchedulerService;
import de.egi.geofence.geozone.geofence.RetryRequestQueue;
import de.egi.geofence.geozone.geofence.SimpleGeofence;
import de.egi.geofence.geozone.tasker.TaskerIntent;
import de.egi.geofence.geozone.tracker.TrackingUtils;
import de.egi.geofence.geozone.utils.Api;
import de.egi.geofence.geozone.utils.AuthenticationParameters;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.IOUtil;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.Utils;

public class Worker implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{
	private final Context context;
	private final Logger log = Logger.getLogger(Worker.class);
	private Api geoApi;
	private DbServerHelper datasourceServer;
	private String fallback;
	private static int kJobId = 0;
	private GoogleApiClient mLocationClient;
	private Location checkLocation;
	private int transition;
	private String ids;
	private String type;
	private float accuracy;
	private Location location;
	private String origin;
	private GeofenceRequester mGeofenceRequester;
    private PathsenseGeofence mPathsenseGeofence;
    private DbGlobalsHelper dbGlobalsHelper;

	final BroadcastReceiver myReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action_name = intent.getAction();
			if (action_name.equals(Constants.ACTION_DONOTDISTURB_OK)) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					context.startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
				}

			}
			if (action_name.equals(Constants.ACTION_DONOTDISTURB_NOK)) {
				// Do nothing
			}
			// Get an instance of the Notification manager
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(222);
			context.unregisterReceiver(myReceiver);
		};
	};


	public Worker(Context context){
		this.context = context;
		// Instantiate a Geofence requester
		if (mGeofenceRequester == null){
			mGeofenceRequester = new GeofenceRequester(context);
		}
        if (mPathsenseGeofence == null){
            mPathsenseGeofence = new PathsenseGeofence(context);
        }
        dbGlobalsHelper = new DbGlobalsHelper(context);
	}

	/**
	 * Report geofence transitions to the UI
	 *
	 * context A Context for this component
	 * intent The Intent containing the transition
	 */
	public void handleTransition(int transition, String ids, String type, float accuracy, Location location, String origin) {
		Log.i(Constants.APPTAG, "in handleTransition");
		log.info("in handleTransition");
		log.info("Zones: " +  ids);

		this.transition = transition;
		this.ids = ids;
		this.type = type;
		this.accuracy = accuracy;
		this.location = location;
		this.origin = origin;

		Log.d("", "Geofe: " + Double.valueOf(location.getLatitude()).toString());
		Log.d("", "Geofe: " + Double.valueOf(location.getLongitude()).toString());
		log.debug("Geofe: " + Double.valueOf(location.getLatitude()).toString());
		log.debug("Geofe: " + Double.valueOf(location.getLongitude()).toString());

		if (Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_FALSE_POSITIVES))) {
			mLocationClient = new GoogleApiClient.Builder(context, this, this).addApi(LocationServices.API).build();
			mLocationClient.connect();
		}else{
			doWork();
		}
	}

	private void doWork() {
		StringTokenizer st = new StringTokenizer(ids, ",");
		while (st.hasMoreTokens()) {
			DbZoneHelper datasource = new DbZoneHelper(context);
			ZoneEntity ze = datasource.getCursorZoneByName(st.nextToken());

			// Bug: Manchmal ist Geofence NULL! Grund nicht bekannt.
			if (ze == null){
				continue;
			}

			// Doublecheck
			if (Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_FALSE_POSITIVES))) {
				Location locationZone = new Location("locationZone");
				locationZone.setLatitude(Double.valueOf(ze.getLatitude()));
				locationZone.setLongitude(Double.valueOf(ze.getLongitude()));

				int radius = ze.getRadius();
				float distanceMeters = 0;
				if (checkLocation != null) {
					distanceMeters = checkLocation.distanceTo(locationZone);
				}else{
					log.debug("5-0 GeofencingFalsePositives doWork checkLocation = null");
					if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
						distanceMeters = 0;
					}else{
						distanceMeters = radius + 1;
					}
				}

				List<Geofence> currentGeofence = new ArrayList<>();

				if (distanceMeters > radius) {
					log.debug("5-a GeofencingFalsePositives DoubleCheck - We are outside of the fence " + ze.getName() + ". DistanceToCenter: " + distanceMeters + " Radius: " + radius);
					if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
						log.debug("5-b GeofencingFalsePositives DoubleCheck - NOK - Enter event " + ze.getName() + " : - set Enter and return");
						// Set Geofence Enter
						// Return
						if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
							currentGeofence.add(getGeofence(ze, Geofence.GEOFENCE_TRANSITION_ENTER));
							mGeofenceRequester.addGeofences(currentGeofence);
						}else{
							SimpleGeofence simpleGeofence = new SimpleGeofence(ze.getName(), ze.getLatitude(), ze.getLongitude(),
									Integer.toString(ze.getRadius()), null, Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER, true, null);
							mPathsenseGeofence.addGeofence(simpleGeofence);
						}
						// Post a notification
//						NotificationUtil.showError(context, "Enter " + ze.getName() + " : False positives", origin + ": " + (distanceMeters - radius) + " difference");
						log.debug("5-c GeofencingFalsePositives Enter " + ze.getName() + " : False positives" + origin + ": " + (distanceMeters - radius) + " difference");
						continue;
					} else {
						log.debug("5-d GeofencingFalsePositives DoubleCheck - OK - Exit event " + ze.getName() + " : - set Enter and continue");
						// Set Geofence Enter
						// Continue
						if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
							currentGeofence.add(getGeofence(ze, Geofence.GEOFENCE_TRANSITION_ENTER));
							mGeofenceRequester.addGeofences(currentGeofence);
						}else{
							SimpleGeofence simpleGeofence = new SimpleGeofence(ze.getName(), ze.getLatitude(), ze.getLongitude(),
									Integer.toString(ze.getRadius()), null, Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER, true, null);
							mPathsenseGeofence.addGeofence(simpleGeofence);
						}
					}
				} else {
					log.debug("6-a GeofencingFalsePositives DoubleCheck - We are inside the fence " + ze.getName() + ". DistanceToCenter: " + distanceMeters + " Radius: " + radius);
					if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
						log.debug("6-b GeofencingFalsePositives DoubleCheck - NOK - Exit event " + ze.getName() + " : - set Exit and return");
						// Set Geofence Exit
						// Return
						if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
							currentGeofence.add(getGeofence(ze, Geofence.GEOFENCE_TRANSITION_EXIT));
							mGeofenceRequester.addGeofences(currentGeofence);
						}else{
							SimpleGeofence simpleGeofence = new SimpleGeofence(ze.getName(), ze.getLatitude(), ze.getLongitude(),
									Integer.toString(ze.getRadius()), null, Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_EXIT, true, null);
							mPathsenseGeofence.addGeofence(simpleGeofence);
						}
						log.debug("6-c GeofencingFalsePositives Exit " + ze.getName() + " : False positives" + origin + ": " + (radius - distanceMeters) + " difference");
//						NotificationUtil.showError(context, "Exit " + ze.getName() + " : False positives", origin + ": " + (radius - distanceMeters) + " difference");
						continue;
					} else {
						log.debug("6-d GeofencingFalsePositives DoubleCheck - OK - Enter event " + ze.getName() + " : - set Exit and continue");
						// Set Geofence Exit
						// Continue
						if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
							currentGeofence.add(getGeofence(ze, Geofence.GEOFENCE_TRANSITION_EXIT));
							mGeofenceRequester.addGeofences(currentGeofence);
						}else{
							SimpleGeofence simpleGeofence = new SimpleGeofence(ze.getName(), ze.getLatitude(), ze.getLongitude(),
									Integer.toString(ze.getRadius()), null, Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_EXIT, true, null);
							mPathsenseGeofence.addGeofence(simpleGeofence);
						}
					}
				}
			}


			// Doublecheck ##
			if (Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_FALSE_POSITIVES))) {
				if (checkLocation != null) {
					Location locationZone = new Location("locationZone");
					locationZone.setLatitude(Double.valueOf(ze.getLatitude()));
					locationZone.setLongitude(Double.valueOf(ze.getLongitude()));

					int radius = ze.getRadius();
					float distanceMeters = checkLocation.distanceTo(locationZone);

					if (distanceMeters > radius) {
						log.error("DoubleCheck - We are outside of the fence " + ze.getName() + ". DistanceToCenter: " + distanceMeters + " Radius: " + radius);
						if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
							log.error("DoubleCheck - NOK - Enter " + ze.getName() + " : - set Enter and return");
							// Set Geofence Enter
							// Return
							if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
								List<Geofence> currentGeofence = new ArrayList<>();
								currentGeofence.add(getGeofence(ze, Geofence.GEOFENCE_TRANSITION_ENTER));
								mGeofenceRequester.addGeofences(currentGeofence);
							}
							// Post a notification
							NotificationUtil.showError(context, "Enter " + ze.getName() + " : False positives", origin + ": " + (distanceMeters - radius) + " difference");
							continue;
						} else {
							log.error("DoubleCheck - OK - Exit " + ze.getName() + " : - set Enter and continue");
							// Set Geofence Enter
							// Continue
							if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
								List<Geofence> currentGeofence = new ArrayList<>();
								currentGeofence.add(getGeofence(ze, Geofence.GEOFENCE_TRANSITION_ENTER));
								mGeofenceRequester.addGeofences(currentGeofence);
							}
						}
					} else {
						log.error("DoubleCheck - We are inside the fence " + ze.getName() + ". DistanceToCenter: " + distanceMeters + " Radius: " + radius);
						if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
							log.error("DoubleCheck - NOK - Exit " + ze.getName() + " : - set Exit and return");
							// Set Geofence Exit
							// Return
							if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
								List<Geofence> currentGeofence = new ArrayList<>();
								currentGeofence.add(getGeofence(ze, Geofence.GEOFENCE_TRANSITION_EXIT));
								mGeofenceRequester.addGeofences(currentGeofence);
							}
							NotificationUtil.showError(context, "Exit " + ze.getName() + " : False positives", origin + ": " + (radius - distanceMeters) + " difference");
							continue;
						} else {
							log.error("DoubleCheck - OK - Enter " + ze.getName() + " : - set Exit and continue");
							// Set Geofence Exit
							// Continue
							if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
								List<Geofence> currentGeofence = new ArrayList<>();
								currentGeofence.add(getGeofence(ze, Geofence.GEOFENCE_TRANSITION_EXIT));
								mGeofenceRequester.addGeofences(currentGeofence);
							}
						}
					}
				}
			}
			// Gesamtgenauigkeit/Accuracy berücksichtigen
			if (ze.getAccuracy() > 0 && accuracy > -1){
				if (accuracy > ze.getAccuracy()){
					log.debug("Actions will not be performed. Location accuracy bigger then given accuracy: " + accuracy + " > " + ze.getAccuracy());
					continue;
				}
			}

			// Post a notification
			NotificationUtil.sendNotification(context, getTransitionString(transition), ids, origin);

			// Fallback setzen
			if (ze.getServerEntity() != null && ze.getServerEntity().getId_fallback() != null && !ze.getServerEntity().getId_fallback().equals("")){
				fallback = ze.getServerEntity().getId_fallback();
			}else{
				fallback = null;
			}

			// Für Anzeige der Anwesenheit in der App, Status hier merken
			if (transition == Geofence.GEOFENCE_TRANSITION_ENTER){
				ze.setStatus(true);
			}else{
				ze.setStatus(false);
			}
			datasource.updateZoneField(ze.getName(), DbContract.ZoneEntry.CN_STATUS, ze.isStatus());
			// Broadcast to the Main, to refresh drawer.
			Intent intent = new Intent();
			intent.setAction(Constants.ACTION_STATUS_CHANGED);
			if (type.equalsIgnoreCase(Constants.GEOZONE)){
				intent.putExtra("state", Constants.GEOZONE);
			}else{
				intent.putExtra("state", Constants.BEACON);
			}
			context.sendBroadcast(intent);

			if (!checkWeekday(ze)){
				log.debug("Condition day of week for zone " + ze.getName() + " false!");
				continue;
			}

			// Kann erst ab Version 4.3 verwendet werden!
			if (checkConditionBluetoothDeviceConnected(context, ze, transition)){
				// Weiter
			}else{
				// Gerät nicht mit Bluetooth verbunden oder condition nicht konfiguriert
				continue;
			}

			String realLat = null;
			String realLng = null;
			String location_accuracy = null;
			String locationDate = null;
			String localLocationDate = null;
			if (location != null){
				realLat = Double.toString(location.getLatitude());
				realLng = Double.toString(location.getLongitude());
				location_accuracy = Float.valueOf(location.getAccuracy()).toString();

				TimeZone tz = TimeZone.getTimeZone("UTC");
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
				df.setTimeZone(tz);
				locationDate = df.format(new Date(location.getTime()));

				TimeZone tz1 = TimeZone.getDefault();
				DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
				df1.setTimeZone(tz1);
				localLocationDate = df1.format(new Date(location.getTime()));
			}

			// Broadcast to the plugins.
			boolean doBroadcast = Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_BROADCAST));
			if (doBroadcast){
				doBroadcastToPlugins(transition, ze, realLat, realLng, location_accuracy);
			}

			if (ze.getServerEntity() != null){
				log.info("Send server request...");

				String urlEntered = Utils.replaceAll(context, ze.getServerEntity().getUrl_enter(), ze.getName(), ze.getAlias(), transition, ze.getRadius(),
						ze.getLatitude(), ze.getLongitude(), realLat, realLng, locationDate, localLocationDate, location_accuracy);

				String urlExited = Utils.replaceAll(context, ze.getServerEntity().getUrl_exit(), ze.getName(), ze.getAlias(), transition, ze.getRadius(),
						ze.getLatitude(), ze.getLongitude(), realLat, realLng, locationDate, localLocationDate, location_accuracy);

				doServerRequest(transition, context, urlEntered, urlExited, ze.getServerEntity().getUrl_fhem(),
						ze.getName(), ze.getLatitude() == null ? "0" : ze.getLatitude(),
						ze.getLongitude() == null ? "0" : ze.getLongitude(),
						ze.getServerEntity().getCert(), ze.getServerEntity().getCert_password(),
						ze.getServerEntity().getCa_cert(), ze.getServerEntity().getUser(),
						ze.getServerEntity().getUser_pw(), ze.getServerEntity().getTimeout(), ze.getAlias(), realLat, realLng, null, false, 0);
			}
			if (ze.getMailEntity() != null){
				log.info("Send email...");
				if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER && ze.getMailEntity().isEnter()) ||
						(transition == Geofence.GEOFENCE_TRANSITION_EXIT && ze.getMailEntity().isExit())){

					String subjectReplace = Utils.replaceAll(context, ze.getMailEntity().getSubject(), ze.getName(), ze.getAlias(), transition, ze.getRadius(),
							ze.getLatitude(), ze.getLongitude(), realLat, realLng, locationDate, localLocationDate, location_accuracy);
					String textReplace = Utils.replaceAll(context, ze.getMailEntity().getBody(), ze.getName(), ze.getAlias(), transition, ze.getRadius(),
							ze.getLatitude(), ze.getLongitude(), realLat, realLng, locationDate, localLocationDate, location_accuracy);

					doSendMail(context, ze.getName(), subjectReplace, textReplace, ze.getMailEntity().getSmtp_user(), ze.getMailEntity().getSmtp_pw(), ze.getMailEntity().getSmtp_server(),
							ze.getMailEntity().getSmtp_port(), ze.getMailEntity().getFrom(), ze.getMailEntity().getTo(), ze.getMailEntity().isSsl(), ze.getMailEntity().isStarttls(), false);
				}
			}

			if (ze.getSmsEntity() != null){
				log.error(context.getString(R.string.info_1));
			}

			if (ze.getMoreEntity() != null){
				doWifi(context, ze, transition);
				doBluetooth(context, ze, transition);
				doSound(context, ze, transition);
				doSoundMM(context, ze, transition);
				doCallTasker(context, ze, transition);
			}

			doTracking(context, ze, transition);
		}
		// Reset
		checkLocation = null;
	}


	// checkConditionBluetoothDeviceConnected
	private boolean checkConditionBluetoothDeviceConnected(Context context, ZoneEntity zone, int transition){
		log.info("checkConditionBluetoothDeviceConnected ...");

		if (zone.getRequirementsEntity() == null) return true;

		String bt_name_enter = zone.getRequirementsEntity().getEnter_bt();
		String bt_name_exit = zone.getRequirementsEntity().getExit_bt();

		PackageManager packageManager = context.getPackageManager();
		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
			log.error("No bluetooth on device!");
			Toast.makeText(context, "No bluetooth on device!", Toast.LENGTH_LONG).show();
			return true;
		}

		if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
			if (bt_name_enter == null || bt_name_enter.equalsIgnoreCase("") || bt_name_enter.equalsIgnoreCase("none")) {
				return true;
			}
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			return bluetoothAdapter.isEnabled() && GlobalSingleton.getInstance().getBtDevicesConnected().contains(bt_name_enter);
		}

		if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
			if (bt_name_exit == null || bt_name_exit.equalsIgnoreCase("") || bt_name_exit.equalsIgnoreCase("none")) {
				return true;
			}
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			return bluetoothAdapter.isEnabled() && GlobalSingleton.getInstance().getBtDevicesConnected().contains(bt_name_exit);

		}
		return false;
	}

	// Auf Wochentage prüfen
	private boolean checkWeekday(ZoneEntity zone){
		log.info("checkWeekdays ...");

		if (zone.getRequirementsEntity() == null) return true;

		Calendar c = Calendar.getInstance();
		// Sonntag = 1
		// Montag = 2
		// usw.
		int day_of_week = c.get(Calendar.DAY_OF_WEEK);
		log.debug("Day is " + day_of_week);

		switch (day_of_week) {
			case Calendar.SUNDAY:
				return zone.getRequirementsEntity().isSun();
			case Calendar.MONDAY:
				return zone.getRequirementsEntity().isMon();
			case Calendar.TUESDAY:
				return zone.getRequirementsEntity().isTue();
			case Calendar.WEDNESDAY:
				return zone.getRequirementsEntity().isWed();
			case Calendar.THURSDAY:
				return zone.getRequirementsEntity().isThu();
			case Calendar.FRIDAY:
				return zone.getRequirementsEntity().isFri();
			case Calendar.SATURDAY:
				return zone.getRequirementsEntity().isSat();
			default:
				return true;
		}
	}



	// WLAN/Wifi
	public void doWifi(Context context, ZoneEntity zone, int transition){
		log.info("doWifi ...");
		PackageManager packageManager = context.getPackageManager();
		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)){
			log.error("No Wifi on device!");
			Toast.makeText(context, "No Wifi on device!", Toast.LENGTH_LONG).show();
			return;
		}

		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (transition == Geofence.GEOFENCE_TRANSITION_ENTER){
			if (zone.getMoreEntity().getEnter_wifi() != null){
				if (zone.getMoreEntity().getEnter_wifi() == 1){
					if (!wifiManager.isWifiEnabled())
						wifiManager.setWifiEnabled(true);
				}
				if (zone.getMoreEntity().getEnter_wifi() == 0){
					if (wifiManager.isWifiEnabled())
						wifiManager.setWifiEnabled(false);
				}
			}
		}
		if (transition == Geofence.GEOFENCE_TRANSITION_EXIT){
			if (zone.getMoreEntity().getExit_wifi() != null){
				if (zone.getMoreEntity().getExit_wifi() == 1){
					if (!wifiManager.isWifiEnabled())
						wifiManager.setWifiEnabled(true);
				}
				if (zone.getMoreEntity().getExit_wifi() == 0){
					if (wifiManager.isWifiEnabled())
						wifiManager.setWifiEnabled(false);
				}
			}
		}
	}
	// Bluetooth
	public void doBluetooth(Context context, ZoneEntity zone, int transition){
		log.info("doBluetooth ...");
		PackageManager packageManager = context.getPackageManager();
		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
			log.error("No bluetooth on device!");
			Toast.makeText(context, "No bluetooth on device!", Toast.LENGTH_LONG).show();
			return;
		}

		BluetoothAdapter bluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
		if (transition == Geofence.GEOFENCE_TRANSITION_ENTER){
			if (zone.getMoreEntity().getEnter_bt() != null){
				if (zone.getMoreEntity().getEnter_bt() == 1){
					if (!bluetoothAdapter.isEnabled())
						bluetoothAdapter.enable();
				}
				if (zone.getMoreEntity().getEnter_bt() == 0){
					if (bluetoothAdapter.isEnabled())
						bluetoothAdapter.disable();
				}
			}
		}
		if (transition == Geofence.GEOFENCE_TRANSITION_EXIT){
			if (zone.getMoreEntity().getExit_bt() != null){
				if (zone.getMoreEntity().getExit_bt() == 1){
					if (!bluetoothAdapter.isEnabled())
						bluetoothAdapter.enable();
				}
				if (zone.getMoreEntity().getExit_bt() == 0){
					if (bluetoothAdapter.isEnabled())
						bluetoothAdapter.disable();
				}
			}
		}
	}

	// Sound
	public void doSound(Context context, ZoneEntity zone, int transition){
		log.info("doSound ...");

		AudioManager aManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		if (transition == Geofence.GEOFENCE_TRANSITION_ENTER){
			if (zone.getMoreEntity().getEnter_sound() != null){
				final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					if (!notificationManager.isNotificationPolicyAccessGranted()) {
						NotificationUtil.sendErrorNotificationWithButtons(context, context.getString(R.string.doNotDisturbPermissionsTitle), context.getString(R.string.doNotDisturbPermissionsMessage));
						context.registerReceiver(myReceiver, new IntentFilter(Constants.ACTION_DONOTDISTURB_OK));
						context.registerReceiver(myReceiver, new IntentFilter(Constants.ACTION_DONOTDISTURB_NOK));
						return;
					}
				}
                if (zone.getMoreEntity().getEnter_sound() == 1) {
                    // Ton an
                    aManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
                if (zone.getMoreEntity().getEnter_sound() == 0) {
                    // Ton aus
                    aManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
                if (zone.getMoreEntity().getEnter_sound() == 3){
                    // Vibration an
                    aManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                }
			}
		}
		if (transition == Geofence.GEOFENCE_TRANSITION_EXIT){
			if (zone.getMoreEntity().getExit_sound() != null){
				final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					if (!notificationManager.isNotificationPolicyAccessGranted()) {
						NotificationUtil.sendErrorNotificationWithButtons(context, context.getString(R.string.doNotDisturbPermissionsTitle), context.getString(R.string.doNotDisturbPermissionsMessage));
						context.registerReceiver(myReceiver, new IntentFilter(Constants.ACTION_DONOTDISTURB_OK));
						context.registerReceiver(myReceiver, new IntentFilter(Constants.ACTION_DONOTDISTURB_NOK));
						return;
					}
				}
                if (zone.getMoreEntity().getExit_sound() == 1) {
                    // Ton an
                    aManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
                if (zone.getMoreEntity().getExit_sound() == 0) {
                    // Ton aus
                    aManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
                if (zone.getMoreEntity().getExit_sound() == 3){
                    // Vibration an
                    aManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                }
			}
		}
	}

	// SoundMM
	public void doSoundMM(Context context, ZoneEntity zone, int transition) {
		log.info("doSoundMM ...");

		AudioManager aManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
			if (zone.getMoreEntity().getEnter_soundMM() != null) {
				final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					if (!notificationManager.isNotificationPolicyAccessGranted()) {
						NotificationUtil.sendErrorNotificationWithButtons(context, context.getString(R.string.doNotDisturbPermissionsTitle), context.getString(R.string.doNotDisturbPermissionsMessage));
						context.registerReceiver(myReceiver, new IntentFilter(Constants.ACTION_DONOTDISTURB_OK));
						context.registerReceiver(myReceiver, new IntentFilter(Constants.ACTION_DONOTDISTURB_NOK));
					}
				}
				if (zone.getMoreEntity().getEnter_soundMM() == 1) {
					// MM Ton an
					int vol = aManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2;
					aManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
				}
				if (zone.getMoreEntity().getEnter_soundMM() == 0) {
					// MM Ton aus
					aManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
				}
			}
		}
		if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
			if (zone.getMoreEntity().getExit_soundMM() != null) {
				final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					if (!notificationManager.isNotificationPolicyAccessGranted()) {
						NotificationUtil.sendErrorNotificationWithButtons(context, context.getString(R.string.doNotDisturbPermissionsTitle), context.getString(R.string.doNotDisturbPermissionsMessage));
						context.registerReceiver(myReceiver, new IntentFilter(Constants.ACTION_DONOTDISTURB_OK));
						context.registerReceiver(myReceiver, new IntentFilter(Constants.ACTION_DONOTDISTURB_NOK));
					}
				}
				if (zone.getMoreEntity().getExit_soundMM() == 1) {
					// MM Ton an
					int vol = aManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2;
					aManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
				}
				if (zone.getMoreEntity().getExit_soundMM() == 0) {
					// MM Ton aus
					aManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
				}
			}
		}
	}


	// Mail senden
	public void doSendMail(Context context, String zone, String subject, String text, String mailUser, String mailUserPw, String mailSmtpHost,
						   String mailSmtpPort, String mailSender, String mailEmpf, boolean mailSsl, boolean mailStarttls, boolean test) {
		log.info("doSendMail");
		log.debug("zone: " + zone);
		log.debug("mail to: " + mailEmpf);
		log.debug("mail subject: " + subject);
		log.debug("mail text: " + text);
		log.debug("mail user: " + mailUser);
		log.debug("mail host: " + mailSmtpHost);
		log.debug("mail port: " + mailSmtpPort);
		log.debug("mail sender: " + mailSender);
		log.debug("mail ssl: " + mailSsl);
		log.debug("mail starttls: " + mailStarttls);

		try {
			// Mail senden
			SendMail smail = new SendMail(context, mailUser, mailUserPw, mailSmtpHost, mailSmtpPort, mailSender, mailEmpf, mailSsl, mailStarttls);
			smail.sendMail(subject, text, test);

		} catch (Exception ex) {
			Log.e(Constants.APPTAG, "error sending mail", ex);
			log.error(zone + ": Error sending mail", ex);
			NotificationUtil.showError(context, zone + ": Error sending mail", ex.toString());
			// TestErgebnis
			if (test){
				// Broadcats an die Main, damit der Drawer sich refreshed.
				Intent intent = new Intent();
				intent.setAction(Constants.ACTION_TEST_STATUS_NOK);
				intent.putExtra("TestResult", "Error sending mail: " + ex.toString());
				context.sendBroadcast(intent);
			}
		}
	}

	// Tasker aufrufen
	public void doCallTasker(Context context, ZoneEntity zone, int transition) {
		log.info("doCallTasker");
		log.debug("zone: " + zone.getId());

		try {
			// Task im Tasker aufrufen
			String task = null;
			String taskTransition = "";
			if (transition == Geofence.GEOFENCE_TRANSITION_ENTER){
				task = zone.getMoreEntity().getEnter_task();
				taskTransition = "1"; // wie in fhem
			}
			if (transition == Geofence.GEOFENCE_TRANSITION_EXIT){
				task = zone.getMoreEntity().getExit_task();
				taskTransition = "0"; // wie in fhem
			}

			// Wenn kein Task angegeben, dann nichts tun
			if (task == null || task.equals("")) return;

			// Task wurde angeben
			if (TaskerIntent.testStatus(context).equals( TaskerIntent.Status.OK)){
				TaskerIntent i = new TaskerIntent(task);

				// Parameter und Variablen für Task, die damit was anfangen können.
				i.addParameter(zone.getName()); // Zonenname
				i.addParameter(taskTransition); // Transition: 1 = Enter; 0 = Exit
				i.addParameter(zone.getLatitude()); // Breitengrad
				i.addParameter(zone.getLongitude()); // Längengrad

				i.addLocalVariable("%zone", zone.getName()); // Zonenname
				i.addLocalVariable("%transition", taskTransition); // Transition: 1 = Enter; 0 = Exit
				i.addLocalVariable("%latitude", zone.getLatitude()); // Breitengrad
				i.addLocalVariable("%longitude", zone.getLongitude()); // Längengrad

				context.sendBroadcast(i);

			}else if (TaskerIntent.testStatus(context).equals( TaskerIntent.Status.NoPermission)){
				Log.e(Constants.APPTAG, "NoPermission: calling app does not have the needed Android permission");
				log.error(zone.getId() + ": NoPermission: calling app does not have the needed Android permission");
				NotificationUtil.showError(context, zone.getId() + ": Error calling Tasker", "NoPermission: calling app does not have the needed Android permission");
			}else if (TaskerIntent.testStatus(context).equals( TaskerIntent.Status.NoReceiver)){
				Log.e(Constants.APPTAG, "NoReceiver: nothing is listening for TaskerIntents. Probably a Tasker bug.");
				log.error(zone.getId() + ": NoReceiver: nothing is listening for TaskerIntents. Probably a Tasker bug.");
				NotificationUtil.showError(context, zone.getId() + ": Error calling Tasker", "NoReceiver: nothing is listening for TaskerIntents. Probably a Tasker bug.");
			}else if (TaskerIntent.testStatus(context).equals( TaskerIntent.Status.NotEnabled)){
				Log.e(Constants.APPTAG, "NotEnabled: Tasker is disabled by the user.");
				log.error(zone.getId() + ": NotEnabled: Tasker is disabled by the user.");
				NotificationUtil.showError(context, zone.getId() + ": Error calling Tasker", "NotEnabled: Tasker is disabled by the user.");
			}else if (TaskerIntent.testStatus(context).equals( TaskerIntent.Status.NotInstalled)){
				Log.e(Constants.APPTAG, "NotInstalled: no Tasker App could be found on the device");
				log.error(zone.getId() + ": NotInstalled: no Tasker App could be found on the device");
				NotificationUtil.showError(context, zone.getId() + ": Error calling Tasker", "NotInstalled: no Tasker App could be found on the device");
			}else if (TaskerIntent.testStatus(context).equals( TaskerIntent.Status.AccessBlocked)){
				Log.e(Constants.APPTAG, "AccessBlocked: external access is blocked in the user preferences.");
				log.error(zone.getId() + ": AccessBlocked: external access is blocked in the user preferences.");
				NotificationUtil.showError(context, zone.getId() + ": Error calling Tasker", "AccessBlocked: external access is blocked in the user preferences.");
			}
		} catch (Exception ex) {
			Log.e(Constants.APPTAG, "error calling Tasker", ex);
			log.error(zone.getId() + ": Error calling Tasker", ex);
			NotificationUtil.showError(context, zone.getId() + ": Error calling Tasker", ex.toString());
		}
	}

	// SMS senden
	public void doSendSms(Context context, String zone, String to, String text, boolean test) {
		PackageManager packageManager = context.getPackageManager();
		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)){
			log.error("No Telephony on device!");
			Toast.makeText(context, "No Telephony on device!", Toast.LENGTH_LONG).show();
			return;
		}

		try {
			log.info("doSendSms");
			log.debug("zone: " + zone);
			log.debug("sms to: " + to);
			log.debug("sms text: " + text);

			// SMS senden
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(to, null, text, null, null);

			// TestErgebnis
			if (test){
				// Broadcats damit der Test-Dialog angezeigt wird
				Intent intent = new Intent();
				intent.setAction(Constants.ACTION_TEST_STATUS_OK);
				context.sendBroadcast(intent);
			}

		} catch (Exception ex) {
			Log.e(Constants.APPTAG, "error sending sms", ex);
			log.error(zone + ": Error sending sms", ex);
			NotificationUtil.showError(context, zone + ": Error sending sms", ex.toString());
			// TestErgebnis
			if (test){
				// Broadcats damit der Test-Dialog angezeigt wird
				Intent intent = new Intent();
				intent.setAction(Constants.ACTION_TEST_STATUS_NOK);
				context.sendBroadcast(intent);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doServerRequest(final int transition, final Context context, final String urlEntered, final String urlExited, final String fhemGeofancyUrl, final String zone, final String latitude, final String longitude,
								final String cert, final String certPasswd, final String caCert, final String user, final String userPasswd, final String timeout, final String alias,
								final String realLat, final String realLng, final String realTime, final boolean test, final int retrys) {

		log.info("doServerRequest");
		try {
			AuthenticationParameters authParams = new AuthenticationParameters();
			String fhemTransition = "";
			log.debug("Transition: " + transition);

			// 1 == Enter
			if (transition == Geofence.GEOFENCE_TRANSITION_ENTER){
				authParams.setUrl(urlEntered);
				fhemTransition = "1";
			}
			// 2 = Exit
			if (transition == Geofence.GEOFENCE_TRANSITION_EXIT){
				authParams.setUrl(urlExited);
				fhemTransition = "0";
			}
			// Wenn keine URL angegeben wurde, dann nichts tun
			if (TextUtils.isEmpty(urlEntered) && TextUtils.isEmpty(urlExited) && TextUtils.isEmpty(fhemGeofancyUrl)){
				log.info("No URL set. Exiting.");
				return;
			}else{
				// Hier URL für Fhem basteln, wenn Fhem-Adresse/Location angegeben wurde.
				if (!TextUtils.isEmpty(fhemGeofancyUrl)){
					log.info("FhemGeofancyUrl: requested");

					TimeZone tz = TimeZone.getTimeZone("UTC");
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
					df.setTimeZone(tz);
					String nowAsISO = df.format(new Date());
					final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
					// Use the Android ID unless it's broken, in which case fallback on deviceId,
					// unless it's not available, then fallback on a random number which we store
					// to a prefs file
					UUID uuid = null;
					try {
						if (!"9774d56d682e549c".equals(androidId)) {
							uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
						} else {
							final String deviceId = ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
							uuid = deviceId!=null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
						}
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					} catch (SecurityException e) {
					// Permission read phone state is missing
					}

				// /$infix?id=UUIDloc&name=locName&entry=(1|0)&date=DATE&latitude=xx.x&longitude=xx.x&device=UUIDdev
					StringBuilder fhemGeofancy = new StringBuilder();
					fhemGeofancy.append("id=");
					fhemGeofancy.append(uuid != null ? uuid.toString() : "0");
					fhemGeofancy.append("&name=");
					fhemGeofancy.append(TextUtils.isEmpty(alias) ? zone : alias);
					fhemGeofancy.append("&entry=");
					fhemGeofancy.append(fhemTransition);
					fhemGeofancy.append("&date=");
					fhemGeofancy.append(realTime != null ? realTime : nowAsISO);
					fhemGeofancy.append("&latitude=");
					fhemGeofancy.append(latitude);
					fhemGeofancy.append("&longitude=");
					fhemGeofancy.append(longitude);
					fhemGeofancy.append("&device=");
					fhemGeofancy.append(uuid.toString());

					if (fhemGeofancyUrl.endsWith("?")){
						authParams.setUrl(fhemGeofancyUrl + fhemGeofancy.toString());
					}else{
						authParams.setUrl(fhemGeofancyUrl + "?" + fhemGeofancy.toString());
					}
				}
			}

            // No Url set --> return
            if (authParams.getUrl().isEmpty()) return;

			authParams.setClientCertificate(TextUtils.isEmpty(cert) ? null : getClientCertFile(cert));
			authParams.setClientCertificatePassword(TextUtils.isEmpty(certPasswd) ? null : certPasswd);
			authParams.setCaCertificate(TextUtils.isEmpty(caCert) ? null : readCaCert(caCert));
			authParams.setUser(TextUtils.isEmpty(user) ? null : user);
			authParams.setUserPasswd(TextUtils.isEmpty(userPasswd) ? null : userPasswd);

			log.debug("server zone: " + zone);
			log.debug("server alias: " + alias);
			log.debug("server urlEntered: " + urlEntered);
			log.debug("server urlExited: " + urlExited);
			log.debug("server urlFhemGeofancy: " + fhemGeofancyUrl);

			log.debug("server url chosen: " + authParams.getUrl());
			log.debug("server  user: " + user);
			log.debug("server client_cert: " + cert);
			log.debug("server ca_cert: " + caCert);

			log.debug("server latitude: " + latitude);
			log.debug("server longitude: " + longitude);
			log.debug("server timeout: " + timeout);


			Log.d(Constants.APPTAG, "SimpleGeofence: " + authParams.getUrl());

			geoApi = new Api(authParams, timeout);

			new AsyncTask() {
				@Override
				protected Object doInBackground(Object... objects) {

					try {
						geoApi.doGet();

						int responseCode = geoApi.getLastResponseCode();
						if (responseCode == 200) {
							if (test){
								// Broadcats damit der Test-Dialog angezeigt wird
								Intent intent = new Intent();
								intent.setAction(Constants.ACTION_TEST_STATUS_OK);
								intent.putExtra("TestType", "GeoZone");
								context.sendBroadcast(intent);
							}

							// Löschen des Events, damit der "alte" nicht mehr ausgeführt wird.
							// Wenn Schalter Retry true
							RetryRequestQueue.removePref(context, zone);

							log.info("Response code after get: "  + responseCode);
						} else {
							if(fallback != null){
								datasourceServer = new DbServerHelper(context);
								ServerEntity se = datasourceServer.getCursorServerByName(fallback);
//								String fallbackServer = fallback;
								fallback = null;
								doServerRequest(transition, context, se.getUrl_enter(), se.getUrl_exit(), se.getUrl_fhem(), TextUtils.isEmpty(alias) ? zone : alias, latitude, longitude, se.getCert(),
										se.getCert_password(), se.getCa_cert(), se.getUser(), se.getUser_pw(), se.getTimeout(), alias, realLat, realLng, realTime, test, 0);
                                // Rest überspringen, da Fallback
                                return null;
                            }
							log.error("Response code after get: "  + responseCode);
							NotificationUtil.showError(context, zone + ": Error (GR01) in get of the server response", "Response Code: " + responseCode);
							if (test){
								// Broadcats damit der Test-Dialog angezeigt wird
								Intent intent = new Intent();
								intent.setAction(Constants.ACTION_TEST_STATUS_NOK);
								intent.putExtra("TestResult", "Error (GR01) in get of the server response. Response Code: " + responseCode);
								intent.putExtra("TestType", "GeoZone");
								context.sendBroadcast(intent);
							}

						}
					} catch (Throwable ex) {
						if(fallback != null){
							datasourceServer = new DbServerHelper(context);
							ServerEntity se = datasourceServer.getCursorServerByName(fallback);
							String fallbackServer = fallback;
							fallback = null;
							doServerRequest(transition, context, se.getUrl_enter(), se.getUrl_exit(), se.getUrl_fhem(), TextUtils.isEmpty(alias) ? zone : alias, latitude, longitude, se.getCert(),
									se.getCert_password(), se.getCa_cert(), se.getUser(), se.getUser_pw(), se.getTimeout(), alias, realLat, realLng, realTime, test, 0);
                            // Rest überspringen, da Fallback
                            return null;
                        }

						log.error(zone + ": Error (GR02) in get of the server response", ex);
						NotificationUtil.showError(context, zone + ": Error (GR02) in get of the server response", ex.toString());

						// Speichern des Events für einen späteren Request, wenn wieder Internet verfügbar ist.
						// Wenn Schalter Retry true
						if (!test && retrys < 6) {
							log.debug("Store retry number: " + retrys + 1);
							RetryRequestQueue.setRequest(context, zone, transition, realLat, realLng, realTime, retrys + 1);
							log.debug("############ RetryJob: 1 ");
							// If Android 7+ retry with JobScheduler
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
								log.debug("############ RetryJob: 2 ");
								try {
									RetryJobSchedulerService retryJobScheduler;
									ComponentName serviceComponent = new ComponentName(context, RetryJobSchedulerService.class);
									JobInfo.Builder builder = new JobInfo.Builder(kJobId++, serviceComponent);
									builder.setMinimumLatency(5 * 1000);
									builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
									JobInfo jobInfo = builder.build();
									JobScheduler jobScheduler =  (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
									int result = jobScheduler.schedule(jobInfo);
									log.debug("############ RetryJob: result = " + result);
									if (result == JobScheduler.RESULT_SUCCESS) {
										log.error("RetryJob scheduled successfully!");
									}
								}catch(Exception e){
									log.error("Error Starting RetryJob with JobScheduler: " + e);
								}
							}
							log.error("The request for " + zone + " is queued and will be retried, when internet connection is available.");
						}

						if (test){
							// Broadcats damit der Test-Dialog angezeigt wird
							Intent intent = new Intent();
							intent.setAction(Constants.ACTION_TEST_STATUS_NOK);
							intent.putExtra("TestResult", "Error (GR02) in get of the server response: " + ex.toString());
							intent.putExtra("TestType", "GeoZone");
							context.sendBroadcast(intent);
						}
					}
					return null;
				}

				@Override
				protected void onProgressUpdate(final Object... values) {
//                        StringBuilder buf = new StringBuilder();
//                        for (final Object value : values) {
//                            buf.append(value.toString());
//                        }
				}

				@Override
				protected void onPostExecute(final Object result) {
				}

			}.execute();

		} catch (Exception ex) {
			Log.e(Constants.APPTAG, "error sending server request", ex);
			log.error(zone + ": Error (GR03) sending server request", ex);
			NotificationUtil.showError(context, zone + ": Error (GR03) sending server request", ex.toString());
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

	// Broadcast only if plugins are installed
	private void doBroadcastToPlugins(int transition, ZoneEntity ze, String realLat, String realLng, String location_accuracy){

		PackageManager manager = context.getPackageManager();
		Intent intent = new Intent();
		intent.setAction(Constants.ACTION_EGIGEOZONE_GETPLUGINS);

		// Query for all activities that match my filter and request that the filter used
		//  to match is returned in the ResolveInfo
		List<ResolveInfo> infos = manager.queryIntentActivities (intent, PackageManager.GET_RESOLVED_FILTER);
		for (ResolveInfo info : infos) {
			ActivityInfo activityInfo = info.activityInfo;
			IntentFilter filter = info.filter;
			if (filter != null && filter.hasAction(Constants.ACTION_EGIGEOZONE_GETPLUGINS)){
				String pckg = activityInfo.packageName;

				if (pckg == null || pckg.equals("")) continue;

				// Broadcast starten
				Intent plugintIntent = new Intent();
				plugintIntent.setAction(Constants.ACTION_EGIGEOZONE_PLUGIN_EVENT);
				plugintIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				plugintIntent.setPackage(pckg);

				String pluginTransition = "";
				if (transition == Geofence.GEOFENCE_TRANSITION_ENTER){
					pluginTransition = "1"; // wie in fhem
				}
				if (transition == Geofence.GEOFENCE_TRANSITION_EXIT){
					pluginTransition = "0"; // wie in fhem
				}

				plugintIntent.putExtra("zone_name", TextUtils.isEmpty(ze.getAlias()) ? ze.getName() : ze.getAlias()); // String
				plugintIntent.putExtra("transition", pluginTransition); // int
				plugintIntent.putExtra("latitude", ze.getLatitude()); // String
				plugintIntent.putExtra("longitude", ze.getLongitude()); // String

				plugintIntent.putExtra("realLatitude", realLat);
				plugintIntent.putExtra("realLongitude", realLng);
				plugintIntent.putExtra("location_accuracy", location_accuracy);

				TimeZone tz = TimeZone.getTimeZone("UTC");
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
				df.setTimeZone(tz);
				String nowAsISO = df.format(new Date());
				final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
				// Use the Android ID unless it's broken, in which case fallback on deviceId,
				// unless it's not available, then fallback on a random number which we store
				// to a prefs file
				UUID uuid = null;
				try {
					if (!"9774d56d682e549c".equals(androidId)) {
						uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
					} else {
						final String deviceId = ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
						uuid = deviceId!=null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
					}
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				} catch (SecurityException e) {
					// Permission read phone state is missing
				}

				plugintIntent.putExtra("device_id", uuid != null ? uuid.toString() : "0"); // String
				plugintIntent.putExtra("date_iso", nowAsISO); // String

				TimeZone tz1 = TimeZone.getDefault();
				DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
				df.setTimeZone(tz1);
				String nowAsLocal = df1.format(new Date());

				plugintIntent.putExtra("date_device", nowAsLocal); // String

				context.sendBroadcast(plugintIntent);
			}
		}
	}

	// Trackeing starten/stoppen
	private void doTracking(Context context, ZoneEntity ze, int transition) {
		log.info("doTracker");
		log.debug("zone: " + ze.getName());
		try {

			// Zonen Einstellungen
			String zone = ze.getName();
			boolean trackEnter = ze.isEnter_tracker();
			boolean trackExit = ze.isExit_tracker();
			boolean trackToFile = ze.isTrack_to_file();
//			String trackUrl = ze.getTrack_url();
			int trackIntervallZone = ze.getLocal_tracking_interval() == null || ze.getLocal_tracking_interval() == 0 ? 5 : ze.getLocal_tracking_interval();

			if (transition == Geofence.GEOFENCE_TRANSITION_ENTER){
				if (trackEnter){
					TrackingUtils.startTracking(context, zone, trackIntervallZone, trackToFile, (ze.getTrackServerEntity() != null), (ze.getTrackMailEntity() != null));
				}else{
					if (TrackingUtils.exists(context, zone) > 0) TrackingUtils.stopTracking(context, zone);
				}
			}

			if (transition == Geofence.GEOFENCE_TRANSITION_EXIT){
				if (trackExit){
					TrackingUtils.startTracking(context, zone, trackIntervallZone, trackToFile, (ze.getTrackServerEntity() != null), (ze.getTrackMailEntity() != null));
				}else{
					if (TrackingUtils.exists(context, zone) > 0) TrackingUtils.stopTracking(context, zone);
				}
			}

		} catch (Exception ex) {
			Log.e(Constants.APPTAG, "error starting location tracking", ex);
			log.error(ze.getName() + ": Error starting location tracking", ex);
			NotificationUtil.showError(context, ze.getName() + ": Error starting location tracking", ex.toString());
		}
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		// Begin polling for new location updates.
		// Note that this can be NULL if last location isn't already known.
		LocationRequest mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		try {
			// Get last known recent location.
			Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
			if (mCurrentLocation != null) {
				// Print current location if not null
				log.debug("1-a GeofencingFalsePositives onConnected: " + mCurrentLocation.toString());
			}

			LocationServices.FusedLocationApi.requestLocationUpdates (mLocationClient, mLocationRequest, this);

		}catch (SecurityException se){
			// Display UI and wait for user interaction
//			AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
//			alertDialogBuilder.setMessage(this.getString(R.string.alertPermissions));
//			alertDialogBuilder.setTitle(this.getString(R.string.titleAlertPermissions));
//
//			alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface arg0, int arg1) {
//				}
//			});
//			AlertDialog alertDialog = alertDialogBuilder.create();
//			alertDialog.show();
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		log.debug("2-a GeofencingFalsePositives onConnectionSuspended ");
		doWork();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		log.debug("3-a GeofencingFalsePositives onConnectionFailed ");
		doWork();
	}

	@Override
	public void onLocationChanged(Location location) {
		log.debug("4-a GeofencingFalsePositives onLocationChanged ");
		log.debug("4-b GeofencingFalsePositives location reported: " + Double.valueOf(location.getLatitude()).toString());
		log.debug("4-c GeofencingFalsePositives location reported: " + Double.valueOf(location.getLongitude()).toString());

		checkLocation = location;

		mLocationClient.disconnect();

		doWork();
	}

    private Geofence getGeofence(ZoneEntity ze, int transition){
        return  new Geofence.Builder().setRequestId(ze.getName())
                .setTransitionTypes(transition)
                .setCircularRegion(Double.valueOf(ze.getLatitude()), Double.valueOf(ze.getLongitude()), ze.getRadius())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    private SimpleGeofence getSimpleGeofence(ZoneEntity ze){
        return  new SimpleGeofence(ze.getName(), ze.getLatitude(), ze.getLongitude(),
                Integer.toString(ze.getRadius()), null, 0, 0, true, null);
    }
	/**
	 * Maps geofence transition types to their human-readable equivalents.
	 * @param transitionType A transition type constant defined in Geofence
	 * @return A String indicating the type of transition
	 */
	private String getTransitionString(int transitionType) {
		switch (transitionType) {
			case Geofence.GEOFENCE_TRANSITION_ENTER:
				return context.getString(R.string.geofence_transition_entered);

			case Geofence.GEOFENCE_TRANSITION_EXIT:
				return context.getString(R.string.geofence_transition_exited);

			default:
				return context.getString(R.string.geofence_transition_unknown);
		}
	}
}




















