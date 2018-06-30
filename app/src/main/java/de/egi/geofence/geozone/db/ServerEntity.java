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

public class ServerEntity {

	private Integer id;
	private String name;
	private String url_fhem;
	private String url_tracking;
	private String url_enter;
	private String url_exit;
	private String user;
	private String user_pw;
	private String cert;
	private String cert_password;
	private String ca_cert;
	private String timeout;
	private String id_fallback;

	public String getUrl_tracking() {
		return url_tracking;
	}

	public void setUrl_tracking(String url_tracking) {
		this.url_tracking = url_tracking;
	}

	public String getId_fallback() {
		return id_fallback;
	}

	public void setId_fallback(String id_fallback) {
		this.id_fallback = id_fallback;
	}

	public ServerEntity() {
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
	 * @return the url_fhem
	 */
	public String getUrl_fhem() {
		return url_fhem;
	}

	/**
	 * @param url_fhem the url_fhem to set
	 */
	public void setUrl_fhem(String url_fhem) {
		this.url_fhem = url_fhem;
	}

	/**
	 * @return the url_enter
	 */
	public String getUrl_enter() {
		return url_enter;
	}

	/**
	 * @param url_enter the url_enter to set
	 */
	public void setUrl_enter(String url_enter) {
		this.url_enter = url_enter;
	}

	/**
	 * @return the url_exit
	 */
	public String getUrl_exit() {
		return url_exit;
	}

	/**
	 * @param url_exit the url_exit to set
	 */
	public void setUrl_exit(String url_exit) {
		this.url_exit = url_exit;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the user_pw
	 */
	public String getUser_pw() {
		return user_pw;
	}

	/**
	 * @param user_pw the user_pw to set
	 */
	public void setUser_pw(String user_pw) {
		this.user_pw = user_pw;
	}

	/**
	 * @return the cert
	 */
	public String getCert() {
		return cert;
	}

	/**
	 * @param cert the cert to set
	 */
	public void setCert(String cert) {
		this.cert = cert;
	}

	/**
	 * @return the cert_password
	 */
	public String getCert_password() {
		return cert_password;
	}

	/**
	 * @param cert_password the cert_password to set
	 */
	public void setCert_password(String cert_password) {
		this.cert_password = cert_password;
	}

	/**
	 * @return the ca_cert
	 */
	public String getCa_cert() {
		return ca_cert;
	}

	/**
	 * @param ca_cert the ca_cert to set
	 */
	public void setCa_cert(String ca_cert) {
		this.ca_cert = ca_cert;
	}

	/**
	 * @return the timeout
	 */
	public String getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

}
