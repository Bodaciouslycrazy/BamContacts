package com.example.bodie.bamcontacts;

import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.ArrayList;

/**
 * Created by Bodie on 3/8/2018.
 *
 * Reads from a sensor, and calls onShake of the handeler when
 * there is a significant ammount of shaking.
 * Values may need to be tweaked.
 *
 * COOLDOWN makes it so that onShake can't be called within a certain time period
 * of last detected shake.
 */
public class ShakeListener implements SensorEventListener {

    //constants
    final float DIFFERENCE_THRESHOLD = 33f;
    final int TIME = 500; //measured in millis
    final int COOLDOWN = 1000; //measured in millis.

    protected ShakeHandler Handeler;

    protected long LastRecordedShake = 0;
    ArrayList<MotionPoint> RecentEvents = new ArrayList<>();

    public ShakeListener(ShakeHandler handeler)
    {
        Handeler = handeler;
    }

    @Override
    public void onAccuracyChanged(Sensor s, int amm)
    {
        //DO NOTHING! :D
    }

    /**
     * Reads inputs from the accelerometer.
     * If there is significant enough changes on the X axis, it will call onShake
     * Has a cooldown so that onShake is not called many times in a row.
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        RecentEvents.add( new MotionPoint(event) );


        float totalDifference = 0;
        for(int i = 0; i < RecentEvents.size(); i++)
        {
            long dt = MotionPoint.milliDifference(RecentEvents.get(RecentEvents.size() - 1), RecentEvents.get(i));

            if(dt > TIME)
            {
                RecentEvents.remove(i);
                i--;
            }
            else if(i > 0)
            {
                totalDifference += MotionPoint.vectorDifferenceMagnitude(RecentEvents.get(i), RecentEvents.get(i-1));
            }
        }

        long shakeTime = System.currentTimeMillis() - LastRecordedShake;

        if( shakeTime > COOLDOWN && totalDifference > DIFFERENCE_THRESHOLD)
        {
            LastRecordedShake = System.currentTimeMillis();
            Handeler.onShake();
        }

    }
}
