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
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

import de.egi.geofence.geozone.utils.Constants;
import de.egi.geofence.geozone.utils.Utils;

public class Karte extends AppCompatActivity implements OnMapLongClickListener, OnMapClickListener, OnMarkerClickListener, OnMapReadyCallback,
		OnInfoWindowClickListener, OnMarkerDragListener, SearchView.OnQueryTextListener{

	private final Logger log = Logger.getLogger(Karte.class);
	private GoogleMap mMap;
	private Circle circle;
	private Double latx;
	private Double lngx;
	private Double radx;
	private String zonex;
	private int globe_state = 0;
	private final static int RADIUS_INCREMENT = 3;
	private Marker markerInfoWindow;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.map);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Utils.changeBackGroundToolbar(this, toolbar);

		Bundle b = getIntent().getExtras();
		String slatx = b.getString("de.egi.geofence.geozone.lat");
		String slngx = b.getString("de.egi.geofence.geozone.lng");
		String sradx = b.getString("de.egi.geofence.geozone.rad");
		String szonex = b.getString("de.egi.geofence.geozone.zone");

		if (TextUtils.isEmpty(szonex)) {
			zonex = "Neue Zone";
		} else {
			zonex = szonex;
		}

		if (TextUtils.isEmpty(slatx)) {
			latx = 48.13725825;
		} else {
			latx = Double.valueOf(slatx);
		}
		if (TextUtils.isEmpty(slngx)) {
			lngx = 11.576578058;
		} else {
			lngx = Double.valueOf(slngx);
		}
		if (TextUtils.isEmpty(sradx)) {
			radx = 500.0;
		} else {
			radx = Double.valueOf(sradx);
		}
		if (TextUtils.isEmpty(slatx) || TextUtils.isEmpty(slngx)) {
			FusedLocationProviderClient mLocationClient = LocationServices.getFusedLocationProviderClient(this);
			try{
			mLocationClient.getLastLocation()
					.addOnSuccessListener(mlocation -> {
						// Get the current location
						// Display the current location in the UI
						if (mlocation != null) {
							latx = mlocation.getLatitude();
							lngx = mlocation.getLongitude();
						}
						setUpMapIfNeeded();
					})
					.addOnFailureListener(e -> log.error("Could not determine location."));
			}catch(SecurityException se){
				// Display UI and wait for user interaction
				AlertDialog.Builder alertDialogBuilder = Utils.onAlertDialogCreateSetTheme(this);
				alertDialogBuilder.setMessage(getString(R.string.alertPermissions));
				alertDialogBuilder.setTitle(getString(R.string.titleAlertPermissions));

				alertDialogBuilder.setPositiveButton("OK", (arg0, arg1) -> {
				});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}

		}

		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}



	private void setUpMapIfNeeded() {
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

				alertDialogBuilder.setPositiveButton("OK", (arg0, arg1) -> {
				});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}

				mMap.getUiSettings().setZoomControlsEnabled(true);

				mMap.setOnMapLongClickListener(this);
				mMap.setOnInfoWindowClickListener(this);
				mMap.setOnMarkerDragListener(this);
