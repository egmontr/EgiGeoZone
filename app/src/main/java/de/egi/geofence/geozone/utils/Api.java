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

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Base64;

import org.apache.log4j.Logger;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import de.egi.geofence.geozone.ssl.SSLContextFactory;


/**
 * client-side interface to the back-end application.
 */
public class Api {

    private SSLContext sslContext;
    private int lastResponseCode;
    private final AuthenticationParameters authParams;
    private int timeout = 30000;
	private final Logger log = Logger.getLogger(Api.class);
    public int getLastResponseCode() {
        return lastResponseCode;
    }

    public Api(AuthenticationParameters authParams, String timeout) throws Exception {
    	this.authParams = authParams;
    	if (timeout == null || timeout.isEmpty() || !isNum(timeout)){
    		/* nichts tun, da auf 30000 initialisiert */
    	}else{
    		if (isNum(timeout)){
    			this.timeout = Integer.valueOf(timeout) * 1000;  
    		}
    	}
    	
    	if (authParams.getUrl().toLowerCase(Locale.getDefault()).startsWith("https")){
    		sslContext = SSLContextFactory.getInstance().makeContext(authParams.getClientCertificate(), authParams.getClientCertificatePassword(), authParams.getCaCertificate());
    	}
        CookieHandler.setDefault(new CookieManager()); 
    }


    public void doGet()  throws Exception {
        String result;
        HttpURLConnection urlConnection = null;
        try {
            URL requestedUrl = new URL(authParams.getUrl());
            URI uri = new URI(requestedUrl.getProtocol(), requestedUrl.getUserInfo(), requestedUrl.getHost(), requestedUrl.getPort(), requestedUrl.getPath(), requestedUrl.getQuery(), requestedUrl.getRef());
            requestedUrl = uri.toURL();
            urlConnection = (HttpURLConnection) requestedUrl.openConnection();
            // Nur wenn SSL
            if(urlConnection instanceof HttpsURLConnection) {
            	log.info("SSL connection required");
                ((HttpsURLConnection)urlConnection).setSSLSocketFactory(sslContext.getSocketFactory());
//                ((HttpsURLConnection)urlConnection).setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                ((HttpsURLConnection)urlConnection).setHostnameVerifier(new HostnameVerifier() {
                    @SuppressLint("BadHostnameVerifier")
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }
            // Basicauth nur wenn User gefüllt wurde.
            if (!TextUtils.isEmpty(authParams.getUser())){
            	log.info("Basic authentication required");
	            String userpass = authParams.getUser() + ":" + authParams.getUserPasswd();
	       		String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes(), Base64.DEFAULT));
	       		urlConnection.setRequestProperty ("Authorization", basicAuth);
            }            
            
            // Wegen JSON und java.io.FileNotFoundException: http://my_ip:1010/json.htm?type=command&param=switchlight&idx=664&switchcmd=Off
            // http://stackoverflow.com/questions/941628/urlconnection-filenotfoundexception-for-non-standard-http-port-sources/2274535#2274535
            urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
            urlConnection.setRequestProperty("Accept","*/*");
            
            urlConnection.setRequestMethod("GET");
        	log.info("timeout: " + timeout);
            urlConnection.setConnectTimeout(timeout);
            urlConnection.setReadTimeout(timeout);

            lastResponseCode = urlConnection.getResponseCode();
            // BAD_REQUEST
            if (lastResponseCode >= 400){
            	result = IOUtil.readFully(urlConnection.getErrorStream());
            	log.error("error result after get: " + result + " HttpStatus: " + lastResponseCode);
            }else{
            	result = IOUtil.readFully(urlConnection.getInputStream());
                log.info("result after get: " + result + " HttpStatus: " + lastResponseCode);
            }
            
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
//        return result;
    }

    private static boolean isNum(String strNum) {
        boolean ret = true;
        try {

            //noinspection ResultOfMethodCallIgnored
            Double.parseDouble(strNum);

        }catch (NumberFormatException e) {
            ret = false;
        }
        return ret;
    }
}
