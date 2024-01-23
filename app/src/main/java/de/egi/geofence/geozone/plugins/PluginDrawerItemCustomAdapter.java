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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.egi.geofence.geozone.R;

class PluginDrawerItemCustomAdapter extends ArrayAdapter<PluginDrawerItem> {
    private List<String> packages;
    private List<String> clazz;

    private final Context mContext;
    private final int layoutResourceId;
    private final PluginDrawerItem[] data;

    public PluginDrawerItemCustomAdapter(Context mContext, int layoutResourceId, PluginDrawerItem[] data) {
        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    public void setStartInfo(List<String> packages, List<String> clazz){
        this.packages = packages;
        this.clazz = clazz;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View listItem;

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        listItem = inflater.inflate(layoutResourceId, parent, false);

        ImageView imageViewIcon = listItem.findViewById(R.id.icon_plugin);
        TextView textViewName = listItem.findViewById(R.id.text_plugin);
        imageViewIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(position);
            }
        });
        textViewName.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(position);
            }
        });
        PluginDrawerItem folder = data[position];
        imageViewIcon.setImageDrawable(folder.drawable);
        textViewName.setText(folder.name);
        return listItem;
    }
    private void startActivity(int position) {
        Intent plugin = new Intent();
		plugin.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        plugin.setComponent(new ComponentName(packages.get(position), clazz.get(position)));
        getContext().startActivity(plugin);
    }

}