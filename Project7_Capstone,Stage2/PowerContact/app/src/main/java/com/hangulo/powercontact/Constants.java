/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hangulo.powercontact;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*
*    Constants.java
*    -------------
*    Constants values
*
*
*/

public final class Constants {

    public static final String PACKAGE_NAME = "com.hangulo.powercontact";


    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";

    // fetch power address intent service
    public static final String SERVICE_ACCOUNT_NAME_EXTRA = PACKAGE_NAME + ".ACCOUNT_NAME_EXTRA";
    public static final String SERVICE_LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    public static final String DATA_ID_KEY=PACKAGE_NAME+".DATA_ID_KEY";
    public static final String CONTACT_ID_KEY= PACKAGE_NAME+".CONTACT_ID_KEY";
    public static final String LOOKUP_KEY= PACKAGE_NAME+".LOOKUP_KEY";


    // Broadcast
    public static final String MESSAGE_EVENT = PACKAGE_NAME + ".MESSAGE_EVENT";
    public static final String MESSAGE_KEY = PACKAGE_NAME+ ".MESSAGE_EXTRA";
    public static final String RESULT_KEY= PACKAGE_NAME + ".RESULT_VALUE";


    public static final int FAILURE_RESULT = 0;
    public static final int SUCCESS_RESULT = 1;

    public static final int START_LOADING=10;
    public static final int STOP_LOADING=11;

    // activity result
    public static final int SETTINGS_REQUEST = 10;  // The request code
    public static final int RESULT_SETTINGS_CHANGED = 1; // settings have been changed by user
    public static final int RESULT_SETTINGS_UNCHANGED=0;
    public static final int RESULT_ERROR_CONTACTSLIST= 100;
    public static final int REQUEST_ACCOUNT_SELECT=200;
    public static final int REQUEST_ACCOUNT_SELECT_AND_RUN_SERVICE=201;

    /// settings
    public static final double DEFAULT_DISTANCE_METER = 0; // defulat distance value (meter)
    public static final double DEFAULT_DISTANCE_MILE = 0; // defulat distance value (mile)

    public static final String SETTINGS_ACCOUNT_KEY = "SETTINGS_ACCOUNT_KEY";


    public static final String SETTINGS_LAST_LOCATION_LAT_KEY= "SETTINGS_LAST_LOCATION_LAT_KEY"; // 마지막 위치 로케이션
    public static final String SETTINGS_LAST_LOCATION_LNG_KEY= "SETTINGS_LAST_LOCATION_LNG_KEY"; //마지막 위치 로케이션 (위젯에서 사용위해서)

    // loader
    public static final int POWERCONTACT_LOADER = 0; // loader number
    public static final int CONTACTLIST_LOADER = 10; // loader number


    // get distance
    public static final double EARTH_RADIUS_METER = 6371000.0f; // radius = 6371000 meter = 6371 km = 3,959 mi

    // locale
    public static final int DISTANCE_UNITS_AUTO=0;
    public static final int DISTANCE_UNITS_METER =1;
    public static final int DISTANCE_UNITS_MILE=2;


    // widget
    public static final String FROM_WIDGET_KEY = "FROM_WIDGET_KEY";

    // marker type

    public static final int MARKER_TYPE_DEFAULT = 0;
    public static final int MARKER_TYPE_NAME_ONLY = 1;
    public static final int MARKER_TYPE_NAME_WITH_PHOTO = 2;
    public static final int MARKER_TYPE_PHOTO_ONLY = 3;
    public static final int MARKER_TYPE_SMALL_CIRCLE = 10;


    // requestLocationPermissions code
    // http://googledevkr.blogspot.kr/2015/09/playservice81android60.html
    public static final int REQUEST_CODE_LOCATION = 2; // get location
    public static final int REQUEST_CODE_ENABLE_LOCATION = 3; // google map options (enable current location)
    public static final int REQUEST_CODE_CONTACTS = 4; // get location


}
