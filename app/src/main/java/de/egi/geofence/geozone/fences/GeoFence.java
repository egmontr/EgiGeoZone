package de.egi.geofence.geozone.fences;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.egi.geofence.geozone.GlobalSingleton;
import de.egi.geofence.geozone.Help;
import de.egi.geofence.geozone.Karte;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.db.DbMailHelper;
import de.egi.geofence.geozone.db.DbMoreHelper;
import de.egi.geofence.geozone.db.DbRequirementsHelper;
import de.egi.geofence.geozone.db.DbServerHelper;
import de.egi.geofence.geozone.db.DbZoneHelper;
import de.egi.geofence.geozone.db.MoreEntity;
import de.egi.geofence.geozone.db.RequirementsEntity;
import de.egi.geofence.geozone.db.ServerEntity;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.geofence.SimpleGeofence;
import de.egi.geofence.geozone.geofence.SimpleGeofenceStore;
import de.egi.geofence.geozone.profile.MailProfile;
import de.egi.geofence.geozone.profile.MoreProfile;
import de.egi.geofence.geozone.profile.RequirementsProfile;
import de.egi.geofence.geozone.profile.ServerProfile;
import de.egi.geofence.geozone.tracker.TrackingLocalSettings;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.Utils;
import it.sephiroth.android.library.tooltip.Tooltip;

/**
 * Created by egmontr on 28.07.2016.
 */
