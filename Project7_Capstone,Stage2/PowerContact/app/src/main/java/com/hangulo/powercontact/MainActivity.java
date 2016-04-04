package com.hangulo.powercontact;
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
 *
 *
    // udacity
    // https://github.com/udacity/google-play-services/tree/master/LocationLessons_Final/LocationLesson1

 */

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;

import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.support.design.widget.NavigationView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.hangulo.powercontact.data.PowerContactContract;
import com.hangulo.powercontact.model.PowerContactAddress;
import com.hangulo.powercontact.service.FetchPowerAddressIntentService;
import com.hangulo.powercontact.util.Utils;

import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.hangulo.powercontact.model.PowerContactSettings;
/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*
*    MainActivity.java
*    -------------
*
*
*/

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        NavigationView.OnNavigationItemSelectedListener ,
        ContactsListFragment.ContactsListCallback,
        MapViewFragment.MapViewCallback,
        ConnectionCallbacks, OnConnectionFailedListener {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    // for broadcast
    private BroadcastReceiver messageReceiver;
    private static boolean onFetching = false; // is it now fetching address?

    // search
    public String mSearchKeyword=""; // Stores the current search query term
    public String mPreviousSearchKeyword=""; // Stores the current search query term

    // https://developers.google.com/android/guides/api-client
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    //main menu ui

    private NavigationView mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private FrameLayout mTopFrameLayout;
    private android.support.design.widget.AppBarLayout mTopLayout;
    private FloatingActionButton mMakeDemoButton;
    SwitchCompat mDemoSwitch; //상단 스위치
    TextView mTextDemoMode;


    private boolean mIsExpandedFragment; // 이거 나중에 저장도 해야할거다. 현재 판넬이 올라왔나 내려와나를 기억한다.

    protected final static String LOCATION_KEY = "LOCATION_KEY";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "LAST_UPDATED_TIME_STRING_KEY";
    protected final static String POWER_CONTACT_SETTINGS_KEY = "POWER_CONTACT_SETTINGS";
    protected final static String DEMO_CREATED_KEY = "DEMO_CREATED";
    private static final String IS_EXPANDED_FRAGMENT=  "IS_EXPANDED"; // panel이 펼쳐졌나?

    protected final static String TAG_FRAGMENT_PANE1 = "fragment_pane1";
    protected final static String TAG_FRAGMENT_PANE2 = "fragment_pane2";
    public static final String QUERY_KEY = "QUERY";
//    private static final String STATE_PREVIOUSLY_SELECTED_KEY =
//            "com.hangulo.powercontact.SELECTED_ITEM";
//    //private static final String DISTANCE_UNITS_KEY = "DISTANCE_UNITS_KEY"; // distance units save key

    protected Geocoder mGeocoder; // local geocoding
    ArrayList<PowerContactAddress> mPowerContactArrayList = new ArrayList<>(); // save positions
    boolean mTwoPane; // determine two_pane mode
    ProgressBar mProgressCircle; // loading circle


    // default values --> settings
    public static PowerContactSettings mPowerContactSettings = new PowerContactSettings(); // settings value
    boolean demoCreated; // demo data가 만들어졌나?

    boolean isLoaderInitAlready; // 아직 로더가 init 안됨  (onConnection에서..)
    boolean isLoaderRestartNeed; // 로더가 업데이트 될 필요가 있음 (onConnection에서..)




    protected GoogleApiClient mGoogleApiClient; // Provides the entry point to Google Play services.

    protected String mLastUpdateTime; //  Time when the location was updated represented as a String.
    protected Location mCurrentLocation; //  Represents a geographical location.


    MapViewFragment mMapFragment;
    ContactsListFragment mListFragment;

    String mAccountName=null; // account name


    boolean FROM_WIDGET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // supprot two pane mode
        mTwoPane = getResources().getBoolean(R.bool.two_pane); // two_pane mode check

        // Analytics tracking start
        ((AnalyticsApplication) getApplication()).startTracking();

        // use toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar); // 액션바에 툴바를 할당

        // setting left drawer
        setleftMenuDrawer(toolbar);

        mTopFrameLayout = (FrameLayout) findViewById(R.id.top_frame_layout); // 가장 바깥쪽 레이아웃
        mTopLayout=(android.support.design.widget.AppBarLayout) findViewById(R.id.layout_toolbar); // 서치바가 포함된 레이아웃
        mProgressCircle = (ProgressBar) findViewById(R.id.loading_circle); // loading circle


        // 만약 로케일이 지정되어 있으면 로케일을 넣는다. (옵션에서 읽어와야 한다.)
        mGeocoder = new Geocoder(this, Locale.KOREA); //  public Geocoder (Context context, Locale locale) // why korea??? -=--> 옵션에서 읽어온다. 디폴트는 없음이다.


        mTextDemoMode = (TextView)findViewById(R.id.text_demo_mode);
        mMakeDemoButton = (FloatingActionButton) findViewById(R.id.fab_make_demo);
        mMakeDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                makeDemoData(200);
                getSupportLoaderManager().restartLoader(Constants.POWERCONTACT_LOADER, null, MainActivity.this); // reset Loader

            }
        });

        readDefaultSettings(mPowerContactSettings); // 기본 설정을 읽어온다. (위의 나라도 구해야 한다.)


        if (savedInstanceState != null) {  // 이미 저장된 것이 있다면....
            // If we're restoring state after this fragment was recreated then
            // retrieve previous search term and previously selected search
            // result.


            mSearchKeyword = savedInstanceState.getString(QUERY_KEY);
            mPreviousSearchKeyword = mSearchKeyword; // 맞겠지?
            mPowerContactSettings = savedInstanceState.getParcelable(POWER_CONTACT_SETTINGS_KEY);
            demoCreated = savedInstanceState.getBoolean(DEMO_CREATED_KEY);

            // https://developers.google.com/android/guides/api-client
            mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
            mIsExpandedFragment= savedInstanceState.getBoolean("IS_EXPANDED", false); // 현재 판넬 상태를 가져와라
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
            // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
        }

        Intent intent = getIntent();

        if (intent != null) {
            FROM_WIDGET= intent.getBooleanExtra(Constants.FROM_WIDGET_KEY, false);

            if(FROM_WIDGET) {
                Log.v(LOG_TAG, "From Widget distance");
                setDistance(0.0f); // distance all
                mPowerContactSettings.setDemoMode(false);
            }

        }
        // 위젯 체크 끝

        toggleDemoMode(mPowerContactSettings.isDemoMode()); // demo mode false!



        buildGoogleApiClient();  // 현재 위치 얻기

        // https://github.com/umano/AndroidSlidingUpPanel/blob/master/demo/src/com/sothree/slidinguppanel/demo/DemoActivity.java
        mMapFragment = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_PANE1);
        mListFragment = (ContactsListFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_PANE2);
        if(mMapFragment==null)
            mMapFragment = new MapViewFragment();

        if(mListFragment==null)
            mListFragment = new ContactsListFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contact_map_view_container, mMapFragment, TAG_FRAGMENT_PANE1).commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contact_list_view_container, mListFragment, TAG_FRAGMENT_PANE2).commit();

    }

    // set left Menu drawer
    void setleftMenuDrawer(Toolbar toolbar) {
        mDrawer = (NavigationView) findViewById(R.id.main_drawer);
        mDrawer.setNavigationItemSelectedListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

    }


    // read prefrerence
    void readDefaultSettings(PowerContactSettings settings) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences prefMarkerType = getSharedPreferences(getString(R.string.pref_key_default_marker_type), Context.MODE_PRIVATE);
        settings.setMarkerType(Integer.parseInt(prefs.getString(getString(R.string.pref_key_default_marker_type), "0")));

