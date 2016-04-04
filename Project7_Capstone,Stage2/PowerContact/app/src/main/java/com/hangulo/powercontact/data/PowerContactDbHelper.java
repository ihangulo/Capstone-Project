package com.hangulo.powercontact.data;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import com.hangulo.powercontact.data.PowerContactContract.PowerContactEntry;


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
*    PowerContactdDbHelper.java
*    -------------
*    Contact Db Helper
*
*/


public class PowerContactDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "hangulo_power_contact.db"; // database name


    public PowerContactDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE "+ PowerContactEntry.TABLE_NAME+" (" +

                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "data_id" + " LONG  NOT NULL ," + // data id (data 그 자체)
                        ContactsContract.RawContacts.CONTACT_ID + " LONG NOT NULL," + // contact id (사람)
                        ContactsContract.Contacts.LOOKUP_KEY + " TEXT, "+ // "lookup"
                        " name TEXT NOT NULL," + // 이름
                        " type INTEGER ,"+
                        " label TEXT, " +
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI +" TEXT, " +     // "photo_thumb_uri" (사진)
                        " addr TEXT NOT NULL," + // 주소
                        " lat REAL NOT NULL," + // 위도
                        " lng REAL NOT NULL," + // 경도
                        " sin_lat REAL," + //  Math.sin(Math.toRadians(lat)) -- 거리 계산을 위함
                        " sin_lng REAL," +
                        " cos_lat REAL," + //  Math.cos(Math.toRadians(lat))
                        " cos_lng REAL," +
                        " contact_data_type INTEGER DEFAULT 1, " + // 0:demo(dummy) 1:real -1:error (2016.1.4추가)
                        "UNIQUE (" + "data_id" + ") ON CONFLICT REPLACE" + // insert시 충돌나면 교체해라
                        ");";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.

        /*
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PowerContactEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
        */

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PowerContactContract.PowerContactEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
