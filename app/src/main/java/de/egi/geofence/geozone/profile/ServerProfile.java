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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.egi.geofence.geozone.InfoReplace;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.Worker;
import de.egi.geofence.geozone.db.DbServerHelper;
import de.egi.geofence.geozone.db.ServerEntity;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.tracker.TrackingReceiverWorkerService;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.Utils;

// @SuppressWarnings("deprecation")
public class ServerProfile  extends AppCompatActivity implements View.OnClickListener{
	private DbServerHelper datasource;
	private String aktion;
	private String ind;
	private boolean _update = false;
	private DbServerHelper datasourceFallbackServer;
	private ServerEntity srv;
	private View viewMerk;
	ImageButton buttonSetCaCert = null;
	ImageButton buttonSetCert = null;

	private final Logger log = Logger.getLogger(ServerProfile.class);

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.server_profile);
		buttonSetCert = this.findViewById(R.id.setClientCert);
		buttonSetCaCert = this.findViewById(R.id.setCaCert);

		buttonSetCert.setOnClickListener(this);
		buttonSetCaCert.setOnClickListener(this);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Utils.changeBackGroundToolbar(this, toolbar);

		FloatingActionButton fab = findViewById(R.id.fab_server_profile);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Speichern
				saveServer();
			}
		});

		viewMerk = findViewById(R.id.snackbarPosition);
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
		Spinner spinner_fallbackServer = findViewById(R.id.spinner_server_profile_fallback);

		adapterServer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_fallbackServer.setAdapter(adapterServer);

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
		}else if (aktion.equalsIgnoreCase("update")){
			_update = true;
		}

		if (_update){
			// Satz aus der DB lesen
			srv = datasource.getCursorServerById(Integer.parseInt(ind));

			((EditText) this.findViewById(R.id.value_name)).setText(srv.getName());
			((EditText) this.findViewById(R.id.value_urlEntered)).setText(srv.getUrl_enter());
			((EditText) this.findViewById(R.id.value_urlExited)).setText(srv.getUrl_exit());
			((EditText) this.findViewById(R.id.value_user)).setText(srv.getUser());
			((EditText) this.findViewById(R.id.value_userPasswd)).setText(srv.getUser_pw());
			((EditText) this.findViewById(R.id.value_certName)).setText(srv.getCert());
			((EditText) findViewById(R.id.value_certName)).setSelection(((EditText) findViewById(R.id.value_certName)).getText().length());
			((EditText) this.findViewById(R.id.value_certNamePasswd)).setText(srv.getCert_password());
			((EditText) this.findViewById(R.id.value_caCertName)).setText(srv.getCa_cert());
			((EditText) findViewById(R.id.value_caCertName)).setSelection(((EditText) findViewById(R.id.value_caCertName)).getText().length());
			((EditText) this.findViewById(R.id.value_fhem_geofancy)).setText(srv.getUrl_fhem());
			((EditText) this.findViewById(R.id.value_tracking)).setText(srv.getUrl_tracking());
			((EditText) this.findViewById(R.id.value_timeout)).setText(srv.getTimeout());

			if(srv != null && srv.getId_fallback() != null){
				ServerEntity se = datasourceFallbackServer.getCursorServerByName(srv.getId_fallback());
				if (se != null) {
					int ind_se = !listSrv.contains(se.getName()) ? 0 : listSrv.indexOf(se.getName());
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

		Spinner mSpinner_server = findViewById(R.id.spinner_server_profile_fallback);
		String serverFallback = (String)mSpinner_server.getSelectedItem();

		srvEntity.setId_fallback(serverFallback.equals("none") ? null : serverFallback);

		datasource.storeServer(srvEntity);

		setResult(4811);
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
		int itemId = item.getItemId();
		if (itemId == R.id.menu_delete_profile_log) {
			AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
			ab.setMessage(R.string.action_delete).setPositiveButton(R.string.action_yes, dialogClickListener).setNegativeButton(R.string.action_no, dialogClickListener).show();
			return true;
		} else if (itemId == R.id.menu_help) {
			Intent i = new Intent(this, InfoReplace.class);
			startActivity(i);
			return true;
		} else if (itemId == R.id.menu_test) {
			if (checkInputFields()) {
				return true;
			}

			// Stores the current instantiation of the location client in this object
			FusedLocationProviderClient mLocationClient = LocationServices.getFusedLocationProviderClient(this);
			try {
				mLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
						.addOnSuccessListener(location -> {
							if (location != null) {
								// Start test
								log.debug("onConnected - location: " + (Double.valueOf(location.getLatitude()).toString()) + "##" + (Double.valueOf(location.getLongitude()).toString()));
								doTest(location);
							}else{
								Toast.makeText(this, "Could not determine location. ", Toast.LENGTH_LONG).show();
								log.error("Could not determine location.");
							}
						});

				return true;
				// Pass through any other request
			} catch (SecurityException se) {
				// Display UI and wait for user interaction
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
		}
		return super.onOptionsItemSelected(item);
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
					Intent intent = new Intent();
					setResult(4811, intent);
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
		}
	};

	// Load private certificate and copy it to apps private space
	ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					// Load private certificate and copy it to apps private space
					if (result.getResultCode() == RESULT_OK && result.getData() != null) {
						Uri uriInput = result.getData().getData();
						String[] dateiName = uriInput.getPath().split("/");
						String neuerDateiName = dateiName[dateiName.length - 1];
						File certDir = getDir("certificates", Context.MODE_PRIVATE);
						File certificateFile = new File(certDir, neuerDateiName);
						// copyInputStreamToFile
						try (InputStream is = getContentResolver().openInputStream(uriInput); FileOutputStream outputStream = new FileOutputStream(certificateFile, false)) {
							// append = false
							int read;
							byte[] bytes = new byte[8192];
							while ((read = is.read(bytes)) != -1) {
								outputStream.write(bytes, 0, read);
							}
						} catch (IOException e) {
							log.error("Could not load private certificate. Result: " + e.getMessage());
							Toast.makeText(getApplicationContext(), "Could not load private certificate. Result: " + e.getMessage(), Toast.LENGTH_LONG).show();
							return;
						}

						((EditText) findViewById(R.id.value_certName)).setText(certificateFile.getPath());
						((EditText) findViewById(R.id.value_certName)).setSelection(((EditText) findViewById(R.id.value_certName)).getText().length());

					}else{
						log.error("Could not load private certificate. Result: " + result.getResultCode());
						Toast.makeText(getApplicationContext(), "Could not load private certificate. Result: " + result.getResultCode(), Toast.LENGTH_LONG).show();
					}
				}
			});
	/**
	 * Load certificate
	 */
	public void onCertClicked(View view) {
		log.debug("onCertClicked");

		// Load certificate
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
		activityResultLaunch.launch(intent);
	}

	// Load CA certificate and copy it to apps private space
	ActivityResultLauncher<Intent> activityResultLaunch2 = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					// Load CA certificate and copy it to apps private space
					if (result.getResultCode() == RESULT_OK && result.getData() != null) {
						Uri uriInput = result.getData().getData();
						String[] dateiName = uriInput.getPath().split("/");
						String neuerDateiName = dateiName[dateiName.length - 1];
						File certDir = getDir("certificates", Context.MODE_PRIVATE);
						File certificateFile = new File(certDir, neuerDateiName);
						// copyInputStreamToFile
						try (InputStream is = getContentResolver().openInputStream(uriInput); FileOutputStream outputStream = new FileOutputStream(certificateFile, false)) {
							// append = false
							int read;
							byte[] bytes = new byte[8192];
							while ((read = is.read(bytes)) != -1) {
								outputStream.write(bytes, 0, read);
							}
						} catch (IOException e) {
							log.error("Could not load CA certificate. Result: " + e.getMessage());
							Toast.makeText(getApplicationContext(), "Could not load CA certificate. Result: " + e.getMessage(), Toast.LENGTH_LONG).show();
							return;
						}

						((EditText) findViewById(R.id.value_caCertName)).setText(certificateFile.getPath());
						((EditText) findViewById(R.id.value_caCertName)).setSelection(((EditText) findViewById(R.id.value_caCertName)).getText().length());

					}else{
						log.error("Could not load CA certificate. Result: " + result.getResultCode());
						Toast.makeText(getApplicationContext(), "Could not load CA certificate. Result: " + result.getResultCode(), Toast.LENGTH_LONG).show();
					}
				}
			});
	/**
	 * Load CA certificate
	 */
	public void onCaCertClicked(View view) {
		log.debug("onCaCertClicked");

		// Load CA certificate
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
		activityResultLaunch2.launch(intent);
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
		} catch (Exception ignored) {
		}
	}

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
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.setClientCert) {
			log.debug("onOptionsItemSelected: setClientCert");
			onCertClicked(buttonSetCert);
		} else if (id == R.id.setCaCert) {
			log.debug("onOptionsItemSelected: setCaCert");
			onCaCertClicked(buttonSetCaCert);
		}
	}
}