//        SharedPreferences prefDefaultDistance = getSharedPreferences(getString(R.string.pref_key_default_range_distance), Context.MODE_PRIVATE);
        // 여기는 auto일때 디폴트 문제가 날 수 있겠는데?
        settings.setDistance(Double.parseDouble(prefs.getString(getString(R.string.pref_key_default_range_distance), String.valueOf(Constants.DEFAULT_DISTANCE_METER))));

//        SharedPreferences prefDemoMode = getSharedPreferences(getString(R.string.pref_key_demo_mode_setting), Context.MODE_PRIVATE);
        settings.setDemoMode(prefs.getBoolean(getString(R.string.pref_key_demo_mode_setting), false));

        // distance units  0:auto 1:meter 2:mile
        settings.setDistanceUnits(Integer.parseInt(prefs.getString(getString(R.string.pref_key_default_distance_units), String.valueOf(Constants.DISTANCE_UNITS_AUTO))));
    }


    @Override
    protected void onResume() {
        // 로더를 시작한다. --> 현재 위치가 오면 시작해야 한다. --> 옮긴다...

        // 현재 셀렉트 되어 있는 거리를 기준으로 로더를 리셋해야 한다. (현재 잘 안되어 있는 부분) 2016.3.25
        if(FROM_WIDGET) {
            Log.v(LOG_TAG,"onResume now setting spinner");
            mListFragment.setSpinnerDistanceChanged(getResources().getStringArray(R.array.pref_range_distance_values_meter).length-1); // 가장 마지막 것을 고른다.
            FROM_WIDGET=false; // reset for next
        }

        toggleDemoMode(mPowerContactSettings.isDemoMode()); // 데모모드 버튼, 메뉴스위치, 화면 전환 등을 처리한다.

        if (mCurrentLocation == null) {
            isLoaderInitAlready=false; // wait until connected
        }
        else {
            Log.v(LOG_TAG, "Main/onResume-restartLoader()");

            getSupportLoaderManager().restartLoader(Constants.POWERCONTACT_LOADER, null, this); // 여기서 시작된다.

        }

        super.onResume();
    }


    // on/off loading circle
    void showLoadingCircle(boolean onoff) {
        if (onoff) {
            mProgressCircle.bringToFront();
            mProgressCircle.setVisibility(View.VISIBLE); // show loading circle
        } else
            mProgressCircle.setVisibility(View.GONE); // show off

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawer)) // 왼쪽 드로워가 열렸으면 닫아라
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else if (mIsExpandedFragment)
            launchSearchFragment(false); // 서치뷰가 열렸으면..닫아라
        else
            super.onBackPressed();
    }



    // 저장하기
    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelable(POWER_CONTACT_SETTINGS_KEY, mPowerContactSettings);
        outState.putString(QUERY_KEY, mSearchKeyword);
        outState.putParcelable(LOCATION_KEY, mCurrentLocation);
        outState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);

        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError); // https://developers.google.com/android/guides/api-client
        outState.putBoolean(IS_EXPANDED_FRAGMENT,mIsExpandedFragment);

        // 여기에 현재 데이터를 모두 저장해? 아니다. 이게 회전된 것이라는
        super.onSaveInstanceState(outState);
    }

    // demo data delete
    private void deleteDemoData () {
        // delete previous data first
        Uri uri = PowerContactContract.PowerContactEntry.CONTENT_URI;

        String sWhere = PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE + " = ?";
        String[] sSelection = { String.valueOf(PowerContactContract.PowerContactEntry.CONTACT_DATA_TYPE_DEMO) };
        int del_count = getContentResolver().delete(uri, sWhere, sSelection); // delete it

        Log.v(LOG_TAG, "makeDemoData()/deleted demo : "+del_count);
    }

    // demo mode인지 여부에 따라 처리
    void toggleDemoMode (boolean newMode) {
        // 셋팅을 바꾸고
        // 버튼을 없애고
        // 데모모드 표기를 하고
        // 화면을 갱신한다.


        if(mPowerContactSettings==null) return;

        // ok... change it
        mPowerContactSettings.setDemoMode(newMode); // demo mode
        /*
        if (mDemoSwitch!=null)
            mDemoSwitch.setChecked(newMode);
            */

        if(newMode) { // demo mode
            mMakeDemoButton.show(); // refresh역할도 있음... 나중에 구현해야 함.
            mTextDemoMode.setVisibility(View.VISIBLE); // 데모모드라는 것을 알리는 텍스트를 보이게 한다.
        } else {
            mMakeDemoButton.hide(); // 버튼 감추기
            mTextDemoMode.setVisibility(View.INVISIBLE);
        }

    }

    // make dummy addresses for testing
    private int makeDemoData(int max) {

        showLoadingCircle(true);
        // 먼저 여기서 지우는 루틴이 들어가야 함~~~!!!!!

        deleteDemoData();

        // 이름을 로딩한다.

        String[] dummyNames;
        dummyNames = getResources().getStringArray(R.array.dummy_names);
        int maxNames= dummyNames.length-1;


        Vector<ContentValues> cVVector = new Vector<>(max + 10);

        // ArrayList<PowerContactAddress> retAddress = new ArrayList<>();
        LatLng currentLatLng = getCurrentLatLng();
        double lat, lng;
        for (int i = 1; i <= (max + 2); i++) {
            // South Korea
            // double latitude = Utils.getRandomNumber((double) 36.4922565f, (double) 38.4922565f);
            // double longitude = Utils.getRandomNumber((double) 125.92319053333333f, (double) 127.92319053333333f);

            ContentValues addrValues = new ContentValues(); // addr 밸류

            String name = dummyNames[ Utils.getRandomNumber(0,maxNames )]+" :"+ i;

            if (i <= max) {
                lat = Utils.getRandomNumber((currentLatLng.latitude - 1), currentLatLng.latitude + 1);
                lng = Utils.getRandomNumber(currentLatLng.longitude - 1, currentLatLng.longitude + 1);
            } else {// to make same location data
                lat = currentLatLng.latitude;
                lng = currentLatLng.longitude;
                name = "duplicated"+i;
            }

            long contactId = 100000 + i;
            long dataId = 1000000 + i;
            String lookupKey = "";

            String address = "Address Phone " + i;
            int type = 1;
            String addrLabel = "";

           // double dist = Utils.getDistanceX(currentLatLng.latitude, currentLatLng.longitude, lat, lng); // 거리

            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_CONTACT_ID, contactId);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_DATA_ID, dataId);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_LOOKUP_KEY, lookupKey);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_NAME, name);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_ADDR, address);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_TYPE, type);
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_LABEL, addrLabel);

            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_LAT, lat); // 위도
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_LNG, lng); //경도

            // constants for calculation distance query   * http://king24.tistory.com/4
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_SIN_LAT, Math.sin(Math.toRadians(lat)));
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_SIN_LNG, Math.sin(Math.toRadians(lng)));
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_COS_LAT, Math.cos(Math.toRadians(lat)));
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_COS_LNG, Math.cos(Math.toRadians(lng)));

            // this is demo
            addrValues.put(PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE, PowerContactContract.PowerContactEntry.CONTACT_DATA_TYPE_DEMO);

            cVVector.add(addrValues); // save this row --> 메모리 문제를 체크해보자.
        }


        // wrote to database // bulk update
        int return_count = 0;
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            return_count = getContentResolver().bulkInsert(PowerContactContract.PowerContactEntry.CONTENT_URI, cvArray);
        }


        showDemoInfo(return_count);

        Log.v(LOG_TAG, "makeDemoData()/dummydata input : " + return_count);

        showLoadingCircle(false);
        return return_count;
    }


    // 스낵바로 알려줌.. 다시 만들기
   void showDemoInfo(int count) {
       //final int max_num = max;

       Snackbar snackbar = Snackbar
               .make(mTopFrameLayout, count + " "+getString(R.string.msg_demo_data_created), Snackbar.LENGTH_LONG)
               .setAction(getString(R.string.msg_delete), new View.OnClickListener() { // remake
                   @Override
                   public void onClick(View view) {
                       deleteDemoData();
                   }
               });

       snackbar.show();
   }

    // left drawer