//        		mMap.setOnCameraChangeListener(this);
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(latx, lngx))      // Sets the center of the map to Mountain View
						.zoom(14)                   // Sets the zoom
						.bearing(0)                // Sets the orientation of the camera to north
						.tilt(1)                   // Sets the tilt of the camera to 30 degrees
						.build();                   // Creates a CameraPosition from the builder
				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

				MarkerOptions mo =  new MarkerOptions()
						.position(new LatLng(latx, lngx))
						.draggable(true);

				// Adding marker on the GoogleMap
				markerInfoWindow = mMap.addMarker(mo);
				// Showing InfoWindow on the GoogleMap
				if (markerInfoWindow != null) {
					markerInfoWindow.showInfoWindow();
				}

				mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

					@Override
					public View getInfoWindow(@NonNull Marker arg0) {
						return null;
					}

					@SuppressLint({"InflateParams", "SetTextI18n"})
					@Override
					public View getInfoContents(@NonNull Marker arg0) {
						// Getting view from the layout file info_window_layout
						View v = getLayoutInflater().inflate(R.layout.info_map, null);

						// Getting the position from the marker
						LatLng latLng = arg0.getPosition();

						TextView tvFence = v.findViewById(R.id.tv_fence);
						TextView tvRadius = v.findViewById(R.id.tv_radius);
						TextView tvLatLng = v.findViewById(R.id.tv_lat_lng);
						TextView tvAction = v.findViewById(R.id.tv_action);

						// Setting the zone
						tvFence.setText("GeoZone: " + zonex);
						// Setting the radius
						tvRadius.setText("Radius: " + radx);
						tvLatLng.setText(latLng.latitude + "/" + latLng.longitude);
						tvAction.setText(R.string.map_action);
						tvAction.setTextColor(Color.parseColor("#A62AF0"));

						// Returning the view containing InfoWindow contents
						return v;					}
				});

				//Instantiates a new CircleOptions object +  center/radius
				CircleOptions circleOptions = new CircleOptions()
						.center(new LatLng(latx, lngx))
						.radius( radx )
//        		  .fillColor(0x40ff0000)
						.fillColor(0x40ff4e40)
						.strokeColor(Color.TRANSPARENT)
						.strokeWidth(2);

				// Get back the mutable Circle
				circle = mMap.addCircle(circleOptions);
				// more operations on the circle...
