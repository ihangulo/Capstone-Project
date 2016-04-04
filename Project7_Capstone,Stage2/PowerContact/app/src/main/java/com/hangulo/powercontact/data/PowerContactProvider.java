package com.hangulo.powercontact.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.hangulo.powercontact.Constants;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*   Created on : 2015-07-02
*
*    PowerContactProvider.java
*    -------------
*    Contact provider
*
*/

public class PowerContactProvider extends ContentProvider {

    public static final UriMatcher sUriMatcher = buildUriMatcher();
    private PowerContactDbHelper mOpenHelper;


    static final int LOCATION_DISTANCE_ALL = 100;//
    static final int LOCATION_DISTANCE = 200;//
    static final int LOCATION_IDS = 300; // get DIR (리스트)
    static final int LOCATION_BYMODE = 400;
    static final int LOCATION_WIDGET = 500;



    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PowerContactContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.

        matcher.addURI(authority, PowerContactContract.PATH_POWERCONTACT, LOCATION_BYMODE); // all

        matcher.addURI(authority, PowerContactContract.PATH_POWERCONTACT+ "/"+PowerContactContract.PATH_WIDGET , LOCATION_WIDGET); // only 1 (nearest)
        matcher.addURI(authority, PowerContactContract.PATH_POWERCONTACT+ "/"+PowerContactContract.PATH_DISTANCE , LOCATION_DISTANCE_ALL);

        matcher.addURI(authority, PowerContactContract.PATH_POWERCONTACT + "/"+PowerContactContract.PATH_DISTANCE +"/*", LOCATION_DISTANCE); // distance query ?lat=100&lng=100 형태로 옵션필수

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PowerContactDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case LOCATION_BYMODE:
                return PowerContactContract.PowerContactEntry.CONTENT_DIR_TYPE;

            case LOCATION_DISTANCE_ALL:
                return PowerContactContract.PowerContactEntry.CONTENT_DIR_TYPE;

            case LOCATION_WIDGET:
                return PowerContactContract.PowerContactEntry.CONTENT_ITEM_TYPE;

