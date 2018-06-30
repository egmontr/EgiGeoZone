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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.NotificationUtil;

/**
 * @author Egmont Jars für javaMail from
 *         http://code.google.com/p/javamail-android/downloads/list
 */
@SuppressLint("NewApi")
public class SendMail{

	private final Logger log = Logger.getLogger(SendMail.class);

	private final String user;
	private final String password;
	private final String smtpHost;
	private final String smtpPort;
	private final String senderAddress;
	private final String recipientsAddress;
	private final boolean ssl;
	private final boolean starttls;
	private boolean test;
	private final Context context;
	
	public SendMail(Context context, String user, String password, String smtpHost, String smtpPort, String sender, String recipients, boolean ssl, boolean starttls) {
	
		this.user = user;
		this.password = password;
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.senderAddress = sender;
		this.recipientsAddress = recipients;
		this.ssl = ssl;
		this.starttls = starttls;
		this.context = context;
		this.test = false;
	}
	
	public void sendMail(String subject, String body, boolean test) {
		this.test = test;
		try {
			Properties properties = new Properties();
	
			// Den Properties wird die ServerAdresse hinzugefügt
			properties.put("mail.smtp.host", smtpHost);
			properties.put("mail.smtp.port", smtpPort);
			properties.put("mail.smtp.socketFactory.fallback", "false"); 

			if (ssl){
                if (starttls) {
                    properties.put("mail.smtp.socketFactory.port", smtpPort);
                    properties.put("mail.smtp.starttls.enable", "true");
                }else{
			    properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                }
				properties.put("mail.transport.protocol", "smtps");
			}
			
		    Authenticator auth = null;
			if (!user.equalsIgnoreCase("")) {
				// Hier wird mit den Properties und dem implements Contructor erzeugten
				// MailAuthenticator eine Session erzeugt
				auth = new MailAuthenticator(user, password);
				// !!Wichtig!! Falls der SMTP-Server eine Authentifizierung verlangt
				// muss an dieser Stelle die Property auf "true" gesetzt werden
				properties.put("mail.smtp.auth", "true");
			}
		    
			// Hier wird mit den Properties und dem implements Contructor
			// erzeugten
			// MailAuthenticator eine Session erzeugt
			Session session = Session.getInstance(properties, auth);
			// Eine neue Message erzeugen
			Message msg = new MimeMessage(session);
			
			// Hier werden die Absender- und Empfängeradressen gesetzt
			msg.setFrom(new InternetAddress(senderAddress));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientsAddress, false));

			// Der Betreff und Body der Message werden gesetzt
			msg.setSubject(subject);
			msg.setSentDate(new Date());
			msg.setText(body);

			// Zum Schluss wird die Mail natürlich noch verschickt
			// Senden in einem extra Task
			new LongOperation().execute(msg);

		} catch (Exception e) {
			// nichts tun
			String a = e.getMessage();
			Log.e("SendMail", "Error: " + a);
			log.error("Error: " + a, e);
			NotificationUtil.showError(context, "Error sending mail: ", e.toString());
			// TestErgebnis 
			if (test){
	    		// Broadcast an die Main, damit der Drawer sich refreshed.
//				GlobalSingleton.getInstance().setTestResultError(true);
	    		Intent intent = new Intent();
				intent.setAction(Constants.ACTION_TEST_STATUS_NOK);
				intent.putExtra("TestResult", "Error sending mail: " + e.toString());
				context.sendBroadcast(intent);
			}
		}
	}
	
	private class LongOperation extends AsyncTask<Message, Void, String> {

        @Override
        protected String doInBackground(Message... msg) {
        	try {
				Transport.send(msg[0]);
				log.info("### - Mail sent");
				// TestErgebnis 
				if (test){
		    		// Broadcats an die Main, damit der Drawer sich refreshed.
		    		Intent intent = new Intent();
					intent.setAction(Constants.ACTION_TEST_STATUS_OK);
					context.sendBroadcast(intent);
				}

			} catch (Throwable e) {
				String a = e.getMessage();
				Log.e("SendMail", "Error: " + a);
				log.error("Error: " + a,e);
				NotificationUtil.showError(context, "Error (AsynkTask) sending mail: ", e.toString());
				// TestErgebnis 
				if (test){
		    		// Broadcast an die Main, damit der Drawer sich refreshed.
//					GlobalSingleton.getInstance().setTestResultError(true);
					Intent intent = new Intent();
					intent.setAction(Constants.ACTION_TEST_STATUS_NOK);
					intent.putExtra("TestResult", "Error (AsynkTask) sending mail: " + e.toString());
					context.sendBroadcast(intent);
						
				}
			}
            return "Executed";
        }      

        @Override
        protected void onPostExecute(String result) {
			log.info("in onPostExecute");
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
	}	
}