// 왼쪽 드로어

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) { //Navigation의 아이템을 선택했을때 무엇을 실행할지

        // update the main content by replacing fragments
        // 여기에 메뉴를 선택했을 경우에 해야 할 일을 처리한다.
        //Intent intent;
        int mSelectedId;
        menuItem.setChecked(true);
        mSelectedId = menuItem.getItemId();
        switch (mSelectedId) {

            case R.id.drawer_menu_header:
                mDrawerLayout.closeDrawer(GravityCompat.START);     //Drawer를 닫음
                return true;

            case R.id.main_drawer_item_01: { // 싱크
                mDrawerLayout.closeDrawer(GravityCompat.START);     //Drawer를 닫음
                selectAccount(true); // start sync
                return true;
            }
            case R.id.main_drawer_item_02: // 에러체크
                // Toast.makeText(this, "Menu item selected -> " + position, Toast.LENGTH_SHORT).show();
            {
                mDrawerLayout.closeDrawer(GravityCompat.START);     //Drawer를 닫음

                Intent intentNext = new Intent(this, ErrorContactsListActivity.class);
                startActivity(intentNext);
                return true;
            }


            case R.id.main_drawer_item_04: { // 설정 Settings
                mDrawerLayout.closeDrawer(GravityCompat.START);
                Intent SettingActivity = new Intent(this, SettingsActivity.class);
                startActivityForResult(SettingActivity, Constants.SETTINGS_REQUEST); // when settings changed then apply it immediately
                return true;
            }

            case R.id.main_drawer_item_05: // about
                mDrawerLayout.closeDrawer(GravityCompat.START);     //Drawer를 닫음
                OpensourceFragment.displayLicensesFragment(getSupportFragmentManager());
                return true;
        }
        return false;
    }

    // Loader callback

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        LatLng  currentLatLng = getCurrentLatLng();

        String[] projection = {
                PowerContactContract.PowerContactEntry.COLUMN_CONTACT_ID,
                PowerContactContract.PowerContactEntry.COLUMN_DATA_ID,
                PowerContactContract.PowerContactEntry.COLUMN_LOOKUP_KEY,
                PowerContactContract.PowerContactEntry.COLUMN_NAME,
                PowerContactContract.PowerContactEntry.COLUMN_ADDR,
                PowerContactContract.PowerContactEntry.COLUMN_TYPE,
                PowerContactContract.PowerContactEntry.COLUMN_LABEL,
                PowerContactContract.PowerContactEntry.COLUMN_PHOTO,
                PowerContactContract.PowerContactEntry.COLUMN_LAT,
                PowerContactContract.PowerContactEntry.COLUMN_LNG,
                PowerContactContract.PowerContactEntry.COLUMN_SIN_LAT,
                PowerContactContract.PowerContactEntry.COLUMN_SIN_LNG,
                PowerContactContract.PowerContactEntry.COLUMN_COS_LAT,
                PowerContactContract.PowerContactEntry.COLUMN_COS_LNG,
        };

        Log.v(LOG_TAG, "LOADER:MainActivity/onCreateLoader : distance " +mPowerContactSettings.getDistance());

        int mode;

        mode = (mPowerContactSettings.isDemoMode())?
             PowerContactContract.PowerContactEntry.POWERCONTACT_MODE_DEMO :
                                   PowerContactContract.PowerContactEntry.POWERCONTACT_MODE_NORMAL;

        Log.v(LOG_TAG, "LOADER:MainActivity/onCreateLoader: DEMOMODE"+mode);

        return new CursorLoader(this,
                PowerContactContract.PowerContactEntry.buildLocationByDistance
                        (currentLatLng.latitude, currentLatLng.longitude,
                                mPowerContactSettings.getDistance(), mode),
                projection, // projection // only works
                null, // cols for "where" clause // ignored 어차피 무시됨
                null, // values for "where" clause // ignored 어차피 무시됨
                PowerContactContract.PowerContactEntry.COLUMN_AS_DISTANCE + " DESC");  // sort order --> distance);
    }

    // Query index

    static final int INDEX_COL_CONTACT_ID = 0;
    static final int INDEX_COL_DATA_ID = 1;
    static final int INDEX_COL_LOOKUP_KEY = 2;
    static final int INDEX_COL_NAME = 3;
    static final int INDEX_COL_ADDR = 4;
    static final int INDEX_COL_TYPE = 5;
    static final int INDEX_COL_LABEL = 6;
    static final int INDEX_COL_PHOTO = 7;
    static final int INDEX_COL_LAT = 8;
    static final int INDEX_COL_LNG = 9;
    static final int INDEX_COL_DISTANCE = 14;

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        Log.v(LOG_TAG, "LOADER:MainActivity/onLoaderFinished : cursor num " + cursor.getCount());

        mPowerContactArrayList.clear(); // first clear!

        if (cursor.moveToFirst()) {
            do {
                long contact_id = cursor.getLong(INDEX_COL_CONTACT_ID);
                long data_id = cursor.getLong(INDEX_COL_DATA_ID);
                String lookup_key = cursor.getString(INDEX_COL_LOOKUP_KEY);
                String name = cursor.getString(INDEX_COL_NAME);
                String addr = cursor.getString(INDEX_COL_ADDR);
                int type = cursor.getInt(INDEX_COL_TYPE);
                String label = cursor.getString(INDEX_COL_LABEL);
                double lat = cursor.getDouble(INDEX_COL_LAT);
                double lng = cursor.getDouble(INDEX_COL_LNG);
                double dist = cursor.getDouble(INDEX_COL_DISTANCE); // distance
                String photo = cursor.getString(INDEX_COL_PHOTO) ; // photo
                mPowerContactArrayList.add(new PowerContactAddress(contact_id, data_id, lookup_key, name, addr, type, label, photo, lat, lng, dist));

            } while (cursor.moveToNext());
        }

        Log.v(LOG_TAG, "LOADER:MainActivity/onLoaderFinished : powercontact size "+mPowerContactArrayList.size());

        // 맵쪽과 리스트쪽을 업데이트 한다.
        // 맵프래그먼트를 찾는다.
        if (mMapFragment != null) {

            Log.v(LOG_TAG, "LOADER:MainActivity/mMapFragment/setClusterManager :distance " + mPowerContactSettings.getDistance());
            mPreviousSearchKeyword=mSearchKeyword;
            mMapFragment.setClusterManager(mPowerContactArrayList, mPowerContactSettings.getMarkerType()); // 지도에 표시한다.
        }
        if (mListFragment != null) {
         //   mListFragment.setContactsList(mPowerContactArrayList, mPowerContactSettings.getDistance()); // 리스트에 표시한다.
            Log.v(LOG_TAG, "LOADER:MainActivity/mListFragment/restartLoader() : ");
            mListFragment.restartLoader();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // 맵쪽과 리스트쪽을 업데이트 한다.
        // 가만..
        // 다시 만들어야지... 위와 같이..
        Log.v(LOG_TAG, "LOADER:MainActivity/onLoaderReset : ");
        mPowerContactArrayList.clear(); // clear all data
         // 맵의 모든 것을 지운다 --> 어떻게?

        if (mMapFragment != null) {
            mPreviousSearchKeyword=mSearchKeyword;
            mMapFragment.setClusterManager(mPowerContactArrayList,  mPowerContactSettings.getMarkerType()); // 지도에 표시한다.
        }
        if (mListFragment != null) {
            //getSupportLoaderManager().restartLoader(Constants.CONTACTLIST_LOADER, null, mListFragment);
            mListFragment.setLoaderReset(); // call loader routain
            //mListFragment.setContactsList(mPowerContactArrayList, mPowerContactSettings.getDistance()); // 리스트에 표시한다.
        }
    }

    // -==================== ListView callbacks ===============================================
