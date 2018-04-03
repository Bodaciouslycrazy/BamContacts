package com.example.bodie.bamcontacts;

import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Switch;

import java.io.File;

public class Options extends AppCompatActivity {

    protected Switch opShake;
    protected Spinner opSort;

    protected static final String filename = "contacts.txt";
    protected ReadContactsAsync importer;


    //I should probably use intents to get/return values to the list activity...


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);




        opShake = findViewById(R.id.opShake);
        //opSort = findViewById(R.id.opSort);

        opShake.setChecked(CList.ShakeAllowed);
    }

    public void OnShakeChange(View v)
    {
        CList.ShakeAllowed = opShake.isChecked();
    }

    public void OnSortMethodChange(View v)
    {

    }

    /**
     * Imports contacts from the old text file.
     * Uses my old ReadContactsAsync class
     * @param v
     */
    public void OnImport(View v)
    {
        if(importer == null)
        {
            importer = new ReadContactsAsync();
            importer.database = CList.DB;

            File dir = getExternalFilesDir(null);
            if(!dir.mkdirs())
            {
                Log.w("berror","DIDN'T MAKE DIRECTORY!!!");
            }

            File StorageFile = new File(dir.getAbsolutePath(), filename);
            //Log.w("bwarn", StorageFile.getAbsolutePath());
            importer.execute(StorageFile);
        }
    }

    /**
     * Tells the database to reinitialize.
     * @param v
     */
    public void OnReinitialize(View v)
    {
        CList.DB.ReInitialize();
    }
}
