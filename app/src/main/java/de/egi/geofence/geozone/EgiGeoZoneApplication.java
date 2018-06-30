package de.egi.geofence.geozone;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.utils.Constants;

// http://altbeacon.github.io/android-beacon-library/documentation.html
public class EgiGeoZoneApplication extends Application{
	private static final String TAG = "EgiGeoZoneApplication";

	private DbGlobalsHelper dbGlobalsHelper;
	private String currentApplicationVersion = "UNKNOWN";

	@Override
	public void onCreate() {
		super.onCreate();
		dbGlobalsHelper = new DbGlobalsHelper(this);
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_CONFIGURATIONS);
			String v = pi.versionName;
			String f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss - ", Locale.getDefault()).format(new Date(pi.firstInstallTime));
			String l = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss - ", Locale.getDefault()).format(new Date(pi.lastUpdateTime));
			Log.i(TAG, "*** Current application version: " + v + " ***");
			Log.i(TAG, "*** First install time: " + f + " ***");
			Log.i(TAG, "*** Last update time: " + l + " ***");
		} catch (PackageManager.NameNotFoundException ignored) {
		}
		// Dann auch Version Exportieren/Importieren
		makeUpdates();
	}

	/**
	 * Updates durchführen
	 */
	private void makeUpdates() {
		if (dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_NEW_API) == null) {
			dbGlobalsHelper.storeGlobals(Constants.DB_KEY_NEW_API, "false");
		}

		try {
			currentApplicationVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(TAG, "Could not lookup current application version", e);
		}

		String lastInstalledApplicationVersion = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_LASTINSTALLEDAPPLICATIONVERSION);
		if (lastInstalledApplicationVersion == null || !lastInstalledApplicationVersion.equals(currentApplicationVersion)) {
			// Etwas tun
			// Wenn Updates nötig
			dbGlobalsHelper.storeGlobals(Constants.DB_KEY_LASTINSTALLEDAPPLICATIONVERSION, currentApplicationVersion);
		}

	}
}
