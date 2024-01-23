package de.egi.geofence.geozone;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

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
            ((EditText) this.findViewById(R.id.editEGZVersion)).setText(String.format("EgiGeoZone: %s", pi.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ((EditText) this.findViewById(R.id.editDeviceName)).setText(getDeviceName());
        ((EditText) this.findViewById(R.id.editDeviceBrand)).setText(Build.BRAND);
        ((EditText) this.findViewById(R.id.editAndroidVersion)).setText(String.format("%s (%s)", Build.VERSION.RELEASE, Build.VERSION.CODENAME));

        UUID uuidAndroidId = Utils.getGuid(getApplicationContext());

        ((EditText) this.findViewById(R.id.editDeviceId)).setText(uuidAndroidId != null ? uuidAndroidId.toString() : "0");
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
