package com.example.bodie.bamcontacts;

import android.app.Activity;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Switch;

import java.io.File;
import java.util.List;

//public class Options extends PreferenceActivity {
public class Options extends Activity {

    protected static final String filename = "contacts.txt";
    protected ReadContactsAsync importer;


    //I should probably use intents to get/return values to the list activity...


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_options);

        getFragmentManager().beginTransaction().replace( android.R.id.content , new BamPreferenceFragment()).commit();
    }


    /*
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragName)
    {
        return BamPreferenceFragment.class.getName().equals(fragName);
    }
    */
}
