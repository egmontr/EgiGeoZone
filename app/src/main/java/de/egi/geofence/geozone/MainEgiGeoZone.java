package de.egi.geofence.geozone;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.db.DbZoneHelper;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.fences.GeoFence;
import de.egi.geofence.geozone.geofence.GeofenceRemover;
import de.egi.geofence.geozone.geofence.GeofenceRequester;
import de.egi.geofence.geozone.geofence.PathsenseGeofence;
import de.egi.geofence.geozone.geofence.SimpleGeofence;
import de.egi.geofence.geozone.geofence.SimpleGeofenceStore;
import de.egi.geofence.geozone.profile.Profiles;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NavDrawerItem;
import de.egi.geofence.geozone.utils.NavDrawerListAdapter;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.RuntimePermissionsActivity;
import de.egi.geofence.geozone.utils.Utils;
import de.mindpipe.android.logging.log4j.LogConfigurator;


// http://www.myandroidsolutions.com/2016/07/13/android-navigation-view-tabs/
// https://www.materialui.co/icons

// http://www.flaticon.com/search/13?word=map

// <div>Icons made by <a href="http://www.freepik.com" title="Freepik">Freepik</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
// <div>Icons made by <a href="http://www.flaticon.com/authors/vignesh-oviyan" title="Vignesh Oviyan">Vignesh Oviyan</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
// <div>Icons made by <a href="http://www.flaticon.com/authors/freepik" title="Freepik">Freepik</a> from <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
// Icon made by Freepik from www.flaticon.com
public class MainEgiGeoZone extends RuntimePermissionsActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private Location locationMerk = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    // Dangerous permissions and permission groups.
    // http://developer.android.com/guide/topics/security/permissions.html
//    public static final int REQUEST_LOCATION = 1;
//    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;
//    public static final int REQUEST_PHONE_STATE = 3;
//    public static final int REQUEST_BLUETOOTH = 4; // Location
//    public static final int REQUEST_SMS = 5;
//    public static final int REQUEST_GET_ACCOUNTS = 6;

//    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//    Manifest.permission.READ_PHONE_STATE,
//    Manifest.permission.ACCESS_FINE_LOCATION,
//    Manifest.permission.SEND_SMS,
//    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//    Manifest.permission.GET_ACCOUNTS

    public static final String SEED_MASTER = "Ok.KOmM_V04_60#_HugeNdubEl";

    // Add geofences handler
    private GeofenceRequester mGeofenceRequester;
    private PathsenseGeofence mPathsenseGeofence;
    // Remove geofences handler
    private GeofenceRemover mGeofenceRemover;
    // Store the list of geofences to remove
    private List<String> mGeofenceIdsToRemove;

    // Store the current request
    private static Constants.REQUEST_TYPE mRequestType;
    // Store the current type of removal
    public static Constants.REMOVE_TYPE mRemoveType;

    private ListView list;
    // Persistent storage for geofences
    private SimpleGeofenceStore geofenceStore;
    // Store a list of geofences to add
    private List<Geofence> mCurrentGeofences;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private DbGlobalsHelper dbGlobalsHelper;

    final static LogConfigurator logConfigurator = new LogConfigurator();
    private Logger log;

    private Toolbar toolbar;

    private DbZoneHelper dbZoneHelper;

    //The BroadcastReceiver that listens for bluetooth broadcasts and status of zones
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
                }
                GlobalSingleton.getInstance().getBtDevicesConnected().add(device.getName());
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                //Device is now connected
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
                }
                GlobalSingleton.getInstance().getBtDevicesConnected().remove(device.getName());
            }else if (Constants.ACTION_STATUS_CHANGED.equals(action)) {
                // Status einer Zone hat sich geändert
                // Zonen im Drawer neu laden
                fillListGeofences();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.onActivityCreateSetTheme(this);

        setContentView(R.layout.activity_main_nav);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Utils.changeBackGroundToolbar(this, toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        if (notAllNeededPermissionsGranted()){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestAppPermission(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.alert2040Read, 2040);
                }
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestAppPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.alert2060Write, 2060);
                }
            }

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestAppPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.alert2010Location, 2010);
            }
        }else{
            init();
        }
    }

