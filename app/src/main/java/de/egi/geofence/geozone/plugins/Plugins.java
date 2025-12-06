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
package de.egi.geofence.geozone.plugins;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.List;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.Worker;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;
import de.egi.geofence.geozone.utils.Utils;

public class Plugins extends AppCompatActivity {
    private final List<String> packages = new ArrayList<>();
    private final List<String> clazz = new ArrayList<>();
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);

        setContentView(R.layout.plugins);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Utils.changeBackGroundToolbar(this, toolbar);

        ListView listViewPlugins = findViewById(R.id.listView_plugins);

        PackageManager manager = getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_EGIGEOZONE_GETPLUGINS);
        // Query for all activities that match my filter and request that the filter used
        // to match is returned in the ResolveInfo
        packages.clear();
        clazz.clear();
        List<PluginDrawerItem> pluginDrawerItems = new ArrayList<>();
        List<ResolveInfo> infos = manager.queryIntentActivities (intent, PackageManager.GET_RESOLVED_FILTER);
        for (ResolveInfo info : infos) {
            ActivityInfo activityInfo = info.activityInfo;
            IntentFilter filter = info.filter;
            if (filter != null && filter.hasAction(Constants.ACTION_EGIGEOZONE_GETPLUGINS)){
                pluginDrawerItems.add(new PluginDrawerItem(activityInfo.applicationInfo.loadIcon(manager), activityInfo.applicationInfo.loadLabel(manager).toString()));
                packages.add(activityInfo.packageName);
                clazz.add(activityInfo.name);
            }
        }

        PluginDrawerItemCustomAdapter adapter = new PluginDrawerItemCustomAdapter(this, R.layout.plugin_list_item, pluginDrawerItems.toArray(new PluginDrawerItem[0]));
        listViewPlugins.setAdapter(adapter);
        // Übergeben damit der OnClickListener die Werte hat
        adapter.setStartInfo(packages, clazz);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_test, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        int itemId = item.getItemId();
        if (itemId == R.id.menu_test) {
            doTest();
            return true;
            // Pass through any other request
        }
        return super.onOptionsItemSelected(item);
    }

    private void doTest(){
        ZoneEntity ze = Utils.makeTestZone();

        int transition = Geofence.GEOFENCE_TRANSITION_ENTER;

        Worker worker = new Worker(this.getApplicationContext());
        try {

            worker.doBroadcastToPlugins(transition, ze, "46", "10", "90" );

            showAlert(Constants.ACTION_TEST_STATUS_OK, "");
        } catch (Exception ex) {
            Log.e(Constants.APPTAG, "error testing profile", ex);
            NotificationUtil.showError(this.getApplicationContext(), "TestPlugins" + ": Error testing plugins", ex.toString());
            showAlert(Constants.ACTION_TEST_STATUS_NOK, "TestPlugins" + ": Error testing plugins. " + ex);
        }
    }

    private void showAlert(String action, String result){
        if (Constants.ACTION_TEST_STATUS_OK.equals(action)) {
            // Teststatus anzeigen
            AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
            ab.setMessage(R.string.test_send).setPositiveButton(R.string.action_ok, testDialogClickListener).setTitle(R.string.test_send_title).setIcon(R.drawable.ic_lens_green_24dp).show();
        }
//        if (Constants.ACTION_TEST_STATUS_NOK.equals(action)) {
//            // Teststatus anzeigen
//            AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
//            ab.setMessage(result).setPositiveButton(R.string.action_ok, testDialogClickListener)
//                    .setTitle(R.string.test_nok_title).setIcon(R.drawable.ic_lens_red_24dp).show();
//        }
    }

    // Dialog für TestErgebnis
    private final DialogInterface.OnClickListener testDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
    };


}
















