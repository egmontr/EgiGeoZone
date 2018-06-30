package de.egi.geofence.geozone.utils;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;

import de.egi.geofence.geozone.R;

public abstract class RuntimePermissionsActivity extends AppCompatActivity {
    private SparseIntArray mErrorString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mErrorString = new SparseIntArray();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int permission : grantResults) {
            permissionCheck = permissionCheck + permission;
        }
        if ((grantResults.length > 0) && permissionCheck == PackageManager.PERMISSION_GRANTED) {
            onPermissionsGranted(requestCode);
        } else {
            // The base permissions are needed for the app to work properly
            android.support.v7.app.AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);

            if (requestCode == 2000) {
                alertDialogBuilder.setTitle(getString(R.string.titleAlertPermissions));
                alertDialogBuilder.setMessage(getString(R.string.descAskAll));
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(intent);
                    }
                });

            }else if (requestCode == 2001){
                alertDialogBuilder.setTitle(getString(R.string.titleSMS));
                alertDialogBuilder.setMessage(getString(R.string.descAskSMS));
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
            }else if (requestCode == 2002){
                alertDialogBuilder.setTitle(getString(R.string.titleGCM));
                alertDialogBuilder.setMessage(getString(R.string.descAskGCM));

                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
            }
            android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    public void requestAppPermissions(final String[] requestedPermissions, final int stringId, final int requestCode) {
        mErrorString.put(requestCode, stringId);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        boolean shouldShowRequestPermissionRationale = false;
        for (String permission : requestedPermissions) {
            permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission);
            shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale) {
//                Snackbar.make(findViewById(android.R.id.content), stringId,
//                        Snackbar.LENGTH_INDEFINITE).setAction("GRANT",
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                ActivityCompat.requestPermissions(RuntimePermissionsActivity.this, requestedPermissions, requestCode);
//                            }
//                        }).show();

                // Display UI and wait for user interaction
                android.support.v7.app.AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
                alertDialogBuilder.setMessage(stringId);
                if (requestCode == 2000) {
                    alertDialogBuilder.setTitle(getString(R.string.titleAlertPermissions));
                }if (requestCode == 2001) {
                    alertDialogBuilder.setTitle(getString(R.string.titleSMS));
                }if (requestCode == 2002) {
                    alertDialogBuilder.setTitle(getString(R.string.titleGCM));
                }
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        ActivityCompat.requestPermissions(RuntimePermissionsActivity.this, requestedPermissions, requestCode);
                    }
                });
                android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            } else {
                ActivityCompat.requestPermissions(this, requestedPermissions, requestCode);
            }
        } else {
            onPermissionsGranted(requestCode);
        }
    }

    // Check for a permission
    public boolean checkPermission(String permission){
        int result = ContextCompat.checkSelfPermission(this, permission);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    // Check for all needed permissions
    public boolean checkAllNeededPermissions(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED){
            return false;
        }
        result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result != PackageManager.PERMISSION_GRANTED){
            return false;
        }
//        result = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
//        if (result != PackageManager.PERMISSION_GRANTED){
//            return false;
//        }
        result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (result != PackageManager.PERMISSION_GRANTED){
            return false;
        }
//        result = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
//        if (result != PackageManager.PERMISSION_GRANTED){
//            return false;
//        }
        return true;
    }

    public abstract void onPermissionsGranted(int requestCode);
}
