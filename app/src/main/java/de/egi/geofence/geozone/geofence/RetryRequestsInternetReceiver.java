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
package de.egi.geofence.geozone.geofence;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.legacy.content.WakefulBroadcastReceiver;

import org.apache.log4j.Logger;

public class RetryRequestsInternetReceiver extends WakefulBroadcastReceiver {
	private final Logger log = Logger.getLogger(RetryRequestsInternetReceiver.class.getSimpleName());

	@Override
	public void onReceive(Context context, Intent intent) {
		log.debug("onReceive");

		if (RetryRequestQueue.getAllPref(context).isEmpty()) return;

		// Explicitly specify that RetryRequestsInternetReceiverService will handle the intent.
		ComponentName comp = new ComponentName(context.getPackageName(), RetryRequestsInternetReceiverService.class.getName());
		// Start the service, keeping the device awake while it is launching.
		startWakefulService(context, (intent.setComponent(comp)));
	}
}



















