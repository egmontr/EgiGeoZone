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
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import de.egi.geofence.geozone.InfoReplace;
import de.egi.geofence.geozone.MainEgiGeoZone;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.Worker;
import de.egi.geofence.geozone.db.DbServerHelper;
import de.egi.geofence.geozone.db.ServerEntity;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.tracker.TrackingReceiverWorkerService;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.Utils;

@SuppressWarnings("deprecation")
public class ServerProfile  extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
	private DbServerHelper datasource;
	private String aktion;
	private String ind;
	private boolean _update = false;
	private DbServerHelper datasourceFallbackServer;
	private ServerEntity srv;
	private View viewMerk;

	private GoogleApiClient mLocationClient;
	private final Logger log = Logger.getLogger(ServerProfile.class);

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.server_profile);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Utils.changeBackGroundToolbar(this, toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_server_profile);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Speichern
				saveServer();
			}
		});

		viewMerk = findViewById(R.id.snackbarPosition);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			// Check Permissions Now
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
				// Display UI and wait for user interaction
				AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
				alertDialogBuilder.setMessage(getString(R.string.checkPhone));
				alertDialogBuilder.setTitle(getString(R.string.titlePhone));

						alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								ActivityCompat.requestPermissions(ServerProfile.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MainEgiGeoZone.REQUEST_PHONE_STATE);
