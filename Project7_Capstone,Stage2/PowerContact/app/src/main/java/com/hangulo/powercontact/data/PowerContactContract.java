package com.hangulo.powercontact.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.hangulo.powercontact.Constants;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung
*   ================================================
*
*   date : Apr. 4th 2016
*   Created on : 2015-07-02
*
*     Kwanghyun JUNG
*     ihangulo@gmail.com
*
*    Android Devlelopment Nanodegree
*    Udacity
*
*    PowerContactContract.java
*
*
*/

public class PowerContactContract {

    public static final String CONTENT_AUTHORITY = "com.hangulo.android.powercontact.apps";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_POWERCONTACT = "powercontact";
    public static final String PATH_DISTANCE="distance";
    public static final String PATH_WIDGET="widget";



    /* Inner class that defines the table contents of the weather table */
    public static final class PowerContactEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POWERCONTACT).build();


        // CURSOR_DIR_BASE_TYPE = "vnd.android.cursor.dir"; //안드로이드 기본 설정값
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POWERCONTACT;

        // CURSOR_ITEM_BASE_TYPE = "vnd.android.cursor.item" // 안드로이드 기본 설정값
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POWERCONTACT;

        // 테이블 정의
        public static final String TABLE_NAME = "hangulo_power_contact";

        // 컬럼 정의
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DATA_ID = "data_id"; // data 고유의 id
        public static final String COLUMN_NAME = "name"; // 이름
        public static final String COLUMN_CONTACT_ID = ContactsContract.RawContacts.CONTACT_ID; // "contact_id"
        public static final String COLUMN_LOOKUP_KEY = ContactsContract.Contacts.LOOKUP_KEY; // "lookup"

        public static final String COLUMN_ADDR = "addr"; // 주소
        public static final String COLUMN_TYPE = "type"; // 주소타입
        public static final String COLUMN_LABEL = "label"; // 주소라벨
        public static final String COLUMN_PHOTO = ContactsContract.Contacts.PHOTO_THUMBNAIL_URI; // 포토 썸네일
        public static final String COLUMN_LAT = "lat"; //위도
        public static final String COLUMN_LNG = "lng"; // 경도
        public static final String COLUMN_SIN_LAT = "sin_lat";  // Math.sin(Math.toRadians(lat)) -- 거리 계산을 위함
        public static final String COLUMN_SIN_LNG = "sin_lng";
        public static final String COLUMN_COS_LAT = "cos_lat";
        public static final String COLUMN_COS_LNG = "cos_lng";
        public static final String COLUMN_CONTACT_DATA_TYPE = "contact_data_type"; // // 0:demo(dummy) 1:real 2:reserved

        // 상수 정의
        public static final String COLUMN_AS_DISTANCE = "partial_distance"; // distance when it query

        public static final String POWERCONTACT_MODE = "mode"; // demo mode? normal mode?

        public static final int CONTACT_DATA_TYPE_DEMO = 0;

        public static final int POWERCONTACT_MODE_DEMO = 0; // demo mode
        public static final int POWERCONTACT_MODE_NORMAL = 1; // normal mode
        public static final int POWERCONTACT_MODE_ERROR = -1; // error show mode


        /*
        "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "data_id"+ " LONG  NOT NULL ," + // data id (data 그 자체)
        ContactsContract.RawContacts.CONTACT_ID  + " LONG NOT NULL," + // contact id (사람)
                " name TEXT NOT NULL,"+ // 이름
                " addr TEXT NOT NULL,"+ // 주소
                " lat REAL NOT NULL,"+ // 위도
                " lng REAL NOT NULL"+ // 경도
                " sin_lat REAL," + //  Math.sin(Math.toRadians(lat)) -- 거리 계산을 위함
                " sin_lng REAL," +
                " cos_lat REAL," + //  Math.cos(Math.toRadians(lat))
                " cos_lng REAL," +
                "UNIQUE ("+"data_id"+") ON CONFLICT REPLACE"+ // insert시 충돌나면 교체해라
                ");";
        */




