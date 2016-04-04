package com.hangulo.powercontact.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import com.google.android.gms.maps.model.LatLng;
import com.hangulo.powercontact.Constants;
import com.hangulo.powercontact.R;
import com.hangulo.powercontact.data.PowerContactContract;
import com.hangulo.powercontact.util.Utils;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*   Created on : 2015-05-20
*
*    FetchPowerAddressIntentService.java
*    -------------
*   class for Settings menu
*
*/

/**
 * based on
 * http://stackoverflow.com/questions/26804387/android-fetch-all-contact-list-name-email-phone-takes-more-then-a-minute-for
 * (linkedlist를 제외하고 sparseArray로만 사용함)
 *
 * Intent Service
 * http://developer.android.com/training/run-background-service/report-status.html
 *
 * Change to intent service (2015.12.11)
 * Based on
 * https://github.com/udacity/google-play-services/blob/master/LocationLessons_Final/LocationLesson2_3/app/src/main/java/com/google/devplat/lmoroney/locationlesson2_3/FetchAddressIntentService.java
 *
 * And...
 *    // http://stackoverflow.com/questions/10755994/how-to-get-all-contacts-and-all-of-their-attributes
 *   Calculation for distance 거리 계산을 위한 상수들 입력  http://king24.tistory.com/4
 *
 *   for error processing
 *   https://github.com/udacity/google-play-services/blob/master/LocationLessons_Final/LocationLesson2_3/app/src/main/java/com/google/devplat/lmoroney/locationlesson2_3/FetchAddressIntentService.java
 */
public class FetchPowerAddressIntentService extends IntentService {

    private final String LOG_TAG = FetchPowerAddressIntentService.class.getSimpleName();
    private static final String TAG = "fetch-address-intent-service";

    Geocoder mGeocoder;
    final static int MAX_RESULT = 1;
    final static String ACCOUNT_TYPE = "com.google";

