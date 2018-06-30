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

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.egi.geofence.geozone.GlobalSingleton;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.db.DbRequirementsHelper;
import de.egi.geofence.geozone.db.RequirementsEntity;
import de.egi.geofence.geozone.utils.Utils;

public class RequirementsProfile extends AppCompatActivity implements OnItemSelectedListener, OnCheckedChangeListener, TextWatcher {
	private DbRequirementsHelper datasource;
	private String aktion;
	private String ind;

	private Spinner spinner_enter;
    private Spinner spinner_exit;

    private CheckBox mo = null;
    private CheckBox di = null;
    private CheckBox mi = null;
    private CheckBox don = null;
    private CheckBox fr = null;
    private CheckBox sa = null;
    private CheckBox so = null;

	private RequirementsEntity re;
	private ProgressDialog pd;
	private BluetoothAdapter bluetoothAdapter;
	private ArrayAdapter<String> adapter;
	private List<String> btDevs;
	private String bt_enter;
	private String bt_exit;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.requirements_profile);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Utils.changeBackGroundToolbar(this, toolbar);

		datasource = new DbRequirementsHelper(this);

        Bundle b = getIntent().getExtras();
        if (null != b){
	        aktion = b.getString("action");
	        ind = b.getString("ind");
        }

		EditText prof_name = ((EditText) this.findViewById(R.id.value_name));
		prof_name.addTextChangedListener(this);

		spinner_enter = (Spinner)findViewById(R.id.spinner_enter);
		spinner_exit = (Spinner)findViewById(R.id.spinner_exit);
		spinner_enter.setOnItemSelectedListener(this);
		spinner_exit.setOnItemSelectedListener(this);

        if (aktion.equalsIgnoreCase("new")){
        	// Felder leer lassen
//			boolean _new = true;
        	// Neuer Satz
        	re = new RequirementsEntity();
        	re.setId(0);
        	re.setMon(true);
        	re.setTue(true);
        	re.setWed(true);
        	re.setThu(true);
        	re.setFri(true);
        	re.setSat(true);
        	re.setSun(true);
        }else if (aktion.equalsIgnoreCase("update")){
//			boolean _update = true;
        	// Satz aus der DB lesen
        	re = datasource.getCursorRequirementsById(Integer.valueOf(ind));
        }

        GlobalSingleton.getInstance().setRequirementsEntity(re);

		((TextView) this.findViewById(R.id.value_name)).setText(re.getName());
		bt_enter = re.getEnter_bt();
		bt_exit = re.getExit_bt();

        // BT-Geräte anzeigen
        PackageManager packageManager = getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            Toast.makeText(this, "No bluetooth on device!", Toast.LENGTH_LONG).show();
            return;
        }

		// Wait wheel
		pd = new ProgressDialog(this);
		pd.setCancelable(false);
		pd.setIndeterminate(true);
        pd.setMessage("Please wait . . .");
		pd.setProgressStyle(android.R.style.Widget_ProgressBar_Large_Inverse);
		bluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
		new AsyncTask<Void, Void, Void>() {

			protected void onPreExecute() {
				pd.show();
			}

			@Override
			protected Void doInBackground( final Void ... params ) {
				// something you know that will take a few seconds
				boolean btEnabled = false;
				if (!bluetoothAdapter.isEnabled()){
					bluetoothAdapter.enable();
					btEnabled = true;
					try {
						Thread.sleep(3000);
					} catch (InterruptedException ignored) {
					}
				}
				Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
				btDevs = new ArrayList<>();
				btDevs.add("none");
				for (BluetoothDevice bluetoothDevice : bondedDevices) {
					btDevs.add(bluetoothDevice.getName());
				}

				adapter = new ArrayAdapter<>(RequirementsProfile.this, R.layout.spinner_item, btDevs);
				adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						spinner_enter.setAdapter(adapter);
						spinner_exit.setAdapter(adapter);

						int ind_bt_enter = btDevs.indexOf(bt_enter) < 0 ? 0 : btDevs.indexOf(bt_enter);
						int ind_bt_exit = btDevs.indexOf(bt_exit) < 0 ? 0 : btDevs.indexOf(bt_exit);

						spinner_enter.setSelection(ind_bt_enter, true);
						spinner_exit.setSelection(ind_bt_exit, true);
					}
				});

				if (btEnabled){
					bluetoothAdapter.disable();
				}
				return null;
			}

			@Override
			protected void onPostExecute( final Void result ) {
				// continue what you are doing...
				pd.dismiss();
			}
		}.execute();


		mo = ((CheckBox) this.findViewById(R.id.mo));
		di = ((CheckBox) this.findViewById(R.id.di));
		mi = ((CheckBox) this.findViewById(R.id.mi));
		don = ((CheckBox) this.findViewById(R.id.don));
		fr = ((CheckBox) this.findViewById(R.id.fr));
		sa = ((CheckBox) this.findViewById(R.id.sa));
		so = ((CheckBox) this.findViewById(R.id.so));

		mo.setOnCheckedChangeListener(this);
		di.setOnCheckedChangeListener(this);
		mi.setOnCheckedChangeListener(this);
		don.setOnCheckedChangeListener(this);
		fr.setOnCheckedChangeListener(this);
		sa.setOnCheckedChangeListener(this);
		so.setOnCheckedChangeListener(this);

		((CheckBox) this.findViewById(R.id.mo)).setChecked(re.isMon());
		((CheckBox) this.findViewById(R.id.di)).setChecked(re.isTue());
		((CheckBox) this.findViewById(R.id.mi)).setChecked(re.isWed());
		((CheckBox) this.findViewById(R.id.don)).setChecked(re.isThu());
		((CheckBox) this.findViewById(R.id.fr)).setChecked(re.isFri());
		((CheckBox) this.findViewById(R.id.sa)).setChecked(re.isSat());
		((CheckBox) this.findViewById(R.id.so)).setChecked(re.isSun());

	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.d("ConditionsOpt", "onItemSelected");
		if (arg0.getId() == R.id.spinner_enter){
			Log.d("ConditionsOpt", "Selected item is: " + arg0.getSelectedItem());
	        GlobalSingleton.getInstance().getRequirementsEntity().setEnter_bt(arg0.getSelectedItem().toString());
		}
		if (arg0.getId() == R.id.spinner_exit){
			Log.d("ConditionsOpt", "Selected item is: " + arg0.getSelectedItem());
	        GlobalSingleton.getInstance().getRequirementsEntity().setExit_bt(arg0.getSelectedItem().toString());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		Log.d("ConditionsOpt", "onNothingSelected");
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mo){
        	if (isChecked){
    	        GlobalSingleton.getInstance().getRequirementsEntity().setMon(true);
        	}else{
    	        GlobalSingleton.getInstance().getRequirementsEntity().setMon(false);
        	}
        }
        if (buttonView == di){
        	if (isChecked){
    	        GlobalSingleton.getInstance().getRequirementsEntity().setTue(true);
        	}else{
    	        GlobalSingleton.getInstance().getRequirementsEntity().setTue(false);
        	}
        }
        if (buttonView == mi){
        	if (isChecked){
    	        GlobalSingleton.getInstance().getRequirementsEntity().setWed(true);
        	}else{
    	        GlobalSingleton.getInstance().getRequirementsEntity().setWed(false);
        	}
        }
        if (buttonView == don){
        	if (isChecked){
    	        GlobalSingleton.getInstance().getRequirementsEntity().setThu(true);
        	}else{
    	        GlobalSingleton.getInstance().getRequirementsEntity().setThu(false);
        	}
        }
        if (buttonView == fr){
        	if (isChecked){
    	        GlobalSingleton.getInstance().getRequirementsEntity().setFri(true);
        	}else{
    	        GlobalSingleton.getInstance().getRequirementsEntity().setFri(false);
        	}
        }
        if (buttonView == sa){
        	if (isChecked){
    	        GlobalSingleton.getInstance().getRequirementsEntity().setSat(true);
        	}else{
    	        GlobalSingleton.getInstance().getRequirementsEntity().setSat(false);
        	}
        }
        if (buttonView == so){
        	if (isChecked){
    	        GlobalSingleton.getInstance().getRequirementsEntity().setSun(true);
        	}else{
    	        GlobalSingleton.getInstance().getRequirementsEntity().setSun(false);
        	}
        }
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
        re.setName(((EditText) this.findViewById(R.id.value_name)).getText().toString());
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profil_del, menu);
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
        			// Damit im RequProfiles im onActivityResult der Satz nicht wieder angelegt wird, Name auf NULL setzen
        			re.setName(null);

        			datasource.deleteRequirements(ind);
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

}


















