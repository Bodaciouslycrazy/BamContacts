package com.example.bodie.bamcontacts;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Bodie on 2/15/2018.
 * Holds all of the data for a contact, and provides some helper functions.
 */

//Implement comparable?
public class Contact implements Serializable, Comparable<Contact>{
    protected String _id;
    protected String First;
    protected String Last;
    protected char Middle;
    protected String Phone;
    protected Calendar Birthday;
    protected Calendar ContactDate;
    protected String Address1;
    protected String Address2;
    protected String City;
    protected String State;
    protected Integer Zipcode;

    public static final String DateFormat = "MM/dd/yyyy";

    //public static boolean AtoZ;
    //public static boolean SortByLast;

    public Contact()
    {

    }

    public Contact(String first, String last, String pnum)
    {
        First = first;
        Last = last;
        Phone = pnum;
    }

    //region GetFunctions

    public String getId(){
        return _id;
    }

    public String getFirst()
    {
        return First;
    }

    public String getLast()
    {
        return Last;
    }

    public String getMiddle()
    {
        return "" + Middle;
    }

    public boolean hasMiddle()
    {
        return Middle != 0;
    }

    /**
     * Returns a concatenation of the contacts first and last name.
     * @return
     */
    public String getFullName()
    {
        return getFullName(false);
    }

    /**
     * Returns a concatenation of the contact's names.
     * @param mid - true if you want middle name.
     * @return String
     */
    public String getFullName(boolean mid)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(First);

        if(mid && Middle != 0)
            sb.append(" ").append(Middle);

        sb.append(" ").append(Last);

