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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.Worker;
import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.db.DbZoneHelper;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.geofence.RetryRequestQueue;

public class Utils {
	private static int themeInd = 0;
	private static DbGlobalsHelper dbGlobalsHelper;

	/**
	 * Ersetzen von Text
	 */
	private static String replaceVar(String source, String search, String replace) {
		if (search.equals(replace)) {
			return source; // kann ja sein, dass wir nichts tun müssen
		}

		StringBuilder result = new StringBuilder();
		int len = search.length();
		if (len == 0) {
			return source; // verhindert Endlosschleife bei search.equals("");
		}

		int pos = 0; // position
		int nPos; // next position
		do {
			nPos = source.indexOf(search, pos);
			if (nPos != -1) { // gefunden
				result.append(source.substring(pos, nPos));
				result.append(replace);
				pos = nPos + len;
			} else { // nicht gefunden
				result.append(source.substring(pos)); // letzter abschnitt
			}
		} while (nPos != -1);

		return result.toString();
	}
	/**
	 * Aus String true oder false ein boolean zurück geben
	 */
	public static boolean isBoolean(String bool) {
		return bool != null && bool.equals("true");
	}

	public static ZoneEntity makeTestZone(){
		ZoneEntity ze = new ZoneEntity();
		ze.setName("TestZone");
		ze.setLatitude("46");
		ze.setLongitude("10");
		ze.setRadius(500);
		return ze;
	}

	/**
	 * Maps geofence transition types to their human-readable equivalents.
	 * @param transitionType A transition type constant defined in Geofence
	 * @return A String indicating the type of transition
	 */
	private static String getTransitionString(Context context, int transitionType) {
		switch (transitionType) {
			case Geofence.GEOFENCE_TRANSITION_ENTER:
				return context.getString(R.string.geofence_transition_entered);

			case Geofence.GEOFENCE_TRANSITION_EXIT:
				return context.getString(R.string.geofence_transition_exited);

			default:
				return context.getString(R.string.geofence_transition_unknown);
		}
	}

