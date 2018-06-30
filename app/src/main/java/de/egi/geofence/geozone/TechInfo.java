package de.egi.geofence.geozone;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import de.egi.geofence.geozone.db.DbGlobalsHelper;
import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.Utils;

public class TechInfo extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetDialogTheme(this);
        setContentView(R.layout.tech_info);
        try {
            PackageInfo pi = getPackageManager().getPackageInfo("de.egi.geofence.geozone", PackageManager.GET_CONFIGURATIONS);
            ((EditText) this.findViewById(R.id.editEGZVersion)).setText("EgiGeoZone: " + pi.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ((EditText) this.findViewById(R.id.editDeviceName)).setText(getDeviceName());
        ((EditText) this.findViewById(R.id.editDeviceBrand)).setText(Build.BRAND);
        ((EditText) this.findViewById(R.id.editAndroidVersion)).setText(Build.VERSION.RELEASE + " ("  + Build.VERSION.CODENAME + ")");

        final String androidId = android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        // Use the Android ID unless it's broken, in which case fallback on deviceId,
        // unless it's not available, then fallback on a random number which we store
        // to a prefs file
        UUID uuidAndroidId = null;
        UUID uuidDeviceId = null;
        try{
            uuidAndroidId = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
            final String deviceId = ((TelephonyManager) this.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
            try {
                uuidDeviceId = deviceId!=null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } catch (SecurityException e) {
            // Permission read phone state is missing
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        ((EditText) this.findViewById(R.id.editDeviceId)).setText(uuidDeviceId != null ? uuidDeviceId.toString() : "0");
        ((EditText) this.findViewById(R.id.editAndroidId)).setText(uuidAndroidId != null ? uuidAndroidId.toString() : "0");

        DbGlobalsHelper dbGlobalsHelper = new DbGlobalsHelper(this);
        String token = dbGlobalsHelper.getCursorGlobalsByKey(Constants.DB_KEY_GCM_REG_ID);

        ((EditText) this.findViewById(R.id.editGCMAPIKey)).setText(token != null ? token : getString(R.string.noApiKey));
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


}