// mapview의 지도 마커를클릭한 효과 (listfragment에서 부름)

    // callback (ContactsListFragment call back)
    @Override
    public void makeMapClusterItemClick(PowerContactAddress item) {
        if (mMapFragment != null)
            mMapFragment.onClusterItemClickFromListView(item);
    }

//    // slide panel의 움직임을 on/off (listfragment에서 부름)
//    @Override
//    public void setSlidingUpPanelEnabled(boolean isTurnon) {
//        mSlidingUpPanelLayout.setEnabled(isTurnon);
//    }

    // 로더를 리스타트 --> 여기가 좀 삐걱거리나? 2016.1.3
    @Override
    public void restartMainLoader(double distance) {
        Log.v(LOG_TAG,"restartMainLoader ");
        mPowerContactSettings.setDistance(distance);

        // 로더를 리스타트 하면 된다. -->
        if (mCurrentLocation == null)
            isLoaderRestartNeed = true; // set true (when it connected restart)
        else {
            Log.v(LOG_TAG,"Main/Callback _restartMainLoader ");
            getSupportLoaderManager().restartLoader(Constants.POWERCONTACT_LOADER, null, MainActivity.this);
        }
    }

    @Override
    public ArrayList<PowerContactAddress> getContactAddress() {
        return mPowerContactArrayList;
    }


    // 셋팅값을 넘겨준다
    @Override
    public PowerContactSettings getPowerContactSettings() {
        return mPowerContactSettings;
    }

    // 해보자. 프로그래먼트를 크게....
    // http://stackoverflow.com/questions/26715942/change-the-size-of-a-fragment-with-a-button
    // --> 위는 가능성을 찾았고
    // http://stackoverflow.com/questions/32292656/percentrelativelayout-how-to-set-the-height-programatically
    // --> 여기서 대부분의 힌트를 얻었다.
    public void launchSearchFragment(boolean onoff) {

        if(mTwoPane) return; // twopane 모드에서는 동작하지 않는다.
        ActionBar actionBar =  getSupportActionBar();

        if(onoff==mIsExpandedFragment) return; // 현재와 같은 상태로 바꾸라는 것이므로 동작하지 않는다.

        FrameLayout view = (FrameLayout)findViewById(R.id.contact_list_view_container);
        PercentFrameLayout.LayoutParams params = (PercentFrameLayout.LayoutParams) view.getLayoutParams();
// This will currently return null, if it was not constructed from XML.
        PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();

        if(onoff) { // expand
            info.heightPercent = 1.0f;
            mIsExpandedFragment=true;

            // 액션바를 안보이게 한다

            if(actionBar!=null)
                actionBar.hide();

            // 위의 색깔도 바꾼다...
            //        // change status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.myColorPrimaryDark));
        }


            if(mListFragment!=null && mListFragment.mBtnHomeUp!=null)
                mListFragment.mBtnHomeUp.setVisibility(View.VISIBLE); // 리스트가 올라오면 홈버튼을 보이게 한다.

        }
        else { // collapse

            info.heightPercent = 0.4f;
            mIsExpandedFragment=false;

            // 액션바를 보이게 한다.
            if(actionBar!=null)
                actionBar.show();

            // 위의 색깔도 바꾼다...
            //        // change status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.myColorPrimaryDark));
        }

            if(mListFragment!=null && mListFragment.mBtnHomeUp!=null) {
                mListFragment.mBtnHomeUp.setVisibility(View.GONE); // 리스트가 내려가면 홈버튼도 없앤다.

                // 좀 체크하고... 이걸 해야 하는데...
//                mListFragment.mSearchView.setIconified(false);
            }

        }
        view.requestLayout();
