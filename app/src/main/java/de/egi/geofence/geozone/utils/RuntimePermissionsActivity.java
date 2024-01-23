package de.egi.geofence.geozone.utils;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.SparseIntArray;

import de.egi.geofence.geozone.R;

public abstract class RuntimePermissionsActivity extends AppCompatActivity {
    private SparseIntArray mErrorString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mErrorString = new SparseIntArray();
    }

    // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher, as an instance variable.

    // You can directly ask for the permission.
    // The registered ActivityResultCallback gets the result of this request.
//    requestPermissionLauncher.launch(
//    Manifest.permission.REQUESTED_PERMISSION
//
//);



//    private ActivityResultLauncher<String> requestPermissionLauncher =
//            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//                if (isGranted) {
//                    // Permission is granted. Continue the action or workflow in your
//                    // app.
//                } else {
//                    // Explain to the user that the feature is unavailable because the
//                    // feature requires a permission that the user has denied. At the
//                    // same time, respect the user's decision. Don't link to system
//                    // settings in an effort to convince the user to change their
//                    // decision.
//                }
//            });
//




    public void requestAppPermission(final String requestedPermission, final int stringId, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,requestedPermission)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected, and what
            // features are disabled if it's declined. In this UI, include a
            // "cancel" or "no thanks" button that lets the user continue
            // using your app without granting the permission.
//            showInContextUI(...);

            // Display UI and wait for user interaction
            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
            alertDialogBuilder.setMessage(stringId);
            if (requestCode == 2010) {
                alertDialogBuilder.setTitle(getString(R.string.titleAlert2010Location));
            }else if (requestCode == 2020) {
                alertDialogBuilder.setTitle(R.string.titleAlert2020BT);
            }else if (requestCode == 2030) {
                alertDialogBuilder.setTitle(R.string.titleAlert2030BgLocation);
            }else if (requestCode == 2040) {
                alertDialogBuilder.setTitle(R.string.titleAlert2040Read);
            }else if (requestCode == 2050) {
                alertDialogBuilder.setTitle(R.string.titleAlert2050Notification);
            }else if (requestCode == 2060) {
                alertDialogBuilder.setTitle(R.string.titleAlert2060Write);
            }
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    ActivityCompat.requestPermissions(RuntimePermissionsActivity.this, new String[] {requestedPermission}, requestCode);
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                }
            });

            androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        } else {
            // You can directly ask for the permission.
            ActivityCompat.requestPermissions(this, new String[] {requestedPermission}, requestCode);
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

                // The base permissions are needed for the app to work properly
//                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
//
////                if (requestCode == 2000) {
//                    alertDialogBuilder.setTitle(getString(R.string.titleAlertPermissions));
//                    alertDialogBuilder.setMessage(getString(R.string.descAskAll));
//                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface arg0, int arg1) {
//                            Intent intent = new Intent();
////                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
////                            intent.addCategory(Intent.CATEGORY_DEFAULT);
////                            intent.setData(Uri.parse("package:" + getPackageName()));
////                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
////                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
////                            startActivity(intent);
//                        }
//                    });
//
////                }
//
//                // Other 'case' lines to check for other
//                // permissions this app might request
//
//                androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
//                alertDialog.show();
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
