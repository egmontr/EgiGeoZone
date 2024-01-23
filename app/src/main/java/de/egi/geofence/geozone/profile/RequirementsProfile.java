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
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.egi.geofence.geozone.GlobalSingleton;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.db.DbRequirementsHelper;
import de.egi.geofence.geozone.db.RequirementsEntity;
import de.egi.geofence.geozone.utils.RuntimePermissionsActivity;
import de.egi.geofence.geozone.utils.Utils;

public class RequirementsProfile extends RuntimePermissionsActivity implements OnItemSelectedListener, OnCheckedChangeListener, TextWatcher {
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
	private BluetoothAdapter bluetoothAdapter;
	private ArrayAdapter<String> adapter;
	private List<String> btDevs;
	private String bt_enter;
	private String bt_exit;

	ActivityResultLauncher<Intent> activityResultLaunch;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.requirements_profile);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Utils.changeBackGroundToolbar(this, toolbar);

		datasource = new DbRequirementsHelper(this);

		Bundle b = getIntent().getExtras();
		if (null != b) {
			aktion = b.getString("action");
			ind = b.getString("ind");
		}

		EditText prof_name = this.findViewById(R.id.value_name);
		prof_name.addTextChangedListener(this);

		spinner_enter = findViewById(R.id.spinner_enter);
		spinner_exit = findViewById(R.id.spinner_exit);
		spinner_enter.setOnItemSelectedListener(this);
		spinner_exit.setOnItemSelectedListener(this);

		if (aktion.equalsIgnoreCase("new")) {
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
		} else if (aktion.equalsIgnoreCase("update")) {
//			boolean _update = true;
			// Satz aus der DB lesen
			re = datasource.getCursorRequirementsById(Integer.parseInt(ind));
		}

		GlobalSingleton.getInstance().setRequirementsEntity(re);

		((TextView) this.findViewById(R.id.value_name)).setText(re.getName());
		bt_enter = re.getEnter_bt();
		bt_exit = re.getExit_bt();

		// BT-Geräte anzeigen
		PackageManager packageManager = getPackageManager();
		if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
			Toast.makeText(this, "No bluetooth on device!", Toast.LENGTH_LONG).show();
			return;
		}

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// Wait wheel
		ProgressDialog pd = new ProgressDialog(this);
		pd.setCancelable(false);
		pd.setIndeterminate(true);
		pd.setMessage("Please wait . . .");
		pd.setProgressStyle(android.R.style.Widget_ProgressBar_Large_Inverse);

		activityResultLaunch = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				result -> {
					if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
						// TODO: Consider calling
						//    ActivityCompat#requestPermissions
						// here to request the missing permissions, and then overriding
						//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
						//                                          int[] grantResults)
						// to handle the case where the user grants the permission. See the documentation
						// for ActivityCompat#requestPermissions for more details.
						return;
					}


					do_bt(bluetoothAdapter);

