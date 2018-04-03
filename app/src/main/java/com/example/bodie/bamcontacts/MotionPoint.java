package com.example.bodie.bamcontacts;

import android.hardware.SensorEvent;

/**
 * Created by Bodie on 3/8/2018.
 */


public class MotionPoint {
    public float[] values;
    public long timestamp;

    public MotionPoint(SensorEvent event)
    {
        timestamp = event.timestamp;
        values = new float[3];
        for(int i = 0; i < 3; i++)
        {
            values[i] = event.values[i];
        }
    }

    public float magnitude()
    {
        return values[0];
    }

    //Deprecated
    public static float dotProduct( MotionPoint a, MotionPoint b)
    {
        float sum = 0;
        for(int i = 0; i < 3; i++)
        {
            sum += (a.values[i] * b.values[i]);
        }

        return sum;
    }

    public static float vectorDifferenceMagnitude(MotionPoint a, MotionPoint b)
    {
        return a.values[0] - b.values[0];
    }

    public static long milliDifference(MotionPoint a, MotionPoint b)
    {
        return (a.timestamp - b.timestamp) / 1000000;
    }
}
