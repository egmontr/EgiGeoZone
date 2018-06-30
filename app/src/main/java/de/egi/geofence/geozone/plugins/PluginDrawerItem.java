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

import android.graphics.drawable.Drawable;

class PluginDrawerItem {

    public final Drawable drawable;
    public final String name;

    // Constructor.
    public PluginDrawerItem(Drawable drawable, String name) {

        this.drawable = drawable;
        this.name = name;
    }
}