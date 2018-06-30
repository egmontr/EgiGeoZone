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

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

class MailAuthenticator extends Authenticator {

	/**
	 * Ein String, der den Usernamen nach der Erzeugung eines Objektes<br>
	 * dieser Klasse enthalten wird.
	 */
	private final String user;

	/**
	 * Ein String, der das Passwort nach der Erzeugung eines Objektes<br>
	 * dieser Klasse enthalten wird.
	 */
	private final String password;

	/**
	 * Der Konstruktor erzeugt ein MailAuthenticator Objekt<br>
	 * aus den beiden Parametern user und passwort.
	 * 
	 * @param user
	 *            String, der Username fuer den Mailaccount.
	 * @param password
	 *            String, das Passwort fuer den Mailaccount.
	 */
	public MailAuthenticator(String user, String password) {
		this.user = user;
		this.password = password;
	}

	/**
	 * Diese Methode gibt ein neues PasswortAuthentication Objekt zurueck.
	 * 
	 * @see javax.mail.Authenticator#getPasswordAuthentication()
	 */
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.user, this.password);
	}
}
