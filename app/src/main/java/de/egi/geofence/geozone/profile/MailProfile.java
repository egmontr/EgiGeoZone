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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.apache.log4j.Logger;

import de.egi.geofence.geozone.InfoReplace;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.Worker;
import de.egi.geofence.geozone.db.DbMailHelper;
import de.egi.geofence.geozone.db.MailEntity;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.Utils;

@SuppressWarnings("deprecation")
public class MailProfile extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private DbMailHelper datasource;
    private String aktion;
    private String ind;
    private boolean _update = false;
    private CheckBox enter = null;
    private CheckBox exit = null;
    private View viewMerk;
    private GoogleApiClient mLocationClient;
    private final Logger log = Logger.getLogger(MailProfile.class);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        setContentView(R.layout.mail_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Utils.changeBackGroundToolbar(this, toolbar);

        FloatingActionButton fab = findViewById(R.id.fab_mail_profile);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Speichern
                saveMail();
            }
        });

        viewMerk = findViewById(R.id.snackbarPosition);

        datasource = new DbMailHelper(this);

        enter = this.findViewById(R.id.chk_enter);
        exit = this.findViewById(R.id.chk_exit);

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
        if (null != b) {
            aktion = b.getString("action");
            ind = b.getString("ind");
        }


        if (aktion.equalsIgnoreCase("new")) {
            // Felder leer lassen
//			boolean _new = true;
            enter.setChecked(true);
            exit.setChecked(true);
        } else if (aktion.equalsIgnoreCase("update")) {
            _update = true;
        }

        if (_update) {
            // Satz aus der DB lesen
            MailEntity me = datasource.getCursorMailById(Integer.parseInt(ind));

            ((TextView) this.findViewById(R.id.value_name)).setText(me.getName());
            ((EditText) this.findViewById(R.id.value_mail_user)).setText(me.getSmtp_user());
            ((EditText) this.findViewById(R.id.value_mail_user_pw)).setText(me.getSmtp_pw());
            ((EditText) this.findViewById(R.id.value_mail_smtp_host)).setText(me.getSmtp_server());
            ((EditText) this.findViewById(R.id.value_mail_smtp_port)).setText(me.getSmtp_port());
            ((EditText) this.findViewById(R.id.value_mail_sender)).setText(me.getFrom());
            ((EditText) this.findViewById(R.id.value_mail_empf)).setText(me.getTo());
            ((EditText) this.findViewById(R.id.value_mail_subject)).setText(me.getSubject());
            ((EditText) this.findViewById(R.id.value_mail_text)).setText(me.getBody());
            ((CheckBox) this.findViewById(R.id.value_mail_ssl)).setChecked(me.isSsl());
            ((CheckBox) this.findViewById(R.id.value_mail_starttls)).setChecked(me.isStarttls());

            enter.setChecked(me.isEnter());
            exit.setChecked(me.isExit());

        }
    }

    private void saveMail() {
        if (checkInputFields()) {
            return;
        }

        MailEntity mailEntity = new MailEntity();

        if (_update) {
            mailEntity.setId(Integer.parseInt(ind));
        }

        mailEntity.setName(((EditText) this.findViewById(R.id.value_name)).getText().toString());
        mailEntity.setSmtp_user(((EditText) this.findViewById(R.id.value_mail_user)).getText().toString());
        mailEntity.setSmtp_pw(((EditText) this.findViewById(R.id.value_mail_user_pw)).getText().toString());
        mailEntity.setSmtp_server(((EditText) this.findViewById(R.id.value_mail_smtp_host)).getText().toString());
        mailEntity.setSmtp_port(((EditText) this.findViewById(R.id.value_mail_smtp_port)).getText().toString());
        mailEntity.setFrom(((EditText) this.findViewById(R.id.value_mail_sender)).getText().toString());
        mailEntity.setTo(((EditText) this.findViewById(R.id.value_mail_empf)).getText().toString());
        mailEntity.setSubject(((EditText) this.findViewById(R.id.value_mail_subject)).getText().toString());
        mailEntity.setBody(((EditText) this.findViewById(R.id.value_mail_text)).getText().toString());

        mailEntity.setSsl(((CheckBox) this.findViewById(R.id.value_mail_ssl)).isChecked());

        mailEntity.setStarttls(((CheckBox) this.findViewById(R.id.value_mail_starttls)).isChecked());

        mailEntity.setEnter(enter.isChecked());

        mailEntity.setExit(exit.isChecked());

        datasource.storeMail(mailEntity);

        setResult(4813);
        finish();
    }

    /**
     * Check all the input values and flag those that are incorrect
     *
     * @return true if all the widget values are correct; otherwise false
     */
    private boolean checkInputFields() {
        // Start with the input validity flag set to true

        if (TextUtils.isEmpty(((EditText) this.findViewById(R.id.value_name)).getText().toString())) {
            this.findViewById(R.id.value_name).setBackgroundColor(Color.RED);
            this.findViewById(R.id.value_name).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
            return true;
        } else if (((EditText) findViewById(R.id.value_name)).getText().toString().contains(",")) {
            findViewById(R.id.value_name).setBackgroundColor(Color.RED);
            this.findViewById(R.id.value_name).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_comma, Snackbar.LENGTH_LONG).show();
            return true;
        } else if (((EditText) findViewById(R.id.value_name)).getText().toString().contains("'")) {
            findViewById(R.id.value_name).setBackgroundColor(Color.RED);
            this.findViewById(R.id.value_name).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_comma, Snackbar.LENGTH_LONG).show();
            return true;
        }

        // Hier Prüfungen
        if (TextUtils.isEmpty(((EditText) this.findViewById(R.id.value_mail_smtp_host)).getText().toString())) {
            this.findViewById(R.id.value_mail_smtp_host).setBackgroundColor(Color.RED);
//            Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG).show();
            this.findViewById(R.id.value_mail_smtp_host).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        }
        if (TextUtils.isEmpty(((EditText) this.findViewById(R.id.value_mail_smtp_port)).getText().toString())) {
            this.findViewById(R.id.value_mail_smtp_port).setBackgroundColor(Color.RED);
//            Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG).show();
            this.findViewById(R.id.value_mail_smtp_port).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        }
        if (TextUtils.isEmpty(((EditText) this.findViewById(R.id.value_mail_sender)).getText().toString())) {
            this.findViewById(R.id.value_mail_sender).setBackgroundColor(Color.RED);
//            Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG).show();
            this.findViewById(R.id.value_mail_sender).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        }
        if (TextUtils.isEmpty(((EditText) this.findViewById(R.id.value_mail_empf)).getText().toString())) {
            this.findViewById(R.id.value_mail_empf).setBackgroundColor(Color.RED);
//            Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG).show();
            this.findViewById(R.id.value_mail_empf).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        }
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

            mLocationClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
            mLocationClient.connect();

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
                        datasource.deleteMail(ind);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), R.string.profile_in_use, Toast.LENGTH_LONG).show();
                    }
                    Intent intent = new Intent();
                    setResult(4813, intent);
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
            String result = intent.getStringExtra("TestResult") != null ? intent.getStringExtra("TestResult") : "";