//
//        LinearLayout layoutListView = (LinearLayout) findViewById(R.id.testtest);
//        lpListView = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,0);
//        if(onoff)
//            lpListView.weight=20;
//        else
//            lpListView.weight=2;
//        layoutListView.setLayoutParams(lpListView);

    }


    // 현재 expanded 되었는지 리턴한다.
    @Override
    public boolean isExpandedListViewFragment() {
        return mIsExpandedFragment;
    }

    // 새로운 프래그먼트를 열어볼까나...
    // listview call back

    // 현재 unit을 알아낸다. 1(meter) 또는 2(mile)을 리턴한다.
    public int getRealDistanceUnits() {
        return mPowerContactSettings.getRealDistanceUnits();
    }

    // 현재 거리를 셋팅한다.
    @Override
    public void setDistance(double distance) {
        mPowerContactSettings.setDistance(distance);
        Log.v(LOG_TAG, "SET DISTANCE : " + distance);
    }

    // callabck mapview -------------------------
    @Override
    public void selectListMarkerItem(PowerContactAddress item) {
        if (mListFragment != null)
            mListFragment.makeSelectItem(item);
    }

    // 마커타입을 리턴한다.
    @Override
    public int getMarkerType() {
        return mPowerContactSettings.getMarkerType();

    }

    @Override
    public void searchListMarkerItemByCoord(LatLng data) {
        if (mListFragment != null)
            mListFragment.searchItemByCoord(data);

    }
    /*

    http://developer.dramancompany.com/2015/11/%EB%A6%AC%EB%A9%A4%EB%B2%84%EC%9D%98-%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-6-0-m%EB%B2%84%EC%A0%84-%EB%8C%80%EC%9D%91%EA%B8%B0/

    참조해보자
     */

    public boolean getCurrentLocation() {

        return getCurrentLocation(true);
    }


    static boolean isRequestingLocationPermissions = false; // 지금 퍼미션을 요청 중인가?
    public boolean getCurrentLocation(boolean locationChange) {

        // for support mashmallow
        // http://googledevkr.blogspot.kr/2015/09/playservice81android60.html

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            if (mGoogleApiClient != null) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mCurrentLocation != null)
                    if(locationChange)
                        onMyLocationChanged();
                return true;
            }

        }
        else { // Marshmallow

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else
                {
                    // 퍼미션이 없으니 요청한다.


                    requestLocationPermissions(Constants.REQUEST_CODE_LOCATION);

                    // Request missing location permission.
//                    if (!isRequestingLocationPermissions ) {
//                        ActivityCompat.requestPermissions(this,
//                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                REQUEST_CODE_LOCATION);
//                        isRequestingLocationPermissions = true;
//                    }
                }
            } else { // if have permission
                if (mGoogleApiClient != null) {

                    mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mCurrentLocation != null)
                        if(locationChange)
                            onMyLocationChanged();
                    return true;
                }
            }
        }

        return false;
    }




    // 통합관리하자...

    // ACCESS_FINE_LOCATION 을 요청한다.. 결과 코드는 requestCode에 따라 달리 간다.
    @Override
    public void requestLocationPermissions(int requestCode) {

        // only 1) not requesting now 2) above marshmallow
        if (!isRequestingLocationPermissions
                && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    requestCode);

            isRequestingLocationPermissions = true;
        }
    }


    // 퍼미션 결과가 나왔다.
    // http://googledevkr.blogspot.kr/2015/09/playservice81android60.html
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_CODE_LOCATION) { // 현재 위치가 되돌아왔다. 하지만.. 그러면 어떻게.. ㅠㅠ
            isRequestingLocationPermissions = false; // 어쨌든 돌아온 셈.
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // success!
                // http://stackoverflow.com/questions/33562951/android-6-0-location-permissions
                //noinspection ResourceType <-- important
                mCurrentLocation =
                        LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mCurrentLocation != null)
                    onMyLocationChanged();


                if (mMapFragment != null && mMapFragment.mMap != null) {
                    //noinspection ResourceType <-- important
                    mMapFragment.mMap.setMyLocationEnabled(true); // my location enable
                }


            } else {
                // Permission was denied or request was cancelled
                Toast.makeText(this, R.string.msg_need_permission_location, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.REQUEST_CODE_CONTACTS) { // contacts

            isRequestingContactPermissions = false; //

            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if(mAccountName==null)
                    selectAccount(true); // 다시 루프를 탄다.
                else  {
                    startSyncDataIntentService(mAccountName);
                }

            } else { // fail
                Toast.makeText(this, R.string.msg_need_permission_contacts, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // get current location call back
    @Override
    public LatLng getCurrentLatLng() {
        //Location location = getCurrentLocation();
        if (mCurrentLocation != null)
            return new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        else {
            Log.e(LOG_TAG, "ERROR:at getCurrentLocation");
            return new LatLng(0.0f, 0.0f); // default value
        }
    }


    //---------

    // FetchAddressIntentService를 위한 루틴들
// https://github.com/udacity/google-play-services/blob/master/LocationLessons_Final/LocationLesson2_3/app/src/main/java/com/google/devplat/lmoroney/locationlesson2_3/MainActivity.java


    void selectAccount(boolean runService) {
        // 모든 계정종류 알기 :http://stackoverflow.com/questions/22174259/pick-an-email-using-accountpicker-newchooseaccountintent
//        AccountManager accountManager = AccountManager.get(getApplicationContext());
//        Account[] accounts = accountManager.getAccountsByType(null);
//        for (Account account : accounts) {
//            Log.d(LOG_TAG, "account: " + account.name + " : " + account.type);
//        }

        Intent aintent = AccountPicker.newChooseAccountIntent(null, null,   new String[] {"com.google"},
                true, getString(R.string.msg_support_account) , null, null, null); // 네번째것은 '항상'보일것
            // 옵션은 https://developers.google.com/android/reference/com/google/android/gms/common/AccountPicker#public-methods 참고

        int key = (runService)? Constants.REQUEST_ACCOUNT_SELECT_AND_RUN_SERVICE:  Constants.REQUEST_ACCOUNT_SELECT;
        startActivityForResult(aintent,key );

    }
    protected void startSyncDataIntentService(String accountName) {

        // show loading circle (또는 다이얼로그)
        // TODO

        if (accountName==null) return;

        if (onFetching) return; // it cannot running twice same time.

        // register message receiver
        if (messageReceiver == null) {
            messageReceiver = new MessageReciever();
        }



        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter(Constants.MESSAGE_EVENT)); //register receiver


// 이 전에 퍼미션을 얻어야 한다. READ_CONTACTS








        Intent intent = new Intent(this, FetchPowerAddressIntentService.class);
//        intent.putExtra(Constants.RECEIVER, mResultReceiver); // 이건 브로드캐스트로 바뀌면 없어져야 한다.
        intent.putExtra(Constants.SERVICE_ACCOUNT_NAME_EXTRA, accountName);
        //intent.putExtra(Constants.SERVICE_LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
        onFetching = true;
    }






    static boolean isRequestingContactPermissions = false; // 지금 퍼미션을 요청 중인가?
    public boolean getPermissionAndsyncData(String accountName) {

        // for support mashmallow
        // http://googledevkr.blogspot.kr/2015/09/playservice81android60.html

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) { // 이전버전은 그냥 깔끔하게 시작
            startSyncDataIntentService(accountName);
            return true;
        }
        else { // Marshmallow

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else
                {
                    // 퍼미션이 없으니 요청한다.

                   // requestLocationPermissions(Constants.REQUEST_CODE_CONTACTS);

                    if (!isRequestingContactPermissions
                            && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP ) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                Constants.REQUEST_CODE_CONTACTS);

                        isRequestingContactPermissions = true;
                    }


                    // Request missing location permission.
//                    if (!isRequestingLocationPermissions ) {
//                        ActivityCompat.requestPermissions(this,
//                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                REQUEST_CODE_LOCATION);
//                        isRequestingLocationPermissions = true;
//                    }
                }
            } else { // if have permission
                startSyncDataIntentService(accountName);
                return true;
            }
        }

        return false;
    }





    // 생명주기

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    // broadcast
    @Override
    protected void onDestroy() {
        if (messageReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            messageReceiver = null;
        }

        super.onDestroy();
    }

    // get location 구하기
    // package com.google.android.gms.location.sample.locationupdates;
    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(LOG_TAG, "Building GoogleApiClient");
        if (mGoogleApiClient==null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

//    @Override
//    public void onConnectionFailed(ConnectionResult result) {
//        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
//        // onConnectionFailed.
//        Log.i(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
//    }

    // http://googledevkr.blogspot.kr/2015/09/playservice81android60.html
    // 마시멜로우 대응

    // https://developers.google.com/android/guides/api-client
   @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            Log.e(LOG_TAG, "Already attempting to resolve an error.");

        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());

            //getErrorDialog(this, result.getErrorCode(), 0, null);
            mResolvingError = true;
        }
    }

        // The rest of this code is all about building the error dialog
    // https://developers.google.com/android/guides/api-client
    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    // https://developers.google.com/android/guides/api-client
    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }
        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(LOG_TAG, "Connected to GoogleApiClient");

        if (mCurrentLocation == null) {

            getCurrentLocation();
            Log.v(LOG_TAG, "Now getCurrentLocation / onConnected()");

        }
    }

    // toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        /*
        MenuItem demoMenu = menu.findItem(R.id.action_switch_demo);
         mDemoSwitch =  (SwitchCompat) MenuItemCompat.getActionView(demoMenu);


        // http://developer.android.com/guide/topics/ui/controls/togglebutton.html
        mDemoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleDemoMode(isChecked);
                // 화면 갱신
                Log.v(LOG_TAG, "Main/onCreateOptionsMenu/mDemoSwitch - restartLoader()");
                getSupportLoaderManager().restartLoader(Constants.POWERCONTACT_LOADER, null, MainActivity.this); // reset Loader


            }
        });


        if(mPowerContactSettings != null)
            if(mPowerContactSettings.isDemoMode())
                mDemoSwitch.setChecked(true);
            else
                mDemoSwitch.setChecked(false);
