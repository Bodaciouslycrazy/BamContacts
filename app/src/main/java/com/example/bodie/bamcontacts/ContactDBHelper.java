package com.example.bodie.bamcontacts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Bodie on 3/29/2018.
 */

public class ContactDBHelper extends SQLiteOpenHelper {


    private static final String DBName = "ContactDB";
    private static final int DBVersion = 1;

    //This string will create the Contacts table.
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS Contacts (" +
            "_id INTEGER PRIMARY KEY, " +
            "fname TEXT NOT NULL, " +
            "lname TEXT NOT NULL, " +
            "middle CHAR, " +
            "phone TEXT NOT NULL, " +
            "birthday INTEGER, " +
            "first_contact INTEGER" +
            ");";

    public ContactDBHelper(Context ctx)
    {
        super(ctx, DBName, null, DBVersion);
    }


    /**
     * Creates the databases' Contacts table.
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * Used when upgrading from an older version of the app.
     * Currently does nothing.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    /**
     * When the database is opened, it makes sure that the table is created.
     * @param db
     */
    @Override
    public void onOpen(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
}
