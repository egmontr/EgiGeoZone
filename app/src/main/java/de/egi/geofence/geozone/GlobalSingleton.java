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

package de.egi.geofence.geozone;

import java.util.ArrayList;
import java.util.List;

import de.egi.geofence.geozone.db.GlobalsEntity;
import de.egi.geofence.geozone.db.MoreEntity;
import de.egi.geofence.geozone.db.RequirementsEntity;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.geofence.GeofenceRemover;

public class GlobalSingleton {
	private static GlobalSingleton _instance;

	private GeofenceRemover geofenceRemover = null;
    private ZoneEntity zoneEntity = null;
    private MoreEntity moreEntity = null;
    private RequirementsEntity requirementsEntity = null;
	private List<String> btDevicesConnected = new ArrayList<>();

	public List<String> getBtDevicesConnected() {
		return btDevicesConnected;
	}

	public String getNotificationTitel() {
		return notificationTitel;
	}

	public void setNotificationTitel(String notificationTitel) {
		this.notificationTitel = notificationTitel;
	}

	public String getNotificationText() {
		return notificationText;
	}

	public void setNotificationText(String notificationText) {
		this.notificationText = notificationText;
	}

	private String notificationTitel;
	private String notificationText;

	private GlobalSingleton() {
	}

	public static GlobalSingleton getInstance() {
		if (_instance == null) {
			_instance = new GlobalSingleton();
		}
		return _instance;
	}

	public GeofenceRemover getGeofenceRemover() {
		return geofenceRemover;
	}

	public void setGeofenceRemover(GeofenceRemover geofenceRemover) {
		this.geofenceRemover = geofenceRemover;
	}

	/**
	 * @return the moreEntity
	 */
	public MoreEntity getMoreEntity() {
		return moreEntity;
	}

	/**
	 * @param moreEntity the moreEntity to set
	 */
	public void setMoreEntity(MoreEntity moreEntity) {
		this.moreEntity = moreEntity;
	}

	/**
	 * @return the requirementsEntity
	 */
	public RequirementsEntity getRequirementsEntity() {
		return requirementsEntity;
	}

	/**
	 * @param requirementsEntity the requirementsEntity to set
	 */
	public void setRequirementsEntity(RequirementsEntity requirementsEntity) {
		this.requirementsEntity = requirementsEntity;
	}

	/**
	 * @return the zoneEntity
	 */
	public ZoneEntity getZoneEntity() {
		return zoneEntity;
	}

	/**
	 * @param zoneEntity the zoneEntity to set
	 */
	public void setZoneEntity(ZoneEntity zoneEntity) {
		this.zoneEntity = zoneEntity;
	}
}










