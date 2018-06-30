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

import java.io.File;

/**
 * Authentication parameters, including client cert, server cert, user name, and password.
 */
public class AuthenticationParameters {
    private File clientCertificate = null;
    private String clientCertificatePassword = null;
    private String caCertificate = null;
    private String url = null;
    private String user = null;
    private String userPasswd = null;
//    private String acceptAllCerts = null;

    public File getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(File clientCertificate) {
        this.clientCertificate = clientCertificate;
    }

    public String getClientCertificatePassword() {
        return clientCertificatePassword;
    }

    public void setClientCertificatePassword(String clientCertificatePassword) {
        this.clientCertificatePassword = clientCertificatePassword;
    }

    public String getCaCertificate() {
        return caCertificate;
    }

    public void setCaCertificate(String caCertificate) {
        this.caCertificate = caCertificate;
    }

	public String getUserPasswd() {
		return userPasswd;
	}

	public void setUserPasswd(String userPasswd) {
		this.userPasswd = userPasswd;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

//	public String getAcceptAllCerts() {
//		return acceptAllCerts;
//	}
//
//	public void setAcceptAllCerts(String acceptAllCerts) {
//		this.acceptAllCerts = acceptAllCerts;
//	}
}
