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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.egi.geofence.geozone.utils.Utils;

public class GcmLog extends AppCompatActivity {
	public NestedScrollView scrollView;
	private final Logger log = Logger.getLogger(GcmLog.class);
	@SuppressLint({"SetTextI18n"})
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.egilog);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Utils.changeBackGroundToolbar(this, toolbar);

		TextView log = this.findViewById(R.id.value_log);
		scrollView = this.findViewById(R.id.value_scroll);

		File file = new File(getApplicationContext().getFilesDir()
				+ File.separator + "egigeozone" + File.separator
				+ "gcmnotifications.txt");

		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
				total.append("\n");
			}
			log.setText(total.toString());
		} catch (FileNotFoundException e) {
			log.setText("No file to display: \n\n" + e + "\n\n Try to delete old GCM file");
		} catch (IOException e) {
			log.setText("Can not open file: \" + e.toString()");
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
		if (item.getItemId() == R.id.menu_delete_gcm_log) {//            log.info("onOptionsItemSelected: menu_item_clear_geofence");
			AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(this);
//            AlertDialog.Builder ab = new AlertDialog.Builder(this, R.style.StyledDialog2);
			ab.setMessage(R.string.action_delete).setPositiveButton(R.string.action_yes, gcmDialogClickListener).setNegativeButton(R.string.action_no, gcmDialogClickListener).show();
			return true;
		}else if(item.getItemId() == R.id.menu_export_log) {
			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("text/*");
			intent.putExtra(Intent.EXTRA_TITLE, "gcmnotifications.txt");
			activityResultLaunchWriteExportFile.launch(intent);
			return true;
		} else if(item.getItemId() == R.id.menu_scroll_buttom_down) {
			scrollView.smoothScrollTo(0, 0);
			return true;
		} else if (item.getItemId() == R.id.menu_scroll_buttom_up) {
			scrollView.smoothScrollTo(0, scrollView.getChildAt(0).getHeight());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	ActivityResultLauncher<Intent> activityResultLaunchWriteExportFile = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					// Load private certificate and copy it to apps private space
					if (result.getResultCode() == RESULT_OK && result.getData() != null) {

						try (InputStream is = new FileInputStream(getFilesDir() + File.separator + "egigeozone" + File.separator + "gcmnotifications.txt"); OutputStream os = getContentResolver().openOutputStream(result.getData().getData())) {
							// InputStream constructor takes File, String (path), or FileDescriptor
							// data.getData() holds the URI of the path selected by the picker

							byte[] buffer = new byte[1024];
							int length;
							while ((length = is.read(buffer)) > 0) {
								os.write(buffer, 0, length);
							}
						} catch (IOException e) {
							log.error("Could not write Export file. Result: " + e.getMessage());
						}
						Toast.makeText(getApplicationContext(), R.string.export_ok_text, Toast.LENGTH_LONG).show();

						// Datei wurde gespeichert. Nur Dialog ausgeben
//						AlertDialog.Builder ab = Utils.onAlertDialogCreateSetTheme(getApplicationContext());
//						ab.setMessage(R.string.export_ok_text).setPositiveButton(R.string.action_ok, dialogClickListener).setTitle("Export").setIcon(R.drawable.ic_file_upload_black_24dp).show();
					}else{
						log.error("Could not write Export file. Result: " + result.getResultCode());
						Toast.makeText(getApplicationContext(), "Could not write Export file. Result: " + result.getResultCode(), Toast.LENGTH_LONG).show();
					}
				}
			});




	private final DialogInterface.OnClickListener gcmDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                //Do your Yes progress
            	File file = new File(getApplicationContext().getFilesDir()
        				+ File.separator + "egigeozone" + File.separator
						+ "gcmnotifications.txt");
        		try {
        			if (file.exists()) file.delete();
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