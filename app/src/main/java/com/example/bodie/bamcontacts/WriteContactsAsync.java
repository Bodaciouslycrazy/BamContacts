package com.example.bodie.bamcontacts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by Bodie on 3/8/2018.
 */

public class WriteContactsAsync extends AsyncTask<Contact, Integer, Boolean> {

    public File StorageFile;
    /**
     * Writes all contacts to external storage.
     * Does not use params, just feed an empty string.
     * @param ContactList
     * @return
     */
    @Override
    protected Boolean doInBackground(Contact[] ContactList)
    {
        //File StorageFile = params[0];
        if(StorageFile == null)
        {
            Log.e("berrer", "Can't write to a null file!");
        }

        //wait for storage to be available.
        while(!isStorageWriteable())
        {
            if(isCancelled())
            {
                return false;
            }
        }


        try
        {
            StorageFile.createNewFile();
            FileWriter fw = new FileWriter(StorageFile);

            for(int i = 0; i < ContactList.length; i++)
            {
                String next = ContactList[i].getWriteableString();
                fw.write(next + "\n");


                //check for cancellation
                if(isCancelled())
                {
                    fw.close();
                    return false;
                }
            }

            fw.close();

        }
        catch(Exception e)
        {
            Log.e("WriteError", e.toString());
            return false;
        }


        Log.w("bwarn", "finished writing.");
        return true;
    }


    @Override
    protected void onPostExecute(Boolean done)
    {
        //nothing needs to be done, file was written.
    }

    /**
     * returns true if external storage can be written to.
     * @return
     */
    public static boolean isStorageWriteable()
    {
        String state = Environment.getExternalStorageState();
        return(Environment.MEDIA_MOUNTED.equals(state));
    }
}
