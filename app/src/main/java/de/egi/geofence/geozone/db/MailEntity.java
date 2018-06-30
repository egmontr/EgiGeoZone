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

public class MailEntity {

	private Integer id;
	private String name;
	private String smtp_user;
	private String smtp_pw;
	private String smtp_server;
	private String smtp_port;
	private String from;
	private String to;
	private String subject;
	private String body;
	private boolean ssl;

	private boolean starttls;
	private boolean enter;
	private boolean exit;
	
	public MailEntity() {
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
	 * @return the smtp_user
	 */
	public String getSmtp_user() {
		return smtp_user;
	}

	/**
	 * @param smtp_user the smtp_user to set
	 */
	public void setSmtp_user(String smtp_user) {
		this.smtp_user = smtp_user;
	}

	/**
	 * @return the smtp_pw
	 */
	public String getSmtp_pw() {
		return smtp_pw;
	}

	/**
	 * @param smtp_pw the smtp_pw to set
	 */
	public void setSmtp_pw(String smtp_pw) {
		this.smtp_pw = smtp_pw;
	}

	/**
	 * @return the smtp_server
	 */
	public String getSmtp_server() {
		return smtp_server;
	}

	/**
	 * @param smtp_server the smtp_server to set
	 */
	public void setSmtp_server(String smtp_server) {
		this.smtp_server = smtp_server;
	}

	/**
	 * @return the smtp_port
	 */
	public String getSmtp_port() {
		return smtp_port;
	}

	/**
	 * @param smtp_port the smtp_port to set
	 */
	public void setSmtp_port(String smtp_port) {
		this.smtp_port = smtp_port;
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(String to) {
		this.to = to;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return the ssl
	 */
	public boolean isSsl() {
		return ssl;
	}

	/**
	 * @param ssl the ssl to set
	 */
	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public boolean isStarttls() {
		return starttls;
	}

	public void setStarttls(boolean starttls) {
		this.starttls = starttls;
	}

	/**
	 * @return the enter
	 */
	public boolean isEnter() {
		return enter;
	}

	/**
	 * @param enter the enter to set
	 */
	public void setEnter(boolean enter) {
		this.enter = enter;
	}

	/**
	 * @return the exit
	 */
	public boolean isExit() {
		return exit;
	}

	/**
	 * @param exit the exit to set
	 */
	public void setExit(boolean exit) {
		this.exit = exit;
	}
	
	
}
