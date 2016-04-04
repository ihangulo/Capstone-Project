/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.hangulo.powercontact.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.model.LatLng;
import com.hangulo.powercontact.Constants;
import com.hangulo.powercontact.data.PowerContactContract;


/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*
*    Utils.java
*    -------------
*    Utility class
*
*/
public class Utils {

    // Prevents instantiation.
    private Utils() {}

    // 키보드 접기
    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }


    // check current units and return (km, mile)
    // 모드에 따라서 km인지 mile인지를 체크하고 그대로 돌려준다.
    // val = meter
    public static String getDistanceValueStringWithUnits(double val, int units) {

        switch (units) {

            case Constants.DISTANCE_UNITS_METER :
                return getDistanceValueStringWithMeter(val);

            case Constants.DISTANCE_UNITS_MILE :
                return getDistanceValueStringWithMile(  getMileFromMeter(val)); // change to meter to mile


        }
        return String.valueOf(val); // error

    }


    // // check current units and return with units (km, mile)
    // 단위까지 넣어서.. m이면 m로, km이면 km로 (그냥km)
    public static String getDistanceValueStringWithMeter(double val) {
        if (val >=1000) {
            return String.format("%.1fKm", (float)(val/1000));
        }
        else
            return String.format("%dm",Math.round(val));

    }

    public static String getDistanceValueStringWithMile(double val) {
            return String.format("%.2fmi",val);
    }


    // Check internet connection
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // http://stackoverflow.com/questions/12301510/how-to-get-the-actionbar-height
    // 어트리뷰트의 높이 구하기. return by pixel
    public static int getAttributeHeight(  Context ctx, int resid) {
        int retHeight = 0;
        TypedValue tv = new TypedValue();
        if (ctx.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            retHeight = TypedValue.complexToDimensionPixelSize(tv.data, ctx.getResources().getDisplayMetrics());

        }
        return  retHeight;
    }

    // http://stackoverflow.com/questions/8309354/formula-px-to-dp-dp-to-px-android

//http://stackoverflow.com/questions/4605527/converting-pixels-to-dp
//The above method results accurate method compared to below methods
//http://stackoverflow.com/questions/8309354/formula-px-to-dp-dp-to-px-android

    public static float convertPixelsToDp(Context ctx, float px){
        DisplayMetrics metrics = ctx.getResources().getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }

    public static float convertDpToPixel(Context ctx, float dp){
        DisplayMetrics metrics = ctx.getResources().getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }


    // 스크린사이즈를 구한다(높이)
    public static int getScreenHeightSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //int width = size.x;
        return(size.y);
    }

    // 스크린사이즈를 구한다(넓이)
    public static int getScreenWidthtSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //int width = size.x;
        return(size.x);
    }



    private void showInputMethod(Context ctx, View view) {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }


    /**
     * Uses static final constants to detect if the device's platform version is ICS or
     * later.
     */
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }


    // get Random Number
    // https://github.com/kthcorp/Snippets/blob/master/SQLiteDistanceDemo/src/com/kth/common/utils/etc/NumberUtils.java
    public static int getRandomNumber(int $min, int $max)
    {
        return $min + (int) (Math.random() * ($max - $min));
    }

    public static float getRandomNumber(float $min, float $max)
    {
        return (float) ($min + (Math.random() * ($max - $min)));
    }

    public static double getRandomNumber(double $min, double $max)
    {
        return (double) ($min + (Math.random() * ($max - $min)));
    }

    // 지정된 위치에서부터 거리 X값. 기준점 here_lat
    // get Distance X, from 'here_lat'
    public static double getDistanceX (double here_lat, double here_lng, double lat, double lng) {

            final double sinLat = Math.sin(Math.toRadians(lat));
            final double cosLat = Math.cos(Math.toRadians(lat));
            final double sinLng = Math.sin(Math.toRadians(lng));
            final double cosLng = Math.cos(Math.toRadians(lng));

            return   cosLat * Math.cos(Math.toRadians(here_lat))
                    * (Math.cos(Math.toRadians(here_lng))*cosLng +(Math.sin(Math.toRadians(here_lng)) * sinLng))
                    + sinLat * (Math.sin(Math.toRadians(here_lat)));
    }


    // convert between Meter - Mile
    public static float getMileFromMeter(float meter) {
        return 0.00062137f * meter;
    }

    // change Units (for future using)
    public static double getMileFromMeter(double meter) {
         return 0.00062137 * meter;
    }

    public static double getMileFromKiloMeter(double kilometer) {
        return 0.621371 * kilometer;
    }

    public static double getMeterFromMiile (double mile) {
        return 1609.34* mile;
    }

    public static double getKiloMeterFromMile(double mile ) {
        return  1.60934 * mile ;
    }

    public static double getMeterFromYard(double yard) {
        return 0.9144*yard;
    }

    public static double getYardFromMeter(double meter) {
        return 1.09361*meter;
    }


    // not used this version
    public static void swapFragment( int container1, int container2, FragmentManager fm ) {

        Fragment f1 = fm.findFragmentById(container1);
        Fragment f2 = fm.findFragmentById(container2);

        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(f1);
        ft.remove(f2);
        ft.commit();
        fm.executePendingTransactions();

        ft = fm.beginTransaction();
        ft.add(container1, f2);
        ft.add(container2, f1);
        //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        ft.commit();
    }

}
