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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import com.google.android.gms.location.LocationRequest;

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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.general_tracking_settings);
		sharedPrefsUtil = new SharedPrefsUtil(this);
        Spinner spinnerLocPriority = (Spinner) findViewById(R.id.spinner_loc_prio);

		dbGlobalsHelper = new DbGlobalsHelper(this);
        String locPrio = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCPRIORITY);
        int loc_spinner = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
        if (locPrio != null) {
			// Bug from google console
			try{
				loc_spinner = Integer.parseInt(locPrio);
			}catch(Exception e){
				// do nothing
			}
        }

		locInterval = ((EditText) this.findViewById(R.id.value_loc_interval));
		locInterval.setText(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOCINTERVAL));
		locInterval.setSelection(locInterval.getText().length());

		ampel = ((ImageView) this.findViewById(R.id.tracking_service_state));

		if (TrackingUtils.isMyServiceRunning(TrackingLocationService.class, this)){
			ampel.setImageResource(R.drawable.ic_lens_green_24dp);
		}else{
			ampel.setImageResource(R.drawable.ic_lens_red_24dp);
		}
		
		ampel.setOnClickListener(this);
		locInterval.addTextChangedListener(this);
		spinnerLocPriority.setOnItemSelectedListener(this);

        Button stop = ((Button) this.findViewById(R.id.bt_stop_tracking));
		stop.setOnClickListener(this);
		
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
		case LocationRequest.PRIORITY_LOW_POWER:
			ind_loc_spinner = 0;
			break;
		case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
			ind_loc_spinner = 1;
			break;
		case LocationRequest.PRIORITY_HIGH_ACCURACY:
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
		switch (v.getId()) {
		case R.id.bt_stop_tracking:
			// Alle Tracks stoppen
			TrackingUtils.stopAllTrackings(this);
			// ToggleButtons reset
			DbZoneHelper dbZoneHelper = new DbZoneHelper(this);
			List<SimpleGeofence> geofences=  geofenceStore.getGeofences();
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

			break;
			
		case R.id.tracking_service_state:
			List<String> list  = new ArrayList<>();
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
			String[] stringArray = list.toArray(new String[list.size()]);
			ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
			modeList.setAdapter(modeAdapter);

			builder.setView(modeList);
			dialog = builder.create();
			dialog.show();
			
			break;
		default:
			break;
		}
	}

    
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
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_LOCPRIORITY, Integer.valueOf(LocationRequest.PRIORITY_LOW_POWER).toString());
        	}
        	if (prio.equalsIgnoreCase(this.getString(R.string.prio_balanced))){
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_LOCPRIORITY, Integer.valueOf(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).toString());
        	}
        	if (prio.equalsIgnoreCase(this.getString(R.string.prio_high))){
				dbGlobalsHelper.storeGlobals(Constants.DB_KEY_LOCPRIORITY, Integer.valueOf(LocationRequest.PRIORITY_HIGH_ACCURACY).toString());
        	}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		Log.d("Settings", "onNothingSelected");
	}

}