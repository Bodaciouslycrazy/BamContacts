package com.example.bodie.bamcontacts;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *                                   CView Activity
 *
 * This activity manages the view of a single contact.
 * It allows you to create, edit, and delete things in a contact.
 * Has support for First and Last name, Middle name, Phone number,
 * Birthday and First Contact date.
 * Returns edited contact in setResult() with an intent.
 */
public class CView extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    //*************Constants used for request ids and response ids.******************
    static final int REQUEST_NEW_CONTACT = 0;
    static final int REQUEST_VIEW_CONTACT = 1;
    static final int REQUEST_EDIT_CONTACT = 2;
    static final int RESULT_CANCEL = 0;
    static final int RESULT_OK = 1;
    static final int RESULT_DELETED = 2;
    //*******************************************************************************

    //Error text will display error messages to user if they don't format their input right.
    TextView ErrorText;


    //**************************USER INPUTS******************************************
    protected EditText First;
    protected EditText Last;
    protected EditText Middle;
    protected EditText Phone;
    protected EditText Birthday;
    protected EditText ContactDay;
    //*******************************************************************************

    //Temp variables.
    protected View CurrentEditDate;
    protected Contact CurrentContact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cview);


        //find and set every editText and textView.
        ErrorText = findViewById(R.id.errorText);
        First = findViewById(R.id.firstEdt);
        Last = findViewById(R.id.lastEdt);
        Middle = findViewById(R.id.middleEdt);
        Phone = findViewById(R.id.phoneEdt);
        Birthday = findViewById(R.id.bdayEdt);
        ContactDay = findViewById(R.id.cdayEdt);


        //Add phone formatter. This will make the phone number look nice while you are typing it.
        Phone.addTextChangedListener( new PhoneNumberFormattingTextWatcher() );


        //check to see if they wanted to edit a specific contact.
        Intent intent = getIntent();
        Contact c = (Contact) intent.getSerializableExtra("contact");
        if(c != null)
        {
            CurrentContact = c;
            populateViews();
        }
        else
        {
            CurrentContact = new Contact();

            //add current date to contact date.
            Calendar today = Calendar.getInstance();
            CurrentEditDate = ContactDay;
            onDateSet(null, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        }

    }

    /**
     * populateViews()
     *
     * will fill in the editTexts based off the values in CurrentContact.
     */
    protected void populateViews()
    {
        if(CurrentContact != null)
        {
            //Required fields are always set
            First.setText( CurrentContact.getFirst() );
            Last.setText( CurrentContact.getLast() );
            Phone.setText( CurrentContact.getPhone() );

            //other fields may not always be set
            if(CurrentContact.hasMiddle())
                Middle.setText( CurrentContact.getMiddle() );

            StringBuilder sb = new StringBuilder();
            Calendar bday = CurrentContact.getBirthday();
            Calendar cday = CurrentContact.getContactDate();
            if(bday != null)
                Birthday.setText(calendarToString(bday));
            if(cday != null)
                ContactDay.setText(calendarToString(cday));
        }
    }

    /**
     * The onClick event for the save button.
     * Compiles all the editText values into the contact, and returns it
     * to the CList activity.
     *
     * @param v - View that called the event.
     */
    protected void onSave(View v)
    {
        if ( !checkForRequiredFields() )
            return;


        //if required fields are not there, set error.
        String f = First.getText().toString();
        String l = Last.getText().toString();
        String mid = Middle.getText().toString();
        String phoneWithFormat = Phone.getText().toString();
        String phone = "";
        try
        {
            phone = phoneWithFormat.replaceAll("[^0-9+]","");

            if( !PhoneNumberUtils.isGlobalPhoneNumber(phone) )
            {
                setErrorText(R.string.errPhone);
            }
        }
        catch(Exception e)
        {
            //that is not a valid phone number
            setErrorText(R.string.errPhone);

            //setErrorText( e.toString() );
            Log.e("berror", e.toString() );
            return;
        }

        //get bday and cday
        Calendar bday = parseDate(Birthday);
        Calendar cday = parseDate(ContactDay);

        //Set contact and return!
        CurrentContact.setFirst(f);
        CurrentContact.setLast(l);
        CurrentContact.setPhone(phone);
        if(mid.length() > 0)
            CurrentContact.setMiddle(mid.charAt(0));
        CurrentContact.setBirthday(bday);
        CurrentContact.setContactDate(cday);


        Log.d("SavedContact", CurrentContact.getWriteableString());

        Intent I = new Intent();
        I.putExtra("contact", CurrentContact);
        setResult(RESULT_OK, I);
        finish();
    }


    /**
     * checks to make sure something has been inserted into the required fields.
     * Will edit the Error Text textView.
     *
     * @return true if all required fields are met.
     */
    protected boolean checkForRequiredFields()
    {
        EditText[] required = {First, Last, Phone};
        int[] sids = { R.string.requiredFirst, R.string.requiredLast, R.string.requiredPhone};

        for(int i = 0; i < required.length; i++)
        {
            String s = required[i].getText().toString();
            if(s.isEmpty())
            {
                setErrorText(sids[i]);
                return false;
            }
        }

        return true;
    }

    /**
     * Takes and edit text, and turns it into a calendar object.
     *
     * @param et EditText containing some m/d/y text
     * @return Calendar object
     */
    protected Calendar parseDate(EditText et)
    {
        Calendar day = Calendar.getInstance();
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(Contact.DateFormat);
            String dayText = et.getText().toString();

            day.setTime( sdf.parse( et.getText().toString() ) );

        }
        catch(Exception e)
        {
            Log.e("parseDateError", e.toString());
            day = null;
        }

        return day;
    }

    /**
     * returns to the CList, indicating that the contact needs to be deleted.
     * @param v
     */
    protected void onDelete(View v)
    {
        Intent I = new Intent();
        I.putExtra("contact", CurrentContact);
        setResult(RESULT_DELETED, I);
        finish();
    }

    /**
     * onClick for the back button. Cancels the edit/insert.
     */
    @Override
    public void onBackPressed()
    {
        Intent I = new Intent();
        setResult(RESULT_CANCEL, I);
        finish();
    }

    /**
     * onClick for any date EditText. Pulls up a Calendar select dialogue
     * @param v
     */
    public void onClickDate(View v)
    {
        CurrentEditDate = v;
        EditText et = (EditText)v;
        String cur = et.getText().toString();
        int y = 0;
        int m = 0;
        int d = 0;

        //try parsing the string already in the edittext
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(Contact.DateFormat);
            Calendar c = Calendar.getInstance();
            c.setTime( sdf.parse(cur) );

            y = c.get(Calendar.YEAR);
            m = c.get(Calendar.MONTH);
            d = c.get(Calendar.DAY_OF_MONTH);
        }
        catch(Exception e)
        {
            //If we couldn't parse the string, just use the current date.
            Calendar now = Calendar.getInstance();
            d = now.get(Calendar.DAY_OF_MONTH);
            m = now.get(Calendar.MONTH);
            y = now.get(Calendar.YEAR);
        }


        DatePickerDialog dpd = new DatePickerDialog(this, this, y, m, d);
        dpd.show();
    }

    /**
     * called after the date picker is finished.
     *
     * @param dp ref to date picker
     * @param year
     * @param month
     * @param day
     */
    public void onDateSet(DatePicker dp, int year, int month, int day)
    {
        if(CurrentEditDate != null)
        {
            EditText et = (EditText)CurrentEditDate;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);
            et.setText( calendarToString(c) );
        }
    }

    /**
     * Sets the error text to parameter err
     * @param err
     */
    protected void setErrorText(String err)
    {
        ErrorText.setText(err);
    }

    /**
     * Sets the error text to the resource string with resId
     * @param resId
     */
    protected void setErrorText(int resId)
    {
        ErrorText.setText(resId);
    }


    /**
     * returns a formatted date string based of the parameter cld
     * @param cld
     * @return mm/dd/yyyy
     */
    protected static String calendarToString(Calendar cld)
    {
        if(cld == null)
            return null;

        SimpleDateFormat sdf = new SimpleDateFormat(Contact.DateFormat);

        String ret = sdf.format( cld.getTime() );
        Log.d("calendarToString", ret);
        return ret;
    }
}
