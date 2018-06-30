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

package de.egi.geofence.geozone.db;

public class ZoneEntity {

	private Integer id;
	private String name;
	private String latitude;
	private String longitude;
	private Integer radius;
	private String id_server;
	private String id_sms;
	private String id_email;
	private String id_more_actions;
	private String id_requirements;

	private Integer local_tracking_interval;
	private boolean track_to_file;
	private String track_id_email;
	private String track_url;
	private boolean enter_tracker;
	private boolean exit_tracker;

	private MailEntity mailEntity;

	private ServerEntity trackServerEntity;
	private MailEntity trackMailEntity;
	private MoreEntity moreEntity;
	private RequirementsEntity requirementsEntity;
	private ServerEntity serverEntity;
	private SmsEntity smsEntity;

	private boolean status;
	private Integer accuracy;

	private String type;
	private String beacon;

	public MailEntity getTrackMailEntity() {
		return trackMailEntity;
	}

	public void setTrackMailEntity(MailEntity trackMailEntity) {
		this.trackMailEntity = trackMailEntity;
	}

	public ServerEntity getTrackServerEntity() {
		return trackServerEntity;
	}

	public void setTrackServerEntity(ServerEntity trackServerEntity) {
		this.trackServerEntity = trackServerEntity;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	private String alias;

	public String getBeacon() {
		return beacon;
	}

	public void setBeacon(String beacon) {
		this.beacon = beacon;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Integer accuracy) {
		this.accuracy = accuracy;
	}

	public ZoneEntity() {
	}
	public Integer getLocal_tracking_interval() {
		return local_tracking_interval;
	}

	public void setLocal_tracking_interval(Integer local_tracking_interval) {
		this.local_tracking_interval = local_tracking_interval;
	}

	public boolean isTrack_to_file() {
		return track_to_file;
	}

	public void setTrack_to_file(boolean track_to_file) {
		this.track_to_file = track_to_file;
	}

	public String getTrack_id_email() {
		return track_id_email;
	}

	public void setTrack_id_email(String track_id_email) {
		this.track_id_email = track_id_email;
	}

	public String getTrack_url() {
		return track_url;
	}

	public void setTrack_url(String track_url) {
		this.track_url = track_url;
	}

	public boolean isEnter_tracker() {
		return enter_tracker;
	}

	public void setEnter_tracker(boolean enter_tracker) {
		this.enter_tracker = enter_tracker;
	}

	public boolean isExit_tracker() {
		return exit_tracker;
	}

	public void setExit_tracker(boolean exit_tracker) {
		this.exit_tracker = exit_tracker;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the latitude
	 */
	public String getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public String getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the radius
	 */
	public Integer getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(Integer radius) {
		this.radius = radius;
	}


	/**
	 * @return the mailEntity
	 */
	public MailEntity getMailEntity() {
		return mailEntity;
	}

	/**
	 * @param mailEntity the mailEntity to set
	 */
	public void setMailEntity(MailEntity mailEntity) {
		this.mailEntity = mailEntity;
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
	 * @return the serverEntity
	 */
	public ServerEntity getServerEntity() {
		return serverEntity;
	}

	/**
	 * @param serverEntity the serverEntity to set
	 */
	public void setServerEntity(ServerEntity serverEntity) {
		this.serverEntity = serverEntity;
	}

	/**
	 * @return the smsEntity
	 */
	public SmsEntity getSmsEntity() {
		return smsEntity;
	}

	/**
	 * @param smsEntity the smsEntity to set
	 */
	public void setSmsEntity(SmsEntity smsEntity) {
		this.smsEntity = smsEntity;
	}

	/**
	 * @return the id_server
	 */
	public String getId_server() {
		return id_server;
	}

	/**
	 * @param id_server the id_server to set
	 */
	public void setId_server(String id_server) {
		this.id_server = id_server;
	}

	/**
	 * @return the id_sms
	 */
	public String getId_sms() {
		return id_sms;
	}

	/**
	 * @param id_sms the id_sms to set
	 */
	public void setId_sms(String id_sms) {
		this.id_sms = id_sms;
	}

	/**
	 * @return the id_email
	 */
	public String getId_email() {
		return id_email;
	}

	/**
	 * @param id_email the id_email to set
	 */
	public void setId_email(String id_email) {
		this.id_email = id_email;
	}

	/**
	 * @return the id_more_actions
	 */
	public String getId_more_actions() {
		return id_more_actions;
	}

	/**
	 * @param id_more_actions the id_more_actions to set
	 */
	public void setId_more_actions(String id_more_actions) {
		this.id_more_actions = id_more_actions;
	}

	/**
	 * @return the id_requirements
	 */
	public String getId_requirements() {
		return id_requirements;
	}

	/**
	 * @param id_requirements the id_requirements to set
	 */
	public void setId_requirements(String id_requirements) {
		this.id_requirements = id_requirements;
	}

//	/**
//	 * @return the local_tracking_interval
//	 */
//	public Integer getLocal_tracking_interval() {
//		return local_tracking_interval;
//	}
//
//	/**
//	 * @param local_tracking_interval the local_tracking_interval to set
//	 */
//	public void setLocal_tracking_interval(Integer local_tracking_interval) {
//		this.local_tracking_interval = local_tracking_interval;
//	}
//
//	/**
//	 * @return the track_to_file
//	 */
//	public boolean isTrack_to_file() {
//		return track_to_file;
//	}
//
//	/**
//	 * @param track_to_file the track_to_file to set
//	 */
//	public void setTrack_to_file(boolean track_to_file) {
//		this.track_to_file = track_to_file;
//	}
//
//	/**
//	 * @return the track_id_email
//	 */
//	public String getTrack_id_email() {
//		return track_id_email;
//	}
//
//	/**
//	 * @param track_id_email the track_id_email to set
//	 */
//	public void setTrack_id_email(String track_id_email) {
//		this.track_id_email = track_id_email;
//	}
//
//	/**
//	 * @return the track_url
//	 */
//	public String getTrack_url() {
//		return track_url;
//	}
//
//	/**
//	 * @param track_url the track_url to set
//	 */
//	public void setTrack_url(String track_url) {
//		this.track_url = track_url;
//	}
//
//	/**
//	 * @return the trackMailEntity
//	 */
//	public MailEntity getTrackMailEntity() {
//		return trackMailEntity;
//	}
//
//	/**
//	 * @param trackMailEntity the trackMailEntity to set
//	 */
//	public void setTrackMailEntity(MailEntity trackMailEntity) {
//		this.trackMailEntity = trackMailEntity;
//	}
//
//	/**
//	 * @return the enter_tracker
//	 */
//	public boolean isEnter_tracker() {
//		return enter_tracker;
//	}
//
//	/**
//	 * @param enter_tracker the enter_tracker to set
//	 */
//	public void setEnter_tracker(boolean enter_tracker) {
//		this.enter_tracker = enter_tracker;
//	}
//
//	/**
//	 * @return the exit_tracker
//	 */
//	public boolean isExit_tracker() {
//		return exit_tracker;
//	}
//
//	/**
//	 * @param exit_tracker the exit_tracker to set
//	 */
//	public void setExit_tracker(boolean exit_tracker) {
//		this.exit_tracker = exit_tracker;
//	}
//
	/**
	 * @return the status
	 */
	public boolean isStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}


}
