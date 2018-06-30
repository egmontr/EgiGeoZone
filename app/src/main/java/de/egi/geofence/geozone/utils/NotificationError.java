package de.egi.geofence.geozone.utils;
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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.egi.geofence.geozone.GlobalSingleton;
import de.egi.geofence.geozone.R;

public class NotificationError extends AppCompatActivity implements View.OnClickListener{
    private Button b = null;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_error);

        TextView titel = ((TextView) this.findViewById(R.id.notification_titel));
        TextView text = ((TextView) this.findViewById(R.id.notification_text));
        b = (Button) this.findViewById(R.id.button);
        b.setOnClickListener(this);

        titel.setText(GlobalSingleton.getInstance().getNotificationTitel());
        text.setText(GlobalSingleton.getInstance().getNotificationText());
    }

    @Override
    public void onClick(View view) {
        // Cancel error notification
        NotificationUtil.cancelNotification(this, 1);
        finish();
    }
}