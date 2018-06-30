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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.pathsense.android.sdk.location.PathsenseLocationProviderApi;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import de.egi.geofence.geozone.MainEgiGeoZone.ErrorDialogFragment;
import de.egi.geofence.geozone.db.DbContract;
import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.db.DbHelper;
import de.egi.geofence.geozone.db.DbMailHelper;
import de.egi.geofence.geozone.db.DbMoreHelper;
import de.egi.geofence.geozone.db.DbRequirementsHelper;
import de.egi.geofence.geozone.db.DbServerHelper;
import de.egi.geofence.geozone.db.DbSmsHelper;
import de.egi.geofence.geozone.db.DbZoneHelper;
import de.egi.geofence.geozone.db.MailEntity;
import de.egi.geofence.geozone.db.MoreEntity;
import de.egi.geofence.geozone.db.RequirementsEntity;
import de.egi.geofence.geozone.db.ServerEntity;
import de.egi.geofence.geozone.db.SmsEntity;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.gcm.GcmRegistrationIntentService;
import de.egi.geofence.geozone.gcm.GcmTokenDialog;
import de.egi.geofence.geozone.geofence.PathsenseGeofence;
import de.egi.geofence.geozone.geofence.SimpleGeofence;
import de.egi.geofence.geozone.geofence.SimpleGeofenceStore;
import de.egi.geofence.geozone.plugins.Plugins;
import de.egi.geofence.geozone.tracker.TrackingGeneralSettings;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.SimpleCryptoPBKDF2;
import de.egi.geofence.geozone.utils.Themes;
import de.egi.geofence.geozone.utils.Utils;

@SuppressWarnings("deprecation")
public class Settings extends AppCompatActivity implements OnClickListener, OnCheckedChangeListener, TextWatcher, OnItemSelectedListener, RadioGroup.OnCheckedChangeListener {
	private boolean check = false;
	private CheckBox falsePositives = null;
	private CheckBox notification = null;
	private CheckBox errorNotification = null;
    private CheckBox stickyNotification = null;
	private CheckBox broadcast = null;
	private CheckBox gcm = null;
	private CheckBox gcmLog = null;
	private EditText senderId = null;
	private Spinner spinner_export;
	private Spinner spinner_import;
	private DbGlobalsHelper dbGlobalsHelper;

	private final Logger log = Logger.getLogger(Settings.class);
	private SimpleGeofenceStore geofenceStore = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Utils.onActivityCreateSetTheme(this);

        setContentView(R.layout.settings);

//		checkPermissionsExternalStorage();

//		checkPermissionsGCM();

		dbGlobalsHelper = new DbGlobalsHelper(this);
		Properties properties = dbGlobalsHelper.getCursorAllGlobals();

		spinner_export = (Spinner)findViewById(R.id.spinner_export);
		spinner_import = (Spinner)findViewById(R.id.spinner_import);

		Button logB = ((Button) this.findViewById(R.id.button_log));
		Button debugB = ((Button) this.findViewById(R.id.button_debug));
		Button gcmShowLog = ((Button) this.findViewById(R.id.button_show_gcm_log));
		Button sendLogB = ((Button) this.findViewById(R.id.button_send_log));
		Button showPlugins = ((Button) this.findViewById(R.id.button_plugin));
		Button tracking = ((Button) this.findViewById(R.id.button_tracking));
		Button themes = ((Button) this.findViewById(R.id.button_themes));
		falsePositives = ((CheckBox) this.findViewById(R.id.value_false_positives));
		notification = ((CheckBox) this.findViewById(R.id.value_notification));
		errorNotification = ((CheckBox) this.findViewById(R.id.value_error_notification));
        stickyNotification = ((CheckBox) this.findViewById(R.id.value_sticky_notification));
		broadcast = ((CheckBox) this.findViewById(R.id.value_broadcast));
		senderId = ((EditText) this.findViewById(R.id.value_sender_id));

		if(Utils.isBoolean(properties.getProperty(Constants.DB_KEY_NEW_API))) {
			((RadioGroup) this.findViewById(R.id.radioGroupGeofenceType)).check(R.id.radioButtonPathSense);
		}else{
			((RadioGroup) this.findViewById(R.id.radioGroupGeofenceType)).check(R.id.radioButtonGoogle);
		}

		gcm = ((CheckBox) this.findViewById(R.id.value_gcm));
		gcmLog = ((CheckBox) this.findViewById(R.id.value_gcm_logging));
		((RadioGroup)findViewById(R.id.radioGroupGeofenceType)).setOnCheckedChangeListener(this);
		falsePositives.setOnCheckedChangeListener(this);
		notification.setOnCheckedChangeListener(this);
		errorNotification.setOnCheckedChangeListener(this);
        stickyNotification.setOnCheckedChangeListener(this);
		broadcast.setOnCheckedChangeListener(this);
		gcm.setOnClickListener(this);
		gcmLog.setOnClickListener(this);
		senderId.addTextChangedListener(this);

		spinner_export.setOnItemSelectedListener(this);
		spinner_import.setOnItemSelectedListener(this);
		debugB.setOnClickListener(this);
		logB.setOnClickListener(this);
		gcmShowLog.setOnClickListener(this);
		sendLogB.setOnClickListener(this);
		showPlugins.setOnClickListener(this);
		tracking.setOnClickListener(this);
		themes.setOnClickListener(this);
		// Export Spinner
		List<String> exp_li = new ArrayList<>();
		exp_li.add(this.getString(R.string.menu_item_export));
		exp_li.add(this.getString(R.string.spinner_local));
		exp_li.add(this.getString(R.string.spinner_extern));

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, exp_li);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		spinner_export.setAdapter(adapter);

		List<String> imp_li = new ArrayList<>();
		imp_li.add(this.getString(R.string.menu_item_import));
		imp_li.add(this.getString(R.string.spinner_local));
		imp_li.add(this.getString(R.string.spinner_extern));

		ArrayAdapter<String> adapter_imp = new ArrayAdapter<>(this, R.layout.spinner_item, imp_li);
		adapter_imp.setDropDownViewResource(R.layout.spinner_dropdown_item);
		spinner_import.setAdapter(adapter_imp);

		((CheckBox) this.findViewById(R.id.value_false_positives)).setChecked(Utils.isBoolean(properties.getProperty(Constants.DB_KEY_FALSE_POSITIVES)));
		((CheckBox) this.findViewById(R.id.value_notification)).setChecked(Utils.isBoolean(properties.getProperty(Constants.DB_KEY_NOTIFICATION)));
		((CheckBox) this.findViewById(R.id.value_error_notification)).setChecked(Utils.isBoolean(properties.getProperty(Constants.DB_KEY_ERROR_NOTIFICATION)));
        ((CheckBox) this.findViewById(R.id.value_sticky_notification)).setChecked(Utils.isBoolean(properties.getProperty(Constants.DB_KEY_STICKY_NOTIFICATION)));
		((CheckBox) this.findViewById(R.id.value_broadcast)).setChecked(Utils.isBoolean(properties.getProperty(Constants.DB_KEY_BROADCAST)));
		((CheckBox) this.findViewById(R.id.value_gcm)).setChecked(Utils.isBoolean(properties.getProperty(Constants.DB_KEY_GCM)));
		((CheckBox) this.findViewById(R.id.value_gcm_logging)).setChecked(Utils.isBoolean(properties.getProperty(Constants.DB_KEY_GCM_LOGGING)));
		((EditText) this.findViewById(R.id.value_sender_id)).setText(properties.getProperty(Constants.DB_KEY_GCM_SENDERID));

		geofenceStore = new SimpleGeofenceStore(this);

		// Rückkehr nach import
		Bundle b = getIntent().getExtras();
		if (null != b){
			boolean imp = b.getBoolean("import");
			if (imp){
				AlertDialog.Builder ab = new AlertDialog.Builder(this);
				ab.setMessage(R.string.import_ok_text).setPositiveButton(R.string.action_ok, dialogClickListener).setTitle("Geofence").setIcon(R.drawable.ic_file_download_black_24dp).show();
			}
		}
		check = true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_themes:
				log.debug("onOptionsItemSelected: button_themes");
				Intent i = new Intent(this, Themes.class);
                startActivityForResult(i, 4715);
				break;
			case R.id.button_tracking:
				log.debug("onOptionsItemSelected: button_tracking");
				Intent i3 = new Intent(this, TrackingGeneralSettings.class);
				startActivity(i3);
				break;
			case R.id.button_debug:
				log.info("onOptionsItemSelected: menu_debug");
				Intent i4 = new Intent(this, Debug.class);
				startActivityForResult(i4, 4712);
				break;
			case R.id.button_log:
				log.info("onOptionsItemSelected: menu_log");
				Intent i6 = new Intent(this, EgiLog.class);
				startActivity(i6);
				break;
			case R.id.button_show_gcm_log:
				log.info("onOptionsItemSelected: menu_show_gcm_log");
				Intent i7 = new Intent(this, GcmLog.class);
				startActivity(i7);
				break;
			case R.id.button_plugin:
				log.info("onOptionsItemSelected: menu_show_plugins");
				Intent i8 = new Intent(this, Plugins.class);
				startActivity(i8);
				break;
			case R.id.button_send_log:
				log.info("onOptionsItemSelected: menu_send_log");

				log.error("******************************************");
				try {
					PackageInfo pi = getPackageManager().getPackageInfo("de.egi.geofence.geozone", PackageManager.GET_CONFIGURATIONS);
					String v1 = pi.versionName;
					log.error("EgiGeoZone: " + v1);
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}
				log.error("Device name: " + getDeviceName());
				log.error("Device brand: " + Build.BRAND);
				log.error("Android version: " + Build.VERSION.RELEASE + " ("  + Build.VERSION.CODENAME + ")");
				log.error("******************************************");

				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"egmontr@gmail.com"});
				intent.putExtra(Intent.EXTRA_SUBJECT, "EgiGeoZone log file");
				intent.putExtra(Intent.EXTRA_TEXT, "");
				File file = new File(Environment.getExternalStorageDirectory() + File.separator + "egigeozone" + File.separator + "egigeozone.log");
				if (!file.exists() || !file.canRead()) {
					Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
					return;
				}
