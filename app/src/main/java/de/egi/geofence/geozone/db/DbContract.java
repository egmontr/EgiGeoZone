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

import android.provider.BaseColumns;

public class DbContract {
	// If you change the database schema, you must increment the database version.
	public static final  int    DATABASE_VERSION   = 270; // Letzte Version mit DB-Änderung
	public static final  String DATABASE_NAME      = "egigeozone.db";
	private static final String TEXT_TYPE          = " TEXT";
	private static final String INTEGER_TYPE       = " INTEGER";
	private static final String BOOLEAN_TYPE       = " INTEGER DEFAULT 0";
	private static final String NOT_NULL_UNIQUE    = " NOT NULL UNIQUE";
	private static final String COMMA_SEP          = ",";

	// To prevent someone from accidentally instantiating the contract class, give it an empty constructor.
	private DbContract() {
	}
	/* Inner class that defines the server table contents */
	public static abstract class ServerEntry implements BaseColumns {

		private ServerEntry() {}

		//	server: id, name, url_fhem, url_enter, url_exit, user, user_pw, cert, cert_password, ca_cert, timeout
		public static final String TN = "server";
		public static final String CN_NAME = "name";
		public static final String CN_URL_FHEM = "url_fhem";
		public static final String CN_URL_TRACKING = "url_tracking";
		public static final String CN_URL_ENTER = "url_enter";
		public static final String CN_URL_EXIT = "url_exit";
		public static final String CN_USER = "user";
		public static final String CN_USER_PW = "user_pw";
		public static final String CN_CERT = "cert";
		public static final String CN_CERT_PASSWORD = "cert_password";
		public static final String CN_CA_CERT = "ca_cert";
		public static final String CN_TIMEOUT = "timeout";
		public static final String CN_ID_FALLBACK = "id_fallback";

		public static final String[] allColumns = { _ID,
				CN_NAME, CN_URL_FHEM, CN_URL_ENTER, CN_URL_EXIT, CN_USER,
				CN_USER_PW, CN_CERT, CN_CERT_PASSWORD, CN_CA_CERT, CN_TIMEOUT, CN_ID_FALLBACK, CN_URL_TRACKING
		};

		public static final String ALTER_TABLE_ADD_FALLBACK = "ALTER TABLE " +
				TN + " ADD COLUMN " + CN_ID_FALLBACK + TEXT_TYPE;

		public static final String ALTER_TABLE_ADD_URL_TRACKING = "ALTER TABLE " +
				TN + " ADD COLUMN " + CN_URL_TRACKING + TEXT_TYPE;

		public static final String CREATE_TABLE = "CREATE TABLE " +
				TN + " (" +
				_ID + " INTEGER PRIMARY KEY," +
				CN_NAME + TEXT_TYPE + NOT_NULL_UNIQUE + COMMA_SEP +
				CN_URL_FHEM + TEXT_TYPE + COMMA_SEP +
				CN_URL_ENTER + TEXT_TYPE + COMMA_SEP +
				CN_URL_EXIT + TEXT_TYPE + COMMA_SEP +
				CN_USER + TEXT_TYPE + COMMA_SEP +
				CN_USER_PW + TEXT_TYPE + COMMA_SEP +
				CN_CERT + TEXT_TYPE + COMMA_SEP +
				CN_CERT_PASSWORD + TEXT_TYPE + COMMA_SEP +
				CN_CA_CERT + TEXT_TYPE + COMMA_SEP +
				CN_TIMEOUT + INTEGER_TYPE + COMMA_SEP +
				CN_ID_FALLBACK + TEXT_TYPE + COMMA_SEP +
				CN_URL_TRACKING + TEXT_TYPE +
				" )";

		public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TN;
	}

	/* Inner class that defines the sms table contents */
	public static abstract class SmsEntry implements BaseColumns {

		private SmsEntry() {
		}
		//	sms: id, name, number, text
		public static final String TN = "sms";
		public static final String CN_NAME = "name";
		public static final String CN_NUMBER = "number";
		public static final String CN_TEXT = "text";
		public static final String CN_ENTER = "enter";
		public static final String CN_EXIT = "exit";

		public static final String[] allColumns = { _ID,
				CN_NAME, CN_NUMBER, CN_TEXT, CN_ENTER, CN_EXIT};

		public static final String CREATE_TABLE = "CREATE TABLE " +
				TN + " (" +
				_ID + " INTEGER PRIMARY KEY," +
				CN_NAME + TEXT_TYPE + NOT_NULL_UNIQUE + COMMA_SEP +
				CN_NUMBER + TEXT_TYPE + COMMA_SEP +
				CN_TEXT + TEXT_TYPE + COMMA_SEP +
				CN_ENTER + BOOLEAN_TYPE + COMMA_SEP +
				CN_EXIT + BOOLEAN_TYPE +
				" )";

