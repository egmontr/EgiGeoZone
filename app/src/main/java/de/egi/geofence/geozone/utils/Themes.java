package de.egi.geofence.geozone.utils;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioGroup;

import org.apache.log4j.Logger;

import de.egi.geofence.geozone.R;
import de.egi.geofence.geozone.db.DbGlobalsHelper;

/**
 * Created by rittere on 18.10.2016.
 */

public class Themes extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{
    private final Logger log = Logger.getLogger(Themes.class);
    private DbGlobalsHelper dbGlobalsHelper;
    private boolean check = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbGlobalsHelper = new DbGlobalsHelper(this);

        Utils.onActivityCreateSetTheme(this);

        setContentView(R.layout.themes);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Utils.changeBackGroundToolbar(this, toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_themes);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save Theme to Globals
                dbGlobalsHelper.storeGlobals(Constants.DB_KEY_THEME, Integer.toString(Utils.getThemeInd()));
                finish();;
            }
        });

        ((RadioGroup)findViewById(R.id.radioGroupThemes)).setOnCheckedChangeListener(this);

        switch (Utils.getThemeInd()) {
            case 0:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme0);
                break;
            case 1:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme1);
                break;
            case 2:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme2);
                break;
            case 3:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme3);
                break;
            case 4:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme4);
                break;
            case 5:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme5);
                break;
            case 6:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme6);
                break;
            case 7:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme7);
                break;
            case 8:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme8);
                break;
            case 9:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme9);
                break;
            case 10:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme10);
                break;
            case 11:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme11);
                break;
            case 12:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme12);
                break;
            case 13:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme13);
                break;
            case 14:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme14);
                break;
            case 15:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme15);
                break;
            case 16:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme16);
                break;
            case 17:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme17);
                break;
            case 18:
                ((RadioGroup) this.findViewById(R.id.radioGroupThemes)).check(R.id.radioButtonTheme16);
                break;
        }

        check = true;

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (!check) return;
        int themeInd = 0;
        if (group.getId() == R.id.radioGroupThemes) {
            switch (checkedId) {
                case R.id.radioButtonTheme0:
                    Utils.changeToTheme(this, 0);
                    themeInd = 0;
                    break;
                case R.id.radioButtonTheme1:
                    Utils.changeToTheme(this, 1);
                    themeInd = 1;
                    break;
                case R.id.radioButtonTheme2:
                    Utils.changeToTheme(this, 2);
                    themeInd = 2;
                    break;
                case R.id.radioButtonTheme3:
                    Utils.changeToTheme(this, 3);
                    themeInd = 3;
                    break;
                case R.id.radioButtonTheme4:
                    Utils.changeToTheme(this, 4);
                    themeInd = 4;
                    break;
                case R.id.radioButtonTheme5:
                    Utils.changeToTheme(this, 5);
                    themeInd = 5;
                    break;
                case R.id.radioButtonTheme6:
                    Utils.changeToTheme(this, 6);
                    themeInd = 6;
                    break;
                case R.id.radioButtonTheme7:
                    Utils.changeToTheme(this, 7);
                    themeInd = 7;
                    break;
                case R.id.radioButtonTheme8:
                    Utils.changeToTheme(this, 8);
                    themeInd = 8;
                    break;
                case R.id.radioButtonTheme9:
                    Utils.changeToTheme(this, 9);
                    themeInd = 9;
                    break;
                case R.id.radioButtonTheme10:
                    Utils.changeToTheme(this, 10);
                    themeInd = 10;
                    break;
                case R.id.radioButtonTheme11:
                    Utils.changeToTheme(this, 11);
                    themeInd = 11;
                    break;
                case R.id.radioButtonTheme12:
                    Utils.changeToTheme(this, 12);
                    themeInd = 12;
                    break;
                case R.id.radioButtonTheme13:
                    Utils.changeToTheme(this, 13);
                    themeInd = 13;
                    break;
                case R.id.radioButtonTheme14:
                    Utils.changeToTheme(this, 14);
                    themeInd = 14;
                    break;
                case R.id.radioButtonTheme15:
                    Utils.changeToTheme(this, 15);
                    themeInd = 15;
                    break;
                case R.id.radioButtonTheme16:
                    Utils.changeToTheme(this, 16);
                    themeInd = 16;
                    break;
                case R.id.radioButtonTheme17:
                    Utils.changeToTheme(this, 17);
                    themeInd = 17;
                    break;
                case R.id.radioButtonTheme18:
                    Utils.changeToTheme(this, 18);
                    themeInd = 18;
                    break;
            }
        }
        dbGlobalsHelper.storeGlobals(Constants.DB_KEY_THEME, Integer.toString(themeInd));
    }
}
