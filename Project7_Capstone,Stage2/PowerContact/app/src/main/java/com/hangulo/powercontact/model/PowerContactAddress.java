package com.hangulo.powercontact.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.hangulo.powercontact.Constants;

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
*    PowerContactAddress.java
*    -------------
*    Core ClusterItem for porcessing map's cluster
*
*/


public class PowerContactAddress implements ClusterItem, Parcelable {

    long contact_id; // personal contact id : 해당 사람의 id
    long data_id; // contact data id  해당 주소 data의 id
    String lookup_key; // look up key
    String name;
    String addr;
    int type;
    String label; // type of address (only for etc) 주소 종류(기타일 경우에만)
    double lat;
    double lng;
    double distance; // distance from current position  현재 위치로부터의 거리 (DB에서 그때그때 읽어옮)
    String photo; // photo uri

    public PowerContactAddress(long contact_id, long data_id, String lookup_key, String name, String addr, int type, String label, String photo, double lat, double lng, double dist) {
        this.contact_id = contact_id;
        this.data_id = data_id;
        this.lookup_key = lookup_key;
        this.name = name;
        this.addr = addr;
        this.type = type;
        this.label = label;
        if(photo !=null && photo.length() > 0)
            this.photo = photo;
        else
            this.photo="";
        this.lat = lat;
        this.lng = lng;

        this.distance= Math.acos(dist)* Constants.EARTH_RADIUS_METER; // = 6371000.0; // 미터 http://king24.tistory.com/4
    }


    // not used this version
    public void setType(int type, String addr) {
        this.addr = addr;
        this.type = type;

        switch (type) {
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME:
                this.label = "HOME"; // have to change
                break;
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK:
                this.label = "WORK"; // have to change
                break;
            default: // TYPE_OTHER
                this.label = "OTHER"; // some error
                break;
        }
    }

    // setter
    public void setType(int type) {
        this.type = type;
    }

    //--- getter
    public String getAddr() {
        return addr;
    }

    public String getPhoto() {
        return photo;
    }

    public String getLabel() {
        return label;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public LatLng getLatLng() {
        return new LatLng (lat, lng);
    }

    public int getType() {
        return type;
    }

    public long getContact_id() {
        return contact_id;
    }

    public long getData_id() {
        return data_id;
    }

    public String getName() {
        return name;
    }

    public String getLookup_key() {
        return lookup_key;
    }

    // implements ClusterItem
    @Override
    public LatLng getPosition() {
        return new LatLng(lat, lng);
    }

    public double getDistance() {
        return distance;
    }


    // implements PArcelable
    // parcelable ---------------------------
    private PowerContactAddress(Parcel in)
    {
        contact_id = in.readLong();
        data_id = in.readLong();; // contact data id  해당 주소 data의 id
        lookup_key=in.readString(); // look up key
        name=in.readString();
        addr=in.readString();
        type=in.readInt();
        label=in.readString(); ; // type of address (only for etc) 주소 종류(기타일 경우에만)
        lat=in.readDouble();
        lng =in.readDouble();
        distance =in.readDouble(); // distance from current position  현재 위치로부터의 거리 (DB에서 그때그때 읽어옮)
        photo=in.readString();; // photo uri
    }

    public int describeContents()
    {
        return 0;
    }


    public void writeToParcel(Parcel out, int flags)
    {
        out.writeLong(contact_id);
        out.writeLong(data_id);
        out.writeString(lookup_key);
        out.writeString(name);
        out.writeString(addr);
        out.writeInt(type);
        out.writeString(label);
        out.writeDouble(lat);
        out.writeDouble(lng);
        out.writeDouble(distance);
        out.writeString(photo);

    }
    // for receiver
    public static final Parcelable.Creator<PowerContactAddress> CREATOR =
            new Parcelable.Creator<PowerContactAddress>()
            {

                @Override
                public PowerContactAddress createFromParcel(Parcel in) {
                    return new PowerContactAddress(in);
                }

                @Override
                public PowerContactAddress[] newArray(int size) {
                    return new PowerContactAddress[size];
                }
            };

    @Override
    public String toString()
    {
        return contact_id + "," + data_id + "," + lookup_key +"," + name + "," +addr + "," + lat + ","+lng ;
    }


}
