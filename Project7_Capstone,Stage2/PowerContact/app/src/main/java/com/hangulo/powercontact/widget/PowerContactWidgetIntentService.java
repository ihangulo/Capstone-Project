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
package com.hangulo.powercontact.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.hangulo.powercontact.Constants;
import com.hangulo.powercontact.MainActivity;
import com.hangulo.powercontact.R;
import com.hangulo.powercontact.data.PowerContactContract;
import com.hangulo.powercontact.model.PowerContactAddress;


import java.util.ArrayList;


/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*
*    PowerContactWidgetIntentService.java
*    -------------
*
*
*/

/**
 * IntentService which handles updating all Today widgets with the latest data
 */
public class PowerContactWidgetIntentService extends IntentService {




    ArrayList<PowerContactAddress> powerContactArrayList = new ArrayList<>(); // save positions

    public PowerContactWidgetIntentService() {
        super("PowerContactWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                PowerContactWidgetProvider.class));

          final int INDEX_COL_CONTACT_ID = 0;
          final int INDEX_COL_DATA_ID = 1;
          final int INDEX_COL_LOOKUP_KEY = 2;
          final int INDEX_COL_NAME = 3;
          final int INDEX_COL_ADDR = 4;
          final int INDEX_COL_TYPE = 5;
          final int INDEX_COL_LABEL = 6;
          final int INDEX_COL_PHOTO= 7;
          final int INDEX_COL_LAT = 8;
          final int INDEX_COL_LNG = 9;
          final int INDEX_COL_DISTANCE = 14;
        // Get today's data from the ContentProvider
        //String location = Utility.getPreferredLocation(this);

        // get Current location from preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        double latnow = Double.parseDouble(prefs.getString(Constants.SETTINGS_LAST_LOCATION_LAT_KEY, "0.0"));
        double lngnow = Double.parseDouble(prefs.getString(Constants.SETTINGS_LAST_LOCATION_LNG_KEY, "0.0"));


        Cursor cursor = getWidgetData(latnow, lngnow);

        if (cursor==null || !cursor.moveToFirst()) {
            Log.v("Widget", "nodata");

            cursor.close();
            return;
        }

                String name = cursor.getString(INDEX_COL_NAME);
                String addr = cursor.getString(INDEX_COL_ADDR);
                String photo = cursor.getString(INDEX_COL_PHOTO);
                double dist = cursor.getDouble(INDEX_COL_DISTANCE); // distance



//        Log.v("Widget", "name=" + name + "addr " + addr + "lat=" + lat + " lng=" + lngnow);

        cursor.close();




        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = 0;//getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int largeWidth = 0;//getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);
            int layoutId=0; //
            layoutId = R.layout.widget_powercontact;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
           // views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, "PowerContact widget"); // description ---> 필수
            }
           // views.setTextViewText(R.id.widget_description, description);

            if (name==null || name.length()==0) {
                name=getString(R.string.msg_no_data_name);
                addr = getString(R.string.msg_no_data_addr);
            }
            views.setTextViewText(R.id.widget_name, name);
            views.setTextViewText(R.id.widget_addr, addr);


            Uri photoUri;
            if (photo == null || photo.length()==0) {
                photo = "";
                photoUri=null;
            } else
                photoUri = Uri.parse(photo);

            views.setImageViewUri(R.id.widget_icon, photoUri);


            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            launchIntent.putExtra(Constants.FROM_WIDGET_KEY, true); // is from widget?
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent,PendingIntent.FLAG_CANCEL_CURRENT);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);


            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    Cursor getWidgetData(double lat, double lng) {


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



        Cursor retCursor = getContentResolver().query(
                PowerContactContract.PowerContactEntry.buildLocationByDistance(lat, lng,
                        0.0f, PowerContactContract.PowerContactEntry.POWERCONTACT_MODE_NORMAL),
                projection,
                null,
                null,
                PowerContactContract.PowerContactEntry.COLUMN_AS_DISTANCE + " DESC");

        return retCursor;
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_powercontact_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_powercontact_default_width);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }
}