//					Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
//					btDevs = new ArrayList<>();
//					btDevs.add("none");
//					for (BluetoothDevice bluetoothDevice : bondedDevices) {
//						btDevs.add(bluetoothDevice.getName());
//					}
//
//					adapter = new ArrayAdapter<>(RequirementsProfile.this, R.layout.spinner_item, btDevs);
//					adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//					runOnUiThread(() -> {
//						spinner_enter.setAdapter(adapter);
//						spinner_exit.setAdapter(adapter);
//
//						int ind_bt_enter = !btDevs.contains(bt_enter) ? 0 : btDevs.indexOf(bt_enter);
//						int ind_bt_exit = !btDevs.contains(bt_exit) ? 0 : btDevs.indexOf(bt_exit);
//
//						spinner_enter.setSelection(ind_bt_enter, true);
//						spinner_exit.setSelection(ind_bt_exit, true);
//					});
				});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
				requestAppPermission(Manifest.permission.BLUETOOTH_CONNECT, R.string.alert2020BT, 2020);
			} else {
				onPermissionsGranted(2020);
			}
		} else {
			// Für frühere Versionen < Build.VERSION_CODES.S
			new AsyncTask<Void, Void, Void>() {

				protected void onPreExecute() {
					pd.show();
				}

				@SuppressLint("MissingPermission")
				@Override
				protected Void doInBackground(final Void... params) {
					// something you know that will take a few seconds
					boolean btEnabled = false;
					if (!bluetoothAdapter.isEnabled()) {
//						if (ActivityCompat.checkSelfPermission(RequirementsProfile.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//							// TODO: Consider calling
//							//    ActivityCompat#requestPermissions
//							// here to request the missing permissions, and then overriding
//							//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//							//                                          int[] grantResults)
//							// to handle the case where the user grants the permission. See the documentation
//							// for ActivityCompat#requestPermissions for more details.
//							return null;
//						}
						bluetoothAdapter.enable();
						btEnabled = true;
						try {
							Thread.sleep(3000);
						} catch (InterruptedException ignored) {
						}
					}

					do_bt(bluetoothAdapter);

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

		}


		mo = this.findViewById(R.id.mo);
		di = this.findViewById(R.id.di);
		mi = this.findViewById(R.id.mi);
		don = this.findViewById(R.id.don);
		fr = this.findViewById(R.id.fr);
		sa = this.findViewById(R.id.sa);
		so = this.findViewById(R.id.so);

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
		if (arg0.getId() == R.id.spinner_enter) {
			Log.d("ConditionsOpt", "Selected item is: " + arg0.getSelectedItem());
			GlobalSingleton.getInstance().getRequirementsEntity().setEnter_bt(arg0.getSelectedItem().toString());
		}
		if (arg0.getId() == R.id.spinner_exit) {
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
		if (buttonView == mo) {
			GlobalSingleton.getInstance().getRequirementsEntity().setMon(isChecked);
		}
		if (buttonView == di) {
			GlobalSingleton.getInstance().getRequirementsEntity().setTue(isChecked);
		}
		if (buttonView == mi) {
			GlobalSingleton.getInstance().getRequirementsEntity().setWed(isChecked);
		}
		if (buttonView == don) {
			GlobalSingleton.getInstance().getRequirementsEntity().setThu(isChecked);
		}
		if (buttonView == fr) {
			GlobalSingleton.getInstance().getRequirementsEntity().setFri(isChecked);
		}
		if (buttonView == sa) {
			GlobalSingleton.getInstance().getRequirementsEntity().setSat(isChecked);
		}
		if (buttonView == so) {
			GlobalSingleton.getInstance().getRequirementsEntity().setSun(isChecked);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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
	public void onBackPressed() {
		super.onBackPressed();
//		super.onBackPressed(); if not it gives back result_canceled
		setResult(4711);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		if (item.getItemId() == R.id.menu_delete_profile_log) {
			AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
			ab.setMessage(R.string.action_delete).setPositiveButton(R.string.action_yes, dialogClickListener).setNegativeButton(R.string.action_no, dialogClickListener).show();
			return true;
			// Pass through any other request
		}
		return super.onOptionsItemSelected(item);
	}


	private final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					//Do your Yes progress
					try {
						// Damit im RequProfiles im onActivityResult der Satz nicht wieder angelegt wird, Name auf NULL setzen
						re.setName(null);

						datasource.deleteRequirements(ind);
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(), R.string.profile_in_use, Toast.LENGTH_LONG).show();
					}

					Intent intent = new Intent();
					setResult(4815, intent);
					finish();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					//Do your No progress
					break;
			}
		}
	};

	@Override
	public void onPermissionsGranted(int requestCode) {
		if (!bluetoothAdapter.isEnabled()) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			activityResultLaunch.launch(intent);
		}
	}

	@SuppressLint("MissingPermission")
	private void do_bt(BluetoothAdapter bluetoothAdapter) {
//		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//			// TODO: Consider calling
//			//    ActivityCompat#requestPermissions
//			// here to request the missing permissions, and then overriding
//			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//			//                                          int[] grantResults)
//			// to handle the case where the user grants the permission. See the documentation
//			// for ActivityCompat#requestPermissions for more details.
//			return;
//		}
		Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
		btDevs = new ArrayList<>();
		btDevs.add("none");
		for (BluetoothDevice bluetoothDevice : bondedDevices) {
			btDevs.add(bluetoothDevice.getName());
		}

		adapter = new ArrayAdapter<>(RequirementsProfile.this, R.layout.spinner_item, btDevs);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		runOnUiThread(() -> {
			spinner_enter.setAdapter(adapter);
			spinner_exit.setAdapter(adapter);

			int ind_bt_enter = !btDevs.contains(bt_enter) ? 0 : btDevs.indexOf(bt_enter);
			int ind_bt_exit = !btDevs.contains(bt_exit) ? 0 : btDevs.indexOf(bt_exit);

			spinner_enter.setSelection(ind_bt_enter, true);
			spinner_exit.setSelection(ind_bt_exit, true);
		});

	}
}


















