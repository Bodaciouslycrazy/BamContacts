package com.example.bodie.bamcontacts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Bodie on 3/8/2018.
 */

public class ReadContactsAsync extends AsyncTask<File, Integer, ArrayList<Contact>> {

    public File StorageFile;
    ContactDB database;
    /**
     * reads contacts from external storage and puts them into the list
     * does not use the param. Just feed an empty string.
     * @param params
     * @return
     */
    @Override
    protected ArrayList<Contact> doInBackground(File[] params)
    {
        /*
        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        //if don't have storage permission, just die.
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e("berror", "Don't have read permission!!!");
            return null;
        }
        */

        StorageFile = params[0];
        if(StorageFile == null)
        {
            Log.e("berror","Can't read from a null file!");
        }


        //Lock while storage is not readable.
        while(!isStorageReadable());

        ArrayList<Contact> readList = new ArrayList<Contact>();
        int lines = 0;

        try
        {
            FileInputStream fis = new FileInputStream(StorageFile);
            BufferedReader in = new BufferedReader( new InputStreamReader( fis ));

            String data;

            while((data = in.readLine()) != null)
            {
                //do stuff
                Contact c = new Contact();
                c.readFromString(data);
                readList.add(c);
                lines++;
            }

            in.close();

        }
        catch(Exception e)
        {
            Log.e("berror", "File read error: " + e.toString());
            return null;
        }

        Log.w("berror", "Finished reading " + lines + " lines!");
        return readList;
    }


    /**
     * after contacts have been read, put them into the contact list
     * and update the adapter.
     *
     * UPDATED VERSION
     * Simply puts the contacts into given database.
     * @param contacts
     */
    @Override
    protected void onPostExecute(ArrayList<Contact> contacts)
    {
        //callback.loadNewContactList( contacts );

        if(contacts == null)
        {

            Log.e("berror","No file found.");
            return;
        }

        for(int i = 0; i < contacts.size(); i++)
        {
            database.AddContact( contacts.get(i));
        }
    }

    /**
     * returns true if external storage can be read from.
     * @return
     */
    public static boolean isStorageReadable()
    {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

}
