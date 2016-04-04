package com.hangulo.powercontact.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.hangulo.powercontact.Constants;
import com.hangulo.powercontact.util.UnitLocale;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*   Created on : 2015-11-23
*
*    PowerContactSettings.java
*    -------------
*   class for Settings menu
*
*/


public class PowerContactSettings implements Parcelable {

    private double distance;  // last selected distance / mile 처리를 위해서 float로 바꿈
    private int markerType=0;
    private boolean demoMode ; //temp;
    private int distanceUnits; // unit 0:auto / 1:km / 2:mile
    private int realDistanceUnits; // unit 1 or 2 only

    // sort mode
    public PowerContactSettings () {
    }


    public double getDistance() {
        return distance;
    }

    public int getMarkerType() {
        return markerType;
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public int getDistanceUnits() {
        return distanceUnits;
    }

    public int getRealDistanceUnits() {
        return realDistanceUnits;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }


    public void setMarkerType(int markerType) {
        this.markerType = markerType;
    }

    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }


    public void setDistanceUnits(int distanceUnits) {
        this.distanceUnits = distanceUnits;
        setRealDistanceUnits(distanceUnits);
    }

    private void setRealDistanceUnits (int distanceUnits) {
        // set Real Distance Units
        if (distanceUnits != Constants.DISTANCE_UNITS_AUTO) // 여기에 더 엉뚱한 값이 나올때를 대비해야 한다. @어쩌고.. @TODO
            this.realDistanceUnits = distanceUnits;
        else {
            // else then find out
            if (UnitLocale.getDefault() == UnitLocale.Imperial)  // mile
                this.realDistanceUnits=  Constants.DISTANCE_UNITS_MILE;
            else  // meter
                this.realDistanceUnits= Constants.DISTANCE_UNITS_METER;
        }
    }

    // parcelable ---------------------------
    private PowerContactSettings(Parcel in)
    {

        markerType=in.readInt();
        distanceUnits = in.readInt();
        realDistanceUnits = in.readInt();
        demoMode = in.readByte() != 0;
        distance = in.readDouble();

    }

    public int describeContents()
    {
        return 0;
    }


    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(markerType);
        out.writeInt(distanceUnits);
        out.writeInt(realDistanceUnits);
        out.writeByte((byte) (demoMode ? 1 : 0));
        out.writeDouble(distance);

    }
    // for receiver
    public static final Parcelable.Creator<PowerContactSettings> CREATOR =
            new Parcelable.Creator<PowerContactSettings>()
            {

                @Override
                public PowerContactSettings createFromParcel(Parcel in) {
                    return new PowerContactSettings(in);
                }

                @Override
                public PowerContactSettings[] newArray(int size) {
                    return new PowerContactSettings[size];
                }
            };

    @Override
    public String toString()
    {
        return "PowerContactSettings"; // not used this version, so make simple
    }
}