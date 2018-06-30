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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;

import org.apache.log4j.Logger;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.utils.Utils;

@SuppressWarnings("deprecation")
public class Profiles extends AppCompatActivity implements OnClickListener{
	
	private final Logger log = Logger.getLogger(Profiles.class);
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
		setContentView(R.layout.profiles);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
     * SMS-Einstellungen aufrufen
     *
     * @param view The view that triggered this callback
     */
    public void onSmsProfileClicked(View view) {
        log.debug("onSmsProfileClicked");
		Intent iSms = new Intent(this, SmsProfiles.class);
		startActivity(iSms);
    }

    /**
     * More-Einstellungen aufrufen
     *
     * @param view The view that triggered this callback
     */
    public void onMoreProfileClicked(View view) {
        log.debug("onMoreProfileClicked");
		Intent iMore = new Intent(this, MoreProfiles.class);
		startActivityForResult(iMore, 4711);

    }

    /**
     * Requiremets aufrufen
     *
     * @param view The view that triggered this callback
     */
    public void onConditionsProfileClicked(View view) {
        log.debug("onConditionsProfileClicked");
		Intent iCond = new Intent(this, RequirementsProfiles.class);
		startActivityForResult(iCond, 4712);
    }
}