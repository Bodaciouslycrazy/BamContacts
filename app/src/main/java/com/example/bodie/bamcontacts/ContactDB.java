package com.example.bodie.bamcontacts;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;

/**
 * Created by Bodie on 3/29/2018.
 */

public class ContactDB {

    //private Context ctx;
    private ContactDBHelper dbHelper;
    private SQLiteDatabase Database;

    private SharedPreferences preferences;

    public boolean AtoZ = true;

    public ContactDB(Context context)
    {
        //ctx = context;

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        dbHelper = new ContactDBHelper(context);
        Database = dbHelper.getWritableDatabase();
    }

    /**
     * Creates a table entry out of a contact class.
     * @param c
     * @return true if the contact was added successfully.
     */
    public boolean AddContact( Contact c )
    {
        try
        {
            Database.insertOrThrow("Contacts", null, c.getContentValues() );
            return true;
        }
        catch(Exception e)
        {
            Log.e("berror", "DB Insertion Error: " + e.toString());
            return false;
        }
    }

    /**
     * Updates an already existing contact.
     * Make sure the contact you supply has an _id field.
     * @param c
     * @return true on success.
     */
    public boolean UpdateContact( Contact c )
    {
        try
        {
            String[] idArg = new String[1];
            idArg[0] = c.getId();

            Database.update("Contacts", c.getContentValues(), "_id = ?", idArg);
            return true;
        }
        catch(Exception e)
        {
            Log.e("berror", "DB Update error: " + e.toString());
            return false;
        }
    }

    /**
     * Removes a contact from the table.
     * Make sure the contact you supply has an _id field.
     * @param c
     * @return true on success.
     */
    public boolean RemoveContact( Contact c )
    {
        try
        {
            String[] idArg = new String[1];
            idArg[0] = c.getId();
            Database.delete("Contacts", "_id = ?", idArg);

            return true;
        }
        catch(Exception e)
        {
            Log.e("berror", "DB Deletion error: " + e.toString());
            return false;
        }
    }


    /**
     * Returns a Contact class based off an _id in the table.
     * The class will have all fields supplied.
     * @param id
     * @return Contact. null if contact not found.
     */
    public Contact GetContactById(String id)
    {
        String[] args = new String[1];
        args[0] = id;
        Cursor c = Database.query("Contacts", null, "_id = ?", args, null, null, null);

        if(c.moveToNext())
        {
            Contact cont = new Contact();
            cont.readFromCursor(c);
            return cont;
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the entire table sorted.
     * This cursor only has the fname, lname and phone columns.
     * @return Cursor.
     */
    public Cursor GetSortedList()
    {

        String[] cols = {"_id", "fname", "lname", "phone"};
        //String orderBy = Contact.SortByLast ? "lname" : "fname";

        //This gets the sort preference from shared preference. It has to add a space to it though.
        String orderBy = " " + preferences.getString("pref_sort", "fname");
        String direction = AtoZ ? " ASC" : " DESC";

        return Database.query("Contacts", cols, null, null, null, null, orderBy + direction, null);
        //return Database.rawQuery("SELECT * FROM Contacts ORDER BY " + orderBy + direction, null);
    }

    /**
     * Simply deletes every row in the table.
     * DOES NOT DELETE THE TABLE.
     * @return
     */
    public boolean ReInitialize()
    {
        Database.delete("Contacts", null, null);
        return true;
    }

    /*
    public boolean ImportFile()
    {


        return true;
    }
    */
}
