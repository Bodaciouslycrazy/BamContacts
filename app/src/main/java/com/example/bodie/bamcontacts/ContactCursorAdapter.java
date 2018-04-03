package com.example.bodie.bamcontacts;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;

/**
 * Created by Bodie on 4/1/2018.
 */

public class ContactCursorAdapter extends ResourceCursorAdapter {
    // final Context ctx;
    //private Cursor cursor;

    public ContactCursorAdapter(Context context, Cursor curs)
    {
        super(context, R.layout.contact_list_item, curs, 0);

        //ctx = context;
        //cursor = curs;
    }

    @Override
    public View newView(Context context, Cursor curs, ViewGroup parent)
    {
        View v = LayoutInflater.from(context).inflate(R.layout.contact_list_item, parent, false);

        //use viewholder
        ContactViewHolder viewHolder = new ContactViewHolder();
        viewHolder.Name = v.findViewById(R.id.name);
        viewHolder.Phone = v.findViewById(R.id.number);

        v.setTag(viewHolder);

        return v;
    }

    @Override
    public void bindView(View v, Context context, Cursor curs)
    {
        ContactViewHolder holder = (ContactViewHolder)v.getTag();
        holder._id = curs.getString(curs.getColumnIndexOrThrow("_id"));

        String name = curs.getString(curs.getColumnIndexOrThrow("fname")) + " " + curs.getString(curs.getColumnIndexOrThrow("lname"));
        String phone = Contact.formatPhone( curs.getString(curs.getColumnIndexOrThrow("phone")) );

        holder.Name.setText( name );
        holder.Phone.setText(phone);
    }

    //REMEMBER TO USE CHANGE CURSOR
}