//        	moveMapToMyLocation();
			}
		}

	@Override
	public void onMapLongClick(LatLng arg0) {
		Intent data = new Intent();
		data.putExtra("lat", arg0.latitude);
		data.putExtra("lng", arg0.longitude);
		data.putExtra("radius",radx);
		setResult(4711,data);
		finish();
	}

	@Override
	public void onMapClick(LatLng arg0) {
		Intent data = new Intent();
		data.putExtra("lat", arg0.latitude);
		data.putExtra("lng", arg0.longitude);
		data.putExtra("radius",radx);
		setResult(4711,data);
		finish();
	}

	@Override
	public void onBackPressed() {
		Intent data = new Intent();
		data.putExtra("lat",latx);
		data.putExtra("lng",lngx);
		data.putExtra("radius", radx);
		setResult(RESULT_OK, data);

		super.onBackPressed();
	}


	@Override
	public boolean onMarkerClick(@NonNull Marker arg0) {

		return false;
	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		Intent data = new Intent();
		data.putExtra("lat",arg0.getPosition().latitude);
		data.putExtra("lng",arg0.getPosition().longitude);
		data.putExtra("radius",radx);
		setResult(4711,data);
		finish();
	}

	@Override
	public void onMarkerDrag(@NonNull Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		circle.remove();
		CircleOptions circleOptions = new CircleOptions()
				.center( new LatLng(marker.getPosition().latitude, marker.getPosition().longitude) )
				.radius( radx )
				.fillColor(0x40e667af)
				.strokeColor(Color.TRANSPARENT)
				.strokeWidth(2);

		// Get back the mutable Circle
		circle = mMap.addCircle(circleOptions);
		latx = marker.getPosition().latitude;
		lngx = marker.getPosition().longitude;
		this.markerInfoWindow.showInfoWindow();
	}

	private void changeRadius() {
		circle.remove();
		CircleOptions circleOptions = new CircleOptions()
				.center(new LatLng(latx, lngx))
				.radius(radx)
				.fillColor(0x40e667af)
				.strokeColor(Color.TRANSPARENT)
				.strokeWidth(2);

		// Get back the mutable Circle
		circle = mMap.addCircle(circleOptions);
	}

	@Override
	public void onMarkerDragStart(@NonNull Marker marker) {
	}



	/**
	 * Verify that Google Play services is available before making a request.
	 *
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {
		log.debug("servicesConnected");
		// Check that Google Play services is available
		GoogleApiAvailability api = GoogleApiAvailability.getInstance();
		int code = api.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == code) {
			// In debug mode, log the status
			Log.d(Constants.APPTAG, getString(R.string.play_services_available));
			log.info("servicesConnected result from Google Play Services: " + getString(R.string.play_services_available));
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else if (api.isUserResolvableError(code)){
			log.error("servicesConnected result: could not connect to Google Play services");
			api.showErrorDialogFragment(this, code, Constants.PLAY_SERVICES_RESOLUTION_REQUEST);
		} else {
			log.error("servicesConnected result: could not connect to Google Play services");
			Toast.makeText(this, api.getErrorString(code), Toast.LENGTH_LONG).show();
		}
		return false;
	}

	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		mMap = googleMap;
		setUpMapIfNeeded();
	}

	// An AsyncTask class for accessing the GeoCoding Web Service
	private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{

		@Override
		protected List<Address> doInBackground(String... locationName) {
			// Creating an instance of Geocoder class
			Geocoder geocoder = new Geocoder(getBaseContext());
			List<Address> addresses = null;

			try {
				// Getting a maximum of 3 Address that matches the input text
				addresses = geocoder.getFromLocationName(locationName[0], 3);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return addresses;
		}

		@Override
		protected void onPostExecute(List<Address> addresses) {

			// Clears all the existing markers on the map
			mMap.clear();

			if(addresses==null || addresses.size()==0){
				Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
			}else{
				// Adding Markers on Google Map for each matching address
				for(int i=0;i<addresses.size();i++){

					Address address = addresses.get(i);

					// Creating an instance of GeoPoint, to display in Google Map
					LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

					String addressText = String.format("%s, %s",
							address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
							address.getCountryName());

					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.position(latLng);
					markerOptions.title(addressText);

					mMap.addMarker(markerOptions);

					// Locate the first location
					if(i==0)
						mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_map, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);

		SearchView searchView = (SearchView) searchItem.getActionView();
//		SearchView mSearchView = (SearchView) searchItem.getActionView();
		searchView.setOnQueryTextListener(this);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		return true;
	}
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	public boolean onQueryTextSubmit(String query) {
		if(query!=null && !query.equals("")){
			new GeocoderTask().execute(query);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		int itemId = item.getItemId();
		if (itemId == R.id.menu_globe) {
			if (globe_state == 0 || globe_state == R.drawable.ic_language_white_24dp1) {
				globe_state = R.drawable.ic_language_black_24dp;
				item.setIcon(R.drawable.ic_language_black_24dp);
				mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			} else {
				globe_state = R.drawable.ic_language_white_24dp1;
				item.setIcon(R.drawable.ic_language_white_24dp1);
				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			}
			return true;
		} else if (itemId == R.id.menu_increase_radius) {
			modifyGeofenceRadius(RADIUS_INCREMENT);
			changeRadius();
			markerInfoWindow.showInfoWindow();
			return true;
		} else if (itemId == R.id.menu_decrease_radius) {
			modifyGeofenceRadius(-RADIUS_INCREMENT);
			changeRadius();
			markerInfoWindow.showInfoWindow();
			return true;
			// Pass through any other request
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * When using +/- radius buttons on map, increment/decrement by this percentage
	 */
	private void modifyGeofenceRadius(int delta) {
		radx = augmentRadius(radx, delta);
	}

	/**
	 * Increment or decrement a radius value by a percentage, while ensuring that the new
	 * value does not go below a minimum or above a maximum.
	 *
	 * @param radius original radius
	 * @param percentage to increment or decrement
	 * @return new radius value
	 */
	private Double augmentRadius(Double radius, int percentage) {
		Double change = (radius / 100) * percentage;
		double newRadius = radius + change;
		return Math.ceil(newRadius);
	}

}


