public class GeoFence extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private DbZoneHelper datasource;
    private String action;
    private String zone;
    private boolean _new = true;
    private ZoneEntity ze;
    private final Logger log = Logger.getLogger(GeoFence.class);
    private View viewMerk;
    ImageButton buttonKarte = null;
    ImageButton buttonAddServer = null;
    ImageButton buttonAddMail = null;
    ImageButton buttonAddMore = null;
    ImageButton buttonAddRequ = null;

    List<String> listNone;

    Spinner spinner_server;
    Spinner spinner_mail;
    Spinner spinner_more;
    Spinner spinner_requ;

    List<String> listSrvAll;
    List<String> listMailAll;
    List<String> listMoreAll;
    List<String> listRequAll;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.onActivityCreateSetTheme(this);

        setContentView(R.layout.geofence);

        buttonKarte = findViewById(R.id.karte);
        buttonKarte.setOnClickListener(this);
        buttonAddServer = findViewById(R.id.add_server);
        buttonAddServer.setOnClickListener(this);
        buttonAddMail = findViewById(R.id.add_email);
        buttonAddMail.setOnClickListener(this);
        buttonAddMore = findViewById(R.id.add_more);
        buttonAddMore.setOnClickListener(this);
        buttonAddRequ = findViewById(R.id.add_requ);
        buttonAddRequ.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Utils.changeBackGroundToolbar(this, toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        datasource = new DbZoneHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab_geo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                // Speichern
                viewMerk = view;
                saveZone();
            }
        });

        Button trackingButton = findViewById(R.id.tracking);
        trackingButton.setOnClickListener(this);

        // load profiles
        listNone = new ArrayList<>();
        listNone.add("none");

        // Set servers
        fillSpinnerServer();
        spinner_server.setOnItemSelectedListener(this);

        // Set mails
        filleSpinnerMail();

        // Set More
        fillSpinnerMore();

        // Set Requs
        fillSpinnerRequ();

        ze = new ZoneEntity();

        Bundle b = getIntent().getExtras();
        if (null != b) {
            action = b.getString("action");
            zone = b.getString("zone");
        }

        if (action.equalsIgnoreCase("new")) {
            // Felder leer lassen
            _new = true;
        } else if (action.equalsIgnoreCase("update")) {
            _new = false;
        }

        if (_new) {
            // Neue Zone anlegen
            ((EditText) findViewById(R.id.value_geofence)).setText("");
            ((EditText) findViewById(R.id.value_alias)).setText("");
            ((EditText) findViewById(R.id.value_latitude)).setText("");
            ((EditText) findViewById(R.id.value_longitude)).setText("");
            ((EditText) findViewById(R.id.value_radius)).setText("");
            ze.setId(0);
            // Für Tasker-Einstellungen setzen
            GlobalSingleton.getInstance().setZoneEntity(ze);
        } else {
            // Zone anzeigen --> Felder füllen
            // Zone aus der DB lesen
            ze = datasource.getCursorZoneByName(zone);

            // Instantiate a new geofence storage area
            SimpleGeofenceStore geofenceStore = new SimpleGeofenceStore(this);
            SimpleGeofence geofence = geofenceStore.getGeofence(ze.getName());

            ((EditText) findViewById(R.id.value_geofence)).setText(geofence.getId());
            ((EditText) findViewById(R.id.value_alias)).setText(geofence.getmAlias());
            ((EditText) findViewById(R.id.value_latitude)).setText(geofence.getLatitude());
            ((EditText) findViewById(R.id.value_longitude)).setText(geofence.getLongitude());
            ((EditText) findViewById(R.id.value_radius)).setText(geofence.getRadius());

            // Mit den Ids die Namen der Profile lesen
            if (ze.getId_email() != null) {
                int ind_me = (!listMailAll.contains(ze.getId_email())) ? 0 : listMailAll.indexOf(ze.getId_email());
                spinner_mail.setSelection(ind_me, true);
            }
            if (ze.getId_more_actions() != null) {
                int ind_mo = !listMoreAll.contains(ze.getId_more_actions()) ? 0 : listMoreAll.indexOf(ze.getId_more_actions());
                spinner_more.setSelection(ind_mo, true);
            }
            if (ze.getId_server() != null) {
                int ind_se = !listSrvAll.contains(ze.getId_server()) ? 0 : listSrvAll.indexOf(ze.getId_server());
                spinner_server.setSelection(ind_se, true);
            }
            if (ze.getId_requirements() != null) {
                int ind_re = !listRequAll.contains(ze.getId_requirements()) ? 0 : listRequAll.indexOf(ze.getId_requirements());
                spinner_requ.setSelection(ind_re, true);
            }
            // Für Tasker-Einstellungen setzen
            GlobalSingleton.getInstance().setZoneEntity(ze);
        }
    }

    private void fillSpinnerRequ() {
        DbRequirementsHelper datasourceRequ = new DbRequirementsHelper(this);

        Cursor cursorRequ = datasourceRequ.getCursorAllRequirements();
        spinner_requ = findViewById(R.id.spinner_requirements_profile);
        List<String> listRequ = new ArrayList<>();
        while (cursorRequ.moveToNext()) {
            listRequ.add(cursorRequ.getString(1));
        }
        Collections.sort(listRequ, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        cursorRequ.close();

        listRequAll = new ArrayList<>();
        listRequAll.addAll(listNone);
        listRequAll.addAll(listRequ);

        ArrayAdapter<String> adapterRequ = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listRequAll);
        adapterRequ.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_requ.setAdapter(adapterRequ);
    }

    private void filleSpinnerMail() {
        DbMailHelper datasourceMail = new DbMailHelper(this);

        Cursor cursorMail = datasourceMail.getCursorAllMail();
        spinner_mail = findViewById(R.id.spinner_mail_profile);
        List<String> listMail = new ArrayList<>();
        while (cursorMail.moveToNext()) {
            listMail.add(cursorMail.getString(1));
        }
        Collections.sort(listMail, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        cursorMail.close();

        listMailAll = new ArrayList<>();
        listMailAll.addAll(listNone);
        listMailAll.addAll(listMail);

        ArrayAdapter<String> adapterMail = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listMailAll);
        adapterMail.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mail.setAdapter(adapterMail);
    }


    private void fillSpinnerServer() {
        DbServerHelper datasourceServer = new DbServerHelper(this);

        Cursor cursorSrv = datasourceServer.getCursorAllServer();
        List<String> listSrv = new ArrayList<>();
        while (cursorSrv.moveToNext()) {
            listSrv.add(cursorSrv.getString(1));
        }
        Collections.sort(listSrv, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        cursorSrv.close();

        listSrvAll = new ArrayList<>();
        listSrvAll.addAll(listNone);
        listSrvAll.addAll(listSrv);

        ArrayAdapter<String> adapterServer = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listSrvAll);
        spinner_server = findViewById(R.id.spinner_server_profile);

        adapterServer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_server.setAdapter(adapterServer);
    }

    private void fillSpinnerMore() {
        DbMoreHelper datasourceMore = new DbMoreHelper(this);

        Cursor cursorMore = datasourceMore.getCursorAllMore();
        spinner_more = findViewById(R.id.spinner_more_profile);
        List<String> listMore = new ArrayList<>();
        while (cursorMore.moveToNext()) {
            listMore.add(cursorMore.getString(1));
        }
        Collections.sort(listMore, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        cursorMore.close();

        listMoreAll = new ArrayList<>();
        listMoreAll.addAll(listNone);
        listMoreAll.addAll(listMore);

        ArrayAdapter<String> adapterMore = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listMoreAll);
        adapterMore.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_more.setAdapter(adapterMore);
    }

    private void saveZone() {
        if (checkInputFields()) {
            return;
        }

        EditText mZone = findViewById(R.id.value_geofence);
        EditText mLatitude = findViewById(R.id.value_latitude);
        EditText mLongitude = findViewById(R.id.value_longitude);
        EditText mRadius = findViewById(R.id.value_radius);
        EditText mAlias = findViewById(R.id.value_alias);

        Spinner mSpinner_server = findViewById(R.id.spinner_server_profile);
        Spinner mSpinner_mail = findViewById(R.id.spinner_mail_profile);
        Spinner mSpinner_more = findViewById(R.id.spinner_more_profile);
        Spinner mSpinner_requ = findViewById(R.id.spinner_requirements_profile);

        String zone = mZone.getText().toString();

        ze.setLatitude(mLatitude.getText().toString());
        ze.setLongitude(mLongitude.getText().toString());
        ze.setName(zone);
        ze.setRadius(Integer.valueOf(mRadius.getText().toString()));
        ze.setType(Constants.GEOZONE);
        ze.setAccuracy(GlobalSingleton.getInstance().getZoneEntity().getAccuracy());
        ze.setAlias(mAlias.getText().toString());

        String mailProfile = (String) mSpinner_mail.getSelectedItem();
        String moreProfile = (String) mSpinner_more.getSelectedItem();
        String requProfile = (String) mSpinner_requ.getSelectedItem();
        String serverProfile = (String) mSpinner_server.getSelectedItem();

        ze.setId_email(mailProfile.equals("none") ? null : mailProfile);
        ze.setId_more_actions(moreProfile.equals("none") ? null : moreProfile);
        ze.setId_requirements(requProfile.equals("none") ? null : requProfile);
        ze.setId_server(serverProfile.equals("none") ? null : serverProfile);

        // Set tracker settings
        ze.setTrack_id_email(GlobalSingleton.getInstance().getZoneEntity().getTrack_id_email());
        ze.setTrack_to_file(GlobalSingleton.getInstance().getZoneEntity().isTrack_to_file());
        ze.setTrack_url(GlobalSingleton.getInstance().getZoneEntity().getTrack_url());
        ze.setEnter_tracker(GlobalSingleton.getInstance().getZoneEntity().isEnter_tracker());
        ze.setExit_tracker(GlobalSingleton.getInstance().getZoneEntity().isExit_tracker());
        ze.setLocal_tracking_interval(GlobalSingleton.getInstance().getZoneEntity().getLocal_tracking_interval());

        datasource.storeZone(ze);

        Intent data = new Intent();
        data.putExtra("action", "add");
        data.putExtra("zoneToAdd", zone);
        setResult(4730, data);
        finish();
    }

    /**
     * Check all the input values and flag those that are incorrect
     *
     * @return true if all the widget values are correct; otherwise false
     */
    private boolean checkInputFields() {
        // Start with the input validity flag set to true
        if (TextUtils.isEmpty(((EditText) findViewById(R.id.value_geofence)).getText())) {
            findViewById(R.id.value_geofence).setBackgroundColor(Color.RED);
            findViewById(R.id.value_geofence).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        } else if (((EditText) findViewById(R.id.value_geofence)).getText().toString().contains(",")) {
            findViewById(R.id.value_geofence).setBackgroundColor(Color.RED);
            findViewById(R.id.value_geofence).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_comma, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        } else if (((EditText) findViewById(R.id.value_geofence)).getText().toString().contains("'")) {
            findViewById(R.id.value_geofence).setBackgroundColor(Color.RED);
            findViewById(R.id.value_geofence).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_comma, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        }

        if (((EditText) findViewById(R.id.value_alias)).getText().toString().contains(",")) {
            findViewById(R.id.value_alias).setBackgroundColor(Color.RED);
            findViewById(R.id.value_alias).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_comma, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        } else if (((EditText) findViewById(R.id.value_alias)).getText().toString().contains("'")) {
            findViewById(R.id.value_alias).setBackgroundColor(Color.RED);
            findViewById(R.id.value_alias).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_comma, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        }

        /*
         * Latitude, longitude, and radius values can't be empty. If they are, highlight the input
         * field in red and put a Toast message in the UI. Otherwise set the input field highlight
         * to black, ensuring that a field that was formerly wrong is reset.
         */
        if (TextUtils.isEmpty(((EditText) findViewById(R.id.value_latitude)).getText())) {
            findViewById(R.id.value_latitude).setBackgroundColor(Color.RED);
            findViewById(R.id.value_latitude).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        }

        if (TextUtils.isEmpty(((EditText) findViewById(R.id.value_longitude)).getText())) {
            findViewById(R.id.value_longitude).setBackgroundColor(Color.RED);
            findViewById(R.id.value_longitude).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        }
        if (TextUtils.isEmpty(((EditText) findViewById(R.id.value_radius)).getText())) {
            findViewById(R.id.value_radius).setBackgroundColor(Color.RED);
            findViewById(R.id.value_radius).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_missing, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        }
        try {
            if (Integer.parseInt(((EditText) findViewById(R.id.value_radius)).getText().toString()) < 50) {
                findViewById(R.id.value_radius).setBackgroundColor(Color.RED);
                findViewById(R.id.value_radius).requestFocus();
                Snackbar.make(viewMerk, R.string.geofence_input_error_radius_invalid, Snackbar.LENGTH_LONG).show();
                return true;
            }
        } catch (NumberFormatException ne) {
            findViewById(R.id.value_radius).setBackgroundColor(Color.RED);
            findViewById(R.id.value_radius).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_radius_invalid, Snackbar.LENGTH_LONG).show();
            return true;
        }
        /*
         * If all the input fields have been entered, test to ensure that their values are within
         * the acceptable range. The tests can't be performed until it's confirmed that there are
         * actual values in the fields.
         */

        /*
         * Get values from the latitude, longitude, and radius fields.
         */
        double lat1 = Double.parseDouble(((EditText) findViewById(R.id.value_latitude)).getText().toString());
        double lng1 = Double.parseDouble(((EditText) findViewById(R.id.value_longitude)).getText().toString());