//        // query all location : normal mode
//        public static Uri buildLocationDistanceAll(double lat, double lng) {
//            return buildLocationDistanceAll(lat, lng, POWERCONTACT_MODE_NORMAL);
////            return CONTENT_URI.buildUpon()
////                    .appendQueryParameter(COLUMN_LAT, Double.toString(lat)) // latitude
////                    .appendQueryParameter(COLUMN_LNG, Double.toString(lng))  // longitude
////                    .build();
//        }

        // query all location
        public static Uri buildLocationDistanceAll(double lat, double lng, int mode) {

            Log.v("WIDGET","buildLocationDistanceAll lat="+lat+"lng="+lng);
            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_DISTANCE) // distance path
                    .appendQueryParameter(COLUMN_LAT, Double.toString(lat)) // latitude
                    .appendQueryParameter(COLUMN_LNG, Double.toString(lng))  // longitude
                    .appendQueryParameter(POWERCONTACT_MODE, Integer.toString(mode)) // mode
                    .build();

        }

        // query all widget
        public static Uri buildLocationWidget(double lat, double lng, int mode) {
            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_WIDGET) // distance path
                    .appendQueryParameter(COLUMN_LAT, Double.toString(lat)) // latitude
                    .appendQueryParameter(COLUMN_LNG, Double.toString(lng))  // longitude
                    .appendQueryParameter(POWERCONTACT_MODE, Integer.toString(mode)) // mode
                    .build();

        }



        // get distance from this location -- meter

        public static Uri buildLocationByDistance(double lat, double lng, double distance) {
            return buildLocationByDistance(lat, lng, distance, POWERCONTACT_MODE_NORMAL);
        }

        public static Uri buildLocationByDistance(double lat, double lng, double distance, int mode) {

            if (distance == 0.0f) // return location all
                return  buildLocationDistanceAll(lat, lng, mode);

            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_DISTANCE) // distance path
                    .appendPath(String.valueOf(distance))
                    .appendQueryParameter(COLUMN_LAT, Double.toString(lat)) // latitude
                    .appendQueryParameter(COLUMN_LNG, Double.toString(lng))  // longitude
                    .appendQueryParameter(POWERCONTACT_MODE, Integer.toString(mode)) // mode
                    .build();
        }

        // mode only

//            public static final int POWERCONTACT_MODE_DEMO = 0; // demo mode
//        public static final int POWERCONTACT_MODE_NORMAL = 1; // normal mode
//        public static final int POWERCONTACT_MODE_ERROR = -1; // error show mode
        public static Uri buildLocationByMode(int mode) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(POWERCONTACT_MODE, Integer.toString(mode)) // mode
                    .build();
        }



//        // get location by address
        public static Uri buildLocationByAddr(String addr) {
            return CONTENT_URI.buildUpon().appendPath(addr).build();
        }


        // /location/200?lat=200&lng=2000 형태로 옮 (200은 거리)
        public static double getDistanceFromUri(Uri uri) {
            String ret= uri.getPathSegments().get(2); // 1에서 2로

            if (null != ret && ret.length()>0) {
                try {
                    return Double.parseDouble(ret);
                } catch (NumberFormatException  e) {
                    return Constants.DEFAULT_DISTANCE_METER; // return default
                }
            }
            return Constants.DEFAULT_DISTANCE_METER;
        }
        // /location/200.0?lat=200&lng=2000 형태로 옮 (200은 거리)
        public static LatLng getCurrentLocFromUri(Uri uri) {
            String latitude = uri.getQueryParameter(COLUMN_LAT);
            String longitude = uri.getQueryParameter(COLUMN_LNG);
            try {
                if (null != latitude && latitude.length() > 0 && null != longitude && longitude.length() > 0)
                    return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                else
                    return null;
            } catch (Exception e) {
                return null;
            }
        }

        // 실제 모드인지 데모모드인지(데모 데이터만 사용)
        public static int getPowerContactMode (Uri uri) {
            String data_type = uri.getQueryParameter(POWERCONTACT_MODE);
            try {
                if (null != data_type && data_type.length() > 0)
                    return Integer.parseInt(data_type);
                else
                    return POWERCONTACT_MODE_NORMAL; // default normal data --> 없으면 NORMAL임...
            } catch (Exception e) {
                return POWERCONTACT_MODE_NORMAL;
            }


        }
    }
}