//			GlobalSingleton.getInstance().setTestResultError(false);
            showAlert(action, result);
        }
    };

    private void showAlert(String action, String result) {
        if (Constants.ACTION_TEST_STATUS_OK.equals(action)) {
            // Teststatus anzeigen
            AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
            ab.setMessage(R.string.test_ok_text).setPositiveButton(R.string.action_ok, testDialogClickListener).setTitle(R.string.test_ok_title).setIcon(R.drawable.ic_lens_green_24dp).show();
        }
        if (Constants.ACTION_TEST_STATUS_NOK.equals(action)) {
            // Teststatus anzeigen
            AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
//		    String res = getString(R.string.test_nok_text);
            ab.setMessage(result).setPositiveButton(R.string.action_ok, testDialogClickListener)
                    .setTitle(R.string.test_nok_title).setIcon(R.drawable.ic_lens_red_24dp).show();
        }

    }

    // Dialog für TestErgebnis
    private final DialogInterface.OnClickListener testDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            Location currentLocation = null;
            try {
                currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
            } catch (SecurityException se) {
                // Display UI and wait for user interaction
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
                alertDialogBuilder.setMessage(getString(R.string.alertPermissions));
                alertDialogBuilder.setTitle(getString(R.string.titleAlertPermissions));

                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }

            if (currentLocation != null) {
                // Start test
                log.debug("onConnected - location: " + (Double.valueOf(currentLocation.getLatitude()).toString()) + "##" + (Double.valueOf(currentLocation.getLongitude()).toString()));
                doTest(currentLocation);
            } else {
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
        } else if (api.isUserResolvableError(code)) {
            log.error("servicesConnected result: could not connect to Google Play services");
            api.showErrorDialogFragment(this, code, Constants.PLAY_SERVICES_RESOLUTION_REQUEST);
        } else {
            log.error("servicesConnected result: could not connect to Google Play services");
            Toast.makeText(this, api.getErrorString(code), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void doTest(Location currentLocation) {

        String user = ((EditText) this.findViewById(R.id.value_mail_user)).getText().toString();
        String userPw = ((EditText) this.findViewById(R.id.value_mail_user_pw)).getText().toString();
        String smtpHost = ((EditText) this.findViewById(R.id.value_mail_smtp_host)).getText().toString();
        String smtpPort = ((EditText) this.findViewById(R.id.value_mail_smtp_port)).getText().toString();
        String sender = ((EditText) this.findViewById(R.id.value_mail_sender)).getText().toString();
        String mailEmpf = ((EditText) this.findViewById(R.id.value_mail_empf)).getText().toString();
        String subject = ((EditText) this.findViewById(R.id.value_mail_subject)).getText().toString();
        String text = ((EditText) this.findViewById(R.id.value_mail_text)).getText().toString();
        boolean ssl = ((CheckBox) this.findViewById(R.id.value_mail_ssl)).isChecked();
        boolean startls = ((CheckBox) this.findViewById(R.id.value_mail_starttls)).isChecked();

        ZoneEntity ze = Utils.makeTestZone();

        String subjectReplace = Utils.replaceAll(this, subject, ze.getName(), ze.getAlias(), 1, ze.getRadius(),
                (Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()),
                (Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString())
                , null, null, (Float.valueOf(currentLocation.getAccuracy()).toString()));

        String textReplace = Utils.replaceAll(this, text, ze.getName(), ze.getAlias(), 1, ze.getRadius(),
                (Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()),
                (Double.valueOf(currentLocation.getLatitude()).toString()), (Double.valueOf(currentLocation.getLongitude()).toString()),
                null, null, (Float.valueOf(currentLocation.getAccuracy()).toString()));

        Worker worker = new Worker(this.getApplicationContext());
        try {
            worker.doSendMail(this.getApplicationContext(), Constants.TEST_ZONE, subjectReplace, textReplace, user, userPw, smtpHost, smtpPort, sender, mailEmpf, ssl, startls, true);
        } catch (Exception ex) {
            Log.e(Constants.APPTAG, "error sending test mail", ex);
            NotificationUtil.showError(this.getApplicationContext(), "TestMail" + ": Error sending test mail", ex.toString());
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
    public void onBackPressed() {
        setResult(4813);
        super.onBackPressed();
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
}