		public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TN;
	}

	/* Inner class that defines the email table contents */
	public static abstract class MailEntry implements BaseColumns {

		private MailEntry() {
		}
		//		email: id, name, smtp_user, smtp_pw, smtp_server, smtp_port, from, to, subject, body
		public static final String TN = "email";
		public static final String CN_NAME = "name";
		public static final String CN_SMTP_USER = "smtp_user";
		public static final String CN_SMTP_PW = "smtp_pw";
		public static final String CN_SMTP_SERVER = "smtp_server";
		public static final String CN_SMTP_PORT = "smtp_port";
		public static final String CN_FROM = "f_rom";
		public static final String CN_TO = "t_o";
		public static final String CN_SUBJECT = "subject";
		public static final String CN_BODY = "body";
		public static final String CN_SSL = "ssl";
		public static final String CN_STARTTLS = "starttls";
		public static final String CN_ENTER = "enter";
		public static final String CN_EXIT = "exit";

		public static final String[] allColumns = { _ID,
				CN_NAME, CN_SMTP_USER, CN_SMTP_PW, CN_SMTP_SERVER, CN_SMTP_PORT,
				CN_FROM, CN_TO, CN_SUBJECT, CN_BODY, CN_SSL, CN_ENTER, CN_EXIT, CN_STARTTLS};

		public static final String ALTER_TABLE_ADD_STARTTLS = "ALTER TABLE " +
				TN + " ADD COLUMN " + CN_STARTTLS + BOOLEAN_TYPE;

		public static final String CREATE_TABLE = "CREATE TABLE " +
				TN + " (" +
				_ID + " INTEGER PRIMARY KEY," +
				CN_NAME + TEXT_TYPE + NOT_NULL_UNIQUE + COMMA_SEP +
				CN_SMTP_USER + TEXT_TYPE + COMMA_SEP +
				CN_SMTP_PW + TEXT_TYPE + COMMA_SEP +
				CN_SMTP_SERVER + TEXT_TYPE + COMMA_SEP +
				CN_SMTP_PORT + INTEGER_TYPE + COMMA_SEP +
				CN_FROM + TEXT_TYPE + COMMA_SEP +
				CN_TO + TEXT_TYPE + COMMA_SEP +
				CN_SUBJECT + TEXT_TYPE + COMMA_SEP +
				CN_BODY + TEXT_TYPE + COMMA_SEP +
				CN_SSL + BOOLEAN_TYPE +  COMMA_SEP +
				CN_ENTER + BOOLEAN_TYPE + COMMA_SEP +
				CN_EXIT + BOOLEAN_TYPE + COMMA_SEP +
				CN_STARTTLS + BOOLEAN_TYPE +

				" )";

		public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TN;
	}

	/* Inner class that defines the more_actions table contents */
	public static abstract class MoreEntry implements BaseColumns {

		private MoreEntry() {
		}
		public static final String TN = "more_actions";
		public static final String CN_NAME = "name";
		public static final String CN_ENTER_TASK = "enter_task";
		public static final String CN_ENTER_WIFI = "enter_wifi";
		public static final String CN_ENTER_SOUND = "enter_sound";
		public static final String CN_ENTER_SOUND_MM = "enter_soundMM";
		public static final String CN_ENTER_BT = "enter_bt";
		public static final String CN_EXIT_TASK = "exit_task";
		public static final String CN_EXIT_WIFI = "exit_wifi";
		public static final String CN_EXIT_SOUND = "exit_sound";
		public static final String CN_EXIT_SOUND_MM = "exit_soundMM";
		public static final String CN_EXIT_BT = "exit_bt";

		public static final String[] allColumns = { _ID, CN_NAME,
				CN_ENTER_TASK, CN_ENTER_WIFI, CN_ENTER_SOUND, CN_ENTER_BT,
				CN_EXIT_TASK, CN_EXIT_WIFI, CN_EXIT_SOUND, CN_EXIT_BT, CN_ENTER_SOUND_MM, CN_EXIT_SOUND_MM
		};

		public static final String ALTER_TABLE_ADD_ENTER_SOUND_MM = "ALTER TABLE " +
				TN + " ADD COLUMN " + CN_ENTER_SOUND_MM + INTEGER_TYPE + " DEFAULT 2";

		public static final String ALTER_TABLE_ADD_EXIT_SOUND_MM = "ALTER TABLE " +
				TN + " ADD COLUMN " + CN_EXIT_SOUND_MM + INTEGER_TYPE + " DEFAULT 2";