            case LOCATION_IDS:
                return PowerContactContract.PowerContactEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }



    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor=null;

        int matchtemp=sUriMatcher.match(uri);
        switch (matchtemp) {


            case LOCATION_BYMODE: { // 현재 위치와 상관없이 전체를 돌려준다. 모드가 없으면 디폴트는 1이다.
                int mode =  PowerContactContract.PowerContactEntry.getPowerContactMode(uri); // mode를 찾는다.

                String sel = PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE + " = ? ";
                String[] selArgs = { String.valueOf(mode) };

                retCursor = mOpenHelper.getReadableDatabase().query(
                        PowerContactContract.PowerContactEntry.TABLE_NAME,
                        projection,
                        sel,
                        selArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }



            case LOCATION_DISTANCE_ALL: { // 모든 지역을 돌려준다.

                LatLng current = PowerContactContract.PowerContactEntry.getCurrentLocFromUri(uri);
                int mode =  PowerContactContract.PowerContactEntry.getPowerContactMode(uri); // mode를 찾는다.

                if ( current == null) return null; // 이렇게 부르기 전에 현재 위치가 잡혔는지 확인해야 한다.. --> 에러 처리 --> 조금 더 세심하게 잡아야 한다.


                //String[] mySelectionArgs = { String.format("%1.7f", (Math.cos((double) Integer.valueOf(partialDistance) / 6371000.0))) };


                StringBuilder sBuilder = new StringBuilder(); // support projection
                for (String str : projection) {
                    sBuilder.append(str);
                    sBuilder.append(",");
                }
                sBuilder.append( buildDistanceQuery(current.latitude, current.longitude));
                sBuilder.append(" AS "+ PowerContactContract.PowerContactEntry.COLUMN_AS_DISTANCE);// partial_distance

                String sql = "SELECT " + sBuilder.toString()
                        +" FROM  "+ PowerContactContract.PowerContactEntry.TABLE_NAME
                        + " WHERE "+ PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE
                        + " = " + mode
                        + " ORDER BY "+sortOrder ;

                Log.v("sql", "Sql :"+sql);

                retCursor = mOpenHelper.getReadableDatabase().rawQuery(sql, null); // because of "AS" clause, use this .rawQuery method. (위 방법쓰면 작동안함)


                break;
            }

            case LOCATION_WIDGET: { // same but have limit

                LatLng current = PowerContactContract.PowerContactEntry.getCurrentLocFromUri(uri);
                int mode =  PowerContactContract.PowerContactEntry.getPowerContactMode(uri); // mode를 찾는다.

                if ( current == null) return null; // 이렇게 부르기 전에 현재 위치가 잡혔는지 확인해야 한다.. --> 에러 처리 --> 조금 더 세심하게 잡아야 한다.

                StringBuilder sBuilder = new StringBuilder(); // support projection
                for (String str : projection) {
                    sBuilder.append(str);
                    sBuilder.append(",");
                }
                sBuilder.append( buildDistanceQuery(current.latitude, current.longitude));
                sBuilder.append(" AS " + PowerContactContract.PowerContactEntry.COLUMN_AS_DISTANCE);// partial_distance

                String sql = "SELECT " + sBuilder.toString()
                        +" FROM  "+ PowerContactContract.PowerContactEntry.TABLE_NAME
                        + " WHERE "+ PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE
                        + " = " + mode
                        + " ORDER BY "+sortOrder
                        + " LIMIT 100"; // only 1 return, 1개만 리턴한다.
                Log.v("Widget", "widget Sql :"+sql);

                retCursor = mOpenHelper.getReadableDatabase().rawQuery(sql, null); // because of "AS" clause, use this .rawQuery method. (위 방법쓰면 작동안함)


                break;
            }

            case LOCATION_DISTANCE: { // 거리에 따른 장소를 돌려준다. // 위와 같은 방법을 쓰면 에러나온다. ㅠㅠ

                double distance =  PowerContactContract.PowerContactEntry.getDistanceFromUri(uri);
                LatLng current = PowerContactContract.PowerContactEntry.getCurrentLocFromUri(uri);
                int mode =  PowerContactContract.PowerContactEntry.getPowerContactMode(uri); // mode를 찾는다.

                if ( current == null) return null;
                //String[] mySelectionArgs = { String.format("%1.7f", (Math.cos((double) Integer.valueOf(partialDistance) / 6371000.0))) };


                StringBuilder sBuilder = new StringBuilder(); // support projection
                for (String str : projection) {
                    sBuilder.append(str);
                    sBuilder.append(",");
                }
                sBuilder.append( buildDistanceQuery(current.latitude, current.longitude));
                sBuilder.append(" AS "+ PowerContactContract.PowerContactEntry.COLUMN_AS_DISTANCE);// partial_distance

                String sql = "SELECT " + sBuilder.toString()
                        +" FROM  "+ PowerContactContract.PowerContactEntry.TABLE_NAME
                        +" WHERE "+ PowerContactContract.PowerContactEntry.COLUMN_AS_DISTANCE
                        +"  >= " + Math.cos( distance/ Constants.EARTH_RADIUS_METER ) //meter단위 6371000.0
                        + " AND "
                        +  PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE
                        + " = " + mode
                        + " ORDER BY "+sortOrder ;

                Log.v("sql", "Sql :"+sql + "\ndistance:"+distance);

                retCursor = mOpenHelper.getReadableDatabase().rawQuery(sql, null); // because of "AS" clause, use this .rawQuery method. (위 방법쓰면 작동안함)

                break;

            }



            case LOCATION_IDS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PowerContactContract.PowerContactEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri); // 에러 처리 필요함 TODO
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    // // https://github.com/kthcorp/Snippets/tree/master/SQLiteDistanceDemo
    private String buildDistanceQuery(double latitude, double longitude) {

        final double sinLat = Math.sin(Math.toRadians(latitude));
        final double cosLat = Math.cos(Math.toRadians(latitude));
        final double sinLng = Math.sin(Math.toRadians(longitude));
        final double cosLng = Math.cos(Math.toRadians(longitude));

        Log.v("DBLOG", "cosLng=" + cosLng);

        return "(" + cosLat +"*"+PowerContactContract.PowerContactEntry.COLUMN_COS_LAT
                + "*(" + PowerContactContract.PowerContactEntry.COLUMN_COS_LNG + "*" + cosLng
                + "+" + PowerContactContract.PowerContactEntry.COLUMN_SIN_LNG  + "*" + sinLng
                + ")+" + sinLat + "*" + PowerContactContract.PowerContactEntry.COLUMN_SIN_LAT
                + ")";


    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {

            case LOCATION_DISTANCE_ALL:

                long _id = db.insertWithOnConflict(PowerContactContract.PowerContactEntry.TABLE_NAME, null, values,
                        SQLiteDatabase.CONFLICT_REPLACE); // if there is data already then update 충돌 일으키면, update하는 모드

                if (_id > 0)
                    returnUri = PowerContactContract.PowerContactEntry.buildLocationByAddr("_id"); // _id를 사용해서 고유한 URI를 셋팅해준다 (TODO)
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            case LOCATION_BYMODE:


                _id = db.insertWithOnConflict(PowerContactContract.PowerContactEntry.TABLE_NAME, null, values,
                        SQLiteDatabase.CONFLICT_REPLACE); // if there is data already then update 충돌 일으키면, update하는 모드

                if (_id > 0)
                    returnUri = PowerContactContract.PowerContactEntry.buildLocationByAddr("_id"); // _id를 사용해서 고유한 URI를 셋팅해준다 (TODO)
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case LOCATION_DISTANCE_ALL:
            case LOCATION_BYMODE:
                int mode =  PowerContactContract.PowerContactEntry.getPowerContactMode(uri); // mode를 찾는다.
                rowsDeleted = db.delete(
                        PowerContactContract.PowerContactEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;

    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case LOCATION_DISTANCE_ALL: // 데이터를 업데이트 한다.
                rowsUpdated = db.update(PowerContactContract.PowerContactEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null); // 리졸버에게 알린다.
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int returnCount = 0;
        switch (match) {

            case LOCATION_DISTANCE_ALL:
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {

                        long _id = db.insertWithOnConflict(PowerContactContract.PowerContactEntry.TABLE_NAME, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE); // if there is data already then update 충돌 일으키면, update하는 모드
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }


    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}


