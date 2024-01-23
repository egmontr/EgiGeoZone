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

import android.app.AlarmManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.egi.geofence.geozone.GlobalSingleton;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.db.DbMailHelper;
import de.egi.geofence.geozone.db.DbServerHelper;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.utils.Utils;

public class TrackingLocalSettings extends AppCompatActivity implements TextWatcher, OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    private CheckBox trackToFile = null;
    private SwitchCompat enter = null;
    private SwitchCompat exit = null;
    private ZoneEntity ze;
    private Spinner spinner_mail;
    private Spinner spinner_server;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.onActivityCreateSetTheme(this);

        setContentView(R.layout.local_tracking_settings);

        ze = GlobalSingleton.getInstance().getZoneEntity();

        trackToFile = this.findViewById(R.id.value_trackToFile);
        trackToFile.setOnCheckedChangeListener(this);
        ((CheckBox) this.findViewById(R.id.value_trackToFile)).setChecked(ze.isTrack_to_file());

        ((TextView) this.findViewById(R.id.location_tracking_settings)).setText(ze.getName());
        EditText interval = this.findViewById(R.id.valuelocalInterval);
        ((EditText) this.findViewById(R.id.valuelocalInterval)).setText(String.valueOf(ze.getLocal_tracking_interval() == null ? "5" : ze.getLocal_tracking_interval()));
        interval.addTextChangedListener(this);
        interval.setSelection(interval.getText().length());

        enter = this.findViewById(R.id.enterTracking);
        exit = this.findViewById(R.id.exitTracking);
        enter.setOnCheckedChangeListener(this);
        exit.setOnCheckedChangeListener(this);
        ((SwitchCompat) this.findViewById(R.id.enterTracking)).setChecked(ze.isEnter_tracker());
        ((SwitchCompat) this.findViewById(R.id.exitTracking)).setChecked(ze.isExit_tracker());

        // Mail
        DbMailHelper datasourceMail = new DbMailHelper(this);
        Cursor cursorMail = datasourceMail.getCursorAllMail();
        List<String> listMail = new ArrayList<>();
        listMail.add("none");
        while (cursorMail.moveToNext()) {
            listMail.add(cursorMail.getString(1));
        }
        cursorMail.close();
        ArrayAdapter<String> adapterMail = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listMail);
        spinner_mail = findViewById(R.id.spinner_tracking_mail_profile);

        adapterMail.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mail.setAdapter(adapterMail);

        spinner_mail.setOnItemSelectedListener(this);

        // Gespeicherten Eintrag setzen
        if(ze != null && ze.getTrack_id_email() != null){
            int ind_se = !listMail.contains(ze.getTrack_id_email()) ? 0 : listMail.indexOf(ze.getTrack_id_email());
            spinner_mail.setSelection(ind_se, true);
        }

        // Server
        DbServerHelper datasourceServer = new DbServerHelper(this);
        Cursor serverMail = datasourceServer.getCursorAllServer();
        List<String> listServer = new ArrayList<>();
        listServer.add("none");
        while (serverMail.moveToNext()) {
            listServer.add(serverMail.getString(1));
        }
        serverMail.close();
        ArrayAdapter<String> adapterServer = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listServer);
        spinner_server = findViewById(R.id.spinner_tracking_server_profile);

        adapterServer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_server.setAdapter(adapterServer);

        spinner_server.setOnItemSelectedListener(this);

        // Gespeicherten Eintrag setzen
        if(ze != null && ze.getTrack_url() != null){
            int ind_se = !listServer.contains(ze.getTrack_url()) ? 0 : listServer.indexOf(ze.getTrack_url());
            spinner_server.setSelection(ind_se, true);
        }

        Intent intent = new Intent();
        setResult(4712, intent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    protected void onResume() {
        ((SwitchCompat) this.findViewById(R.id.enterTracking)).setChecked(ze.isEnter_tracker());
        ((SwitchCompat) this.findViewById(R.id.exitTracking)).setChecked(ze.isExit_tracker());
        super.onResume();
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (((EditText) this.findViewById(R.id.valuelocalInterval)).getText().toString().equals(""))
            return;
        int interval = Integer.parseInt(((EditText) this.findViewById(R.id.valuelocalInterval)).getText().toString());
        ze.setLocal_tracking_interval(interval);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = ContextCompat.getSystemService(this, AlarmManager.class);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                this.startActivity(intent);
                return;
            }
        }


        if (buttonView == trackToFile) {
            ze.setTrack_to_file(isChecked);
        }

        // Start/Stop Tracking at enter zone
        if (buttonView == enter) {
            if (isChecked) {
                ze.setEnter_tracker(true);
                // check if inside the zone and start tracking
                if (ze.isStatus()) {
                    TrackingUtils.startTracking(this, ze.getName(),
                            (ze.getLocal_tracking_interval() == null || ze.getLocal_tracking_interval() == 0 ? 5 : ze.getLocal_tracking_interval()),
                            ze.isTrack_to_file(), (ze.getTrackServerEntity() != null), (ze.getTrackMailEntity() != null));
                }
            } else {
                ze.setEnter_tracker(false);
                // Stop tracking
                if (TrackingUtils.exists(this, ze.getName()) > 0)
                    TrackingUtils.stopTracking(this, ze.getName());
            }
        }
        // Start/Stop Tracking at exit zone
        if (buttonView == exit) {
            if (isChecked) {
                ze.setExit_tracker(true);
                // check if outside the zone and start tracking
                if (!ze.isStatus()) {
                    TrackingUtils.startTracking(this, ze.getName(),
                            (ze.getLocal_tracking_interval() == null || ze.getLocal_tracking_interval() == 0 ? 5 : ze.getLocal_tracking_interval()),
                            ze.isTrack_to_file(), (ze.getTrackServerEntity() != null), (ze.getTrackMailEntity() != null));
                }
            } else {
                ze.setExit_tracker(false);
                // Stop tracking
                if (TrackingUtils.exists(this, ze.getName()) > 0)
                    TrackingUtils.stopTracking(this, ze.getName());
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String mailProfile = (String)spinner_mail.getSelectedItem();
        ze.setTrack_id_email(mailProfile.equals("none") ? null : mailProfile);

        String serverProfile = (String)spinner_server.getSelectedItem();
        ze.setTrack_url(serverProfile.equals("none") ? null : serverProfile);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