//        float rd1 = Float.valueOf(((EditText) findViewById(R.id.value_radius)).getText().toString());

        /*
         * Test latitude and longitude for minimum and maximum values. Highlight incorrect
         * values and set a Toast in the UI.
         */

        if (lat1 > Constants.MAX_LATITUDE || lat1 < Constants.MIN_LATITUDE) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.geofence_input_error_latitude_invalid);
            }
            findViewById(R.id.value_latitude).setBackgroundColor(Color.RED);
            findViewById(R.id.value_latitude).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_latitude_invalid, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        }

        if ((lng1 > Constants.MAX_LONGITUDE) || (lng1 < Constants.MIN_LONGITUDE)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.geofence_input_error_longitude_invalid);
            }
            findViewById(R.id.value_longitude).setBackgroundColor(Color.RED);
            findViewById(R.id.value_longitude).requestFocus();
            Snackbar.make(viewMerk, R.string.geofence_input_error_longitude_invalid, Snackbar.LENGTH_LONG).show();
            // Set the validity to "invalid" (false)
            return true;
        }

        // If everything passes, the validity flag will still be true, otherwise it will be false.
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_geofence, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        int itemId = item.getItemId();
        if (itemId == R.id.menu_delete_geofence) {
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setMessage(R.string.action_delete).setPositiveButton(R.string.action_yes, dialogClickListener).setNegativeButton(R.string.action_no, dialogClickListener).show();
            return true;
        } else if (itemId == R.id.menu_item_clear_geofence) {//                log.debug("onOptionsItemSelected: menu_item_clear_geofence");
            ((EditText) findViewById(R.id.value_geofence)).setText(Constants.EMPTY_STRING);
            ((EditText) findViewById(R.id.value_latitude)).setText(Constants.EMPTY_STRING);
            ((EditText) findViewById(R.id.value_longitude)).setText(Constants.EMPTY_STRING);
            ((EditText) findViewById(R.id.value_radius)).setText(Constants.EMPTY_STRING);
            ((EditText) findViewById(R.id.value_alias)).setText(Constants.EMPTY_STRING);
            ((Spinner) findViewById(R.id.spinner_server_profile)).setSelection(0, true);
            ((Spinner) findViewById(R.id.spinner_mail_profile)).setSelection(0, true);
            ((Spinner) findViewById(R.id.spinner_more_profile)).setSelection(0, true);
            ((Spinner) findViewById(R.id.spinner_requirements_profile)).setSelection(0, true);
            return true;
        } else if (itemId == R.id.menu_get_location) {
            log.debug("onOptionsItemSelected: menu_get_location");
            // Stores the current instantiation of the location client in this object
            FusedLocationProviderClient mLocationClient = LocationServices.getFusedLocationProviderClient(this);
            try {
                mLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Display the current location in the UI
                        ((EditText) findViewById(R.id.value_latitude)).setText(Double.valueOf(location.getLatitude()).toString());
                        ((EditText) findViewById(R.id.value_longitude)).setText(Double.valueOf(location.getLongitude()).toString());
//                    log.debug("onConnected - location: " + (Double.valueOf(currentLocation.getLatitude()).toString()) + "##" + (Double.valueOf(currentLocation.getLongitude()).toString()));
                    }else{
                        Toast.makeText(this, "Could not determine location. ", Toast.LENGTH_LONG).show();
//                    log.error("Could not determine location.");
                    }
                });
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
            return true;
        } else if (itemId == R.id.menu_accuracy) {
            Intent iAccuracy = new Intent(this, Accuracy.class);
            activityResultLaunch.launch(iAccuracy); // 4713
            return true;
        } else if (itemId == R.id.menu_item_help) {
            Intent i2 = new Intent(this, Help.class);
            startActivity(i2);
            return true;
            // Pass through any other request
        }
        return super.onOptionsItemSelected(item);
    }

    private final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Do your Yes progress
                    Intent data = new Intent();
                    data.putExtra("action","delete");
                    String zoneToDelete = ((EditText) findViewById(R.id.value_geofence)).getText().toString();
                    data.putExtra("zoneToDelete", zoneToDelete);
                    setResult(4730, data);
                    finish();
                case DialogInterface.BUTTON_NEGATIVE:
                    //Do your No progress
                    break;
            }
        }
    };


    /**
     * Map anzeigen
     */
    public void onKarteClicked(View view) {
        log.debug("onKarteClicked");

        Intent iKarte = new Intent(this, Karte.class);
        Bundle b = new Bundle();

        b.putString("de.egi.geofence.geozone.lat", ((EditText) findViewById(R.id.value_latitude)).getText().toString());
        b.putString("de.egi.geofence.geozone.lng", ((EditText) findViewById(R.id.value_longitude)).getText().toString());
        b.putString("de.egi.geofence.geozone.rad", ((EditText) findViewById(R.id.value_radius)).getText().toString());
        b.putString("de.egi.geofence.geozone.zone", ((EditText) findViewById(R.id.value_geofence)).getText().toString());

        iKarte.putExtras(b);
        activityResultLaunch.launch(iKarte); // 4711
    }

    /**
     * Add Server
     */
    public void onAddServerClicked(View view) {
        log.debug("onAddServerClicked");
        Intent i = new Intent(this, ServerProfile.class);
        i.putExtra("action", "new");
        activityResultLaunch.launch(i); // 4811
    }
    /**
     * Add Mail
     */
    public void onAddMailClicked(View view) {
        log.debug("onAddMailClicked");
        Intent i = new Intent(this, MailProfile.class);
        i.putExtra("action", "new");
        activityResultLaunch.launch(i); // 4813
    }

    /**
     * Add More
     */
    public void onAddMoreClicked(View view) {
        log.debug("onAddMoreClicked");
        Intent i = new Intent(this, MoreProfile.class);
        i.putExtra("action", "new");
        activityResultLaunch.launch(i); // 4814
    }

    /**
     * Add Requs
     */
    public void onAddRequClicked(View view) {
        log.debug("onAddRequClicked");
        Intent i = new Intent(this, RequirementsProfile.class);
        i.putExtra("action", "new");
        activityResultLaunch.launch(i); // 4815
    }

    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Add Requ
                    if (result.getResultCode() == 4815) {
                        RequirementsEntity rq = GlobalSingleton.getInstance().getRequirementsEntity();
                        DbRequirementsHelper dbRequHelper = new DbRequirementsHelper(GeoFence.this);
                        if (rq.getName() != null && !rq.getName().equalsIgnoreCase("")) {
                            dbRequHelper.storeRequirements(rq);
                        }
                        fillSpinnerRequ();
                        // Add More
                    } else if(result.getResultCode() == 4814) {
                        MoreEntity me = GlobalSingleton.getInstance().getMoreEntity();
                        DbMoreHelper dbMoreHelper = new DbMoreHelper(GeoFence.this);
                        if (me.getName() != null && !me.getName().equalsIgnoreCase("")) {
                            dbMoreHelper.storeMore(me);
                        }
                        fillSpinnerMore();
                        // Add Mail
                    }else if(result.getResultCode() == 4813) {
                        filleSpinnerMail();
                        // Add Server
                    }else if(result.getResultCode() == 4811) {
                        fillSpinnerServer();
                        // Accuracy
                    }else if(result.getResultCode() == 4713) {
                        View view_acc;
                        String toolTip_acc;
                        if (GlobalSingleton.getInstance().getZoneEntity().getId() == 0) {
                            // Register
                            view_acc = findViewById(R.id.fab_geo);
                            toolTip_acc = GeoFence.this.getString(R.string.toolTipRegisterZone);
                        } else {
                            // Alter
                            view_acc = findViewById(R.id.fab_geo);
                            toolTip_acc = GeoFence.this.getString(R.string.toolTipAlterZone);
                        }
                        Tooltip.make(GeoFence.this,
                                new Tooltip.Builder(101)
                                        .anchor(view_acc, Tooltip.Gravity.BOTTOM)
                                        .closePolicy(new Tooltip.ClosePolicy().insidePolicy(true, false).outsidePolicy(true, false), 5000)
                                        .activateDelay(800)
                                        .showDelay(300)
                                        .text(toolTip_acc)
                                        .maxWidth(500)
                                        .withArrow(true)
                                        .withOverlay(false)
//                                .typeface(mYourCustomFont)
                                        .floatingAnimation(Tooltip.AnimationBuilder.SLOW)
                                        .fitToScreen(true)
                                        .build()
                        ).show();
                        // tracking settings
                    }else if(result.getResultCode() == 4712) {
                            View view;
                            String toolTip;
                            if (GlobalSingleton.getInstance().getZoneEntity().getId() == 0) {
                                // Register
                                view = findViewById(R.id.fab_geo);
                                toolTip = GeoFence.this.getString(R.string.toolTipRegisterZone);
                            } else {
                                // Alter
                                view = findViewById(R.id.fab_geo);
                                toolTip = GeoFence.this.getString(R.string.toolTipAlterZone);
                            }
                            Tooltip.make(GeoFence.this,
                                    new Tooltip.Builder(101)
                                            .anchor(view, Tooltip.Gravity.BOTTOM)
                                            .closePolicy(new Tooltip.ClosePolicy().insidePolicy(true, false).outsidePolicy(true, false), 5000)
                                            .activateDelay(800)
                                            .showDelay(300)
                                            .text(toolTip)
                                            .maxWidth(500)
                                            .withArrow(true)
                                            .withOverlay(false)
//                                .typeface(mYourCustomFont)
                                            .floatingAnimation(Tooltip.AnimationBuilder.SLOW)
                                            .fitToScreen(true)
                                            .build()
                            ).show();
                        // Karte
                    }else if ((result.getResultCode() == 4711) || result.getResultCode() == Activity.RESULT_OK && (result.getData() != null && (result.getData()).getExtras().containsKey("radius"))) {
                        if (result.getData() != null){
                            if (result.getData().getExtras() != null) {
                                double lat = result.getData().getExtras().getDouble("lat", 0);
                                double lng = result.getData().getExtras().getDouble("lng", 0);
                                Double radius = result.getData().getExtras().getDouble("radius", 500);
                                long radi = radius.longValue();
                                // Display the current location in the UI
                                ((EditText) findViewById(R.id.value_latitude)).setText(Double.toString(lat));
                                ((EditText) findViewById(R.id.value_longitude)).setText(Double.toString(lng));
                                ((EditText) findViewById(R.id.value_radius)).setText(Long.toString(radi));
                            }
                        }
                    }
                }
            });

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
            api.showErrorDialogFragment(this, code, Constants.PLAY_SERVICES_RESOLUTION_REQUEST);
        } else {
            log.error("servicesConnected result: could not connect to Google Play services");
            Toast.makeText(this, api.getErrorString(code), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tracking) {//                log.debug("onOptionsItemSelected: button_tracking");
            onLocationTrackerLokalSettingsClicked();
        }else if (v.getId() == R.id.karte) {
            log.info("onOptionsItemSelected: buttonKarte");
            onKarteClicked(buttonKarte);
        }else if (v.getId() == R.id.add_server) {
            log.info("onOptionsItemSelected: buttonAddServer");
            onAddServerClicked(buttonAddServer);
        }else if (v.getId() == R.id.add_email) {
            log.info("onOptionsItemSelected: buttonAddMail");
            onAddMailClicked(buttonAddMail);
        }else if (v.getId() == R.id.add_more) {
            log.info("onOptionsItemSelected: buttonAddMore");
            onAddMoreClicked(buttonAddMore);
        }else if (v.getId() == R.id.add_requ) {
            log.info("onOptionsItemSelected: buttonAddRequ");
            onAddRequClicked(buttonAddRequ);
        }
    }

    /**
     * Weiter lokale Tracking Einstellungen
     */
    private void onLocationTrackerLokalSettingsClicked() {
        Intent iLocationTracker = new Intent(this, TrackingLocalSettings.class);
        Bundle b = new Bundle();
        b.putString("de.egi.geofence.geozone.tracking.zone", ((EditText) findViewById(R.id.value_geofence)).getText().toString());
        iLocationTracker.putExtras(b);
        activityResultLaunch.launch(iLocationTracker); // 4712
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String serverProfile = (String)spinner_server.getSelectedItem();
        // Show Tooltip, if track url is set @@
        View view_track;
        String toolTip_track;
        DbServerHelper dbServerHelper = new DbServerHelper(this);
        ServerEntity serverEntity = dbServerHelper.getCursorServerByName(serverProfile);

        if (serverEntity != null && serverEntity.getUrl_tracking() != null && !serverEntity.getUrl_tracking().equals("")){
            view_track  = findViewById(R.id.tracking);
            toolTip_track = this.getString(R.string.toolTipTrackingSettings);
            Tooltip.make(this,
                    new Tooltip.Builder(101)
                            .anchor(view_track, Tooltip.Gravity.BOTTOM)
                            .closePolicy(new Tooltip.ClosePolicy().insidePolicy(true, false).outsidePolicy(true, false), 10000)
                            .activateDelay(800)
                            .showDelay(300)
                            .text(toolTip_track)
                            .maxWidth(500)
                            .withArrow(true)
                            .withOverlay(false)
                            //                      .typeface(mYourCustomFont)
                            .floatingAnimation(Tooltip.AnimationBuilder.SLOW)
                            .fitToScreen(true)
                            .build()
            ).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}