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

package de.egi.geofence.geozone.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.db.DbContract;
import de.egi.geofence.geozone.db.DbMailHelper;
import de.egi.geofence.geozone.utils.Utils;

public class MailProfiles extends AppCompatActivity implements OnItemClickListener{
	private ListView list;
	private DbMailHelper datasource;
	private Cursor cursorMerk = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.profile_alle);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Utils.changeBackGroundToolbar(this, toolbar);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);
		}
		FloatingActionButton fab = findViewById(R.id.fab_profiles);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(MailProfiles.this, MailProfile.class);
				i.putExtra("action", "new");
				activityResultLaunch.launch(i); // 4711
			}
		});


		datasource = new DbMailHelper(this);
		
	    list = findViewById (R.id.list);
        registerForContextMenu(list);
        fillList();
	}
	
	private void fillList(){
		final Cursor cursor = datasource.getCursorAllMailSorted();
		cursorMerk = cursor;
		ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.profile_list_item, cursor,
				new String[]{DbContract.MailEntry.CN_NAME, DbContract.MailEntry.CN_TO},
				new int[]{R.id.profName, R.id.profWert}, 0) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final View row = super.getView(position, convertView, parent);
				if (position % 2 == 0)
					row.setBackgroundColor(Color.parseColor("#F7F7F7"));
				else
					row.setBackgroundColor(Color.parseColor("#E7E7E7"));
				return row;
			}
		};

		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
    	// Eintrag evtl. ändern
		cursorMerk.moveToPosition(position);
		String ind = cursorMerk.getString(cursorMerk.getColumnIndexOrThrow(DbContract.MailEntry._ID));
		
		Intent is = new Intent(this, MailProfile.class);
		is.putExtra("action", "update");
		is.putExtra("ind", ind);
		activityResultLaunch.launch(is); // 4711
	}

	ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
				@SuppressLint("SetTextI18n")
				@Override
				public void onActivityResult(ActivityResult result) {
					fillList();
				}
			});
}

