//				Uri uri = Uri.fromFile(file);
				Uri uri = FileProvider.getUriForFile(this, "de.egi.geofence.geozone.fileContentProvider", file);

				intent.putExtra(Intent.EXTRA_STREAM, uri);
				startActivity(Intent.createChooser(intent, "Send email..."));
				break;
			case R.id.value_gcm:
				if (gcm.isChecked()){
					// Check permission GCM
//					if (!checkPermission(Manifest.permission.GET_ACCOUNTS)){
//						requestAppPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, R.string.checkGCM, 2002);
//					}else {
						initGcm();
//					}
				}else {
					dbGlobalsHelper.storeGlobals(Constants.DB_KEY_GCM, "false");
					// Unregister ist schlecht, da bei einem neuen register() meistens eine neue RegId vergeben wird. Somit müsste man diese wieder z.B. in FHEM konfigurieren.
					// Darum lieber über Schalter die Ausgaben steuern oder am Server deaktivieren, oder in Google deaktivieren?!
				}

				break;
			case R.id.value_gcm_logging:
				if (gcmLog.isChecked()){
					dbGlobalsHelper.storeGlobals(Constants.DB_KEY_GCM_LOGGING, "true");

				}else{
					dbGlobalsHelper.storeGlobals(Constants.DB_KEY_GCM_LOGGING, "false");
				}
				break;
			default:
				break;
		}
	}

	private String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == falsePositives){
			if (isChecked){
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_FALSE_POSITIVES, "true");
			}else{
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_FALSE_POSITIVES, "false");
				// Zurück zur Main und fences registrieren
				// Wieder Settings aufrufen, damit der User das Verlassen nicht merkt.
				Intent data = new Intent();
				data.putExtra("import", true);
				setResult(RESULT_OK, data);
				log.debug("add Google geofences");
				finish();

			}
		}
		if (buttonView == notification){
			if (isChecked){
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_NOTIFICATION, "true");
			}else{
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_NOTIFICATION, "false");
			}
		}
		if (buttonView == errorNotification){
			if (isChecked){
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_ERROR_NOTIFICATION, "true");
			}else{
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_ERROR_NOTIFICATION, "false");
			}
		}
        if (buttonView == stickyNotification){
            if (isChecked){
                dbGlobalsHelper.storeGlobals(Constants.DB_KEY_STICKY_NOTIFICATION, "true");
                NotificationUtil.sendPermanentNotification(getApplicationContext(), R.drawable.locating_geo, getString(R.string.text_running_notification), 7676);
            }else{
                dbGlobalsHelper.storeGlobals(Constants.DB_KEY_STICKY_NOTIFICATION, "false");
                NotificationUtil.cancelPermanentNotification(getApplicationContext(), 7676);
            }
        }
		if (buttonView == broadcast){
			if (isChecked){
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_BROADCAST, "true");
			}else{
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_BROADCAST, "false");
			}
		}
	}

	/**
	 * DB exportieren nach XML
	 * @throws Exception
	 */
	private void exportConfig() throws Exception{
		Properties properties = new Properties();
		int z = 0;
		PackageInfo pi = getPackageManager().getPackageInfo("de.egi.geofence.geozone", PackageManager.GET_CONFIGURATIONS);
		String v = pi.versionName;
		log.info("Export EgiGeoZone version: " + v);

		DbZoneHelper dbZoneHelper = new DbZoneHelper(this);
		ZoneEntity ze;
		Cursor cursor =  dbZoneHelper.getCursorAllZone(null);
		while (cursor.moveToNext()) {
			ze = dbZoneHelper.getCursorZoneByName(cursor.getString(1));
			if (ze != null){
				log.info(ze.getName());
				log.info(ze.getName() + " - "  + getString(R.string.alias) + ": " + ze.getAlias());
				log.info(ze.getName() + " - " +  getString(R.string.latitude) + ": " + ze.getLatitude());
				log.info(ze.getName() + " - "  + getString(R.string.longitude) + ": " + ze.getLongitude());
				log.info(ze.getName() + " - "  + getString(R.string.radius) + ": " + ze.getRadius());
				log.info(ze.getName() + " - "  + getString(R.string.accuracy) + ": " + ze.getAccuracy());
				log.info(ze.getName() + " - "  + "type" + ": " + ze.getType());
				log.info(ze.getName() + " - "  + "beacon" + ": " + ze.getBeacon());
				log.info(ze.getName() + " - "  + "id_server" + ": " + ze.getId_server());
				log.info(ze.getName() + " - "  + "id_sms" + ": " + ze.getId_sms());
				log.info(ze.getName() + " - "  + "id_email" + ": " + ze.getId_email());
				log.info(ze.getName() + " - "  + "id_more" + ": " + ze.getId_more_actions());
				log.info(ze.getName() + " - "  + "id_requirements" + ": " + ze.getId_requirements());

                log.info(ze.getName() + " - "  + "id_trackFile" + ": " + ze.isTrack_to_file());
                log.info(ze.getName() + " - "  + "id_trackEnter" + ": " + ze.isEnter_tracker());
                log.info(ze.getName() + " - "  + "id_trackExit" + ": " + ze.isExit_tracker());

                log.info(ze.getName() + " - "  + "id_trackInterval" + ": " + ze.getLocal_tracking_interval());
                log.info(ze.getName() + " - "  + "id_trackMail" + ": " + ze.getTrack_id_email());
                log.info(ze.getName() + " - "  + "id_trackServer" + ": " + ze.getTrack_url());

				log.info("----------------------------------------");
				String zname = ze.getName();
				properties.put("zone_name" + ++z, zname);
				properties.put("zone_" + zname + "_alias", getStringNotNull(zname + "_alias", ze.getAlias()));
				properties.put("zone_" + zname + "_latitude", getStringNotNull(zname + "_latitude", ze.getLatitude()));
				properties.put("zone_" + zname + "_longitude", getStringNotNull(zname + "_longitude", ze.getLongitude()));
				properties.put("zone_" + zname + "_radius", ze.getRadius().toString());
				properties.put("zone_" + zname + "_accuracy", ze.getAccuracy().toString());
				properties.put("zone_" + zname + "_type", getStringNotNull(zname + "_type", ze.getType()));
				properties.put("zone_" + zname + "_beacon", ze.getBeacon() == null ? "" : getStringNotNull(zname + "_beacon", ze.getBeacon()));
				properties.put("zone_" + zname + "_id_server", getStringNotNull(zname + "_id_server", ze.getId_server()));
				properties.put("zone_" + zname + "_id_sms", getStringNotNull(zname + "_id_sms", ze.getId_sms()));
				properties.put("zone_" + zname + "_id_email", getStringNotNull(zname + "_id_email", ze.getId_email()));
				properties.put("zone_" + zname + "_id_more", getStringNotNull(zname + "_id_more", ze.getId_more_actions()));
				properties.put("zone_" + zname + "_id_requirements", getStringNotNull(zname + "_id_requirements", ze.getId_requirements()));

                properties.put("zone_" + zname + "_id_trackFile", getBooleanString(ze.isTrack_to_file()));
                properties.put("zone_" + zname + "_id_trackEnter", getBooleanString(ze.isEnter_tracker()));
                properties.put("zone_" + zname + "_id_trackExit", getBooleanString(ze.isExit_tracker()));

                properties.put("zone_" + zname + "_id_trackInterval", ze.getLocal_tracking_interval().toString());
                properties.put("zone_" + zname + "_id_trackMail", getStringNotNull(zname + "_id_trackMail", ze.getTrack_id_email()));
                properties.put("zone_" + zname + "_id_trackServer", getStringNotNull(zname + "_id_trackServer", ze.getTrack_url()));
			}
		}


		DbServerHelper dbServerHelper = new DbServerHelper(this);
		ServerEntity srv;
		cursor =  dbServerHelper.getCursorAllServer();
		while (cursor.moveToNext()) {
			srv = dbServerHelper.getCursorServerByName(cursor.getString(1));
			if (srv != null){
				log.info("srv_name: " + srv.getName());
				log.info(srv.getName() + " - "  + getString(R.string.fhem_geofancy) + ": " + srv.getUrl_fhem());
				log.info(srv.getName() + " - "  + getString(R.string.urlEntered) + ": " + srv.getUrl_enter());
				log.info(srv.getName() + " - "  + getString(R.string.urlExited) + ": " + srv.getUrl_exit());
				log.info(srv.getName() + " - "  + getString(R.string.user) + ": " + srv.getUser());
				log.info(srv.getName() + " - "  + getString(R.string.userPasswd) + ": " + "**********");
				log.info(srv.getName() + " - "  + getString(R.string.certName) + ": " + srv.getCert());
				log.info(srv.getName() + " - "  + getString(R.string.certPasswd) + ": " + "**********");
				log.info(srv.getName() + " - "  + getString(R.string.caCertName) + ": " + srv.getCa_cert());
				log.info(srv.getName() + " - "  + getString(R.string.timeout) + ": " + srv.getTimeout());
				log.info(srv.getName() + " - "  + getString(R.string.srv_tracking) + ": " + srv.getUrl_tracking());
				log.info(srv.getName() + " - "  + getString(R.string.prof_server_fallback) + ": " + srv.getId_fallback());
				log.info("----------------------------------------");
				String srvname = srv.getName();
				properties.put("srv_name" + ++z, srvname);
				properties.put("srv_" + srvname + "_url_fhem", getStringNotNull(srvname + "_url_fhem", srv.getUrl_fhem()));
				properties.put("srv_" + srvname + "_urlEntered", getStringNotNull(srvname + "_urlEntered", srv.getUrl_enter()));
				properties.put("srv_" + srvname + "_urlExited", getStringNotNull(srvname + "_urlExited", srv.getUrl_exit()));
				properties.put("srv_" + srvname + "_user", getStringNotNull(srvname + "_user", srv.getUser()));
				try{
					properties.put("srv_" + srvname + "_userPasswd", SimpleCryptoPBKDF2.encrypt(MainEgiGeoZone.SEED_MASTER, getStringNotNull(srvname + "_userPasswd", srv.getUser_pw())));
				}catch(Exception ignore){} // nothing to do: old algo
				properties.put("srv_" + srvname + "_certName", getStringNotNull(srvname + "_certName", srv.getCert()));
				try{
					properties.put("srv_" + srvname + "_certPasswd", SimpleCryptoPBKDF2.encrypt(MainEgiGeoZone.SEED_MASTER, getStringNotNull(srvname + "_certPasswd", srv.getCert_password())));
				}catch(Exception ignore){} // nothing to do: old algo
				properties.put("srv_" + srvname + "_caCertName", getStringNotNull(srvname + "_caCertName", srv.getCa_cert()));
				properties.put("srv_" + srvname + "_timeout", getStringNotNull(srvname + "_timeout", srv.getTimeout()));
				properties.put("srv_" + srvname + "_urlTracking", getStringNotNull(srvname + "_urlTracking", srv.getUrl_tracking()));
				properties.put("srv_" + srvname + "_fallbackServer", getStringNotNull(srvname + "_fallbackServer", srv.getId_fallback()));
			}
		}

		DbSmsHelper dbSmsHelper = new DbSmsHelper(this);
		SmsEntity sms;
		cursor =  dbSmsHelper.getCursorAllSms();
		while (cursor.moveToNext()) {
			sms = dbSmsHelper.getCursorSmsByName(cursor.getString(1));
			if (sms != null){
				log.info("sms_name: " + sms.getName());
				log.info(sms.getName() + " - "  + getString(R.string.smsTo) + ": " + sms.getNumber());
				log.info(sms.getName() + " - "  + getString(R.string.smsText) + ": " + sms.getText());
				log.info(sms.getName() + " - " +  "SMS enter" + ": " + sms.isEnter());
				log.info(sms.getName() + " - " +  "SMS exit" + ": " + sms.isExit());
				log.info("----------------------------------------");
				String smsname = sms.getName();
				properties.put("sms_name" + ++z, smsname);
				properties.put("sms_" + smsname + "_smsNumber", getStringNotNull(smsname + "_smsNumber", sms.getNumber()));
				properties.put("sms_" + smsname + "_smsText", getStringNotNull(smsname + "_smsText", sms.getText()));
				properties.put("sms_" + smsname + "_enter", getBooleanString(sms.isEnter()));
				properties.put("sms_" + smsname + "_exit", getBooleanString(sms.isExit()));
			}
		}

		DbMailHelper dbMailHelper = new DbMailHelper(this);
		MailEntity mail;
		cursor =  dbMailHelper.getCursorAllMail();
		while (cursor.moveToNext()) {
			mail = dbMailHelper.getCursorMailByName(cursor.getString(1));
			if (mail != null){
				log.info("mail_name: " + mail.getName());
				log.info(mail.getName() + " - "  + getString(R.string.mailUser) + ": " + mail.getSmtp_user());
				log.info(mail.getName() + " - "  + getString(R.string.mailUserPw) + ": " + "**********");
				log.info(mail.getName() + " - "  + getString(R.string.mailSmtpHost) + ": " + mail.getSmtp_server());
				log.info(mail.getName() + " - "  + getString(R.string.mailSmtpPort) + ": " + mail.getSmtp_port());
				log.info(mail.getName() + " - "  + getString(R.string.mailEmpf) + ": " + mail.getTo());
				log.info(mail.getName() + " - "  + getString(R.string.mailSender) + ": " + mail.getFrom());
				log.info(mail.getName() + " - "  + getString(R.string.mailSubject) + ": " + mail.getSubject());
				log.info(mail.getName() + " - "  + getString(R.string.mailText) + ": " + mail.getBody());
				log.info(mail.getName() + " - "  + getString(R.string.mailSsl) + ": " + mail.isSsl());
				log.info(mail.getName() + " - "  + getString(R.string.mailStartTls) + ": " + mail.isStarttls());
				log.info(mail.getName() + " - " +  "Mail enter" + ": " + mail.isEnter());
				log.info(mail.getName() + " - " +  "Mail exit" + ": " + mail.isExit());
				log.info("----------------------------------------");
				String mailname = mail.getName();
				properties.put("mail_name" + ++z, mailname);
				properties.put("mail_" + mailname + "_mailUser", getStringNotNull(mailname + "_mailUser", mail.getSmtp_user()));
				try{
					properties.put("mail_" + mailname + "_mailUserPw", SimpleCryptoPBKDF2.encrypt(MainEgiGeoZone.SEED_MASTER, getStringNotNull(mailname + "_mailUserPw", mail.getSmtp_pw())));
				}catch(Exception ignore){} // nothing to do: old algo
				properties.put("mail_" + mailname + "_mailSmtpHost", getStringNotNull(mailname + "_mailSmtpHost", mail.getSmtp_server()));
				properties.put("mail_" + mailname + "_mailSmtpPort", getStringNotNull(mailname + "_mailSmtpPort", mail.getSmtp_port()));
				properties.put("mail_" + mailname + "_mailEmpf", getStringNotNull(mailname + "_mailEmpf", mail.getTo()));
				properties.put("mail_" + mailname + "_mailSender", getStringNotNull(mailname + "_mailSender", mail.getFrom()));
				properties.put("mail_" + mailname + "_mailSubject", getStringNotNull(mailname + "_mailSubject", mail.getSubject()));
				properties.put("mail_" + mailname + "_mailText", getStringNotNull(mailname + "_mailText", mail.getBody()));
				properties.put("mail_" + mailname + "_mailSsl", getBooleanString(mail.isSsl()));
				properties.put("mail_" + mailname + "_mailStarttls", getBooleanString(mail.isStarttls()));
				properties.put("mail_" + mailname + "_enter", getBooleanString(mail.isEnter()));
				properties.put("mail_" + mailname + "_exit", getBooleanString(mail.isExit()));
			}
		}


		DbMoreHelper dbMoreHelper = new DbMoreHelper(this);
		MoreEntity more;
		cursor =  dbMoreHelper.getCursorAllMore();
		while (cursor.moveToNext()) {
			more = dbMoreHelper.getCursorMoreByName(cursor.getString(1));
			if (more != null){
				log.info("more_name: " + more.getName());
				log.info(more.getName() + " - "  + "WLAN enter zone: " + more.getEnter_wifi());
				log.info(more.getName() + " - "  + "WLAN exit zone: " + more.getExit_wifi());
				log.info(more.getName() + " - "  + "Bluetooth enter zone: " + more.getEnter_bt());
				log.info(more.getName() + " - "  + "Bluetooth exit zone: " + more.getExit_bt());
				log.info(more.getName() + " - "  + "Sound enter zone: " + more.getEnter_sound());
				log.info(more.getName() + " - "  + "Sound exit zone: " + more.getExit_sound());
				log.info(more.getName() + " - "  + "Tasker enter zone: " + more.getEnter_task());
				log.info(more.getName() + " - "  + "Tasker exit zone: " + more.getExit_task());
				log.info("----------------------------------------");
				String morename = more.getName();
				properties.put("more_name" + ++z, morename);
				properties.put("more_" + morename + "_moreWlanEnter", more.getEnter_wifi().toString());
				properties.put("more_" + morename + "_moreWlanExit", more.getExit_wifi().toString());
				properties.put("more_" + morename + "_moreBluetoothEnter", more.getEnter_bt().toString());
				properties.put("more_" + morename + "_moreBluetoothExit", more.getExit_bt().toString());
				properties.put("more_" + morename + "_moreSoundEnter", more.getEnter_sound().toString());
				properties.put("more_" + morename + "_moreSoundExit", more.getExit_sound().toString());
				properties.put("more_" + morename + "_moreTaskEnter", getStringNotNull(more.getEnter_task() + "_moreTaskEnter", more.getEnter_task()));
				properties.put("more_" + morename + "_moreTaskExit", getStringNotNull(more.getExit_task() + "_moreTaskExit", more.getExit_task()));
			}
		}

		DbRequirementsHelper dbRequirementsHelper = new DbRequirementsHelper(this);
		RequirementsEntity requirements;
		cursor =  dbRequirementsHelper.getCursorAllRequirements();
		while (cursor.moveToNext()) {
			requirements = dbRequirementsHelper.getCursorRequirementsByName(cursor.getString(1));
			if (requirements != null){
				log.info("requirements_name: " + requirements.getName());
				log.info(requirements.getName() + " - "  + "BT Device enter zone: " + requirements.getEnter_bt());
				log.info(requirements.getName() + " - "  + "BT Device exit zone: " + requirements.getExit_bt());
				log.info(requirements.getName() + " - "  + getString(R.string.mo) + ": " + requirements.isMon());
				log.info(requirements.getName() + " - "  + getString(R.string.di) + ": " + requirements.isTue());
				log.info(requirements.getName() + " - "  + getString(R.string.mi) + ": " + requirements.isWed());
				log.info(requirements.getName() + " - "  + getString(R.string.don) + ": " + requirements.isThu());
				log.info(requirements.getName() + " - "  + getString(R.string.fr) + ": " + requirements.isFri());
				log.info(requirements.getName() + " - "  + getString(R.string.sa) + ": " + requirements.isSat());
				log.info(requirements.getName() + " - "  + getString(R.string.so) + ": " + requirements.isSun());
				log.info("----------------------------------------");
				String requirementsname = requirements.getName();
				properties.put("requirements_name" + ++z, requirementsname);
				properties.put("requirements_" + requirementsname + "_condBluetoothDeviceEnter", getStringNotNull(requirements.getEnter_bt() + "_condBluetoothDeviceEnter", requirements.getEnter_bt()));
				properties.put("requirements_" + requirementsname + "_condBluetoothDeviceExit", getStringNotNull(requirements.getExit_bt() + "_condBluetoothDeviceExit", requirements.getExit_bt()));
				properties.put("requirements_" + requirementsname + "_mo", getBooleanString(requirements.isMon()));
				properties.put("requirements_" + requirementsname + "_di", getBooleanString(requirements.isTue()));
				properties.put("requirements_" + requirementsname + "_mi", getBooleanString(requirements.isWed()));
				properties.put("requirements_" + requirementsname + "_don", getBooleanString(requirements.isThu()));
				properties.put("requirements_" + requirementsname + "_fr", getBooleanString(requirements.isFri()));
				properties.put("requirements_" + requirementsname + "_sa", getBooleanString(requirements.isSat()));
				properties.put("requirements_" + requirementsname + "_so", getBooleanString(requirements.isSun()));
			}
		}

		log.info("Global settings");
		String doNotification = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NOTIFICATION);
		properties.put("Globals" + "_notification", getBooleanNotNull("Globals" + "_notification", doNotification));
		log.info("Globals" + " - "  + getString(R.string.notification) + ": " + doNotification);

		String doFalsePositives = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_FALSE_POSITIVES);
		properties.put("Globals" + "_falsePositives", getBooleanNotNull("Globals" + "_falsePositives", doFalsePositives));
		log.info("Globals" + " - "  + "falsePositives" + ": " + doFalsePositives);

		String doErrorNotification = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_ERROR_NOTIFICATION);
		properties.put("Globals" + "_errorNotification", getBooleanNotNull("Globals" + "_errorNotification", doErrorNotification));
		log.info("Globals" + " - "  + getString(R.string.error_notification) + ": " + doErrorNotification);

        String doStickyNotification = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_STICKY_NOTIFICATION);
        properties.put("Globals" + "_stickyNotification", getBooleanNotNull("Globals" + "_stickyNotification", doStickyNotification));
        log.info("Globals" + " - "  + getString(R.string.sticky_notification) + ": " + doStickyNotification);

		String doBroadcast = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_BROADCAST);
		properties.put("Globals" + "_broadcast", getBooleanNotNull("Globals" + "_broadcast", doBroadcast));
		log.info("Globals" + " - "  + getString(R.string.broadcast) + ": " + doBroadcast);

		String gcm = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_GCM);
		properties.put("Globals" + "_gcm", getBooleanNotNull("Globals" + "_gcm", gcm));
		log.info("Globals" + " - "  + getString(R.string.gcm) + ": " + gcm);

		String gcmLogging = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_GCM_LOGGING);
		properties.put("Globals" + "_gcmLogging", getBooleanNotNull("Globals" + "_gcmLogging", gcmLogging));
		log.info("Globals" + " - "  + getString(R.string.gcm_logging) + ": " + gcmLogging);

		String gcmSenderID = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_GCM_SENDERID);
		properties.put("Globals" + "_gcmSenderID", getStringNotNull("Globals" + "_gcmSenderID", gcmSenderID));
		log.info("Globals" + " - "  + getString(R.string.gcm_sender_id) + ": " + gcmSenderID);

		String beaconsScan = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_BEACON_SCAN);
		properties.put("Globals" + "_beaconsScan", getStringNotNull("Globals" + "_beaconsScan", beaconsScan));
		log.info("Globals" + " - "  + "Beacons scan" + ": " + beaconsScan);

		String geofenceType = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API);
		properties.put("Globals" + "_geofenceType", getStringNotNull("Globals" + "_geofenceType", geofenceType));
		log.info("Globals" + " - "  + "Geofence Type" + ": " + geofenceType);

		String locInterval = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCINTERVAL);
		properties.put("Globals" + "_locInterval", getStringNotNull("Globals" + "_locInterval", locInterval));
		log.info("Globals" + " - "  + "location interval" + ": " + locInterval);

		String locPrio = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCPRIORITY);
		properties.put("Globals" + "_locPrio", getStringNotNull("Globals" + "_locPrio", locPrio));
		log.info("Globals" + " - "  + "location priority" + ": " + locPrio);

		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "egigeozone" + File.separator + "egigeozone_db_export.xml");
			properties.storeToXML(outputStream, "EgiGeoZone " + v + " - exported configuration on " + new Date(), "utf-8");
			outputStream.flush();
			outputStream.close();
		} catch (Exception e) {
			log.error("Could not write to export file.", e);
		}

		// Show Alert
		Toast.makeText(this, R.string.export_ok_text, Toast.LENGTH_LONG).show();
	}

	/**
	 * DB importieren von XML
	 * @throws Exception
	 */
	private void importConfig(FileInputStream fis) throws Exception{
		// Show Alert
		Toast.makeText(this, R.string.import_ok_text, Toast.LENGTH_LONG).show();

		Properties properties = new Properties();
		properties.loadFromXML(fis);

		// Zonen-Namen extrahieren
		Set<Object> keys = properties.keySet();
		List<String> pZones = new ArrayList<>();
		List<String> pServer = new ArrayList<>();
		List<String> pSms = new ArrayList<>();
		List<String> pMail = new ArrayList<>();
		List<String> pMore = new ArrayList<>();
		List<String> pRequ = new ArrayList<>();
		String schl;
		for (Object key1 : keys) {
			String key = (String) key1;
			if (key.startsWith("zone_name")) {
				schl = properties.getProperty(key);
				pZones.add(schl);
				Log.i("pZone", schl);
			}
			if (key.startsWith("srv_name")) {
				schl = properties.getProperty(key);
				pServer.add(schl);
				Log.i("pServer", schl);
			}
			if (key.startsWith("sms_name")) {
				schl = properties.getProperty(key);
				pSms.add(schl);
				Log.i("pSms", schl);
			}
			if (key.startsWith("mail_name")) {
				schl = properties.getProperty(key);
				pMail.add(schl);
				Log.i("pMail", schl);
			}
			if (key.startsWith("more_name")) {
				schl = properties.getProperty(key);
				pMore.add(schl);
				Log.i("pMore", schl);
			}
			if (key.startsWith("requirements_name")) {
				schl = properties.getProperty(key);
				pRequ.add(schl);
				Log.i("pRequirements", schl);
			}
		}

		log.info("");

		// Aktuelle Zonen entfernen
		List<SimpleGeofence> geofences = geofenceStore.getGeofences();
		List<String> dZones = new ArrayList<>();
		for (SimpleGeofence simpleGeofence : geofences) {
			dZones.add(simpleGeofence.getId());
		}

		// 1. Fences entfernen
		PathsenseGeofence pathsenseGeofence = new PathsenseGeofence(getBaseContext());
		if (dZones.size() > 0) {
			unregisterGeofences(dZones);
			pathsenseGeofence.removeGeofences();
		}

		// 2. Alle Tabellen dropen und neu anlegen.
		DbHelper.getInstance(getApplicationContext()).dropAndCreate(DbHelper.getInstance(getApplicationContext()).getWritableDatabase(), 0, 0);

		// 3. Importieren
		// Nun Profile importieren.
		// Server Profile
		DbServerHelper dbServerHelper = new DbServerHelper(this);
		for (String srvname : pServer) {
			ServerEntity serverEntity = new ServerEntity();

			serverEntity.setName(srvname);
			serverEntity.setUrl_fhem(properties.getProperty("srv_" + srvname + "_url_fhem"));
			serverEntity.setUrl_enter(properties.getProperty("srv_" + srvname + "_urlEntered"));
			serverEntity.setUrl_exit(properties.getProperty("srv_" + srvname + "_urlExited"));
			serverEntity.setUser(properties.getProperty("srv_" + srvname + "_user"));
			try {
				serverEntity.setUser_pw(SimpleCryptoPBKDF2.decrypt(MainEgiGeoZone.SEED_MASTER, properties.getProperty("srv_" + srvname + "_userPasswd")));
			}catch(Exception ignore){} // nothing to do: old algo
			serverEntity.setCert(properties.getProperty("srv_" + srvname + "_certName"));
			try {
				serverEntity.setCert_password(SimpleCryptoPBKDF2.decrypt(MainEgiGeoZone.SEED_MASTER, properties.getProperty("srv_" + srvname + "_certPasswd")));
			}catch(Exception ignore){} // nothing to do: old algo
			serverEntity.setCa_cert(properties.getProperty("srv_" + srvname + "_caCertName"));
			serverEntity.setTimeout(properties.getProperty("srv_" + srvname + "_timeout"));
			serverEntity.setUrl_tracking(properties.getProperty("srv_" + srvname + "_urlTracking"));
			serverEntity.setId_fallback(properties.getProperty("srv_" + srvname + "_fallbackServer"));

			dbServerHelper.createServer(serverEntity);
		}

		// Sms Profile
		DbSmsHelper dbSmsHelper = new DbSmsHelper(this);
		for (String smsname : pSms) {
			SmsEntity smsEntity = new SmsEntity();

			smsEntity.setName(smsname);
			smsEntity.setNumber(properties.getProperty("sms_" + smsname + "_smsNumber"));
			smsEntity.setText(properties.getProperty("sms_" + smsname + "_smsText"));
			smsEntity.setEnter(getBoolean(properties.getProperty("sms_" + smsname + "_enter")));
			smsEntity.setExit(getBoolean(properties.getProperty("sms_" + smsname + "_exit")));

			dbSmsHelper.createSms(smsEntity);
		}

		// Mail Profile
		DbMailHelper dbMailHelper = new DbMailHelper(this);
		for (String mailname : pMail) {
			MailEntity mailEntity = new MailEntity();

			mailEntity.setName(mailname);
			mailEntity.setSmtp_user(properties.getProperty("mail_" + mailname + "_mailUser"));
			try {
				mailEntity.setSmtp_pw(SimpleCryptoPBKDF2.decrypt(MainEgiGeoZone.SEED_MASTER, properties.getProperty("mail_" + mailname + "_mailUserPw")));
			}catch(Exception ignore){} // nothing to do: old algo
			mailEntity.setSmtp_server(properties.getProperty("mail_" + mailname + "_mailSmtpHost"));
			mailEntity.setSmtp_port(properties.getProperty("mail_" + mailname + "_mailSmtpPort"));
			mailEntity.setTo(properties.getProperty("mail_" + mailname + "_mailEmpf"));
			mailEntity.setFrom(properties.getProperty("mail_" + mailname + "_mailSender"));
			mailEntity.setSubject(properties.getProperty("mail_" + mailname + "_mailSubject"));
			mailEntity.setBody(properties.getProperty("mail_" + mailname + "_mailText"));
			mailEntity.setSsl(getBoolean(properties.getProperty("mail_" + mailname + "_mailSsl")));
			mailEntity.setStarttls(getBoolean(properties.getProperty("mail_" + mailname + "_mailStarttls")));
			mailEntity.setEnter(getBoolean(properties.getProperty("mail_" + mailname + "_enter")));
			mailEntity.setExit(getBoolean(properties.getProperty("mail_" + mailname + "_exit")));

			dbMailHelper.createMail(mailEntity);
		}

		// More Profile
		DbMoreHelper dbMoreHelper = new DbMoreHelper(this);
		for (String morename : pMore) {
			MoreEntity moreEntity = new MoreEntity();

			moreEntity.setName(morename);

			moreEntity.setEnter_wifi(Integer.parseInt(properties.getProperty("more_" + morename + "_moreWlanEnter")));
			moreEntity.setExit_wifi(Integer.parseInt(properties.getProperty("more_" + morename + "_moreWlanExit")));
			moreEntity.setEnter_bt(Integer.parseInt(properties.getProperty("more_" + morename + "_moreBluetoothEnter")));
			moreEntity.setExit_bt(Integer.parseInt(properties.getProperty("more_" + morename + "_moreBluetoothExit")));
			moreEntity.setEnter_sound(Integer.parseInt(properties.getProperty("more_" + morename + "_moreSoundEnter")));
			moreEntity.setExit_sound(Integer.parseInt(properties.getProperty("more_" + morename + "_moreSoundExit")));
			moreEntity.setEnter_task(properties.getProperty("more_" + morename + "_moreTaskEnter"));
			moreEntity.setExit_task(properties.getProperty("more_" + morename + "_moreTaskExit"));

			dbMoreHelper.createMore(moreEntity);
		}

		// Requirements Profile
		DbRequirementsHelper dbRequirementsHelper = new DbRequirementsHelper(this);
		for (String requname : pRequ) {
			RequirementsEntity requirementsEntity = new RequirementsEntity();

			requirementsEntity.setName(requname);

			requirementsEntity.setEnter_bt(properties.getProperty("requirements_" + requname + "_condBluetoothDeviceEnter"));
			requirementsEntity.setExit_bt(properties.getProperty("requirements_" + requname + "_condBluetoothDeviceExit"));
			requirementsEntity.setMon(getBoolean(properties.getProperty("requirements_" + requname + "_mo")));
			requirementsEntity.setTue(getBoolean(properties.getProperty("requirements_" + requname + "_di")));
			requirementsEntity.setWed(getBoolean(properties.getProperty("requirements_" + requname + "_mi")));
			requirementsEntity.setThu(getBoolean(properties.getProperty("requirements_" + requname + "_don")));
			requirementsEntity.setFri(getBoolean(properties.getProperty("requirements_" + requname + "_fr")));
			requirementsEntity.setSat(getBoolean(properties.getProperty("requirements_" + requname + "_sa")));
			requirementsEntity.setSun(getBoolean(properties.getProperty("requirements_" + requname + "_so")));

			dbRequirementsHelper.createRequirements(requirementsEntity);
		}

		// Zone Profile
		DbZoneHelper dbZoneHelper = new DbZoneHelper(this);
		for (String zonename : pZones) {
			ZoneEntity zoneEntity = new ZoneEntity();

			zoneEntity.setName(zonename);
			zoneEntity.setAlias(properties.getProperty("zone_" + zonename + "_alias"));
			zoneEntity.setLatitude(properties.getProperty("zone_" + zonename + "_latitude"));
			zoneEntity.setLongitude(properties.getProperty("zone_" + zonename + "_longitude"));
			zoneEntity.setRadius(Integer.parseInt(properties.getProperty("zone_" + zonename + "_radius")));
			zoneEntity.setAccuracy(properties.getProperty("zone_" + zonename + "_accuracy") != null ? Integer.parseInt(properties.getProperty("zone_" + zonename + "_accuracy")) : 0);
			zoneEntity.setType(properties.getProperty("zone_" + zonename + "_type") == null ? Constants.GEOZONE : properties.getProperty("zone_" + zonename + "_type"));
			zoneEntity.setBeacon(properties.getProperty("zone_" + zonename + "_beacon"));
			String idServer = properties.getProperty("zone_" + zonename + "_id_server");
			zoneEntity.setId_server(idServer.equals("") ? null : idServer);
			String idSms = properties.getProperty("zone_" + zonename + "_id_sms");
			zoneEntity.setId_sms(idSms.equals("") ? null : idSms);
			String idEmail = properties.getProperty("zone_" + zonename + "_id_email");
			zoneEntity.setId_email(idEmail.equals("") ? null : idEmail);
			String idMore = properties.getProperty("zone_" + zonename + "_id_more");
			zoneEntity.setId_more_actions(idMore.equals("") ? null : idMore);
			String idReq = properties.getProperty("zone_" + zonename + "_id_requirements");
			zoneEntity.setId_requirements(idReq.equals("") ? null : idReq);

            zoneEntity.setTrack_to_file(getBoolean(properties.getProperty("zone_" + zonename + "_id_trackFile")));
            zoneEntity.setEnter_tracker(getBoolean(properties.getProperty("zone_" + zonename + "_id_trackEnter")));
            zoneEntity.setExit_tracker(getBoolean(properties.getProperty("zone_" + zonename + "_id_trackExit")));
			zoneEntity.setLocal_tracking_interval(properties.getProperty("zone_" + zonename + "_id_trackInterval") != null
					? Integer.parseInt(properties.getProperty("zone_" + zonename + "_id_trackInterval")) : 5);
            String idTrackMail = properties.getProperty("zone_" + zonename + "_id_trackMail");
            zoneEntity.setTrack_id_email(idTrackMail == null || idTrackMail.equals("") ? null : idTrackMail);
            String idTrackServer = properties.getProperty("zone_" + zonename + "_id_trackServer");
            zoneEntity.setTrack_url(idTrackServer == null || idTrackServer.equals("") ? null : idTrackServer);

			dbZoneHelper.createZone(zoneEntity);
		}

		// Globals
		DbGlobalsHelper dbGlobalsHelper = new DbGlobalsHelper(this);

		dbGlobalsHelper.createGlobals(Constants.DB_KEY_FALSE_POSITIVES, properties.getProperty("Globals" + "_falsePositives"));
		dbGlobalsHelper.createGlobals(Constants.DB_KEY_NOTIFICATION, properties.getProperty("Globals" + "_notification"));
		dbGlobalsHelper.createGlobals(Constants.DB_KEY_ERROR_NOTIFICATION, properties.getProperty("Globals" + "_errorNotification"));
        dbGlobalsHelper.createGlobals(Constants.DB_KEY_STICKY_NOTIFICATION, properties.getProperty("Globals" + "_stickyNotification"));
		dbGlobalsHelper.createGlobals(Constants.DB_KEY_BROADCAST, properties.getProperty("Globals" + "_broadcast"));
		dbGlobalsHelper.createGlobals(Constants.DB_KEY_GCM, properties.getProperty("Globals" + "_gcm"));
		dbGlobalsHelper.createGlobals(Constants.DB_KEY_GCM_LOGGING, properties.getProperty("Globals" + "_gcmLogging"));
		dbGlobalsHelper.createGlobals(Constants.DB_KEY_GCM_SENDERID, properties.getProperty("Globals" + "_gcmSenderID"));
		dbGlobalsHelper.createGlobals(Constants.DB_KEY_BEACON_SCAN, properties.getProperty("Globals" + "_beaconsScan"));
		dbGlobalsHelper.createGlobals(Constants.DB_KEY_NEW_API, properties.getProperty("Globals" + "_geofenceType"));

		dbGlobalsHelper.createGlobals(Constants.DB_KEY_LOCINTERVAL, properties.getProperty("Globals" + "_locInterval"));
		dbGlobalsHelper.createGlobals(Constants.DB_KEY_LOCPRIORITY, properties.getProperty("Globals" + "_locPrio"));

		// Zurück zur Main und fences registrieren
		// Wieder Settings aufrufen, damit der User das Verlassen nicht merkt.
		if (Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
			List<SimpleGeofence> gfences = geofenceStore.getGeofences();
			boolean radiusError = false;
			Exception eMerk = null;
			for (SimpleGeofence simpleGeofence : gfences) {
				try {
					pathsenseGeofence.addGeofence(simpleGeofence);
				}catch (Exception e){
					radiusError = true;
					eMerk = e;
				}
			}
			if (radiusError) {
				AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
				ab.setMessage(eMerk.getMessage() + "\n\nMaybe in one of the zones the radius is less then 50 meters. Please check and register again.").setPositiveButton(R.string.action_ok, dialogClickListener).setTitle("Pathsense").setIcon(R.drawable.ic_lens_red_24dp).show();
			}
			log.debug("add Pathsense geofences at import.");
		}else {
			Intent data = new Intent();
			data.putExtra("import", true);
			setResult(RESULT_OK, data);
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Choose what to do based on the request code
		switch (requestCode) {
			// Debug Level setzen
			case 4712 :
				if (resultCode == RESULT_OK) {
					String level = intent.getStringExtra("level");
					MainEgiGeoZone.logConfigurator.setRootLevel(Level.toLevel(level));
					MainEgiGeoZone.logConfigurator.setLevel("de.egi.geofence.geozone", Level.toLevel(level));
					try{
						MainEgiGeoZone.logConfigurator.configure();
						log.debug("Log-Level now: " + level);
					} catch (Exception e) {
						// Nichts tun. Manchmal kann auf den Speicher nicht zugegriffen werden.
					}
					dbGlobalsHelper.storeGlobals(Constants.DB_KEY_LOG_LEVEL, level);
					// Show Alert
					Toast.makeText(this, " Level : " + level , Toast.LENGTH_LONG).show();
				}
				break;
			// Import Konfig
			case 4713 :
				if (resultCode == RESULT_OK) {
					try {
						Uri dataUri = intent.getData();
						InputStream inputStream = getContentResolver().openInputStream(dataUri);
						importConfig((FileInputStream) inputStream);
					} catch (Exception e) {
						log.error("Could not import configuration.", e);
						Toast.makeText(this, "Could not import configuration: " + e.toString(), Toast.LENGTH_LONG).show();
						return;
					}
				}
				break;
            // Export Konfig
            case 4714 :
				if (resultCode != RESULT_OK && intent != null) {
                    log.error("Could not export configuration. Result: " + resultCode);
                    Toast.makeText(this, "Could not export configuration. Result: " + resultCode, Toast.LENGTH_LONG).show();
                }
                break;
            // Colors
            case 4715 :
                if (resultCode != RESULT_OK) {
                    Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
//                    Intent i = new Intent(this, Settings.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                break;
			// If any other request code was received
			default:
				// Report that this Activity received an unknown requestCode
				Log.d(Constants.APPTAG, getString(R.string.unknown_activity_request_code, requestCode));
				break;
		}
	}

	/**
	 *  ####  Zonen löschen ####
	 */
	private void unregisterGeofences(List<String> mZones) {

        /*
         * Remove the geofence by creating a List of geofences to
         * remove and sending it to Location Services. The List
         * contains the name=id of the geofence.
         * The removal happens asynchronously; Location Services calls
         * onRemoveGeofencesByPendingIntentResult() (implemented in
         * the current Activity) when the removal is done.
         */
		log.info("unregisterGeofences: Remove geofences ");

        /*
         * Record the removal as remove by list. If a connection error occurs,
         * the app can automatically restart the removal if Google Play services
         * can fix the error
         */
		MainEgiGeoZone.mRemoveType = Constants.REMOVE_TYPE.LIST;
        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
		if (!servicesConnected()) {
			return;
		}
		// Try to remove the geofences
		try {
			GlobalSingleton.getInstance().getGeofenceRemover().removeGeofencesById(mZones);
			// Catch errors with the provided geofence IDs
		} catch (IllegalArgumentException e) {
			log.error("unregisterGeofences: Error removing Geofences", e);
//            showError(zone + ": Error removing Geofence", e.toString());
		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this, R.string.remove_geofences_already_requested_error, Toast.LENGTH_LONG).show();
			log.error("unregisterGeofences: Error removing Geofences", e);
//            showError(zone + ": Error removing Geofence", e.toString());
		}

	}


	/**
	 * Verify that Google Play services is available before making a request.
	 *
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {
		log.info("servicesConnected");
		// Check that Google Play services is available
		int resultCode =
				GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {

			// In debug mode, log the status
			Log.d(Constants.APPTAG, getString(R.string.play_services_available));
			log.info("servicesConnected result from Google Play services: " +  getString(R.string.play_services_available));
			// Continue
			return true;

			// Google Play services was not available for some reason
		} else {

			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
			if (dialog != null) {
				log.error("servicesConnected result: could not connect to Google Play services");
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);

				errorFragment.show(this.getFragmentManager(), Constants.APPTAG);
			}
			return false;
		}
	}

	private String getStringNotNull(String bez, String s){
		if (s == null){
			log.debug("String " + bez + " was NULL.");
			return "";
		}
		return  s;
	}

	private String getBooleanNotNull(String bez, String s){
		if (s == null){
			log.debug("Boolean " + bez + " was NULL.");
			return "true";
		}
		return  s;
	}

	private String getBooleanString(boolean b){
		if (b){
			return "true";
		}else{
			return "false";
		}
	}

	private boolean getBoolean(String b) {
		return b != null && !b.equals("false");
	}

	// OK-Dialog
	private final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					break;

			}
		}
	};
	@Override
	public void afterTextChanged(Editable s) {
		dbGlobalsHelper.storeGlobals(Constants.DB_KEY_GCM_SENDERID, senderId.getText().toString());
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}


	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.d("Settings", "onItemSelected");
//		Export
		if (arg0.getId() == R.id.spinner_export){

			Log.d("Settings Export", "Selected item is: " + arg0.getSelectedItem());
			String ziel = arg0.getSelectedItem().toString();
			if (ziel.equalsIgnoreCase(this.getString(R.string.menu_item_export))){
				return;
			}

			// Erst lokal speichern
			try {
				exportConfig();
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Settings: Error exporting configuration! " + e.getMessage(), e);
				return;
			}

			if (!ziel.equalsIgnoreCase("")){
				// Wieder auf 1. Element setzen
				spinner_export.setSelection(0, true);
				if (ziel.equalsIgnoreCase(this.getString(R.string.spinner_local))){
					// Datei wurde gespeichert. Nur Dialog ausgeben
					AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
					ab.setMessage(R.string.export_ok_text).setPositiveButton(R.string.action_ok, dialogClickListener).setTitle("Export").setIcon(R.drawable.ic_file_upload_black_24dp).show();
				}
				if (ziel.equalsIgnoreCase(this.getString(R.string.spinner_extern))){
					// Auf Dropbox u.a. speichern
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("multipart/*");
					i.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.app_name));
					i.putExtra(Intent.EXTRA_TEXT, this.getString(R.string.spinner_titel_exp));
					Log.d("EgiGeoZone Settings", Environment.getExternalStorageDirectory() + File.separator + "egigeozone" + File.separator + "egigeozone_db_export.xml");

					File file = new File(Environment.getExternalStorageDirectory() + File.separator + "egigeozone" + File.separator + "egigeozone_db_export.xml");
					if (!file.exists() || !file.canRead()) {
						Toast.makeText(this, "Attachment Error: Can not export configuration.", Toast.LENGTH_SHORT).show();
						return;
					}
//					Uri uri = Uri.fromFile(file);
					Uri uri = FileProvider.getUriForFile(this, "de.egi.geofence.geozone.fileContentProvider", file);

					i.putExtra(Intent.EXTRA_STREAM, uri);
					startActivityForResult(Intent.createChooser(i, this.getString(R.string.spinner_titel_exp)), 4714);
				}
			}
		}
		// Import
		if (arg0.getId() == R.id.spinner_import){

			Log.d("Settings Import", "Selected item is: " + arg0.getSelectedItem());
			String ziel = arg0.getSelectedItem().toString();
			if (ziel.equalsIgnoreCase(this.getString(R.string.menu_item_import))){
				return;
			}

			if (!ziel.equalsIgnoreCase("")){
				// Wieder auf 1. Element setzen
				spinner_import.setSelection(0, true);
				FileInputStream inputStream;
				if (ziel.equalsIgnoreCase(this.getString(R.string.spinner_local))){
					try {
						inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + "egigeozone" + File.separator + "egigeozone_db_export.xml");
						importConfig(inputStream);
					} catch (Exception e) {
						log.error("Could not import configuration.", e);
						Toast.makeText(this, "Could not import configuration: " + e.toString(), Toast.LENGTH_LONG).show();
						return;
					}
				}
				if (ziel.equalsIgnoreCase(this.getString(R.string.spinner_extern))){
					// Von Dropbox u.a. Konfig. holen.
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_GET_CONTENT);
					intent.setType("*/*");
					startActivityForResult(intent, 4713);
				}
			}
		}
	}

	public void showGcmApiKey(View v){
		dbGlobalsHelper = new DbGlobalsHelper(this);
		String token = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_GCM_REG_ID);
		if (token == null){
			token = getString(R.string.noApiKey);
		}else {
			// Reg-Id nach Zwischenablage
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData.newPlainText("RegId to clipboard", token);
			clipboard.setPrimaryClip(clip);
		}
		Intent intent = new Intent(this, GcmTokenDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("de.egi.geofence.geozone.gcm.token", token);
		startActivity(intent);
	}

	public void resetGeofenceStatus(View v){
		// Display UI and wait for user interaction
		AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
		alertDialogBuilder.setMessage(getString(R.string.stateResetMsg));
		alertDialogBuilder.setTitle(getString(R.string.stateReset));

		alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				DbZoneHelper zoneHelper = new DbZoneHelper(Settings.this);
				// Give all back
				Cursor cursor =  zoneHelper.getCursorAllZone(null);
				while (cursor.moveToNext()) {
					String zone = cursor.getString(1);
					zoneHelper.updateZoneField(zone, DbContract.ZoneEntry.CN_STATUS, false);
				}
				cursor.close();

				Toast.makeText(Settings.this, getString(R.string.stateResetToast), Toast.LENGTH_LONG).show();
			}
		});
		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {

			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		Log.d("Settings", "onNothingSelected");
	}

	private void initGcm() {
		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		if (servicesConnected()) {
			if (Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_GCM))){
				String regid = GcmRegistrationIntentService.getRegistrationId(this);
				if (regid.isEmpty()) {
					// Start IntentService to register this application with GCM.
					Intent intentGcm = new Intent(this, GcmRegistrationIntentService.class);
					startService(intentGcm);
				}
			}
		} else {
			log.error("No valid Google Play Services APK found.");
			return;
		}

		dbGlobalsHelper.storeGlobals(Constants.DB_KEY_GCM, "true");

		if (TextUtils.isEmpty(senderId.getText().toString())){
			Toast.makeText(this, "Sender ID is emtpy - no GCM registration", Toast.LENGTH_LONG).show();
		}
		// Start IntentService to register this application with GCM.
		Intent intentGcm = new Intent(this, GcmRegistrationIntentService.class);
		startService(intentGcm);
	}