        return sb.toString();
    }

    public String getPhone()
    {
        return Phone;
    }

    /**
     * Formats the phone number with dashes and stuff...
     */
    public String getFormattedPhone()
    {
        return formatPhone(Phone);
    }

    public Calendar getBirthday()
    {
        return Birthday;
    }

    public Calendar getContactDate()
    {
        return ContactDate;
    }

    public String getAddress1()
    {
        return Address1;
    }

    public String getAddress2()
    {
        return Address2;
    }

    public String getCity()
    {
        return City;
    }

    public String getState()
    {
        return State;
    }

    public Integer getZipcode()
    {
        return Zipcode;
    }

    /**
     * Returns a string that can be used for perminant storage.
     * @return
     */
    public String getWriteableString()
    {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);
        sdf.setTimeZone( Calendar.getInstance().getTimeZone() );
        sb.append(First).append("\t")
                .append(Last).append("\t")
                .append(Middle).append("\t")
                .append(Phone).append("\t");

        if(Birthday != null)
            sb.append( sdf.format(Birthday.getTime()) );
        else
            sb.append("nodate");

        sb.append("\t");

        if(ContactDate != null)
            sb.append( sdf.format(ContactDate.getTime()) );
        else
            sb.append("nodate");

        return sb.toString();
    }

    public ContentValues getContentValues()
    {
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);

        ContentValues cv = new ContentValues();
        cv.put("fname", First);
        cv.put("lname", Last);
        cv.put("middle", Middle + "");
        cv.put("phone", Phone);
        if(Birthday != null )
            cv.put("birthday", Birthday.getTimeInMillis());
        if(ContactDate != null)
            cv.put("first_contact", ContactDate.getTimeInMillis());
        cv.put("address1", Address1);
        cv.put("address2", Address2);
        cv.put("city", City);
        cv.put("state", State);
        if(Zipcode != null)
            cv.put("zip", Zipcode);

        return cv;
    }

    //endregion

    //region SetFunctions

    public void setId(String id)
    {
        _id = id;
    }

    public void setFirst(String first)
    {
        First = first;
    }

    public void setLast(String last)
    {
        Last = last;
    }

    public void setMiddle(char mid)
    {
        Middle = mid;
    }

    public void setPhone(String num)
    {
        Phone = num;
    }

    public void setBirthday(Calendar d)
    {
        Birthday = d;
    }

    public void setContactDate(Calendar d)
    {
        ContactDate = d;
    }

    public void setAddress1(String add)
    {
        Address1 = add;
    }

    public void setAddress2(String add)
    {
        Address2 = add;
    }

    public void setCity(String city)
    {
        City = city;
    }

    public void setState(String state)
    {
        State = state;
    }

    public void setZipcode(int zip)
    {
        Zipcode = zip;
    }

    /**
     * Reads data from cursor and puts it into the object.
     * Make sure to not move the cursor in this method.
     * @param c
     */
    public void readFromCursor(Cursor c)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);

        setId(c.getString(c.getColumnIndexOrThrow("_id")));
        setFirst(c.getString(c.getColumnIndexOrThrow("fname")));
        setLast(c.getString(c.getColumnIndexOrThrow("lname")));
        setMiddle(c.getString(c.getColumnIndexOrThrow("middle")).charAt(0));
        setPhone(c.getString(c.getColumnIndexOrThrow("phone")));
        long bmillis = c.getLong(c.getColumnIndexOrThrow("birthday"));
        long cmillis = c.getLong(c.getColumnIndexOrThrow("first_contact"));

        if(bmillis > 0)
        {
            Birthday = Calendar.getInstance();
            Birthday.setTime( new Date(bmillis));
        }

        if(cmillis > 0)
        {
            ContactDate = Calendar.getInstance();
            ContactDate.setTime( new Date(cmillis));
        }

        setAddress1(c.getString(c.getColumnIndexOrThrow("address1")));
        setAddress2(c.getString(c.getColumnIndexOrThrow("address2")));
        setCity(c.getString(c.getColumnIndexOrThrow("city")));
        setState(c.getString(c.getColumnIndexOrThrow("state")));
        setZipcode(c.getInt(c.getColumnIndexOrThrow("zip")));
    }

    /**
     * reads a string and populates all data based on it.
     * @param data
     */
    public void readFromString(String data)
    {
        Log.w("RFS", data);
        String[] split = data.split("\t");


        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);
        sdf.setTimeZone( Calendar.getInstance().getTimeZone() );

        try {
            for (int i = 0; i < split.length; i++) {
                //Log.w("RFS2", split[i]);
                switch (i) {
                    case 0:
                        First = split[i];
                        break;
                    case 1:
                        Last = split[i];
                        break;
                    case 2:
                        Middle = split[i].charAt(0);
                        break;
                    case 3:
                        Phone = split[i];
                        break;
                    case 4:
                        Birthday = stringToCalendar(split[i]);
                        break;
                    case 5:
                        ContactDate = stringToCalendar(split[i]);
                        break;
                    default:
                        Log.w("bwarn","Unknown data from file...");
                        break;
                }
            }
        }
        catch(Exception e)
        {
            Log.e("Read from File Error", e.toString());
        }
    }

    //endregion


    /**DEPRECATED
     *
     *
     * Used to sort contacts with each other.
     * @param other
     * @return
     */
    public int compareTo(Contact other)
    {
        /*
        int comp = 0;

        if(SortByLast)
        {
            comp = Last.compareToIgnoreCase(other.getLast());
        }
        else
        {
            comp = First.compareToIgnoreCase(other.getFirst());
        }

        if(comp == 0)
        {
            if(SortByLast)
            {
                comp = First.compareToIgnoreCase(other.getFirst());
            }
            else
            {
                comp = Last.compareToIgnoreCase(other.getLast());
            }
        }

        //retuns the negative version of the value when FirstToLast is false.
        return AtoZ ? comp : -1 * comp;
        */

        return 0;
    }

    //region StaticFunctions
    public static String formatPhone(String num)
    {
        if(num == null)
            return "";
        return PhoneNumberUtils.formatNumber(num);
    }

    public static Calendar stringToCalendar(String s)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);
        Calendar c = Calendar.getInstance();

        try
        {
            c.setTime( sdf.parse(s));
        }
        catch(Exception e)
        {
            c = null;
        }

        return c;
    }

    //endregion
}
