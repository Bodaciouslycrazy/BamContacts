package com.example.bodie.bamcontacts;

import java.io.Serializable;

/**
 * Created by Bodie on 4/9/2018.
 */

public class MapAddress implements Serializable{

    public String Address1;
    public String Address2;
    public String City;
    public String State;
    public int Zipcode;

    public MapAddress()
    {

    }

    public MapAddress(String line1, String line2, String city, String state, int zip)
    {
        Address1 = line1;
        Address2 = line2;
        City = city;
        State = state;
        Zipcode = zip;
    }
}