    public FetchPowerAddressIntentService() {
        super(TAG);         // Use the TAG to name the worker thread.
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        sendBroadcastMessage(Constants.START_LOADING); // show loading circle

        // check if internet connection is available
        if (!Utils.isNetworkAvailable(this)) {
            // send "internet connection error" message
            sendBroadcastMessage(Constants.FAILURE_RESULT,
                            getResources().getString(R.string.error_msg_no_internet_connection) );
            return;
        }

        long start = System.currentTimeMillis(); // for logging not used


        // get intent parameter
        Bundle bundle = intent.getExtras();
        String accountName = bundle.getString(Constants.SERVICE_ACCOUNT_NAME_EXTRA, null); // account name


        String strDataId = bundle.getString(Constants.DATA_ID_KEY, null);
        String strContactId = bundle.getString(Constants.CONTACT_ID_KEY, null);
        String strLookupKey = bundle.getString(Constants.LOOKUP_KEY,null);

        if (accountName==null) {
            sendBroadcastMessage(Constants.FAILURE_RESULT,
                    getResources().getString(R.string.error_msg_no_account_name) ); // no account name!
            return;

        }


        // if common sync, then delete all first
        // 만약 일반적인 싱크라면.. 먼저 다 지우고 시작한다.
        if (strDataId == null && strContactId==null && strLookupKey==null) {

            // deletedatabase
            // delete
            Uri uri = PowerContactContract.PowerContactEntry.CONTENT_URI;

            String sWhere = PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE + " = ? OR " +
                    PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE + " = ? ";

            String[] sSelection = { String.valueOf(PowerContactContract.PowerContactEntry.POWERCONTACT_MODE_NORMAL),
                    String.valueOf(PowerContactContract.PowerContactEntry.POWERCONTACT_MODE_ERROR) };

            int del_count = getContentResolver().delete(uri, sWhere, sSelection); // delete it

            Log.v(LOG_TAG, "deleted :  " +del_count);

        } else {

            // 하나만 업데이트 하는 것이라면... 해당 것만 먼저 지우고 업데이트 한다. (해당것은 _id 값을 가지고 판단, data_id는 수정시 변경될 수 있음)
            Uri uri = PowerContactContract.PowerContactEntry.CONTENT_URI;

            String sWhere = PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE + " = ? AND " +
                    PowerContactContract.PowerContactEntry.COLUMN_DATA_ID  + " = ? "; // 정확히 에러난 그것만 지운다.


            String[] sSelection = { String.valueOf(PowerContactContract.PowerContactEntry.POWERCONTACT_MODE_ERROR),
                    String.valueOf(strDataId) };

            int del_count = getContentResolver().delete(uri, sWhere, sSelection); // delete it

            Log.v(LOG_TAG, "deleted :  " +del_count);

        }

        // ----------

        String[] projection = {
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data._ID, // contact data 고유한 id ( data_id 로 바꾸어서 사용한다.)
                ContactsContract.Contacts.LOOKUP_KEY, // lookup key
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Contactables.DATA,
                ContactsContract.CommonDataKinds.Contactables.TYPE,
                ContactsContract.CommonDataKinds.Contactables.LABEL,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI // thumbnail "photo_thumb_uri"
        };

        final int INDEX_COL_MIMETYPE = 0;
        final int INDEX_COL_CONTACT_ID = 1;
        final int INDEX_COL_DATA_ID = 2;
        final int INDEX_COL_LOOKUP_KEY = 3;
        final int INDEX_COL_NAME = 4;
        final int INDEX_COL_ADDR = 5;
        final int INDEX_COL_TYPE = 6;
        final int INDEX_COL_LABEL = 7;
        final int INDEX_COL_PHOTO = 8;

        String selection = ContactsContract.Data.MIMETYPE + " = ? AND  "
                + ContactsContract.RawContacts.ACCOUNT_NAME + " = ? AND "
                + ContactsContract.RawContacts.ACCOUNT_TYPE + " = ? AND "
                + ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS + "<> ? ";


        String[] selectionArgs;

        selectionArgs = new String[]
                {
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                        accountName,// account name // 타입도 비교해야 함 --> 이건 설정에서 가져오도록 한다. --> 시작시에 고르도록 해야 한다.
                        ACCOUNT_TYPE, //  only support google...
                        "NULL",
                };

        if (strContactId!=null && strLookupKey!=null ) { // 만약 값이 있다면 ... 셀렉션에 더한다.

            selection = selection + " AND "

                    +  ContactsContract.Data.CONTACT_ID  + " = ? AND "
                    +   ContactsContract.Contacts.LOOKUP_KEY  + " = ? ";

            selectionArgs = new String[]
                    {
                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                            accountName,// account name // 타입도 비교해야 함 --> 이건 설정에서 가져오도록 한다. --> 시작시에 고르도록 해야 한다.
                            ACCOUNT_TYPE,
                            "NULL",

                            strContactId,
                            strLookupKey
                    };

        }


        String sortOrder = ContactsContract.Contacts.SORT_KEY_ALTERNATIVE;


        Uri uri = ContactsContract.Data.CONTENT_URI;


        start = System.currentTimeMillis();

        mGeocoder = new Geocoder(this, Locale.getDefault());

        // sync db <-> contacts
        // Insert the new contact information into the database
        // permission check?
        Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

        if (cursor == null || cursor.getCount() == 0) {
            Log.v(LOG_TAG, "Recoder not found");

            return;
        }

        Vector<ContentValues> cVVector = new Vector<>(cursor.getCount());

        while (cursor.moveToNext()) {
            long contactId = cursor.getLong(INDEX_COL_CONTACT_ID);
            long dataId = cursor.getLong(INDEX_COL_DATA_ID); // 일반 data Idx // 해당 data의 고유한 id 이 값을 알면... 나중에 업데이트가 가능하다.

            String lookupKey = cursor.getString(INDEX_COL_LOOKUP_KEY); // look up key
            String name = cursor.getString(INDEX_COL_NAME); // name? 이름은?
            String photo = cursor.getString(INDEX_COL_PHOTO); // photo thumbnail
            if (photo==null)
                photo="";


            int type = cursor.getInt(INDEX_COL_TYPE);
            String mimeType = cursor.getString(INDEX_COL_MIMETYPE);
            String address = cursor.getString(INDEX_COL_ADDR);
            String addrLabel = cursor.getString(INDEX_COL_LABEL); // get label,  (not used this version ) label 얻기 (type=  TYPE_OTHER 일때만 셋팅된다.


            // get address // 주소를 얻는다

           if(address==null) // exception situation
               continue;

            LatLng loc;
            try {
                loc = getLatLngFromAddr(address);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                sendBroadcastMessage(Constants.FAILURE_RESULT, getString(R.string.error_msg_service_not_available));
                Log.e(LOG_TAG, errorMessage, ioException);
                return;

            } catch (IllegalArgumentException illegalArgumentException) {

                Log.e(LOG_TAG, "Error address!");

                loc=null;
            }


            ContentValues addrValues = new ContentValues(); // addr 밸류

            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_CONTACT_ID, contactId);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_DATA_ID, dataId); // 원래는 고유한 _ID 임 --> data_id로 바꾸어서 저장한다. 고유한 값이다.
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_LOOKUP_KEY, lookupKey);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_NAME, name);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_ADDR, address);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_TYPE, type);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_LABEL, addrLabel);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_PHOTO, photo); // photo 사진


            // 여기부터는 데이터가 있을때만 사용한다.
            if (loc != null) {

                double lat = loc.latitude;
                double lng = loc.longitude;
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_LAT, lat); // latutiude 위도
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_LNG, lng); // longitude 경도

                // constants for calculation distance query   * http://king24.tistory.com/4
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_SIN_LAT, Math.sin(Math.toRadians(lat)));
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_SIN_LNG, Math.sin(Math.toRadians(lng)));
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_COS_LAT, Math.cos(Math.toRadians(lat)));
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_COS_LNG, Math.cos(Math.toRadians(lng)));

                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE,PowerContactContract.PowerContactEntry.POWERCONTACT_MODE_NORMAL); // set error
            } else { // if there is error --> set all 0 & error mode

                Log.v(LOG_TAG, "Error address! loc == null"+address);

                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_LAT, 0.0f); // latutiude 위도
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_LNG, 0.0f); // longitude 경도
                // constants for calculation distance query   * http://king24.tistory.com/4
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_SIN_LAT,0.0f);
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_SIN_LNG,0.0f);
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_COS_LAT,0.0f);
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_COS_LNG,0.0f);
                addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE,PowerContactContract.PowerContactEntry.POWERCONTACT_MODE_ERROR); // set error
            }


            cVVector.add(addrValues); // save this row --> is there any memory problem? 메모리 문제를 체크해보자.

        }

        cursor.close();

       // wrote to database
       // bulk update
        int return_count = 0;
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            return_count = getContentResolver().bulkInsert(PowerContactContract.PowerContactEntry.CONTENT_URI, cvArray);
        }

        long ms = System.currentTimeMillis() - start;

        sendBroadcastMessage(Constants.SUCCESS_RESULT, "Done:" + String.valueOf(return_count) + "data updated"); // send to main

        Log.v(LOG_TAG,"Done:" + String.valueOf(return_count) + "data updated / "+ ms +"ms" );

    }


    // * https://github.com/udacity/google-play-services/blob/master/LocationLessons_Final/LocationLesson2_3/app/src/main/java/com/google/devplat/lmoroney/locationlesson2_3/FetchAddressIntentService.java
    // 을 기반으로 한 루틴 (에러처리부분)
    LatLng getLatLngFromAddr(String addr) throws IOException, IllegalArgumentException {

        List<Address> address_result = null;
        //StringBuilder sBuilder = new StringBuilder();

        if (addr == null) return null; // 에러 처리필요

        if (mGeocoder != null) {
            address_result = mGeocoder.getFromLocationName(addr, MAX_RESULT); // get location from address
            // this method throws IOException, IllegalArgumentException
            // exception will be catched by caller
        }

        // return first result // 일단은 제일 첫번째 것만 돌려준다.
        if (address_result != null && address_result.size()!=0) {
            Address ret = address_result.get(0);
            return new LatLng(ret.getLatitude(), ret.getLongitude());
        }


        return null; // error
    }

    // send local broadcast message (result code & message)
    void sendBroadcastMessage(int result_code , String msg){
        Intent messageIntent = new Intent(Constants.MESSAGE_EVENT);
        messageIntent.putExtra(Constants.RESULT_KEY, result_code);
        messageIntent.putExtra(Constants.MESSAGE_KEY, msg);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }

    // send local broadcast message (only have reuslt code)
    void sendBroadcastMessage(int result_code){
        Intent messageIntent = new Intent(Constants.MESSAGE_EVENT);
        messageIntent.putExtra(Constants.RESULT_KEY, result_code);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }


} //------------------------------ End Of Program ----------------------------------------------
