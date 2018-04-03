package com.example.bodie.bamcontacts;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Bodie on 2/18/2018.
 *
 * Used the contact_list_item layout to put them in a list.
 * Has it's own contact list.
 */

public class ContactArrayAdapter extends ArrayAdapter<Contact>{
    private final Context ctx;
    public ArrayList<Contact> contacts;

    /**
     *
     * @param c context (use the activity)
     * @param clist ArrayList of Contacts
     */
    public ContactArrayAdapter(Context c, ArrayList<Contact> clist)
    {
        super(c, R.layout.contact_list_item, clist);

        ctx = c;
        contacts = clist;
    }


    /**
     * Populates a view with data from a contact.
     *
     * @param position index in contacts array
     * @param v re-used view. Can be null.
     * @param parent Parent that the view should be a child of.
     * @return
     */
    @Override
    public View getView(int position, View v, ViewGroup parent)
    {
        //use viewholder so that we can access TextViews easily
        ContactViewHolder viewHolder;

        if(v == null) //View doesn't exist, create new one
        {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.contact_list_item, parent, false);
            viewHolder = new ContactViewHolder();

            viewHolder.Name = v.findViewById(R.id.name);
            viewHolder.Phone = v.findViewById(R.id.number);

            v.setTag(viewHolder);
        }
        else //view already made, repopulate it with new data.
        {
            viewHolder = (ContactViewHolder) v.getTag();
        }

        //populate textviews
        viewHolder.Name.setText(  contacts.get(position).getFullName() );
        viewHolder.Phone.setText( contacts.get(position).getFormattedPhone() );

        return v;
    }

}
