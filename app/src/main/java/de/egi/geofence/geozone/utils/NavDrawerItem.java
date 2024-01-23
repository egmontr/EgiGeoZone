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

package de.egi.geofence.geozone.utils;

public class NavDrawerItem {
    private final String zone;

    public String getDistance() {
        return distance;
    }

    private final String distance;
    private final int icon;

    public NavDrawerItem(String zone, int icon, String distance){
        this.zone = zone;
        this.icon = icon;
        this.distance = distance;
    }

    public String getZone(){
        return this.zone;
    }

    public int getIcon(){
        return this.icon;
    }
}