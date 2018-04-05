package com.example.bodie.bamcontacts;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class CList extends AppCompatActivity implements AdapterView.OnItemClickListener, ShakeHandler, ContactWriteListener, ContactReadListener{

    protected ListView LV;
    protected ContactCursorAdapter Adapter;

    protected static final String filename = "contacts.txt";

    //shake listener
    protected ShakeListener SListener;
    protected SensorManager SManager;

    //Database vars
    public static ContactDB DB;

    //Preferences
    protected SharedPreferences preferences;


    //OldFile readers and writers
    WriteContactsAsync oldWrite;
    ReadContactsAsync oldRead;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clist);
        setSupportActionBar( (Toolbar) findViewById(R.id.toolbar));

        //***************SETUP******************
        DB = new ContactDB(this);


        //ShakeDebugConfirm = findViewById(R.id.shakeDebugConfirm);
        //ShakeDebugUpdate = findViewById(R.id.shakeDebugUpdate);

        LV = findViewById(R.id.mainList);
        Adapter = new ContactCursorAdapter(this, null);
        LV.setAdapter( Adapter );
        LV.setOnItemClickListener(this);


        SManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        SListener = new ShakeListener(this);

        //*************************************
        //Old code from reading data from file.
        //READ FILE
        //Find the file where contacts are stored.
        /*
        File dir = getExternalFilesDir(null);
        if(!dir.mkdirs())
        {
            Log.w("berror","DIDN'T MAKE DIRECTORY!!!");
        }

        StorageFile = new File(dir.getAbsolutePath(), filename);
        //Log.w("bwarn", StorageFile.getAbsolutePath());
        ReadContactsAsync read = new ReadContactsAsync();
        read.callback = this; //give the async task a reference so it can call a method when done.
        read.execute(StorageFile);
        */

        //Set up Database
    }

    ///region OptionsMenu
    /**
     * Creates the options menu in the app bar.
     * Option items are defined in menu_main.xml
     * @param m
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu m)
    {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.menu_main, m);
        return true;
    }


    /**
     * This gets a callback whenever an option item has been clicked.
     * Use these for adding new contacts and opening menu activity.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        Log.w("bwarn", "Menu Item has been selected: " + item.toString() );

        switch(item.getItemId())
        {
            case R.id.menuAddContact:
                MenuAddContact();
                break;
            case R.id.menuSettings:
                MenuOpenSettings();
                break;
            case R.id.menuImport:
                MenuImport();
                break;
            case R.id.menuExport:
                MenuExport();
                break;
            case R.id.menuReinitialize:
                MenuReinitialize();
                break;
        }

        return true;
    }

    /**
     * Menu event for adding a new contact.
     */
    public void MenuAddContact()
    {
        Intent I = new Intent(this, CView.class);
        startActivityForResult(I, CView.REQUEST_NEW_CONTACT);
    }

    /**
     * Menu event for opening the options activity.
     */
    public void MenuOpenSettings()
    {
        Intent I = new Intent( this, Options.class);
        //startActivityForResult(I, 30);
        startActivity(I);

    }

    /**
     * Menu event for importing contacts from the old file system.
     */
    public void MenuImport()
    {
        if(oldRead == null) {
            oldRead = new ReadContactsAsync();
            oldRead.listener = this;
            oldRead.StorageFile = getOldStorageFile();
            oldRead.execute( DB );
        }
        else
        {
            //Already reading, hoooold on!
        }
        //Toast.makeText(this, "MenuImport clicked.", Toast.LENGTH_SHORT).show();
    }

    public void MenuExport()
    {
        if(oldWrite == null)
        {
            oldWrite = new WriteContactsAsync();
            oldWrite.listener = this;
            oldWrite.StorageFile = getOldStorageFile();
            oldWrite.execute( DB );
        }
        else
        {
            //Already writing! Hold ooooon!
        }
    }

    /**
     * Menu event for clearing the database.
     */
    public void MenuReinitialize()
    {
        DB.ReInitialize();
        Adapter.changeCursor(DB.GetSortedList());
        Adapter.notifyDataSetChanged();
        Toast.makeText(this, "Contacts cleared.", Toast.LENGTH_SHORT).show();
    }
    ///endregion





    @Override
    public void onResume()
    {
        super.onResume();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if( preferences.getBoolean("pref_shake", true) )
        {
            Sensor s = SManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            SManager.registerListener(SListener, s, SensorManager.SENSOR_DELAY_NORMAL);
        }

        Adapter.changeCursor( DB.GetSortedList() );
        Adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        SManager.unregisterListener(SListener);
        super.onPause();
    }

    /**
     * Recieves the result from CView activity.
     * @param reqCode the request code you sent to the activity
     * @param resCode the response code it returned to you
     * @param data Intent data, which includes the contact.
     */
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data)
    {
        //do nothing if they pressed the back button.
        if(resCode == CView.RESULT_CANCEL)
            return;

        Contact returnedContact = (Contact)data.getSerializableExtra("contact");

        if(reqCode == CView.REQUEST_NEW_CONTACT)
        {
            if(resCode == CView.RESULT_OK)
                DB.AddContact( returnedContact );
        }
        else if(reqCode == CView.REQUEST_EDIT_CONTACT)
        {
            if(resCode == CView.RESULT_OK)
            {
                DB.UpdateContact( returnedContact );
            }
            else if(resCode == CView.RESULT_DELETED)
            {
                DB.RemoveContact( returnedContact );
            }
        }


        /*
        Adapter.notifyDataSetChanged();

        if(WriteAsync != null)
            WriteAsync.cancel(false);


        WriteAsync = new WriteContactsAsync();
        WriteAsync.StorageFile = StorageFile;
        Object[] uncastedList = ContactList.toArray();
        Contact[] castedList = new Contact[uncastedList.length];
        for(int i = 0; i < castedList.length; i++)
        {
            castedList[i] = (Contact)uncastedList[i];
        }
        WriteAsync.execute(castedList);
        */
    }



    /**
     * Click handler for list items.
     * @param apt which adapter was interacted with
     * @param v which view was clicked
     * @param pos position of contact in array
     * @param id I don't think this needs to be used....
     */
    @Override
    public void onItemClick(AdapterView<?> apt, View v, int pos, long id)
    {
        ContactViewHolder holder = (ContactViewHolder)v.getTag();
        Contact c = DB.GetContactById(holder._id);

        if(c != null)
        {
            Intent I = new Intent(this, CView.class);
            I.putExtra("contact", c);
            startActivityForResult(I, CView.REQUEST_EDIT_CONTACT);
        }
        else
        {
            Log.e("berror", "Could not find contact.");
        }
    }



    /**
     * Implemented from ShakeHandeler
     * Receives the onShake call from Shake listener
     * Switches the sorting style, and then notifies adapter of change.
     */
    public void onShake()
    {
        DB.AtoZ = !DB.AtoZ;
        Adapter.changeCursor( DB.GetSortedList() );
        Toast.makeText(this, "Contacts reversed.", Toast.LENGTH_SHORT).show();
    }


    ///region Old File Interface

    public void onContactFileRead(boolean ok)
    {
        oldRead = null;
        Adapter.changeCursor(DB.GetSortedList());
        Adapter.notifyDataSetChanged();

        getOldStorageFile().delete();

        String readText = ok ? "Read contacts from old file." : "Text file not found.";
        Toast.makeText(this, readText, Toast.LENGTH_SHORT).show();
    }

    public void onContactFileWrite(boolean ok)
    {
        oldWrite = null;

        String writeText = ok ? "Finished writing to old file!" : "There was an error writing to old file.";
        Toast.makeText(this, writeText, Toast.LENGTH_SHORT).show();
    }

    ///endregion

    ///region DEPRECATED METHODS

    protected File getOldStorageFile()
    {
        File dir = getExternalFilesDir(null);
        if(!dir.mkdirs())
        {
            Log.w("berror","DIDN'T MAKE DIRECTORY!!!");
        }

        File StorageFile = new File(dir.getAbsolutePath(), filename);

        if(StorageFile == null)
        {
            Log.e("berror", "Could not find OldStorageFile!");
        }

        return StorageFile;
    }


    /**DEPRECATED
     *
     *
     * Sorts the contact lists in the given order.
     *
     * Does NOT notify the adapter that the list changed.
     * You have to notify it yourself.
     *
     * @param AtoZ - if true, sort AtoZ. Otherwize, sort ZtoA.
     */
    protected void SortContacts(boolean AtoZ)
    {
        //Contact.AtoZ = AtoZ;
        //Collections.sort(ContactList);
        //Adapter.notifyDataSetChanged();
    }


    /**DEPRECATED
     *
     *
     * Shorthand for sorting the list with the current direction.
     */
    public void SortContacts()
    {
        //SortContacts(Contact.AtoZ);
    }


    /**
     * DEPRECATED
     *
     * Loads a list of contacts into the activity.
     * Used as a callback for ReadContactsAsync
     * @param newList
     */
    public void loadNewContactList(ArrayList<Contact> newList)
    {
        //ContactList.clear();
        //ContactList.addAll(newList);
        SortContacts(true);
        Adapter.notifyDataSetChanged();
    }
    ///endregion

}