		public static final String CREATE_TABLE = "CREATE TABLE " +
				TN + " (" +
				_ID + " INTEGER PRIMARY KEY," +
				CN_NAME + TEXT_TYPE + NOT_NULL_UNIQUE + COMMA_SEP +
				CN_ENTER_TASK + TEXT_TYPE + COMMA_SEP +
				CN_ENTER_WIFI + INTEGER_TYPE + COMMA_SEP +
				CN_ENTER_SOUND + INTEGER_TYPE + COMMA_SEP +
				CN_ENTER_BT + INTEGER_TYPE + COMMA_SEP +
				CN_EXIT_TASK + TEXT_TYPE + COMMA_SEP +
				CN_EXIT_WIFI + INTEGER_TYPE + COMMA_SEP +
				CN_EXIT_SOUND + INTEGER_TYPE + COMMA_SEP +
				CN_EXIT_BT + INTEGER_TYPE + COMMA_SEP +
				CN_ENTER_SOUND_MM + INTEGER_TYPE + " DEFAULT 2" + COMMA_SEP +
				CN_EXIT_SOUND_MM + INTEGER_TYPE + " DEFAULT 2" +
				" )";

		public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TN;
	}

	/* Inner class that defines the requirements table contents */
	public static abstract class RequirementsEntry implements BaseColumns {

		private RequirementsEntry() {
		}
		//		requirements: id, name, enter_bt, exit_bt, mon, tue, wed, thu, fri, sat, sun
		public static final String TN = "requirements";
		public static final String CN_NAME = "name";
		public static final String CN_ENTER_BT = "enter_bt";
		public static final String CN_EXIT_BT = "exit_bt";
		public static final String CN_MON = "mon";
		public static final String CN_TUE = "tue";
		public static final String CN_WED = "wed";
		public static final String CN_THU = "thu";
		public static final String CN_FRI = "fri";
		public static final String CN_SAT = "sat";
		public static final String CN_SUN = "sun";

		public static final String[] allColumns = { _ID, CN_NAME,
				CN_ENTER_BT, CN_EXIT_BT,
				CN_MON, CN_TUE, CN_WED, CN_THU, CN_FRI, CN_SAT, CN_SUN
		};

		public static final String CREATE_TABLE = "CREATE TABLE " +
				TN + " (" +
				_ID + " INTEGER PRIMARY KEY," +
				CN_NAME + TEXT_TYPE + NOT_NULL_UNIQUE + COMMA_SEP +
				CN_ENTER_BT + TEXT_TYPE + COMMA_SEP +
				CN_EXIT_BT + TEXT_TYPE + COMMA_SEP +
				CN_MON + BOOLEAN_TYPE + COMMA_SEP +
				CN_TUE + BOOLEAN_TYPE + COMMA_SEP +
				CN_WED + BOOLEAN_TYPE + COMMA_SEP +
				CN_THU + BOOLEAN_TYPE + COMMA_SEP +
				CN_FRI + BOOLEAN_TYPE + COMMA_SEP +
				CN_SAT + BOOLEAN_TYPE + COMMA_SEP +
				CN_SUN + BOOLEAN_TYPE +
				" )";

		public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TN;
	}

	/* Inner class that defines the global table contents */
	public static abstract class GlobalsEntry implements BaseColumns {
		private GlobalsEntry() {
		}

		//		global: id, key, value
		public static final String TN = "globals";
		public static final String CN_KEY = "schluessel";
		public static final String CN_VALUE = "wert";

		public static final String[] allColumns = { _ID, CN_KEY, CN_VALUE};

		public static final String CREATE_TABLE = "CREATE TABLE " +
				TN + " (" +
				_ID + " INTEGER PRIMARY KEY," +
				CN_KEY + TEXT_TYPE + COMMA_SEP +
				CN_VALUE + TEXT_TYPE +
				" )";

		public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TN;
	}

	/* Inner class that defines the zone table contents */
	public static abstract class ZoneEntry implements BaseColumns {
		private ZoneEntry() {
		}

		public static final String TN = "zone";
		public static final String CN_NAME = "name";
		public static final String CN_LATITUDE = "latitude";
		public static final String CN_LONGITUDE = "longitude";
		public static final String CN_RADIUS = "radius";
		public static final String CN_ACCURACY = "accuracy";
		public static final String CN_ID_SERVER = "id_server";
		public static final String CN_ID_SMS = "id_sms";
		public static final String CN_ID_EMAIL = "id_email";
		public static final String CN_ID_MORE_ACTIONS = "id_more_actions";
		public static final String CN_ID_REQUIREMENTS = "id_requirements";
		public static final String CN_LOCAL_TRACKING_INTERVAL = "local_tracking_interval";
		public static final String CN_TRACK_TO_FILE = "track_to_file";
		public static final String CN_TRACK_ID_EMAIL = "track_id_email";
		public static final String CN_TRACK_TO_SERVER = "track_to_server";
		public static final String CN_ENTER_TRACKER = "enter_tracker";
		public static final String CN_EXIT_TRACKER = "exit_tracker";
		public static final String CN_STATUS = "status";
		public static final String CN_TYPE = "type";
		public static final String CN_BEACON = "beacon";
		public static final String CN_ALIAS = "alias";

