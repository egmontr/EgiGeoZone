package de.egi.geofence.geozone;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.apache.log4j.Logger;

import java.util.List;

import de.egi.geofence.geozone.geofence.SimpleGeofence;
import de.egi.geofence.geozone.geofence.SimpleGeofenceStore;
import de.egi.geofence.geozone.utils.Utils;

public class KarteAll extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback{

    private final Logger log = Logger.getLogger(KarteAll.class);
    private GoogleMap mMap;
    private Marker markerInfoWindow;
    private boolean zoomIn = true;
    private LatLngBounds.Builder builder;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Utils.onActivityCreateSetTheme(this);
        setContentView(R.layout.map_all);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        Utils.changeBackGroundToolbar(this, toolbar);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapAll);
        mapFragment.getMapAsync(this);
    }

    private void setFences() {
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            // The Map is verified. It is now safe to manipulate the map.
            try{
                mMap.setMyLocationEnabled(true);
            }catch(SecurityException se){
                // Display UI and wait for user interaction
                AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
                alertDialogBuilder.setMessage(getString(R.string.alertPermissions));
                alertDialogBuilder.setTitle(getString(R.string.titleAlertPermissions));

                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            mMap.setOnMarkerClickListener(this);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            // Instantiate a new geofence storage area
            SimpleGeofenceStore geofenceStore = new SimpleGeofenceStore(this);
            List<SimpleGeofence> geofences = geofenceStore.getGeofences();
            if (geofences.size() == 0) return;

            builder = new LatLngBounds.Builder();
            for (SimpleGeofence sg : geofences) {
                //Instantiates a new CircleOptions object +  center/radius
                CircleOptions circleOptions = new CircleOptions()
                        .center(new LatLng(Double.valueOf(sg.getLatitude()), Double.valueOf(sg.getLongitude())))
                        .radius(Double.valueOf(sg.getRadius()))
                        .fillColor(0x40ff4e40)
                        .strokeColor(Color.TRANSPARENT)
                        .strokeWidth(2);

                mMap.addCircle(circleOptions);

                IconGenerator ig = new IconGenerator(this);
                ig.setStyle(sg.isStatus() ? IconGenerator.STYLE_GREEN : IconGenerator.STYLE_RED);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(Double.valueOf(sg.getLatitude()), Double.valueOf(sg.getLongitude())))
                        .icon(BitmapDescriptorFactory.fromBitmap(ig.makeIcon(sg.getId())))
                        .anchor(ig.getAnchorU(), ig.getAnchorV())
                        ;

                builder.include(new LatLng(Double.valueOf(sg.getLatitude()), Double.valueOf(sg.getLongitude())));
                markerInfoWindow = mMap.addMarker(markerOptions);
                markerInfoWindow.showInfoWindow();
            }

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width-200, height-200, 5);
            mMap.animateCamera(cu);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (zoomIn) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(marker.getPosition()) // Sets the center of the map to Mountain View
                    .zoom(14)                     // Sets the zoom
                    .bearing(0)                   // Sets the orientation of the camera to north
                    .tilt(1)                      // Sets the tilt of the camera to xx degrees
                    .build();                     // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            zoomIn = false;
        }else{
            if (builder !=null) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int width = displayMetrics.widthPixels;
                int height = displayMetrics.heightPixels;
                LatLngBounds bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width - 200, height - 200, 5);
                mMap.animateCamera(cu);
            }
            zoomIn = true;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setFences();
    }
}






























