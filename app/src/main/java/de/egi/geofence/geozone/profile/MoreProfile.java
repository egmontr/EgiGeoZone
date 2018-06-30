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

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;

import de.egi.geofence.geozone.GlobalSingleton;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.Worker;
import de.egi.geofence.geozone.db.DbMoreHelper;
import de.egi.geofence.geozone.db.MoreEntity;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.Utils;

@SuppressWarnings("deprecation")
public class MoreProfile extends AppCompatActivity implements OnCheckedChangeListener, TextWatcher{
	private boolean check = false;
	private DbMoreHelper datasource;
	private String aktion;
	private String ind;
	private boolean _update = false;

	private EditText moreTaskB = null;
	private EditText moreTaskV = null;

	private MoreEntity me;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.more_profile);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Utils.changeBackGroundToolbar(this, toolbar);

		datasource = new DbMoreHelper(this);

		moreTaskB = ((EditText) this.findViewById(R.id.value_tasker_enter_id));
		moreTaskV = ((EditText) this.findViewById(R.id.value_tasker_exit_id));
		EditText prof_name = ((EditText) this.findViewById(R.id.value_name));

		moreTaskB.addTextChangedListener(this);
		moreTaskV.addTextChangedListener(this);
		prof_name.addTextChangedListener(this);
		
		((RadioGroup)findViewById(R.id.radioGroupWlanB)).setOnCheckedChangeListener(this);
		((RadioGroup)findViewById(R.id.radioGroupWlanV)).setOnCheckedChangeListener(this);
		((RadioGroup)findViewById(R.id.radioGroupBluetoothB)).setOnCheckedChangeListener(this);
		((RadioGroup)findViewById(R.id.radioGroupBluetoothV)).setOnCheckedChangeListener(this);
		((RadioGroup)findViewById(R.id.radioGroupSoundB)).setOnCheckedChangeListener(this);
		((RadioGroup)findViewById(R.id.radioGroupSoundV)).setOnCheckedChangeListener(this);
		((RadioGroup)findViewById(R.id.radioGroupMmB)).setOnCheckedChangeListener(this);
		((RadioGroup)findViewById(R.id.radioGroupMmV)).setOnCheckedChangeListener(this);

		Bundle b = getIntent().getExtras();
        if (null != b){
	        aktion = b.getString("action");
	        ind = b.getString("ind");
	        ((RadioGroup) this.findViewById(R.id.radioGroupWlanB)).check(R.id.radioButtonWlanBNone);
	        ((RadioGroup) this.findViewById(R.id.radioGroupWlanV)).check(R.id.radioButtonWlanVNone);
	        ((RadioGroup) this.findViewById(R.id.radioGroupBluetoothB)).check(R.id.radioButtonBluetoothBNone);
	        ((RadioGroup) this.findViewById(R.id.radioGroupBluetoothV)).check(R.id.radioButtonBluetoothVNone);
	        ((RadioGroup) this.findViewById(R.id.radioGroupSoundB)).check(R.id.radioButtonSoundBNone);
	        ((RadioGroup) this.findViewById(R.id.radioGroupSoundV)).check(R.id.radioButtonSoundVNone);
			((RadioGroup) this.findViewById(R.id.radioGroupMmB)).check(R.id.radioButtonMmBNone);
			((RadioGroup) this.findViewById(R.id.radioGroupMmV)).check(R.id.radioButtonMmVNone);
        }
        if (aktion.equalsIgnoreCase("new")){
        	// Felder leer lassen
//			boolean _new = true;
        	// Neuer Satz
        	me = new MoreEntity();
        	me.setId(0);
        }else if (aktion.equalsIgnoreCase("update")){
        	_update = true;
        	// Satz aus der DB lesen
        	me = datasource.getCursorMoreById(Integer.valueOf(ind));
        }
        
        GlobalSingleton.getInstance().setMoreEntity(me);
        	
        if (_update){	
    		
			((TextView) this.findViewById(R.id.value_name)).setText(me.getName());
    		((EditText) this.findViewById(R.id.value_tasker_enter_id)).setText(me.getEnter_task());
    		((EditText) this.findViewById(R.id.value_tasker_exit_id)).setText(me.getExit_task());

    		switch (me.getEnter_wifi()) {
			case 0: ((RadioGroup) this.findViewById(R.id.radioGroupWlanB)).check(R.id.radioButtonWlanBOff);
				break;
			case 1: ((RadioGroup) this.findViewById(R.id.radioGroupWlanB)).check(R.id.radioButtonWlanBOn);
				break;
			case 2: ((RadioGroup) this.findViewById(R.id.radioGroupWlanB)).check(R.id.radioButtonWlanBNone);
				break;
			default:
				break;
			}
    		
    		switch (me.getExit_wifi()) {
			case 0: ((RadioGroup) this.findViewById(R.id.radioGroupWlanV)).check(R.id.radioButtonWlanVOff);
				break;
			case 1: ((RadioGroup) this.findViewById(R.id.radioGroupWlanV)).check(R.id.radioButtonWlanVOn);
				break;
			case 2: ((RadioGroup) this.findViewById(R.id.radioGroupWlanV)).check(R.id.radioButtonWlanVNone);
				break;
			default:
				break;
			}
    		
    		switch (me.getEnter_bt()) {
			case 0: ((RadioGroup) this.findViewById(R.id.radioGroupBluetoothB)).check(R.id.radioButtonBluetoothBOff);
				break;
			case 1: ((RadioGroup) this.findViewById(R.id.radioGroupBluetoothB)).check(R.id.radioButtonBluetoothBOn);
				break;
			case 2: ((RadioGroup) this.findViewById(R.id.radioGroupBluetoothB)).check(R.id.radioButtonBluetoothBNone);
				break;
			default:
				break;
			}

    		switch (me.getExit_bt()) {
			case 0: ((RadioGroup) this.findViewById(R.id.radioGroupBluetoothV)).check(R.id.radioButtonBluetoothVOff);
				break;
			case 1: ((RadioGroup) this.findViewById(R.id.radioGroupBluetoothV)).check(R.id.radioButtonBluetoothVOn);
				break;
			case 2: ((RadioGroup) this.findViewById(R.id.radioGroupBluetoothV)).check(R.id.radioButtonBluetoothVNone);
				break;
			default:
				break;
			}

    		switch (me.getEnter_sound()) {
			case 0: ((RadioGroup) this.findViewById(R.id.radioGroupSoundB)).check(R.id.radioButtonSoundBOff);
				break;
			case 1: ((RadioGroup) this.findViewById(R.id.radioGroupSoundB)).check(R.id.radioButtonSoundBOn);
				break;
			case 2: ((RadioGroup) this.findViewById(R.id.radioGroupSoundB)).check(R.id.radioButtonSoundBNone);
				break;
			case 3: ((RadioGroup) this.findViewById(R.id.radioGroupSoundB)).check(R.id.radioButtonSoundBVib);
				break;
			default:
				break;
			}

    		switch (me.getExit_sound()) {
			case 0: ((RadioGroup) this.findViewById(R.id.radioGroupSoundV)).check(R.id.radioButtonSoundVOff);
				break;
			case 1: ((RadioGroup) this.findViewById(R.id.radioGroupSoundV)).check(R.id.radioButtonSoundVOn);
				break;
			case 2: ((RadioGroup) this.findViewById(R.id.radioGroupSoundV)).check(R.id.radioButtonSoundVNone);
				break;
			case 3: ((RadioGroup) this.findViewById(R.id.radioGroupSoundV)).check(R.id.radioButtonSoundVVib);
				break;
			default:
				break;
			}
			// Multimedia sound
			switch (me.getEnter_soundMM()) {
				case 0: ((RadioGroup) this.findViewById(R.id.radioGroupMmB)).check(R.id.radioButtonMmBOff);
					break;
				case 1: ((RadioGroup) this.findViewById(R.id.radioGroupMmB)).check(R.id.radioButtonMmBOn);
					break;
				case 2: ((RadioGroup) this.findViewById(R.id.radioGroupMmB)).check(R.id.radioButtonMmBNone);
					break;
				default:
					break;
			}

			switch (me.getExit_soundMM()) {
				case 0: ((RadioGroup) this.findViewById(R.id.radioGroupMmV)).check(R.id.radioButtonMmVOff);
					break;
				case 1: ((RadioGroup) this.findViewById(R.id.radioGroupMmV)).check(R.id.radioButtonMmVOn);
					break;
				case 2: ((RadioGroup) this.findViewById(R.id.radioGroupMmV)).check(R.id.radioButtonMmVNone);
					break;
				default:
					break;
			}
		}

        check = true;
        
	}
	
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
    	
    	if (!check) return;
    	
        if (radioGroup.getId() == R.id.radioGroupWlanB){            
            switch (checkedId) {
            case R.id.radioButtonWlanBOn:
    	        me.setEnter_wifi(1);
                break;
            case R.id.radioButtonWlanBOff:
        	    AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
        	    ab.setMessage(R.string.wlan_on_text).setPositiveButton(R.string.action_ok, dialogClickListener).setTitle("WLAN").setIcon(R.drawable.ic_network_wifi_black_24dp).show();

    	        me.setEnter_wifi(0);
                break;
            case R.id.radioButtonWlanBNone:
    	        me.setEnter_wifi(2);
                break;
            }
        }
        if (radioGroup.getId() == R.id.radioGroupWlanV){            
            switch (checkedId) {
            case R.id.radioButtonWlanVOn:
    	        me.setExit_wifi(1);
                break;
            case R.id.radioButtonWlanVOff:
    	        me.setExit_wifi(0);
                break;
            case R.id.radioButtonWlanVNone:
    	        me.setExit_wifi(2);
                break;
            }
        }

        if (radioGroup.getId() == R.id.radioGroupBluetoothB){            
            switch (checkedId) {
            case R.id.radioButtonBluetoothBOn:
    	        me.setEnter_bt(1);
                break;
            case R.id.radioButtonBluetoothBOff:
    	        me.setEnter_bt(0);
                break;
            case R.id.radioButtonBluetoothBNone:
    	        me.setEnter_bt(2);
                break;
            }
        }
        if (radioGroup.getId() == R.id.radioGroupBluetoothV){            
            switch (checkedId) {
            case R.id.radioButtonBluetoothVOn:
    	        me.setExit_bt(1);
                break;
            case R.id.radioButtonBluetoothVOff:
    	        me.setExit_bt(0);
                break;
            case R.id.radioButtonBluetoothVNone:
    	        me.setExit_bt(2);
                break;
            }
        }

        if (radioGroup.getId() == R.id.radioGroupSoundB){            
			// Android 7: check if notification policy is granted
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				if (!notificationManager.isNotificationPolicyAccessGranted()) {
					// Display UI and ask the user to put app to the battery optimization whitelist.
					AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
					alertDialogBuilder.setMessage(getString(R.string.doNotDisturbPermissionsMessage));
					alertDialogBuilder.setTitle(getString(R.string.doNotDisturbPermissionsTitle));

					alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
								startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0);
							}
						}
					});
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
//					return;
				}
			}
            switch (checkedId) {
            case R.id.radioButtonSoundBOn:
    	        me.setEnter_sound(1);
                break;
            case R.id.radioButtonSoundBOff:
    	        me.setEnter_sound(0);
                break;
			case R.id.radioButtonSoundBNone:
				me.setEnter_sound(2);
				break;
			case R.id.radioButtonSoundBVib:
				me.setEnter_sound(3);
				break;
            }
        }

        if (radioGroup.getId() == R.id.radioGroupSoundV){
            // Android 7: check if notification policy is granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (!notificationManager.isNotificationPolicyAccessGranted()) {
                    // Display UI and ask the user to put app to the battery optimization whitelist.
                    AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
                    alertDialogBuilder.setMessage(getString(R.string.doNotDisturbPermissionsMessage));
                    alertDialogBuilder.setTitle(getString(R.string.doNotDisturbPermissionsTitle));

                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0);
                            }
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
//                    return;
                }
            }

            switch (checkedId) {
            case R.id.radioButtonSoundVOn:
    	        me.setExit_sound(1);
                break;
            case R.id.radioButtonSoundVOff:
    	        me.setExit_sound(0);
                break;
			case R.id.radioButtonSoundVNone:
				me.setExit_sound(2);
				break;
			case R.id.radioButtonSoundVVib:
				me.setExit_sound(3);
				break;
            }
        }
		// Multimedia sound
		if (radioGroup.getId() == R.id.radioGroupMmB){
			// Android 7: check if notification policy is granted
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				if (!notificationManager.isNotificationPolicyAccessGranted()) {
					// Display UI and ask the user to put app to the battery optimization whitelist.
					AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
					alertDialogBuilder.setMessage(getString(R.string.doNotDisturbPermissionsMessage));
					alertDialogBuilder.setTitle(getString(R.string.doNotDisturbPermissionsTitle));

					alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
								startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0);
							}
						}
					});
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
//					return;
				}
			}
			switch (checkedId) {
				case R.id.radioButtonMmBOn:
					me.setEnter_soundMM(1);
					break;
				case R.id.radioButtonMmBOff:
					me.setEnter_soundMM(0);
					break;
				case R.id.radioButtonMmBNone:
					me.setEnter_soundMM(2);
					break;
			}
		}

		if (radioGroup.getId() == R.id.radioGroupMmV){
			// Android 7: check if notification policy is granted
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				if (!notificationManager.isNotificationPolicyAccessGranted()) {
					// Display UI and ask the user to put app to the battery optimization whitelist.
					AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
					alertDialogBuilder.setMessage(getString(R.string.doNotDisturbPermissionsMessage));
					alertDialogBuilder.setTitle(getString(R.string.doNotDisturbPermissionsTitle));

					alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
								startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0);
							}
						}
					});
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
//                    return;
				}
			}

			switch (checkedId) {
				case R.id.radioButtonMmVOn:
					me.setExit_soundMM(1);
					break;
				case R.id.radioButtonMmVOff:
					me.setExit_soundMM(0);
					break;
				case R.id.radioButtonMmVNone:
					me.setExit_soundMM(2);
					break;
			}
		}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
	public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (moreTaskB.getText().hashCode() == s.hashCode()){
			me.setEnter_task(((EditText) this.findViewById(R.id.value_tasker_enter_id)).getText().toString());
		}else if (moreTaskV.getText().hashCode() == s.hashCode()){
			me.setExit_task(((EditText) this.findViewById(R.id.value_tasker_exit_id)).getText().toString());
		}else{
			me.setName(((EditText) this.findViewById(R.id.value_name)).getText().toString());
		}
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
		    ab.setMessage(R.string.action_delete).setPositiveButton(R.string.action_yes, mdialogClickListener).setNegativeButton(R.string.action_no, mdialogClickListener).show();
            return true;
			case R.id.menu_test:
				doTest();
				return true;
        // Pass through any other request
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	
    private final DialogInterface.OnClickListener mdialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                //Do your Yes progress
            	try{
        			// Damit im MoreProfiles im onActivityResult der Satz nicht wieder angelegt wird, Name auf NULL setzen
        			me.setName(null);
        			
        			datasource.deleteMore(ind);
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

	private void doTest(){
		ZoneEntity ze = Utils.makeTestZone();
		ze.setMoreEntity(me);

		int transition = Geofence.GEOFENCE_TRANSITION_ENTER;

		Worker worker = new Worker(this.getApplicationContext());
		try {
			worker.doWifi(this.getApplicationContext(), ze, transition);
			worker.doBluetooth(this.getApplicationContext(), ze, transition);
			worker.doSound(this.getApplicationContext(), ze, transition);
			worker.doSoundMM(this.getApplicationContext(), ze, transition);
			worker.doCallTasker(this.getApplicationContext(), ze, transition);

			showAlert(Constants.ACTION_TEST_STATUS_OK, "");
		} catch (Exception ex) {
			Log.e(Constants.APPTAG, "error testing profile", ex);
			NotificationUtil.showError(this.getApplicationContext(), "TestMoreProfile" + ": Error testing profile", ex.toString());
			showAlert(Constants.ACTION_TEST_STATUS_NOK, "TestMoreProfile" + ": Error testing profile. " + ex.toString());
		}
	}

	private void showAlert(String action, String result){
		if (Constants.ACTION_TEST_STATUS_OK.equals(action)) {
			// Teststatus anzeigen
			AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
			ab.setMessage(R.string.test_ok_text_more).setPositiveButton(R.string.action_ok, testDialogClickListener).setTitle(R.string.test_ok_title).setIcon(R.drawable.ic_lens_green_24dp).show();
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
}