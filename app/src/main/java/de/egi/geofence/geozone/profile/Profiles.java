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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.apache.log4j.Logger;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.utils.Utils;

public class Profiles extends AppCompatActivity implements OnClickListener{
	
	private final Logger log = Logger.getLogger(Profiles.class);
    Button buttonServerProfileClicked = null;
    Button buttonMailProfileClicked = null;
    Button buttonMoreProfileClicked = null;
    Button buttonConditionsProfileClicked = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        setContentView(R.layout.profiles);

        buttonServerProfileClicked = this.findViewById(R.id.button_onServerProfileClicked);
        buttonMailProfileClicked = this.findViewById(R.id.button_onMailProfileClicked);
        buttonMoreProfileClicked = this.findViewById(R.id.button_onMoreProfileClicked);
        buttonConditionsProfileClicked = this.findViewById(R.id.button_onConditionsProfileClicked);

        buttonServerProfileClicked.setOnClickListener(this);
        buttonMailProfileClicked.setOnClickListener(this);
        buttonMoreProfileClicked.setOnClickListener(this);
        buttonConditionsProfileClicked.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Utils.changeBackGroundToolbar(this, toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }
	@Override
	public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_onServerProfileClicked) {
            log.debug("onOptionsItemSelected: button_onServerProfileClicked");
            onServerProfileClicked(buttonServerProfileClicked);
        } else if (id == R.id.button_onMailProfileClicked) {
            log.debug("onOptionsItemSelected: button_onMailProfileClicked");
            onMailProfileClicked(buttonMailProfileClicked);
        } else if (id == R.id.button_onMoreProfileClicked) {
            log.debug("onOptionsItemSelected: button_onMoreProfileClicked");
            onMoreProfileClicked(buttonMoreProfileClicked);
        } else if (id == R.id.button_onConditionsProfileClicked) {
            log.debug("onOptionsItemSelected: button_onConditionsProfileClicked");
            onConditionsProfileClicked(buttonConditionsProfileClicked);
        }

    }
	
    /**
     * Server Einstellungen aufrufen
     *
     * @param view The view that triggered this callback
     */
    public void onServerProfileClicked(View view) {
        log.debug("onServerProfileClicked");
		Intent iServer = new Intent(this, ServerProfiles.class);
		startActivity(iServer);

    }

    /**
     * EMail-Einstellungen aufrufen
     *
     * @param view The view that triggered this callback
     */
    public void onMailProfileClicked(View view) {
        log.debug("onMailProfileClicked");
		Intent iMail = new Intent(this, MailProfiles.class);
		startActivity(iMail);

    }

    /**
     * More-Einstellungen aufrufen
     *
     * @param view The view that triggered this callback
     */
    public void onMoreProfileClicked(View view) {
        log.debug("onMoreProfileClicked");
		Intent iMore = new Intent(this, MoreProfiles.class);
		startActivity(iMore);

    }

    /**
     * Requiremets aufrufen
     *
     * @param view The view that triggered this callback
     */
    public void onConditionsProfileClicked(View view) {
        log.debug("onConditionsProfileClicked");
		Intent iCond = new Intent(this, RequirementsProfiles.class);
		startActivity(iCond);
    }
}