//	@Override
//	public void onPermissionsGranted(int requestCode) {
//		initGcm();
//	}
//

	@Override
	public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
		if (!check) return;

		if (radioGroup.getId() == R.id.radioGroupGeofenceType) {
			PathsenseGeofence pathsenseGeofence = new PathsenseGeofence(getBaseContext());
			switch (checkedId) {
				case R.id.radioButtonPathSense:
					// Alle Zonen lesen
					// Beim ersten mal ist der Store NULL
					if (geofenceStore == null) return;
					List<SimpleGeofence> geofences = geofenceStore.getGeofences();
					dbGlobalsHelper.storeGlobals(Constants.DB_KEY_NEW_API, "true");
					// remove all Google Geofences and add Pathsense Geofences
					List<String> zones = new ArrayList<>();
					for (SimpleGeofence simpleGeofence : geofences) {
						zones.add(simpleGeofence.getId());
					}
					unregisterGeofences(zones);
					log.debug("remove Google geofences: " + zones.toString());

					boolean radiusError = false;
					Exception eMerk = null;
					for (SimpleGeofence simpleGeofence : geofences) {
						try {
							pathsenseGeofence.addGeofence(simpleGeofence);
						}catch (Exception e){
							radiusError = true;
							eMerk = e;
						}
					}
					if (radiusError) {
						AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
						ab.setMessage(eMerk.getMessage() + "\n\nMaybe in one of the zones the radius is less then 50 meters. Please check and register again.").setPositiveButton(R.string.action_ok, dialogClickListener).setTitle("Pathsense").setIcon(R.drawable.ic_lens_red_24dp).show();
					}

					log.debug("add Pathsense geofences: " + zones.toString());
					break;
				case R.id.radioButtonGoogle:
					// remove all Pathsense Geofences and add Google Geofences
					dbGlobalsHelper.storeGlobals(Constants.DB_KEY_NEW_API, "false");
					pathsenseGeofence.removeGeofences();
					// Dienst beenden
					PathsenseLocationProviderApi api = PathsenseLocationProviderApi.getInstance(this);
					api.destroy();
					log.debug("remove Pathsense geofences");
					// Zurück zur Main und fences registrieren
					// Wieder Settings aufrufen, damit der User das Verlassen nicht merkt.
					Intent data = new Intent();
					data.putExtra("import", true);
					setResult(RESULT_OK, data);
					log.debug("add Google geofences");
					finish();
					break;
			}
		}
	}
}











