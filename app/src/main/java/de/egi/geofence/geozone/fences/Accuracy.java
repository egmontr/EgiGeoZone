package de.egi.geofence.geozone.fences;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import de.egi.geofence.geozone.GlobalSingleton;
import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.db.ZoneEntity;
import de.egi.geofence.geozone.utils.Utils;

/**
 * Created by rittere on 02.11.2016.
 */

public class Accuracy extends AppCompatActivity implements TextWatcher {
    private ZoneEntity ze;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        setContentView(R.layout.accuracy);

        ze = GlobalSingleton.getInstance().getZoneEntity();

        EditText accuracy = this.findViewById(R.id.value_accuracy);
        ((EditText) this.findViewById(R.id.value_accuracy)).setText(String.valueOf(ze.getAccuracy() == null ? "0" : ze.getAccuracy()));
        accuracy.addTextChangedListener(this);
        accuracy.setSelection(accuracy.getText().length());

        Intent intent = new Intent();
        setResult(4713, intent);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (((EditText) this.findViewById(R.id.value_accuracy)).getText().toString().equals(""))
            return;
        int accuracy = Integer.parseInt(((EditText) this.findViewById(R.id.value_accuracy)).getText().toString());
        ze.setAccuracy(accuracy);
    }
}
