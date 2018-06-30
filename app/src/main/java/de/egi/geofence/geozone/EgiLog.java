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
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import de.egi.geofence.geozone.utils.Utils;

@SuppressWarnings("deprecation")
public class EgiLog extends AppCompatActivity {
	@SuppressLint("SetTextI18n")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Utils.onActivityCreateSetTheme(this);

		setContentView(R.layout.egilog);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Utils.changeBackGroundToolbar(this, toolbar);

		// http://androidopentutorials.com/android-listview-fastscroll/
		TextView log = ((TextView) this.findViewById(R.id.value_log));
		File file = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "egigeozone" + File.separator
				+ "egigeozone.log");
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(file));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
				total.append("\n");
			}
			log.setText(total.toString());
		} catch (FileNotFoundException e) {
			log.setText("No file to display.");
		} catch (IOException e) {
			log.setText("Can not open file.");
		} finally {
			try {
				assert r != null;
				r.close();
			} catch (Exception ignored) {
			}
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        log.info("onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        log.info("onOptionsItemSelected");
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.menu_delete_gcm_log:
//            log.info("onOptionsItemSelected: menu_item_clear_geofence");
			AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
		    ab.setMessage(R.string.action_delete).setPositiveButton(R.string.action_yes, dialogClickListener).setNegativeButton(R.string.action_no, dialogClickListener).show();
            return true;
        // Pass through any other request
        default:
            return super.onOptionsItemSelected(item);
        }
    }

//	@Override
//	public void onClick(View v) {
//	    AlertDialog.Builder ab = new AlertDialog.Builder(this);
//	    ab.setMessage(R.string.action_delete).setPositiveButton(R.string.action_yes, dialogClickListener).setNegativeButton(R.string.action_no, dialogClickListener).show();
//	}
	
    private final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                //Do your Yes progress
            	File file = new File(Environment.getExternalStorageDirectory()
        				+ File.separator + "egigeozone" + File.separator
        				+ "egigeozone.log");
        		try {
        			if (file.exists()) file.delete();
					try{
						MainEgiGeoZone.logConfigurator.configure();
					} catch (Exception e) {
						// Nichts tun. Manchmal kann auf den Speicher nicht zugegriffen werden.
					}
        			finish();
        		} catch (Exception ignored) {
        		}
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                //Do your No progress
                break;
            }
        }
    };

}