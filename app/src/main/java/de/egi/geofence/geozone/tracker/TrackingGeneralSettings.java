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

package de.egi.geofence.geozone.tracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.SavedStateHandle;

import com.google.android.gms.location.Priority;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.db.DbZoneHelper;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.geofence.SimpleGeofence;
import de.egi.geofence.geozone.geofence.SimpleGeofenceStore;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.SharedPrefsUtil;
import de.egi.geofence.geozone.utils.Utils;

public class TrackingGeneralSettings extends AppCompatActivity implements OnClickListener, TextWatcher, OnItemSelectedListener {
	private SharedPrefsUtil sharedPrefsUtil;
    private EditText locInterval = null;
    private ImageView ampel = null;
	private DbGlobalsHelper dbGlobalsHelper;
	private SimpleGeofenceStore geofenceStore = null;

	private final SavedStateHandle savedStateHandle = new SavedStateHandle();


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.general_tracking_settings);
		sharedPrefsUtil = new SharedPrefsUtil(this);
        Spinner spinnerLocPriority = findViewById(R.id.spinner_loc_prio);

		dbGlobalsHelper = new DbGlobalsHelper(this);
        String locPrio = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCPRIORITY);
        int loc_spinner = Priority.PRIORITY_BALANCED_POWER_ACCURACY;
		if (locPrio != null) {
			// Bug from google console
			try{
				loc_spinner = Integer.parseInt(locPrio);
			}catch(Exception e){
				// do nothing
			}
        }

		locInterval = this.findViewById(R.id.value_loc_interval);
		locInterval.setText(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCINTERVAL));
		locInterval.setSelection(locInterval.getText().length());

		ampel = this.findViewById(R.id.tracking_service_state);

		if (TrackingUtils.isMyServiceRunning(TrackingLocationService.class, this)){
			ampel.setImageResource(R.drawable.ic_lens_green_24dp);
		}else{
			ampel.setImageResource(R.drawable.ic_lens_red_24dp);
		}
		
		ampel.setOnClickListener(this);
		locInterval.addTextChangedListener(this);
		spinnerLocPriority.setOnItemSelectedListener(this);

		Button stop = this.findViewById(R.id.bt_stop_tracking);
		stop.setOnClickListener(this);

		Button exportTracks = this.findViewById(R.id.bt_export_tracking);
		exportTracks.setOnClickListener(this);

		// Location priority spinner
		List<String> locPrio_li = new ArrayList<>();
		locPrio_li.add(this.getString(R.string.prio_low));
		locPrio_li.add(this.getString(R.string.prio_balanced));
		locPrio_li.add(this.getString(R.string.prio_high));

		ArrayAdapter<String> adapter_loc_prio = new ArrayAdapter<>(this, R.layout.spinner_item, locPrio_li);
		adapter_loc_prio.setDropDownViewResource(R.layout.spinner_dropdown_item);
		spinnerLocPriority.setAdapter(adapter_loc_prio);
		
		int ind_loc_spinner = 0;
		switch (loc_spinner) {
			case Priority.PRIORITY_BALANCED_POWER_ACCURACY:
			ind_loc_spinner = 1;
			break;
		case Priority.PRIORITY_HIGH_ACCURACY:
			ind_loc_spinner = 2;
			break;
		default:
			break;
		}
		spinnerLocPriority.setSelection(ind_loc_spinner, true);

		geofenceStore = new SimpleGeofenceStore(this);

	}

	@Override
	protected void onResume() {
		if (TrackingUtils.isMyServiceRunning(TrackingLocationService.class, this)){
			ampel.setImageResource(R.drawable.ic_lens_green_24dp);
		}else{
			ampel.setImageResource(R.drawable.ic_lens_red_24dp);
		}
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {
		int vId = v.getId();
		if (vId == R.id.bt_stop_tracking) {// Alle Tracks stoppen
			TrackingUtils.stopAllTrackings(this);
			// ToggleButtons reset
			DbZoneHelper dbZoneHelper = new DbZoneHelper(this);
			List<SimpleGeofence> geofences = geofenceStore.getGeofences();
			for (SimpleGeofence simpleGeofence : geofences) {
				String id = simpleGeofence.getId();
				ZoneEntity ze = dbZoneHelper.getCursorZoneByName(id);
				ze.setEnter_tracker(false);
				ze.setExit_tracker(false);

				dbZoneHelper.updateZone(ze);
			}

			ampel.setImageResource(R.drawable.ic_lens_red_24dp);

			// Meldung, dass gestoppt
			Toast.makeText(this, "All trackings stopped", Toast.LENGTH_LONG).show();
		} else if (vId == R.id.tracking_service_state) {
			List<String> list = new ArrayList<>();
			Map<String, ?> map = sharedPrefsUtil.getAllPref();
			Set<String> keys = map.keySet();
			for (String key : keys) {
				if (key.startsWith(Constants.KEY_PREFIX + "_" + "##TRACKING##" + "_##_")) {
					Log.d("Settings", "Registered tracking rule: " + key);
					list.add(key);
				}
			}

			Dialog dialog;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Running Tracks");
			ListView modeList = new ListView(this);
			String[] stringArray = list.toArray(new String[0]);
			ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
			modeList.setAdapter(modeAdapter);

			builder.setView(modeList);
			dialog = builder.create();
			dialog.show();
		}else if (vId == R.id.bt_export_tracking) {
			List<String> list = new ArrayList<>();
			Map<String, ?> map = sharedPrefsUtil.getAllPref();
			Set<String> keys = map.keySet();

			File dir = new File(getFilesDir() + File.separator + "egigeozone");
			File[] files = dir.listFiles();

			for (File file : files) {
				if (file.getName().startsWith("locationtracker")) {
					list.add(file.getName());
					Log.d("Settings", "Track file: " + file.getName());
				}
			}

			Dialog dialog;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Track files");
			ListView modeList = new ListView(this);
			String[] stringArray = list.toArray(new String[0]);
			ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
			modeList.setAdapter(modeAdapter);

			modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String trackName = (String) ((AppCompatTextView) view).getText();
					Log.d("Settings", "Track file: " + trackName);

					Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("text/*");
					intent.putExtra(Intent.EXTRA_TITLE, trackName);
					//save track name for onActivityResult()
					savedStateHandle.set("trackName", trackName);
					activityResultLaunchWriteExportFile.launch(intent);
				}
			});

			builder.setView(modeList);
			dialog = builder.create();
			dialog.show();
		}
	}

	ActivityResultLauncher<Intent> activityResultLaunchWriteExportFile = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					// Load private certificate and copy it to apps private space
					if (result.getResultCode() == RESULT_OK && result.getData() != null) {

						try (InputStream is = new FileInputStream(getFilesDir() + File.separator + "egigeozone" + File.separator + savedStateHandle.get("trackName")); OutputStream os = getContentResolver().openOutputStream(result.getData().getData())) {
							// InputStream constructor takes File, String (path), or FileDescriptor
							// data.getData() holds the URI of the path selected by the picker

							byte[] buffer = new byte[1024];
							int length;
							while ((length = is.read(buffer)) > 0) {
								os.write(buffer, 0, length);
							}
						} catch (IOException e) {
//							log.error("Could not write Export file. Result: " + e.getMessage());
						}
						Toast.makeText(getApplicationContext(), R.string.export_ok_text, Toast.LENGTH_LONG).show();

						// Datei wurde gespeichert. Nur Dialog ausgeben
//						AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(getApplicationContext());
//						ab.setMessage(R.string.export_ok_text).setPositiveButton(R.string.action_ok, dialogClickListener).setTitle("Export").setIcon(R.drawable.ic_file_upload_black_24dp).show();
					}else{
//						log.error("Could not write Export file. Result: " + result.getResultCode());
						Toast.makeText(getApplicationContext(), "Could not write Export file. Result: " + result.getResultCode(), Toast.LENGTH_LONG).show();
					}
				}
			});


	@Override
	public void afterTextChanged(Editable s) {
		dbGlobalsHelper.storeGlobals(Constants.DB_KEY_LOCINTERVAL, (locInterval.getText().toString().equalsIgnoreCase("") ? "5" : locInterval.getText().toString()));
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// Location priority
		if (arg0.getId() == R.id.spinner_loc_prio){
	        String prio = arg0.getSelectedItem().toString();
        	if (prio.equalsIgnoreCase(this.getString(R.string.prio_low))){
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_LOCPRIORITY, Integer.valueOf(Priority.PRIORITY_LOW_POWER).toString());
        	}
        	if (prio.equalsIgnoreCase(this.getString(R.string.prio_balanced))){
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_LOCPRIORITY, Integer.valueOf(Priority.PRIORITY_BALANCED_POWER_ACCURACY).toString());
        	}
        	if (prio.equalsIgnoreCase(this.getString(R.string.prio_high))){
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_LOCPRIORITY, Integer.valueOf(Priority.PRIORITY_HIGH_ACCURACY).toString());
        	}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		Log.d("Settings", "onNothingSelected");
	}

}