		public static final String ALTER_TABLE_ADD_ACCURACY = "ALTER TABLE " +
				TN + " ADD COLUMN " + CN_ACCURACY + INTEGER_TYPE + " DEFAULT 0";

		public static final String ALTER_TABLE_ADD_TYPE = "ALTER TABLE " +
				TN + " ADD COLUMN " + CN_TYPE + TEXT_TYPE;

		public static final String ALTER_TABLE_ADD_BEACON = "ALTER TABLE " +
				TN + " ADD COLUMN " + CN_BEACON + TEXT_TYPE;

		public static final String ALTER_TABLE_ADD_ALIAS = "ALTER TABLE " +
				TN + " ADD COLUMN " + CN_ALIAS + TEXT_TYPE;

		public static final String[] allColumns = { _ID, CN_NAME,
				CN_LATITUDE, CN_LONGITUDE, CN_RADIUS, CN_ID_SERVER, CN_ID_SMS,
				CN_ID_EMAIL, CN_ID_MORE_ACTIONS, CN_ID_REQUIREMENTS,
				CN_LOCAL_TRACKING_INTERVAL, CN_TRACK_TO_FILE, CN_TRACK_ID_EMAIL, CN_TRACK_TO_SERVER,
				CN_ENTER_TRACKER, CN_EXIT_TRACKER, CN_STATUS, CN_ACCURACY, CN_TYPE, CN_BEACON, CN_ALIAS
		};

		public static final String[] zoneNames = { CN_NAME	};

		public static final String CREATE_TABLE = "CREATE TABLE " +
				TN + " (" +
				_ID + " INTEGER PRIMARY KEY," +
				CN_ID_SERVER + TEXT_TYPE + COMMA_SEP +
				CN_ID_SMS + TEXT_TYPE + COMMA_SEP +
				CN_ID_EMAIL + TEXT_TYPE + COMMA_SEP +
				CN_ID_MORE_ACTIONS + TEXT_TYPE + COMMA_SEP +
				CN_ID_REQUIREMENTS + TEXT_TYPE + COMMA_SEP +
				CN_TRACK_ID_EMAIL + TEXT_TYPE + COMMA_SEP +
				CN_NAME + TEXT_TYPE + NOT_NULL_UNIQUE + COMMA_SEP +
				CN_LATITUDE + TEXT_TYPE + COMMA_SEP +
				CN_LONGITUDE + TEXT_TYPE + COMMA_SEP +
				CN_RADIUS + INTEGER_TYPE + COMMA_SEP +

				CN_LOCAL_TRACKING_INTERVAL + INTEGER_TYPE + COMMA_SEP +
				CN_TRACK_TO_FILE + INTEGER_TYPE + COMMA_SEP +
				CN_TRACK_TO_SERVER + TEXT_TYPE + COMMA_SEP +
				CN_ENTER_TRACKER + BOOLEAN_TYPE + COMMA_SEP +
				CN_EXIT_TRACKER + BOOLEAN_TYPE + COMMA_SEP +
				CN_STATUS + BOOLEAN_TYPE + COMMA_SEP +
				CN_ACCURACY + INTEGER_TYPE + COMMA_SEP +
				CN_TYPE + TEXT_TYPE + COMMA_SEP +
				CN_BEACON + TEXT_TYPE + COMMA_SEP +
				CN_ALIAS + TEXT_TYPE + COMMA_SEP +

				"FOREIGN KEY(" + CN_ID_SERVER + ") REFERENCES " + DbContract.ServerEntry.TN + "(name), " +
				"FOREIGN KEY(" + CN_ID_SMS + ") REFERENCES " + DbContract.SmsEntry.TN + "(name), " +
				"FOREIGN KEY(" + CN_ID_EMAIL + ") REFERENCES " + DbContract.MailEntry.TN + "(name), " +
				"FOREIGN KEY(" + CN_ID_MORE_ACTIONS + ") REFERENCES " + DbContract.MoreEntry.TN + "(name), " +
				"FOREIGN KEY(" + CN_ID_REQUIREMENTS + ") REFERENCES " + DbContract.RequirementsEntry.TN + "(name), " +
				"FOREIGN KEY(" + CN_TRACK_ID_EMAIL + ") REFERENCES " + DbContract.MailEntry.TN + "(name) " +
				"FOREIGN KEY(" + CN_TRACK_TO_SERVER + ") REFERENCES " + DbContract.ServerEntry.TN + "(name) " +
				" )";

		public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TN;
	}
}


















