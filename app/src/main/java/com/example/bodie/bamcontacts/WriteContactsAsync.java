package com.example.bodie.bamcontacts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by Bodie on 3/8/2018.
 */

public class WriteContactsAsync extends AsyncTask<ContactDB, Integer, Boolean> {

    protected File StorageFile;
    protected ContactWriteListener listener;

    protected int status = 0;

    /**
     * Writes all contacts to external storage.
     * Give it a DB to query from.
     * @param DBs
     * @return
     */
    @Override
    protected Boolean doInBackground(ContactDB[] DBs)
    {
        ContactDB DB = DBs[0];

        if(StorageFile == null)
        {
            Log.e("berrer", "Can't write to a null file!");
            return false;
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

            Cursor cursor = DB.GetSortedList(true);

            while( cursor.moveToNext() )
            {
                Contact c = new Contact();
                c.readFromCursor( cursor );
                String next = c.getWriteableString();
                fw.write(next + "\n");

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
        status = 1;
        listener.onContactFileWrite(done);
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
