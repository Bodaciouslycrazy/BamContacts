package com.example.bodie.bamcontacts;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;
import android.widget.ResourceCursorTreeAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Bodie on 4/9/2018.
 *
 * Downloads data from google api to reverse geocode.
 */

public class AsyncGeocode extends AsyncTask<MapAddress, Integer, LatLng> {

    protected static final String appURL = "https://maps.googleapis.com/maps/api/geocode/json?";
    protected static final String appURLend = "&sensor=true_or_false";
    public String apiKey = "";

    public LocationHandler handler;

    @Override
    public LatLng doInBackground(MapAddress[] args)
    {
        try
        {
            MapAddress address = args[0];

            if(address == null)
                return null;

            URL url = new URL( makeURLArg(address) );

            //Create HTTP connection
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            connect.setRequestMethod("GET");
            connect.setReadTimeout(15*1000);
            connect.connect();

            if(isCancelled()) return null;

            int responseCode = connect.getResponseCode();
            if(responseCode == 200)
            {
                //**********READ INPUT AS JSON**********
                InputStream inStream = connect.getInputStream();
                //InputStreamReader streamReader = new InputStreamReader(inStream, "UTF-8");

                JsonReader jread = new JsonReader( new InputStreamReader(inStream, "UTF-8"));
                LatLng value = readJSON(jread);

                return value;
            }
            else
            {
                Log.e("berror", "Bad response code.");
                return null;
            }
        }
        catch(Exception e)
        {
            Log.e("berror", e.toString());
            return null;
        }

    }

    @Override
    public void onPostExecute(LatLng location)
    {
        if(handler != null)
            handler.onLocationFound(location);
    }

    protected String makeURLArg(MapAddress add)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(appURL);
        sb.append("key=").append( apiKey );
        sb.append("&address=");

        String[] split = add.Address1.split(" ");
        for(int i = 0; i < split.length; i++)
        {
            if(i > 0)
                sb.append("+");
            sb.append(split[i]);
        }

        split = add.Address2.split(" ");
        for(int i = 0; i < split.length; i++)
        {
            sb.append("+").append(split[i]);
        }

        sb.append(",+").append(add.State);
        sb.append(appURLend);

        return sb.toString();
    }

    ///region JSON CRAP
    protected LatLng readJSON(JsonReader reader) throws Exception
    {
        ArrayList<LatLng> locations = new ArrayList<LatLng>();
        String em = "ERROR NOT FOUND";
        boolean error = false;

        reader.beginObject();
        while(reader.hasNext())
        {
            String name = reader.nextName();

            if(name.equals("results"))
            {
                locations = readResultArray( reader );
            }
            else if(name.equals("error_message"))
            {
                error = true;
                em = reader.nextString();
            }
            else
            {
                reader.skipValue();
            }
        }
        reader.endObject();
        reader.close();

        if(error)
        {
            Log.e("berror", em);
            //Toast.makeText(this, em, Toast.LENGTH_SHORT).show();
        }
        else
            Log.d("bebug", "Found " + locations.size() + " locations in json.");

        return locations.get(0);
    }

    protected ArrayList<LatLng> readResultArray(JsonReader reader) throws Exception
    {
        ArrayList<LatLng> locations = new ArrayList<LatLng>();

        reader.beginArray();
        while(reader.hasNext())
        {
            locations.add( readResult(reader) );
        }
        reader.endArray();

        return locations;
    }

    protected LatLng readResult(JsonReader reader) throws Exception
    {
        String formt = "";
        LatLng location = new LatLng(0,0);

        reader.beginObject();
        while(reader.hasNext())
        {
            String name = reader.nextName();

            if(name.equals("formatted_address"))
            {
                formt = reader.nextString();
            }
            else if(name.equals("geometry"))
            {
                location = readGeometry(reader);
            }
            else
                reader.skipValue();
        }
        reader.endObject();

        //location.address = formt;
        return location;
    }

    protected LatLng readGeometry(JsonReader reader) throws Exception
    {

        LatLng location = new LatLng(0,0);

        reader.beginObject();
        while(reader.hasNext())
        {
            String name = reader.nextName();

            if(name.equals("location"))
            {

                double lat = 0;
                double lng = 0;

                reader.beginObject();
                while(reader.hasNext())
                {
                    name = reader.nextName();

                    if(name.equals("lat"))
                    {
                        lat = reader.nextDouble();
                    }
                    else if(name.equals("lng"))
                    {
                        lng = reader.nextDouble();
                    }
                    else
                        reader.skipValue();
                }
                reader.endObject();
                location = new LatLng(lat, lng);
            }
            else
                reader.skipValue();
        }
        reader.endObject();

        return location;
    }

    ///endregion
}
