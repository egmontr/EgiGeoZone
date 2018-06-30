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

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.Utils;

public class Plugins extends AppCompatActivity {
    private final List<String> packages = new ArrayList<>();
    private final List<String> clazz = new ArrayList<>();
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);

        setContentView(R.layout.plugins);

        ListView listViewPlugins = (ListView) findViewById(R.id.listView_plugins);

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

        PluginDrawerItemCustomAdapter adapter = new PluginDrawerItemCustomAdapter(this, R.layout.plugin_list_item, pluginDrawerItems.toArray(new PluginDrawerItem[pluginDrawerItems.size()]));
        listViewPlugins.setAdapter(adapter);
        // Übergeben damit der OnClickListener die Werte hat
        adapter.setStartInfo(packages, clazz);
    }
}
















