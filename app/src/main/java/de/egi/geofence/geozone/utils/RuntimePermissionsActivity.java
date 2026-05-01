package de.egi.geofence.geozone.utils;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.SparseIntArray;

import de.egi.geofence.geozone.EgiGeoZoneApplication;
import de.egi.geofence.geozone.R;

public abstract class RuntimePermissionsActivity extends AppCompatActivity {
    private SparseIntArray mErrorString;
    private final SharedPreferences prefs;
    private String title = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mErrorString = new SparseIntArray();
    }

    public RuntimePermissionsActivity() {
        prefs = EgiGeoZoneApplication.getAppContext().getSharedPreferences("perm_prefs", Context.MODE_PRIVATE);
    }

    public void requestAppPermission(String permission, int messageResId, int requestCode) {

        boolean isFirstTime = prefs.getBoolean(permission, true);

        if (requestCode == 2010) {
            title = getString(R.string.titleAlert2010Location);
        }else if (requestCode == 2020) {
            title = getString(R.string.titleAlert2020BT);
        }else if (requestCode == 2030) {
            title = getString(R.string.titleAlert2030BgLocation);
        }else if (requestCode == 2040) {
            title = getString(R.string.titleAlert2040Read);
        }else if (requestCode == 2050) {
            title = getString(R.string.titleAlert2050Notification);
        }else if (requestCode == 2060) {
            title = getString(R.string.titleAlert2060Write);
        }
        if (isFirstTime) {
            prefs.edit().putBoolean(permission, false).apply();

            // 👉 Erstes Mal → direkt Systemdialog
            new androidx.appcompat.app.AlertDialog.Builder(RuntimePermissionsActivity.this)
                    .setTitle(title)
                    .setMessage(messageResId)
                    .setPositiveButton("OK", (dialog, which) -> {
                        ActivityCompat.requestPermissions(RuntimePermissionsActivity.this,
                                new String[]{permission},
                                requestCode);
                    })
                    .setNegativeButton("Abbrechen", null)
                    .show();


        } else if (ActivityCompat.shouldShowRequestPermissionRationale(RuntimePermissionsActivity.this, permission)) {

            // 👉 Nutzer hat einmal abgelehnt → Erklärung anzeigen
            new androidx.appcompat.app.AlertDialog.Builder(RuntimePermissionsActivity.this)
                    .setTitle(title)
                    .setMessage(messageResId)
                    .setPositiveButton("OK", (dialog, which) -> {
                        ActivityCompat.requestPermissions(RuntimePermissionsActivity.this,
                                new String[]{permission},
                                requestCode);
                    })
                    .setNegativeButton("Abbrechen", null)
                    .show();


        } else {

            // 👉 Dauerhaft abgelehnt → Settings öffnen
            new androidx.appcompat.app.AlertDialog.Builder(RuntimePermissionsActivity.this)
                    .setTitle(title)
                    .setMessage(getString(messageResId))
                    .setPositiveButton("Einstellungen öffnen", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", RuntimePermissionsActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        RuntimePermissionsActivity.this.startActivity(intent);
                    })
                    .setNegativeButton("Abbrechen", null)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Continue the action or workflow
                // in your app.
                onPermissionsGranted(requestCode);
            } else {
                // Explain to the user that the feature is unavailable because
                // the feature requires a permission that the user has denied.
                // At the same time, respect the user's decision. Don't link to
                // system settings in an effort to convince the user to change
                // their decision.
            }
        }

    // Check for a permission
    public boolean checkPermission(String permission){
        int result = ContextCompat.checkSelfPermission(this, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    // Check for all needed permissions
    public boolean notAllNeededPermissionsGranted(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result != PackageManager.PERMISSION_GRANTED){
            return true;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result != PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public abstract void onPermissionsGranted(int requestCode);
}