//						Toast.makeText(getBaseContext(),"You clicked yes button",Toast.LENGTH_LONG).show();
							}
						});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();

			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, MainEgiGeoZone.REQUEST_PHONE_STATE);
			}
		} else {
			// permission has been granted, continue as usual
//			Toast.makeText(this,"permission has been granted, continue as usual",Toast.LENGTH_LONG).show();
		}

		datasource = new DbServerHelper(this);
		datasourceFallbackServer = new DbServerHelper(this);

		Cursor cursorSrv = datasourceFallbackServer.getCursorAllServer();
		List<String> listSrv = new ArrayList<>();
		listSrv.add("none");
		while (cursorSrv.moveToNext()) {
			listSrv.add(cursorSrv.getString(1));
		}
		cursorSrv.close();
		ArrayAdapter<String> adapterServer = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listSrv);
		Spinner spinner_fallbackServer = (Spinner) findViewById(R.id.spinner_server_profile_fallback);

		adapterServer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_fallbackServer.setAdapter(adapterServer);

		// Receiver registrieren
		IntentFilter testOkStatusFilter = new IntentFilter(Constants.ACTION_TEST_STATUS_OK);
		this.registerReceiver(mReceiver, testOkStatusFilter);
		IntentFilter testNokStatusFilter = new IntentFilter(Constants.ACTION_TEST_STATUS_NOK);
		this.registerReceiver(mReceiver, testNokStatusFilter);

		Bundle b = getIntent().getExtras();
		if (null != b){
			aktion = b.getString("action");
			ind = b.getString("ind");
		}


		if (aktion.equalsIgnoreCase("new")){
			// Felder leer lassen
//			boolean _new = true;
		}else if (aktion.equalsIgnoreCase("update")){
			_update = true;
		}

		if (_update){
			// Satz aus der DB lesen
			srv = datasource.getCursorServerById(Integer.valueOf(ind));

			((EditText) this.findViewById(R.id.value_name)).setText(srv.getName());
			((EditText) this.findViewById(R.id.value_urlEntered)).setText(srv.getUrl_enter());
			((EditText) this.findViewById(R.id.value_urlExited)).setText(srv.getUrl_exit());
			((EditText) this.findViewById(R.id.value_user)).setText(srv.getUser());
			((EditText) this.findViewById(R.id.value_userPasswd)).setText(srv.getUser_pw());
			((EditText) this.findViewById(R.id.value_certName)).setText(srv.getCert());
			((EditText) this.findViewById(R.id.value_certNamePasswd)).setText(srv.getCert_password());
			((EditText) this.findViewById(R.id.value_caCertName)).setText(srv.getCa_cert());
			((EditText) this.findViewById(R.id.value_fhem_geofancy)).setText(srv.getUrl_fhem());
			((EditText) this.findViewById(R.id.value_tracking)).setText(srv.getUrl_tracking());
			((EditText) this.findViewById(R.id.value_timeout)).setText(srv.getTimeout());

			if(srv != null && srv.getId_fallback() != null){
				ServerEntity se = datasourceFallbackServer.getCursorServerByName(srv.getId_fallback());
				if (se != null) {
					int ind_se = listSrv.indexOf(se.getName()) < 0 ? 0 : listSrv.indexOf(se.getName());
					spinner_fallbackServer.setSelection(ind_se, true);
				}
			}
		}
	}

	private void saveServer() {
		if (checkInputFields()) {
			return;
		}

		ServerEntity srvEntity = new ServerEntity();
		if (_update){
			srvEntity.setId(Integer.parseInt(ind));
		}
		srvEntity.setName(((EditText) this.findViewById(R.id.value_name)).getText().toString());
		srvEntity.setUrl_fhem(((EditText) this.findViewById(R.id.value_fhem_geofancy)).getText().toString());
		srvEntity.setUrl_tracking(((EditText) this.findViewById(R.id.value_tracking)).getText().toString());
		srvEntity.setUrl_enter(((EditText) this.findViewById(R.id.value_urlEntered)).getText().toString());
		srvEntity.setUrl_exit(((EditText) this.findViewById(R.id.value_urlExited)).getText().toString());
		srvEntity.setUser(((EditText) this.findViewById(R.id.value_user)).getText().toString());
		srvEntity.setUser_pw(((EditText) this.findViewById(R.id.value_userPasswd)).getText().toString());
		srvEntity.setCert(((EditText) this.findViewById(R.id.value_certName)).getText().toString());
		srvEntity.setCert_password(((EditText) this.findViewById(R.id.value_certNamePasswd)).getText().toString());
		srvEntity.setCa_cert(((EditText) this.findViewById(R.id.value_caCertName)).getText().toString());
		srvEntity.setTimeout(((EditText) this.findViewById(R.id.value_timeout)).getText().toString());

		Spinner mSpinner_server = (Spinner)findViewById(R.id.spinner_server_profile_fallback);
		String serverFallback = (String)mSpinner_server.getSelectedItem();

		srvEntity.setId_fallback(serverFallback.equals("none") ? null : serverFallback);

		datasource.storeServer(srvEntity);

		setResult(RESULT_OK);
		finish();
	}

	/**
	 * Check all the input values and flag those that are incorrect
	 * @return true if all the widget values are correct; otherwise false
	 */
	private boolean checkInputFields() {
		// Start with the input validity flag set to true
		// Hier Prüfungen
		if (TextUtils.isEmpty(((EditText) this.findViewById(R.id.value_name)).getText().toString())) {
			this.findViewById(R.id.value_name).setBackgroundColor(Color.RED);
//			Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG).show();
			this.findViewById(R.id.value_name).requestFocus();
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
		switch(item.getItemId()) {
			case R.id.menu_delete_profile_log:
				AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
				ab.setMessage(R.string.action_delete).setPositiveButton(R.string.action_yes, dialogClickListener).setNegativeButton(R.string.action_no, dialogClickListener).show();
				return true;
			case R.id.menu_help:
				Intent i = new Intent(this, InfoReplace.class);
				startActivity(i);
				return true;
			case R.id.menu_test:
				if (checkInputFields()) {
					return true;
				}

				mLocationClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
				mLocationClient.connect();

				return true;
			// Pass through any other request
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	private final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					//Do your Yes progress
					try{
						//Nur löschen, wenn nicht als Fallback verwendet wird!
						Cursor cursor = datasourceFallbackServer.getCursorAllServer();
						while (cursor.moveToNext()) {
							if(cursor.getString(11) != null){
								if(cursor.getString(11).equalsIgnoreCase(srv.getName())){
									cursor.close();
									Toast.makeText(getApplicationContext(), R.string.profile_in_use, Toast.LENGTH_LONG).show();
									return;
								}
							}
						}
						cursor.close();
						datasource.deleteServer(ind);
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
			String type = intent.getStringExtra("TestType") != null ? intent.getStringExtra("TestType") : "";
			showAlert(action, result, type);
		}
	};

	private void showAlert(String action, String result, String type){
		if (Constants.ACTION_TEST_STATUS_OK.equals(action)) {
			// Teststatus anzeigen
			AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
			ab.setMessage(R.string.test_ok_text).setPositiveButton(R.string.action_ok, testDialogClickListener).setTitle(type + ": " + getString(R.string.test_ok_title)).setIcon(R.drawable.ic_lens_green_24dp).show();
		}
		if (Constants.ACTION_TEST_STATUS_NOK.equals(action)) {
			// Teststatus anzeigen
			AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
			ab.setMessage(result).setPositiveButton(R.string.action_ok, testDialogClickListener)
					.setTitle(type + ": " + getString(R.string.test_nok_title)).setIcon(R.drawable.ic_lens_red_24dp).show();
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

	@SuppressLint("SetTextI18n")
	@Override
	public void onConnected(@Nullable Bundle bundle) {
		// If Google Play Services is available
		if (servicesConnected()) {
			// Get the current location
			Location currentLocation = null;
			try{
				currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
			}catch(SecurityException se){
				// Display UI and wait for user interaction
				AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
				alertDialogBuilder.setMessage(getString(R.string.alertPermissions));
				alertDialogBuilder.setTitle(getString(R.string.titleAlertPermissions));

				alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
}
				});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
			if (currentLocation != null){
				// Start test
				log.debug("onConnected - location: " + (Double.valueOf(currentLocation.getLatitude()).toString()) + "##" + (Double.valueOf(currentLocation.getLongitude()).toString()));
				doTest(currentLocation);
			}else{
				Toast.makeText(this, "Could not determine location. ", Toast.LENGTH_LONG).show();
                log.error("Could not determine location.");
			}
		}
		mLocationClient.disconnect();
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}
	/**
	 * Verify that Google Play services is available before making a request.
	 *
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {
		log.debug("servicesConnected");
		// Check that Google Play services is available
		GoogleApiAvailability api = GoogleApiAvailability.getInstance();
		int code = api.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == code) {
			// In debug mode, log the status
			Log.d(Constants.APPTAG, getString(R.string.play_services_available));
			log.info("servicesConnected result from Google Play Services: " + getString(R.string.play_services_available));
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else if (api.isUserResolvableError(code)){
			log.error("servicesConnected result: could not connect to Google Play services");
			api.showErrorDialogFragment(this, code, Constants.PLAY_SERVICES_RESOLU‌​TION_REQUEST);
		} else {
			log.error("servicesConnected result: could not connect to Google Play services");
			Toast.makeText(this, api.getErrorString(code), Toast.LENGTH_LONG).show();
		}
		return false;
	}

	private void doTest(Location currentLocation){
		String user = ((EditText) this.findViewById(R.id.value_user)).getText().toString();
		String userPw = ((EditText) this.findViewById(R.id.value_userPasswd)).getText().toString();
		String urlEntered = ((EditText) this.findViewById(R.id.value_urlEntered)).getText().toString();
		String urlExited = ((EditText) this.findViewById(R.id.value_urlExited)).getText().toString();
		String trackUrl = ((EditText) this.findViewById(R.id.value_tracking)).getText().toString();
		String certName = ((EditText) this.findViewById(R.id.value_certName)).getText().toString();
		String certNamePasswd = ((EditText) this.findViewById(R.id.value_certNamePasswd)).getText().toString();
		String caCertName = ((EditText) this.findViewById(R.id.value_caCertName)).getText().toString();
		String fhem_geofancy = ((EditText) this.findViewById(R.id.value_fhem_geofancy)).getText().toString();
		String timeout = ((EditText) this.findViewById(R.id.value_timeout)).getText().toString();
		String alias = "";

		ZoneEntity ze = Utils.makeTestZone();

		String urlEnteredReplace = Utils.replaceAll(this, urlEntered, ze.getName(), ze.getAlias(), 1, ze.getRadius(),
				(Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()),
				(Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()),
				null, null, (Float.valueOf(currentLocation.getAccuracy()).toString()));

		String urlExitedReplace = Utils.replaceAll(this, urlExited, ze.getName(), ze.getAlias(), 1, ze.getRadius(),
				(Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()),
				(Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()),
				null, null, (Float.valueOf(currentLocation.getAccuracy()).toString()));

		Worker worker = new Worker(this.getApplicationContext());

		try {
			worker.doServerRequest(1, this.getApplicationContext(), urlEnteredReplace, urlExitedReplace, fhem_geofancy,
					Constants.TEST_ZONE, (Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()),
					certName, certNamePasswd, caCertName, user, userPw, timeout, alias,
					(Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()), null, true, 0);
		} catch (Exception ex) {
			Log.e(Constants.APPTAG, "error sending TestZone request to server", ex);
			NotificationUtil.showError(this.getApplicationContext(), "TestZone" + ": Error sending test request to server", ex.toString());
		}

		try {
			TrackingReceiverWorkerService receiverWorker = new TrackingReceiverWorkerService();
			receiverWorker.executeCall(this, "TestTracking", "TestAliasTracking",
					(Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()),
					null, null, (Float.valueOf(currentLocation.getAccuracy()).toString()), trackUrl, timeout,
					certName, certNamePasswd, caCertName, user, userPw, true);
		} catch (Exception ex) {
			Log.e(Constants.APPTAG, "error sending TestTracking request to server", ex);
			NotificationUtil.showError(this.getApplicationContext(), "TestTracking" + ": Error sending test request to server", ex.toString());
		}

	}
	@Override
	protected void onPause() {
		super.onPause();
		try {
			this.unregisterReceiver(mReceiver);
		} catch (Exception e) {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Receiver registrieren
		IntentFilter testOkStatusFilter = new IntentFilter(Constants.ACTION_TEST_STATUS_OK);
		this.registerReceiver(mReceiver, testOkStatusFilter);
		IntentFilter testNokStatusFilter = new IntentFilter(Constants.ACTION_TEST_STATUS_NOK);
		this.registerReceiver(mReceiver, testNokStatusFilter);
	}
}






