//    @SuppressLint("BatteryLife")
    protected void init() {
        dbGlobalsHelper = new DbGlobalsHelper(this);
        dbZoneHelper = new DbZoneHelper(this);
        String level = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LOG_LEVEL);
        if (level == null || level.equalsIgnoreCase("")){
            level = Level.ERROR.toString();
        }
        log = Logger.getLogger(MainEgiGeoZone.class);
        if (logConfigurator.getFileName().equalsIgnoreCase("android-log4j.log")){

//            logConfigurator.setFileName(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "egigeozone" + File.separator + "egigeozone.log");

            logConfigurator.setFileName(this.getFilesDir() + File.separator + "egigeozone" + File.separator + "egigeozone.log");

            logConfigurator.setUseFileAppender(true);
            logConfigurator.setRootLevel(Level.toLevel(level));
            // Set log level of a specific logger
            logConfigurator.setLevel("de.egi.geofence.geozone", Level.toLevel(level));
            try {
                logConfigurator.configure();
                log.info("Logger set!");
                Log.i("", "Logger set!");
            } catch (Exception e) {
                // Nichts tun. Manchmal kann auf den Speicher nicht zugegriffen werden.
                Log.e("", e.getMessage());
                Log.e("", String.valueOf(Process.myUid()));
            }
        }

        log.debug("onCreate");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestAppPermission(Manifest.permission.POST_NOTIFICATIONS, R.string.alert2050Notification, 2050);
            }
        }

        // Bildschirm ist grau??!!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestAppPermission(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.alert2040Read, 2040);
            }
        }

        // Akku-Optimierung ausschalten, da sonst kein Netzwerkbetrieb möglich wäre
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        Intent intent = new Intent();
        try {
            //noinspection StatementWithEmptyBody
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                // Nichts tun
            } else {
                intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }catch(Exception e){
            // Ignore
        }

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestAppPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION, R.string.alert2030BgLocation, 2030);
            }
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestAppPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.alert2010Location, 2010);
            }

            // Geofences
            Intent ig = new Intent(MainEgiGeoZone.this, GeoFence.class);
            ig.putExtra("action", "new");
            activityResultLaunch.launch(ig); // 4730
        });

        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        // Permanent notifcation, if requested
        boolean stickyNotification =  Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_STICKY_NOTIFICATION));
        if (stickyNotification) {
            NotificationUtil.sendPermanentNotification(getApplicationContext(), R.drawable.locating_geo, getString(R.string.text_running_notification), 7676);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Instantiate a new geofence storage area
        geofenceStore = new SimpleGeofenceStore(this);
        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<>();
        navDrawerItems = new ArrayList<>();

        // Instantiate a Geofence requester
        if (mGeofenceRequester == null){
            mGeofenceRequester = new GeofenceRequester(this);
        }
        if (mPathsenseGeofence == null){
            mPathsenseGeofence = new PathsenseGeofence(this);
        }
        // Instantiate a Geofence remover
        mGeofenceRemover = new GeofenceRemover(this);

        GlobalSingleton.getInstance().setGeofenceRemover(mGeofenceRemover);

        list = findViewById (R.id.list);
        list.setOnItemClickListener(this);

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        IntentFilter statusFilter = new IntentFilter(Constants.ACTION_STATUS_CHANGED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.registerReceiver(mReceiver, filter1, RECEIVER_EXPORTED);
            this.registerReceiver(mReceiver, filter2, RECEIVER_EXPORTED);
            this.registerReceiver(mReceiver, filter3, RECEIVER_EXPORTED);
            this.registerReceiver(mReceiver, statusFilter, RECEIVER_EXPORTED);
        }else {
            this.registerReceiver(mReceiver, filter1);
            this.registerReceiver(mReceiver, filter2);
            this.registerReceiver(mReceiver, filter3);
            this.registerReceiver(mReceiver, statusFilter);
        }

        refreshFences();
        if (!mCurrentGeofences.isEmpty()){
            fillListGeofences();
        }else{
            drawer.openDrawer(GravityCompat.START);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestAppPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.alert2010Location, 2010);
                return true;
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestAppPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.alert2060Write, 2060);
                    return true;
                }
            }
            log.debug("onOptionsItemSelected: menu_settings");
            Intent i5 = new Intent(this, Settings.class);
            activityResultLaunch.launch(i5); // 5004
            return true;
        } else if (itemId == R.id.action_help) {
            Intent i2 = new Intent(this, Help.class);
            startActivity(i2);
            return true;
        } else if (itemId == R.id.action_profiles) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestAppPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.alert2010Location, 2010);
                return true;
            }
            log.debug("onOptionsItemSelected: menu_profiles");
            Intent i3 = new Intent(this, Profiles.class);
            activityResultLaunch.launch(i3); // 5005
            return true;
        } else if (itemId == R.id.action_tech_info) {
            Intent i4 = new Intent(this, TechInfo.class);
            startActivity(i4);
            return true;
        } else if (itemId == R.id.action_info) {
            Intent i5a = new Intent(this, Info.class);
            activityResultLaunch.launch(i5a); // 6000
            return true;
        } else if (itemId == R.id.action_privacy) {
            Intent i6 = new Intent(this, Privacy.class);
            activityResultLaunch.launch(i6); // 6001
            return true;
        } else if (itemId == R.id.action_map_all) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestAppPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.alert2010Location, 2010);
              return true;
            }
            log.debug("onOptionsItemSelected: menu_item_map_all");
            Intent i7 = new Intent(this, KarteAll.class);
            activityResultLaunch.launch(i7); // 6002
            return true;
        } else if (itemId == R.id.action_refresh) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestAppPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.alert2010Location, 2010);
                return true;
            }
            log.debug("onOptionsItemSelected: menu_item_refresh");
            mSwipeRefreshLayout.setRefreshing(true);
            fillListGeofences();
            mSwipeRefreshLayout.setRefreshing(false);
            return true;
            // Pass through any other request
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        item.setChecked(true);
        if (id == R.id.nav_geofence) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestAppPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.alert2010Location, 2010);
                return true;
            }
            ((TextView) findViewById(R.id.fences)).setText(R.string.geoZones);
            fillListGeofences();
        } else if (id == R.id.nav_profiles) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestAppPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.alert2010Location, 2010);
                return true;
            }
            log.debug("onNavigationItemSelected: menu_item_profile");
            Intent i2 = new Intent(this, Profiles.class);
            activityResultLaunch.launch(i2); // 5005

        } else if (id == R.id.nav_settings) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestAppPermission(Manifest.permission.ACCESS_FINE_LOCATION, R.string.alert2010Location, 2010);
              return true;
            }
            log.debug("onNavigationItemSelected: menu_item_settings");
            Intent i = new Intent(this, Settings.class);
            activityResultLaunch.launch(i); // 5004

        } else if (id == R.id.nav_info) {
            Intent i3 = new Intent(this, Info.class);
            activityResultLaunch.launch(i3); // 6000

        } else if (id == R.id.nav_help) {
            Intent i3 = new Intent(this, Help.class);
            startActivity(i3);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
    }

    private void fillListGeofences(){
        if (!super.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return;

        locationMerk = null;
        FusedLocationProviderClient mLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            final Location[] currentLocation = {null};
            try{
                mLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                currentLocation[0] = location;
                                if (log != null) log.debug("onConnected - location: " + (Double.valueOf(currentLocation[0].getLatitude()).toString()) + "##" + (Double.valueOf(currentLocation[0].getLongitude()).toString()));
                                locationMerk = currentLocation[0];
                                ((TextView) findViewById(R.id.fences)).setText(R.string.geoZones);
                                refreshFences();
                                setGeofenceList2Drawer();
                            }else{
                                Toast.makeText(this, "Could not determine location. ", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                ((TextView) findViewById(R.id.fences)).setText(R.string.geoZones);
                                refreshFences();
                                setGeofenceList2Drawer();
                                log.error("Could not determine location.");
                            }
                        });
            }catch(SecurityException se){
                // Display UI and wait for user interaction
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
                alertDialogBuilder.setMessage(getString(R.string.alertPermissions));
                alertDialogBuilder.setTitle(getString(R.string.titleAlertPermissions));

                alertDialogBuilder.setPositiveButton("OK", (arg0, arg1) -> {
                });
                androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }


    private void setGeofenceList2Drawer() {
        navDrawerItems.clear();
        // Gespeicherte GeoZonen auflisten
        for (int i = 0; i < mCurrentGeofences.size(); i++) {
            Geofence geofence = mCurrentGeofences.get(i);
            String dist;
            if (locationMerk != null){
                // Calculate distance to fence center
                try {
                    ZoneEntity zoneEntity = dbZoneHelper.getCursorZoneByName(geofence.getRequestId());
                    Location locationZone = new Location("locationZone");
                    locationZone.setLatitude(Double.parseDouble(zoneEntity.getLatitude()));
                    locationZone.setLongitude(Double.parseDouble(zoneEntity.getLongitude()));

                    float distanceMeters = locationMerk.distanceTo(locationZone);
                    if (distanceMeters < 50) {
                        dist = getString(R.string.distanceLess);
                    } else if (distanceMeters < 1000) {
                        NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
                        formatter.setMaximumFractionDigits(2);
                        formatter.setMinimumFractionDigits(2);
                        formatter.setRoundingMode(RoundingMode.HALF_UP);
                        String formatedFloat = formatter.format(distanceMeters);
                        dist = getString(R.string.distance) + " " + formatedFloat + "m";
                    } else {
                        NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
                        formatter.setMaximumFractionDigits(2);
                        formatter.setMinimumFractionDigits(2);
                        formatter.setRoundingMode(RoundingMode.HALF_UP);
                        String formatedFloat = formatter.format(distanceMeters / 1000);
                        dist = getString(R.string.distance) + " " + formatedFloat + "km";
                    }
                }catch(Exception e){
                    dist = getString(R.string.distanceNa);
                }
            }else{
                dist = getString(R.string.distanceNa);
            }

            if (geofenceStore.getGeofence(geofence.getRequestId()).isStatus()) {
                navDrawerItems.add(new NavDrawerItem(geofence.getRequestId(), R.drawable.ic_green_circle_24dp, dist));
            } else {
                navDrawerItems.add(new NavDrawerItem(geofence.getRequestId(), R.drawable.ic_red_circle_24dp, dist));
            }
        }

        // Sorting
        Collections.sort(navDrawerItems, new Comparator<NavDrawerItem>() {
            public int compare(NavDrawerItem item2, NavDrawerItem item1)
            {
                return  item2.getZone().compareToIgnoreCase(item1.getZone());
            }
        });

        // setting the nav drawer list adapter
        NavDrawerListAdapter adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        list.setAdapter(adapter);
    }


    private List<SimpleGeofence> refreshFences() {
        if (mCurrentGeofences != null){
            mCurrentGeofences.clear();
        }else{
            // Instantiate the current List of geofences
            mCurrentGeofences = new ArrayList<>();
        }

        List<SimpleGeofence> geofences = geofenceStore.getGeofences();
        for (SimpleGeofence simpleGeofence : geofences)
        {
            mCurrentGeofences.add(simpleGeofence.toGeofence());
        }
        return geofences;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        NavDrawerListAdapter navDrawerListAdapter = (NavDrawerListAdapter)adapterView.getAdapter();
        NavDrawerItem navDrawerItem = (NavDrawerItem)navDrawerListAdapter.getItem(i);
        String zone = navDrawerItem.getZone();

            // Geofences
            Intent is = new Intent(this, GeoFence.class);
            is.putExtra("action", "update");
            is.putExtra("zone", zone);
            activityResultLaunch.launch(is); // 4730
    }

    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Geofence hinzufügen und starte die Überwachung
                    if (result.getResultCode() == 4730) {
                        String action = null;
                        if (result.getData() != null) {
                            action = result.getData().getStringExtra("action");
                        }
                        if (action != null && action.equalsIgnoreCase("delete")) {
                            String zoneToDelete = result.getData().getStringExtra("zoneToDelete");
                            if (zoneToDelete == null || zoneToDelete.equalsIgnoreCase("")) {
                                return;
                            }
                            deleteNow(zoneToDelete);
                            // Display Liste mit Zonen
                            refreshFences();
                            setGeofenceList2Drawer();
                        } else {
                            String zoneToAdd = result.getData().getStringExtra("zoneToAdd");
                            if (zoneToAdd == null || zoneToAdd.equalsIgnoreCase("")) {
                                return;
                            }

                            // Display Liste mit Zonen
                            List<SimpleGeofence> geofences = refreshFences();
                            setGeofenceList2Drawer();
                            // Start the request. Fail if there's already a request in progress
                            try {
                                // Try to add geofences
                                mRequestType = Constants.REQUEST_TYPE.ADD;

                                // Old style, without trying to repair
                                if (mCurrentGeofences.size() > 0) {
                                    if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_FALSE_POSITIVES))) {
                                        if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
                                            mGeofenceRequester.addGeofences(mCurrentGeofences);
                                        } else {
                                            for (SimpleGeofence simpleGeofence : geofences) {
                                                mPathsenseGeofence.addGeofence(simpleGeofence);
                                            }
                                        }
                                    } else {
                                        ZoneEntity ze = dbZoneHelper.getCursorZoneByName(zoneToAdd);
                                        if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
                                            Geofence geof = new Geofence.Builder().setRequestId(zoneToAdd)
                                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                                    .setCircularRegion(Double.parseDouble(ze.getLatitude()), Double.parseDouble(ze.getLongitude()), ze.getRadius())
                                                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                                    .build();

                                            List<Geofence> currentGeofence = new ArrayList<>();
                                            currentGeofence.add(geof);

                                            mGeofenceRequester.addGeofences(currentGeofence);
                                        } else {
                                            SimpleGeofence simpleGeofence = new SimpleGeofence(zoneToAdd, ze.getLatitude(), ze.getLongitude(),
                                                    Integer.toString(ze.getRadius()), null, Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER, true, null);
                                            mPathsenseGeofence.addGeofence(simpleGeofence);
                                        }
                                    }
                                }
                            } catch (UnsupportedOperationException e) {
                                // Notify user that previous request hasn't finished.
                                Toast.makeText(MainEgiGeoZone.this, R.string.add_geofences_already_requested_error, Toast.LENGTH_LONG).show();
                                log.error("Error registering Geofence", e);
                                showError("Error registering Geofence", e.toString());
                            }
                        }
                        // Settings
                    }else if(result.getResultCode() == 5004) {
                        // Nur wenn Import war, dann durchlaufen
                        boolean imp = result.getData() != null && result.getData().getBooleanExtra("import", false);
                        if (imp) {
                            // Drawer neu setzen
                            List<SimpleGeofence> geofences = refreshFences();
                            mRequestType = Constants.REQUEST_TYPE.ADD;
                            // Start the request. Fail if there's already a request in progress
                            try {
                                // Try to add geofences
                                if (mCurrentGeofences.size() > 0) {
                                    if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
                                        if (!servicesConnected()) {
                                            return;
                                        }
                                        mGeofenceRequester.addGeofences(mCurrentGeofences);
                                    } else {
                                        for (SimpleGeofence simpleGeofence : geofences) {
                                            mPathsenseGeofence.addGeofence(simpleGeofence);
                                        }
                                    }
                                }
                            } catch (UnsupportedOperationException e) {
                                // Notify user that previous request hasn't finished.
                                Toast.makeText(MainEgiGeoZone.this, R.string.add_geofences_already_requested_error, Toast.LENGTH_LONG).show();
                                log.error("Import: Error registering Geofence", e);
                                showError("Import: Error registering Geofence", e.toString());
                            }
                            //
                            // Settings wieder aufrufen, da Aktion Import war
                            log.debug("onOptionsItemSelected: menu_settings");
                            Intent i5 = new Intent(MainEgiGeoZone.this, Settings.class);
                            Bundle b = new Bundle();
                            b.putBoolean("import", true);
                            i5.putExtras(b);
                            activityResultLaunch.launch(i5); // 5004
                        }
                    }
                    refreshFences();
                    setGeofenceList2Drawer();
                }
            });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // If the request code matches the code sent in onConnectionFailed
        if (requestCode == Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST) {// If Google Play services resolved the problem
            if (resultCode == Activity.RESULT_OK) {// If the request was to add geofences
                if (Constants.REQUEST_TYPE.ADD == mRequestType) {

                    // Restart the process of adding the current geofences
                    if (mCurrentGeofences.size() > 0) {
                        mGeofenceRequester.addGeofences(mCurrentGeofences);
                    }
                    // If the request was to remove geofences
                } else if (Constants.REQUEST_TYPE.REMOVE == mRequestType) {

                    // Toggle the removal flag and send a new removal request
                    mGeofenceRemover.setInProgressFlag(false);

                    // If the removal was by Intent
                    if (Constants.REMOVE_TYPE.INTENT == mRemoveType) {

                        // Restart the removal of all geofences for the PendingIntent
                        mGeofenceRemover.removeGeofencesByIntent(
                                mGeofenceRequester.getRequestPendingIntent());

                        // If the removal was by a List of geofence IDs
                    } else {

                        // Restart the removal of the geofence list
                        mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
                    }
                }

                // If any other result was returned by Google Play services
            } else {// Report that Google Play services was unable to resolve the problem.
                Log.d(Constants.APPTAG, getString(R.string.no_resolution));
                log.info("onActivityResult: " + getString(R.string.no_resolution));
            }
            // If any other request code was received
        }// Report that this Activity received an unknown requestCode
        Log.d(Constants.APPTAG, getString(R.string.unknown_activity_request_code, requestCode));
    }
    /**
     * Fehlerdialog anzeigen
     */
    private void showError(String title, String error){
        String TAG = "MainEgiGeoZone";
        Log.d(TAG, error);
        NotificationUtil.showError(getApplicationContext(), title, error);
    }

    /**
     * Called when the user clicks the "Remove geofence" button #### Eine Zone löschen ####
     */
    private void deleteNow(String zone) {
        log.info("deleteNow: Remove geofence " + zone);
        // Create a List of 1 Geofence with the ID= name and store it in the global list
        mGeofenceIdsToRemove = Collections.singletonList(zone);
        /*
         * Record the removal as remove by list. If a connection error occurs,
         * the app can automatically restart the removal if Google Play services
         * can fix the error
         */
        mRemoveType = Constants.REMOVE_TYPE.LIST;
        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */

        // Try to remove the geofence
        try {
            if (!Utils.isBoolean(dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API))) {
                if (!servicesConnected()) {
                    return;
                }
                mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
            }else {
                mPathsenseGeofence.removeGeofence(zone);
            }
            // Catch errors with the provided geofence IDs
        } catch (IllegalArgumentException e) {
            log.error(zone + ": Error removing Geofence", e);
            showError(zone + ": Error removing Geofence", e.toString());
            return;
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.remove_geofences_already_requested_error, Toast.LENGTH_LONG).show();
            log.error(zone + ": Error removing Geofence", e);
            showError(zone + ": Error removing Geofence", e.toString());
            return;
        }
        // Remove zone
        dbZoneHelper.deleteZone(zone);
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {
        if (log != null) log.debug("servicesConnected");
        // Check that Google Play services is available
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int code = api.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == code) {
            // In debug mode, log the status
            Log.d(Constants.APPTAG, getString(R.string.play_services_available));
            if (log != null) log.info("servicesConnected result from Google Play Services: " + getString(R.string.play_services_available));
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else if (api.isUserResolvableError(code)){
            if (log != null) log.error("servicesConnected result: could not connect to Google Play services");
            api.showErrorDialogFragment(this, code, Constants.PLAY_SERVICES_RESOLUTION_REQUEST);
        } else {
            if (log != null) log.error("servicesConnected result: could not connect to Google Play services");
            Toast.makeText(this, api.getErrorString(code), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    @Override
    public void onRefresh() {
        log.debug("onRefresh called from SwipeRefreshLayout");
        fillListGeofences();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        init();
    }
}