	public static String replaceAll(Context context, String in, String zone, String alias, int transition, Integer radius, String lat, String lng,
							  String realLat, String realLng, String locationDate, String localLocationDate, String location_accuracy){

		// UTC time
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
		df.setTimeZone(tz);
		String nowAsISO = df.format(new Date());

		// Local device time
		TimeZone tz1 = TimeZone.getDefault();
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		df1.setTimeZone(tz1);
		String nowAsLocal = df1.format(new Date());

		final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		// Use the Android ID unless it's broken, in which case fallback on deviceId,
		// unless it's not available, then fallback on a random number which we store
		// to a prefs file
		UUID uuidAndroidId = null;
		UUID uuidDeviceId = null;
		try{
			uuidAndroidId = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
			final String deviceId = ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
			try {
				uuidDeviceId = deviceId!=null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		} catch (SecurityException e) {
			// Permission read phone state is missing
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		in = replaceVar(in, Constants.ZONE, TextUtils.isEmpty(alias) ? zone : alias);
		in = replaceVar(in, Constants.TRANSITION, getTransitionString(context, transition)); // Textual
		in = replaceVar(in, Constants.TRANSITIONTYPE, transition == 2 ? "0" : Integer.toString(transition)); // 1 or 2 = 0, like in Fhem
		in = replaceVar(in, Constants.LAT, lat);
		in = replaceVar(in, Constants.LNG, lng);
		in = replaceVar(in, Constants.RADIUS, radius.toString());
		in = replaceVar(in, Constants.REALLAT, realLat == null ? "" : realLat);
		in = replaceVar(in, Constants.REALLGN, realLng == null ? "" : realLng);
		in = replaceVar(in, Constants.ANDROIDID, uuidAndroidId != null ? uuidAndroidId.toString() : "0");
		in = replaceVar(in, Constants.DEVICEID, uuidDeviceId != null ? uuidDeviceId.toString() : "0");
		in = replaceVar(in, Constants.ACCURACY, location_accuracy == null ? "0" : location_accuracy );

		in = replaceVar(in, Constants.DATE, nowAsISO);
		in = replaceVar(in, Constants.LOCALDATE, nowAsLocal);
		in = replaceVar(in, Constants.LOCATIONDATE, locationDate == null ? nowAsISO : locationDate);
		in = replaceVar(in, Constants.LOCALLOCATIONDATE, localLocationDate == null ? nowAsLocal : localLocationDate);

		return in;
	}

	public static String replaceAllTracking(Context context, String in, String zone, String alias, String realLat, String realLng, String location_local_time, String location_utc_time, String location_accuracy){
		// UTC time
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
		// Local device time
		TimeZone tz1 = TimeZone.getDefault();
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
		df1.setTimeZone(tz1);
		String nowAsLocal = df1.format(new Date());

		final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        // Use the Android ID unless it's broken, in which case fallback on deviceId,
        // unless it's not available, then fallback on a random number which we store
        // to a prefs file
        UUID uuidAndroidId = null;
        UUID uuidDeviceId = null;
        try{
            uuidAndroidId = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
            final String deviceId = ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
            try {
                uuidDeviceId = deviceId!=null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } catch (SecurityException e) {
            // Permission read phone state is missing
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        in = replaceVar(in, Constants.ZONE, TextUtils.isEmpty(alias) ? zone : alias);
        in = replaceVar(in, Constants.REALLAT, realLat == null ? "" : realLat);
        in = replaceVar(in, Constants.REALLGN, realLng == null ? "" : realLng);
        in = replaceVar(in, Constants.ANDROIDID, uuidAndroidId != null ? uuidAndroidId.toString() : "0");
        in = replaceVar(in, Constants.DEVICEID, uuidDeviceId != null ? uuidDeviceId.toString() : "0");
        in = replaceVar(in, Constants.ACCURACY, location_accuracy);
        // This variables are not available at tracking
        in = replaceVar(in, Constants.TRANSITION, Constants.NA);
        in = replaceVar(in, Constants.TRANSITIONTYPE, Constants.NA);
        in = replaceVar(in, Constants.LAT, Constants.NA);
        in = replaceVar(in, Constants.LNG, Constants.NA);
        in = replaceVar(in, Constants.RADIUS, Constants.NA);

		in = replaceVar(in, Constants.DATE, nowAsISO);
		in = replaceVar(in, Constants.LOCALDATE, nowAsLocal);
		in = replaceVar(in, Constants.LOCATIONDATE, location_utc_time == null ? nowAsISO : location_utc_time);
		in = replaceVar(in, Constants.LOCALLOCATIONDATE, location_local_time == null ? nowAsLocal : location_local_time);

		return in;
    }

	/**
	 * Change backgroundcolor of Toolbar
	 * @param activity
	 * @param toolbar
     */
	public static void changeBackGroundToolbar(Activity activity, Toolbar toolbar) {
		switch (themeInd) {
			case 0:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
				break;
			case 1:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary1));
				break;
			case 2:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary2));
				break;
			case 3:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary3));
				break;
			case 4:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary4));
				break;
			case 5:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary5));
				break;
			case 6:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary6));
				break;
			case 7:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary7));
				break;
			case 8:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary8));
				break;
			case 9:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary9));
				break;
			case 10:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary10));
				break;
			case 11:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary11));
				break;
			case 12:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary12));
				break;
			case 13:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary13));
				break;
			case 14:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary14));
				break;
			case 15:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary15));
				break;
			case 16:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary16));
				break;
			case 17:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary18));
				break;
			case 18:
				toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.primary18));
				break;
		}
	}

	/**
	 * Change background of NavigationView
	 * @param navigationView
     */
