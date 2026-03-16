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

package de.egi.geofence.geozone.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.apache.log4j.Logger;

import de.egi.geofence.geozone.InfoReplace;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.Worker;
import de.egi.geofence.geozone.db.DbSmsHelper;
import de.egi.geofence.geozone.db.SmsEntity;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.RuntimePermissionsActivity;
import de.egi.geofence.geozone.utils.Utils;

@SuppressWarnings("deprecation")
public class SmsProfile extends RuntimePermissionsActivity implements GoogleApiClient.OnConnectionFailedListener{
	private DbSmsHelper datasource;
	private String aktion;
	private String ind;
	private boolean _update = false;
	private CheckBox enter = null;
	private CheckBox exit = null;
	private View viewMerk;
	private final Logger log = Logger.getLogger(SmsProfile.class);
	private String textNeu = null;
	private String to = null;
	private Worker worker = null;
	@SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.sms_profile);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Utils.changeBackGroundToolbar(this, toolbar);

		FloatingActionButton fab = findViewById(R.id.fab_mail_profile);
		fab.setOnClickListener(view -> {
            // Speichern
            saveSms();
        });

		viewMerk = findViewById(R.id.snackbarPosition);

//		if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//			// Check Permissions Now
//			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
//				// Display UI and wait for user interaction
//				AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
//				alertDialogBuilder.setMessage(getString(R.string.checkSMS));
//				alertDialogBuilder.setTitle(getString(R.string.titleSMS));
//
//				alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						ActivityCompat.requestPermissions(SmsProfile.this, new String[]{Manifest.permission.SEND_SMS}, MainEgiGeoZone.REQUEST_SMS);
//					}
//				});
//				AlertDialog alertDialog = alertDialogBuilder.create();
//				alertDialog.show();
//
//			} else {
//				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MainEgiGeoZone.REQUEST_SMS);
//			}
//		} else {
//			// permission has been granted, continue as usual
////			Toast.makeText(this,"permission has been granted, continue as usual",Toast.LENGTH_LONG).show();
//		}

		datasource = new DbSmsHelper(this);

		enter = (this.findViewById(R.id.chk_enter));
		exit = (this.findViewById(R.id.chk_exit));

		// Receiver registrieren
		IntentFilter testOkStatusFilter = new IntentFilter(Constants.ACTION_TEST_STATUS_OK);
		IntentFilter testNokStatusFilter = new IntentFilter(Constants.ACTION_TEST_STATUS_NOK);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			this.registerReceiver(mReceiver, testOkStatusFilter, RECEIVER_EXPORTED);
			this.registerReceiver(mReceiver, testNokStatusFilter, RECEIVER_EXPORTED);
		}else{
			this.registerReceiver(mReceiver, testOkStatusFilter);
			this.registerReceiver(mReceiver, testNokStatusFilter);
		}
		Bundle b = getIntent().getExtras();
		if (null != b){
			aktion = b.getString("action");
			ind = b.getString("ind");
		}


		if (aktion.equalsIgnoreCase("new")){
			// Felder leer lassen
//			boolean _new = true;
			enter.setChecked(true);
			exit.setChecked(true);
		}else if (aktion.equalsIgnoreCase("update")){
			_update = true;
		}

		if (_update){
			// Satz aus der DB lesen
			SmsEntity se = datasource.getCursorSmsById(Integer.valueOf(ind));
			((TextView) this.findViewById(R.id.value_name)).setText(se.getName());
			((EditText) this.findViewById(R.id.value_sms_to)).setText(se.getNumber());
			((EditText) this.findViewById(R.id.value_sms_text)).setText(se.getText());
			enter.setChecked(se.isEnter());
			exit.setChecked(se.isExit());
		}
	}

	private void saveSms(){
		if (checkInputFields()) {
			return;
		}

		SmsEntity smsEntity = new SmsEntity();
		if (_update){
			smsEntity.setId(Integer.parseInt(ind));
		}
		smsEntity.setName(((EditText) this.findViewById(R.id.value_name)).getText().toString());
		smsEntity.setNumber(((EditText) this.findViewById(R.id.value_sms_to)).getText().toString());
		smsEntity.setText(((EditText) this.findViewById(R.id.value_sms_text)).getText().toString());

		if (enter.isChecked()){
			smsEntity.setEnter(true);
		}else{
			smsEntity.setEnter(false);
		}

		if (exit.isChecked()){
			smsEntity.setExit(true);
		}else{
			smsEntity.setExit(false);
		}

		datasource.storeSms(smsEntity);

		setResult(4812);
		finish();
	}

	/**
	 * Check all the input values and flag those that are incorrect
	 * @return true if all the widget values are correct; otherwise false
	 */
	private boolean checkInputFields() {
		// Start with the input validity flag set to true
		if (TextUtils.isEmpty(((EditText) this.findViewById(R.id.value_name)).getText().toString())) {
			this.findViewById(R.id.value_name).setBackgroundColor(Color.RED);
			this.findViewById(R.id.value_name).requestFocus();
//			Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG).show();
			Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
			return true;
		} else if (((EditText) findViewById(R.id.value_name)).getText().toString().contains(",")){
			findViewById(R.id.value_name).setBackgroundColor(Color.RED);
			this.findViewById(R.id.value_name).requestFocus();
			Snackbar.make(viewMerk, R.string.geofence_input_error_comma, Snackbar.LENGTH_LONG).show();
			return true;
		} else if (((EditText) findViewById(R.id.value_name)).getText().toString().contains("'")){
			findViewById(R.id.value_name).setBackgroundColor(Color.RED);
			this.findViewById(R.id.value_name).requestFocus();
			Snackbar.make(viewMerk, R.string.geofence_input_error_comma, Snackbar.LENGTH_LONG).show();
			return true;
		}

		if (TextUtils.isEmpty(((EditText) this.findViewById(R.id.value_sms_to)).getText().toString())) {
			this.findViewById(R.id.value_sms_to).setBackgroundColor(Color.RED);
			this.findViewById(R.id.value_sms_to).requestFocus();
//			Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG).show();
			Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
			return true;
		}

		if (TextUtils.isEmpty(((EditText) this.findViewById(R.id.value_sms_text)).getText().toString())) {
			this.findViewById(R.id.value_sms_text).setBackgroundColor(Color.RED);
			this.findViewById(R.id.value_sms_text).requestFocus();
//			Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG).show();
			Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
			return true;
		}
		// If everything passes, the validity flag will still be true, otherwise it will be false.
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_profil_del_help, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		int itemId = item.getItemId();
		if (itemId == R.id.menu_delete_profile_log) {
			AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
			ab.setMessage(R.string.action_delete).setPositiveButton(R.string.action_yes, dialogClickListener).setNegativeButton(R.string.action_no, dialogClickListener).show();
			return true;
		}else if (itemId == R.id.menu_help){
			Intent i = new Intent(this, InfoReplace.class);
			startActivity(i);
			return true;
		}else if (itemId == R.id.menu_test) {
			if (checkInputFields()) {
				return true;
			}

			FusedLocationProviderClient mLocationClient = LocationServices.getFusedLocationProviderClient(this);

			try {
				mLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
						.addOnSuccessListener(location -> {
							if (location != null) {
								// Start test
								log.debug("onConnected - location: " + (Double.valueOf(location.getLatitude()).toString()) + "##" + (Double.valueOf(location.getLongitude()).toString()));
								doTest(location);
							} else {
								Toast.makeText(this, "Could not determine location. ", Toast.LENGTH_LONG).show();
								log.error("Could not determine location.");
							}
						});
			} catch (SecurityException se) {
				// Display UI and wait for user interaction
				androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
				alertDialogBuilder.setMessage(this.getString(R.string.alertPermissions));
				alertDialogBuilder.setTitle(this.getString(R.string.titleAlertPermissions));

				alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
				androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}

			return true;
		}
		// Pass through any other request
		return super.onOptionsItemSelected(item);
	}

	private final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					//Do your Yes progress
					try{
						datasource.deleteSms(ind);
					}catch(Exception e){
						Toast.makeText(getApplicationContext(), R.string.profile_in_use, Toast.LENGTH_LONG).show();
					}
					finish();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					//Do your No progress
					break;
			}
		}
	};

	//The BroadcastReceiver that listens for broadcasts
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
//			GlobalSingleton.getInstance().setTestResultError(false);
			String result = intent.getStringExtra("TestResult") != null ? intent.getStringExtra("TestResult") : "";
			showAlert(action, result);
		}
	};

	private void showAlert(String action, String result){
		if (Constants.ACTION_TEST_STATUS_OK.equals(action)) {
			// Teststatus anzeigen
			AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
			ab.setMessage(R.string.test_ok_text).setPositiveButton(R.string.action_ok, testDialogClickListener).setTitle(R.string.test_ok_title).setIcon(R.drawable.ic_lens_green_24dp).show();
		}
		if (Constants.ACTION_TEST_STATUS_NOK.equals(action)) {
			// Teststatus anzeigen
			AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
			ab.setMessage(result).setPositiveButton(R.string.action_ok, testDialogClickListener)
					.setTitle(R.string.test_nok_title).setIcon(R.drawable.ic_lens_red_24dp).show();
		}

	}

	// Dialog für TestErgebnis
	private final DialogInterface.OnClickListener testDialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					break;
			}
		}
	};

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	private void doTest(Location currentLocation){

		to = ((EditText) this.findViewById(R.id.value_sms_to)).getText().toString();
		String text = ((EditText) this.findViewById(R.id.value_sms_text)).getText().toString();

		ZoneEntity ze = Utils.makeTestZone();

		String textReplace = Utils.replaceAll(this, text, ze.getName(), ze.getAlias(), 1, ze.getRadius(),
				(Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()),
				(Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()),
				null, null, (Float.valueOf(currentLocation.getAccuracy()).toString()));

		textNeu = textReplace.length() > 155 ? textReplace.substring(0,155) : textReplace;
		worker = new Worker(this.getApplicationContext());
		try {
			if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
				requestAppPermission(Manifest.permission.SEND_SMS, R.string.alert2070Sms, 2070);
			}else {
				worker.doSendSms(this.getApplicationContext(), Constants.TEST_ZONE, to, textNeu, true);
			}
		} catch (Exception ex) {
			Log.e(Constants.APPTAG, "error sending test sms", ex);
			NotificationUtil.showError(this.getApplicationContext(), "TestSms" + ": Error sending test sms", ex.toString());
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		try {
			this.unregisterReceiver(mReceiver);
		} catch (Exception ignored) {
		}
	}

	@SuppressLint("GestureBackNavigation")
	@Override
	public void onBackPressed() {
		setResult(4812);
		super.onBackPressed();
	}
	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	@Override
	protected void onResume() {
		super.onResume();
		// Receiver registrieren
		IntentFilter testOkStatusFilter = new IntentFilter(Constants.ACTION_TEST_STATUS_OK);
		IntentFilter testNokStatusFilter = new IntentFilter(Constants.ACTION_TEST_STATUS_NOK);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			this.registerReceiver(mReceiver, testOkStatusFilter, RECEIVER_EXPORTED);
			this.registerReceiver(mReceiver, testNokStatusFilter, RECEIVER_EXPORTED);
		}else{
			this.registerReceiver(mReceiver, testOkStatusFilter);
			this.registerReceiver(mReceiver, testNokStatusFilter);
		}
	}
	@Override
	public void onPermissionsGranted(int requestCode) {
		worker.doSendSms(this.getApplicationContext(), Constants.TEST_ZONE, to, textNeu, true);
	}
}