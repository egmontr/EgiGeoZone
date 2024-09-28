package de.egi.geofence.geozone.utils;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.egi.geofence.geozone.GlobalSingleton;
import de.egi.geofence.geozone.R;

public class NotificationErrorButtons  extends AppCompatActivity implements View.OnClickListener{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_error_buttons);

        TextView titel = this.findViewById(R.id.notification_titel);
        TextView text = this.findViewById(R.id.notification_text);
        Button b = this.findViewById(R.id.button);
        b.setOnClickListener(this);
        b.setText(getString(R.string.action_yes));
        Button b2 = this.findViewById(R.id.button2);
        b2.setOnClickListener(this);
        b2.setText(getString(R.string.action_no));

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