//	public static void changeBackGroundNavigationView(NavigationView navigationView) {
//		View header = navigationView.getHeaderView(0);
//		switch (themeInd) {
//			case 0:
//				header.setBackgroundResource(R.drawable.side_nav_bar);
//				break;
//			case 1:
//				header.setBackgroundResource(R.drawable.side_nav_header1);
//				break;
//			case 2:
//				header.setBackgroundResource(R.drawable.side_nav_header2);
//				break;
//			case 3:
//				header.setBackgroundResource(R.drawable.side_nav_header3);
//				break;
//		}
//	}

	/**
	 * Change Theme
	 * @param activity
	 * @param theme
     */
	public static void changeToTheme(Activity activity, int theme) {
		themeInd = theme;
		activity.finish();
		activity.startActivity(new Intent(activity, activity.getClass()));
		activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

    /**
     * Set Theme onCreate of AlertDialog
     * @param context
     */
    public static AlertDialog.Builder onAlertDialogCreateSetTheme(Context context) {
        dbGlobalsHelper = new DbGlobalsHelper(context);
        String themeIndexString = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_THEME);
        if (themeIndexString == null){
            themeInd = 0;
        }else{
            try {
                themeInd = Integer.parseInt(themeIndexString);
            }catch (Exception e){
                themeInd = 0;
            }
        }

        switch (themeInd) {
            case 0:
                return new AlertDialog.Builder(context, R.style.StyledDialog);
            case 1:
                return new AlertDialog.Builder(context, R.style.StyledDialog1);
            case 2:
                return new AlertDialog.Builder(context, R.style.StyledDialog2);
            case 3:
                return new AlertDialog.Builder(context, R.style.StyledDialog3);
            case 4:
                return new AlertDialog.Builder(context, R.style.StyledDialog4);
            case 5:
                return new AlertDialog.Builder(context, R.style.StyledDialog5);
            case 6:
                return new AlertDialog.Builder(context, R.style.StyledDialog6);
            case 7:
                return new AlertDialog.Builder(context, R.style.StyledDialog7);
            case 8:
                return new AlertDialog.Builder(context, R.style.StyledDialog8);
            case 9:
                return new AlertDialog.Builder(context, R.style.StyledDialog9);
            case 10:
                return new AlertDialog.Builder(context, R.style.StyledDialog10);
            case 11:
                return new AlertDialog.Builder(context, R.style.StyledDialog11);
            case 12:
                return new AlertDialog.Builder(context, R.style.StyledDialog12);
            case 13:
                return new AlertDialog.Builder(context, R.style.StyledDialog13);
            case 14:
                return new AlertDialog.Builder(context, R.style.StyledDialog14);
            case 15:
                return new AlertDialog.Builder(context, R.style.StyledDialog15);
            case 16:
                return new AlertDialog.Builder(context, R.style.StyledDialog16);
            case 17:
                return new AlertDialog.Builder(context, R.style.StyledDialog17);
            case 18:
                return new AlertDialog.Builder(context, R.style.StyledDialog18);
        }
        return new AlertDialog.Builder(context, R.style.StyledDialog);
    }

	/**
	 * Set DialogTheme onCreate of Activity
	 * @param activity
	 */
	public static void onActivityCreateSetDialogTheme(Activity activity) {
		dbGlobalsHelper = new DbGlobalsHelper(activity);
		String themeIndexString = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_THEME);
		if (themeIndexString == null){
			themeInd = 0;
		}else{
			try {
				themeInd = Integer.parseInt(themeIndexString);
			}catch (Exception e){
				themeInd = 0;
			}
		}

		switch (themeInd) {
			case 0:
				activity.setTheme(R.style.StyledDialog);
				break;
			case 1:
				activity.setTheme(R.style.StyledDialog1);
				break;
			case 2:
				activity.setTheme(R.style.StyledDialog2);
				break;
			case 3:
				activity.setTheme(R.style.StyledDialog3);
				break;
			case 4:
				activity.setTheme(R.style.StyledDialog4);
				break;
			case 5:
				activity.setTheme(R.style.StyledDialog5);
				break;
			case 6:
				activity.setTheme(R.style.StyledDialog6);
				break;
			case 7:
				activity.setTheme(R.style.StyledDialog7);
				break;
			case 8:
				activity.setTheme(R.style.StyledDialog8);
				break;
			case 9:
				activity.setTheme(R.style.StyledDialog9);
				break;
			case 10:
				activity.setTheme(R.style.StyledDialog10);
				break;
			case 11:
				activity.setTheme(R.style.StyledDialog11);
				break;
			case 12:
				activity.setTheme(R.style.StyledDialog12);
				break;
			case 13:
				activity.setTheme(R.style.StyledDialog13);
				break;
			case 14:
				activity.setTheme(R.style.StyledDialog14);
				break;
			case 15:
				activity.setTheme(R.style.StyledDialog15);
				break;
			case 16:
				activity.setTheme(R.style.StyledDialog16);
				break;
			case 17:
				activity.setTheme(R.style.StyledDialog17);
				break;
			case 18:
				activity.setTheme(R.style.StyledDialog18);
				break;
		}
	}

	/**
	 * Set Theme onCreate of Activity
	 * @param activity
	 */
	public static void onActivityCreateSetTheme(Activity activity) {
		dbGlobalsHelper = new DbGlobalsHelper(activity);
		String themeIndexString = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_THEME);
		if (themeIndexString == null){
			themeInd = 0;
		}else{
			try {
				themeInd = Integer.parseInt(themeIndexString);
			}catch (Exception e){
				themeInd = 0;
			}
		}

		switch (themeInd) {
			case 0:
				activity.setTheme(R.style.AppTheme);
				break;
			case 1:
				activity.setTheme(R.style.AppTheme1);
				break;
			case 2:
				activity.setTheme(R.style.AppTheme2);
				break;
			case 3:
				activity.setTheme(R.style.AppTheme3);
				break;
			case 4:
				activity.setTheme(R.style.AppTheme4);
				break;
			case 5:
				activity.setTheme(R.style.AppTheme5);
				break;
			case 6:
				activity.setTheme(R.style.AppTheme6);
				break;
			case 7:
				activity.setTheme(R.style.AppTheme7);
				break;
			case 8:
				activity.setTheme(R.style.AppTheme8);
				break;
			case 9:
				activity.setTheme(R.style.AppTheme9);
				break;
			case 10:
				activity.setTheme(R.style.AppTheme10);
				break;
			case 11:
				activity.setTheme(R.style.AppTheme11);
				break;
			case 12:
				activity.setTheme(R.style.AppTheme12);
				break;
			case 13:
				activity.setTheme(R.style.AppTheme13);
				break;
			case 14:
				activity.setTheme(R.style.AppTheme14);
				break;
			case 15:
				activity.setTheme(R.style.AppTheme15);
				break;
			case 16:
				activity.setTheme(R.style.AppTheme16);
				break;
			case 17:
				activity.setTheme(R.style.AppTheme17);
				break;
			case 18:
				activity.setTheme(R.style.AppTheme18);
				break;
		}
	}

	public static int getThemeInd() {
		return themeInd;
	}


	public static void doRetry(Context context, Logger log){
		ConnectivityManager conn =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conn.getActiveNetworkInfo();
		// RequestQueue abarbeiten
		if (networkInfo != null && networkInfo.isConnected()) {
			log.debug("Network connected...");
			Map<String, ?> map = RetryRequestQueue.getAllPref(context);
			Set<String> set = map.keySet();
			for (String zone : set) {
				// Bug from Play Console
				if (zone == null) continue;

				if (zone.endsWith("_lat##") || zone.endsWith("_lng##") || zone.endsWith("_retrys##") || zone.endsWith("_time##")) continue;

				String realLat = RetryRequestQueue.getPref(context, zone + "_lat##");
				String realLng = RetryRequestQueue.getPref(context, zone + "_lng##");
				String realTime = RetryRequestQueue.getPref(context, zone + "_time##");
				int retrys = RetryRequestQueue.getPrefInt(context, zone + "_retrys##");

				String transition = (String) map.get(zone);
				RetryRequestQueue.removePref(context, zone);
				int transi;
				try {
					transi = Integer.parseInt(transition);
				} catch (NumberFormatException nfe) {
					continue;
				}

				DbZoneHelper datasource = new DbZoneHelper(context);
				ZoneEntity ze = datasource.getCursorZoneByName(zone);
				if (ze != null) {
					if (ze.getServerEntity() != null) {
						log.info("Send server retry request...");
						String urlEntered = Utils.replaceAll(context, ze.getServerEntity().getUrl_enter(), ze.getName(), ze.getAlias(), transi, ze.getRadius(),
								ze.getLatitude(), ze.getLongitude(), realLat, realLng, null, null, null);

						String urlExited = Utils.replaceAll(context, ze.getServerEntity().getUrl_exit(), ze.getName(), ze.getAlias(), transi, ze.getRadius(),
								ze.getLatitude(), ze.getLongitude(), realLat, realLng, null, null, null);

						Worker worker = new Worker(context.getApplicationContext());
						worker.doServerRequest(transi, context, urlEntered, urlExited, ze.getServerEntity().getUrl_fhem(), ze.getName(), ze.getLatitude(), ze.getLongitude(),
								ze.getServerEntity().getCert(), ze.getServerEntity().getCert_password(), ze.getServerEntity().getCa_cert(), ze.getServerEntity().getUser(),
								ze.getServerEntity().getUser_pw(), ze.getServerEntity().getTimeout(), ze.getAlias(), realLat, realLng, realTime, false, retrys);
					}else {
						log.info("Retry request for zone. No server profile found: " + zone);
					}
				} else {
					// Löschen da nicht mehr vorhanden
					RetryRequestQueue.removePref(context, zone);
					log.debug("Retry request for zone deleted. Not found: " + zone);
				}
			}
		}
	}
}