*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default : super.onOptionsItemSelected(item);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    //
    // activity Result =========================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == Constants.SETTINGS_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) { // ok there is some changed vaule, find & activate to current screen
                // PowerContactSettings  mLastSavedSettings = new PowerContactSettings(); // --> 이걸 갖고 있었어야 한다....
                PowerContactSettings changedSettings = new PowerContactSettings();
                readDefaultSettings(changedSettings); // 바뀐것을 읽어온다.

                if (!changedSettings.equals(mPowerContactSettings)) { // 만약 설정이 바뀌었다면...
                    // 여기서는 플래그를 가지고 설정한다.
                    // 어차피 여기를 지나면 onResume에서 두개의 로더는 리셋된다.
                    //스피너만 잘 처리하면 된다.
                    // 3. 스피너가 바뀔경우(스피너 완전체인지)
                    // 4. 스피너의 위치만 변하는 경우

                    boolean mustChangeSpinnerType = false;
                    boolean mustChangeSpinnerPosition = false;


                    if (changedSettings.getDistance() != mPowerContactSettings.getDistance()) {
                        // 거리가 변하면.. 스피너 포지션을 바꾸고.. 모든 로더를 리셋한다.

                        mustChangeSpinnerPosition = true;
//                        mustChangeAllLoader = true;
                        mPowerContactSettings.setDistance(changedSettings.getDistance()); // change ~!
                    }
                    if (changedSettings.getMarkerType() != mPowerContactSettings.getMarkerType()) {
//                        Toast.makeText(this, "Now change marker", Toast.LENGTH_SHORT).show();
                        mPowerContactSettings.setMarkerType(changedSettings.getMarkerType());
                    }
                    if (changedSettings.getRealDistanceUnits() != mPowerContactSettings.getRealDistanceUnits()) {
                        // 단위가 변했으면...
                        // 1. 스피너가 변해야 한다.
                        // 2. 리스트가 변해야 한다.(단위가 변해야 하므로)
                        mustChangeSpinnerType = true;
                        mustChangeSpinnerPosition = true;
//                        mustChangeListView=true;
                        mPowerContactSettings.setDistanceUnits(changedSettings.getRealDistanceUnits());
                        // 이때는 디폴트 값으로 변해야 마땅한데...??? --> settings에서 하기를 빌자.
                    }

                    // demo mode
                    if (changedSettings.isDemoMode() != mPowerContactSettings.isDemoMode()) {
                        // demo mode가 변했으면.. 별다른 것을 안해도 되나? --> 모두 리셋
                        mPowerContactSettings.setDemoMode(changedSettings.isDemoMode());
                    }


                    if (mustChangeSpinnerType) {
                        if (mListFragment != null) {
                            mListFragment.setDistanceSpinnerAdapter();
                        }
                    }

                    if (mustChangeSpinnerPosition) {
                        final int num_array_pref_range_distance_values;

                        // 로케일에 따라서 마일과 미터법을 설정한다.
                        if (mPowerContactSettings.getRealDistanceUnits() == Constants.DISTANCE_UNITS_METER) {
                            num_array_pref_range_distance_values = R.array.pref_range_distance_values_meter;
                        } else { // if (mPowerContactSettings.getRealDistanceUnits() == Constants.DISTANCE_UNITS_MILE)
                            num_array_pref_range_distance_values = R.array.pref_range_distance_values_mile;
                        }

                        if (mListFragment != null) {

                            // 이 루틴 복잡하니 메소드 하나 만드는 것이 나을지도 모르겠다...
                            mListFragment.mSpinnerDistance.setSelection(Arrays.asList(getResources().getStringArray(num_array_pref_range_distance_values))
                                    .indexOf((String.valueOf(mPowerContactSettings.getDistance())))); // set default distance
                            // 2. 리스트의 거리가 변해야 한다.

//                        Toast.makeText(this, "Now change marker", Toast.LENGTH_SHORT).show();
                        }

                    }

                    // 이루틴 후에는 onResume()에서 어차피 로더를 업데이트한다.

                }
            }
        } else if (requestCode == REQUEST_RESOLVE_ERROR) { // https://developers.google.com/android/guides/api-client
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        } else if (requestCode == Constants.REQUEST_ACCOUNT_SELECT || requestCode == Constants.REQUEST_ACCOUNT_SELECT_AND_RUN_SERVICE && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            // write to shared preference
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.SETTINGS_ACCOUNT_KEY, mAccountName);
            editor.apply();

            if (requestCode==Constants.REQUEST_ACCOUNT_SELECT_AND_RUN_SERVICE) // ok start service
                getPermissionAndsyncData(mAccountName);
        }
    }


    // current location button clicked on MapView
    @Override
    public void onMyLocationButtonClick() {
        getCurrentLocation(false); // don't update map, because this calls from map.. it is changed automatilcally

    }

    public void onMyLocationChanged() {

        //Log.v(LOG_TAG, "onLocationChanged() called");
        // onConnection에서 이 함수가 불린 경우
        if (!isLoaderInitAlready) {
            getSupportLoaderManager().initLoader(Constants.POWERCONTACT_LOADER, null, this);
            //Log.v(LOG_TAG, "LoaderInit/onLocationChanged()");
            isLoaderInitAlready = true;
        } else if (isLoaderRestartNeed) {
            getSupportLoaderManager().restartLoader(Constants.POWERCONTACT_LOADER, null, MainActivity.this);
            //Log.v(LOG_TAG, "Main/onLocationChanged()-restartLoader()");
            isLoaderRestartNeed = false;
        }


        if (mMapFragment != null) {
            //Log.v(LOG_TAG, "moveMapLocation/onLocationChanged()");
            mMapFragment.moveMapLocation(getCurrentLatLng());
        }


        // shared prefrerence에 마지막 위치를 기록한다.(위젯에서 사용함)
        // save last position (for using wdiget)

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.SETTINGS_LAST_LOCATION_LAT_KEY, String.valueOf(mCurrentLocation.getLatitude()));
        editor.putString(Constants.SETTINGS_LAST_LOCATION_LNG_KEY, String.valueOf(mCurrentLocation.getLongitude()));
        editor.apply();

    }

    // Broadcast receiver ====================================================================
    // get message from
    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int result = intent.getIntExtra(Constants.RESULT_KEY, Constants.FAILURE_RESULT);
            String msgKey = intent.getStringExtra(Constants.MESSAGE_KEY);
            if (msgKey != null)
                Toast.makeText(MainActivity.this, intent.getStringExtra(Constants.MESSAGE_KEY), Toast.LENGTH_LONG).show();

            switch (result) {
                // if message contains "service is ended" then delete it's result
                case Constants.SUCCESS_RESULT:
                case Constants.FAILURE_RESULT:

                    // 1) remove loading circle
                    // TODO
                    showLoadingCircle(false);
                    onFetching = false; // now turn off

                    // 2) remove broadcast receiver
                    LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(messageReceiver);
                    messageReceiver = null;
                    break;

                case Constants.START_LOADING:
                    showLoadingCircle(true); // start loding circle
                    break;
            }
        }
    }



} // ------------ end of program ---------------------------------------------------------------------------------
