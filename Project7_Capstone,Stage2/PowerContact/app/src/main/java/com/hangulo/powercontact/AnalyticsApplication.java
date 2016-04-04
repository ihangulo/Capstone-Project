/* Copyright Google Inc. All Rights Reserved.
        *
        * Licensed under the Apache License, Version 2.0 (the "License");
        * you may not use this file except in compliance with the License.
        * You may obtain a copy of the License at
        *
        *     http://www.apache.org/licenses/LICENSE-2.0
        *
        * Unless required by applicable law or agreed to in writing, software
        * distributed under the License is distributed on an "AS IS" BASIS,
        * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        * See the License for the specific language governing permissions and
        * limitations under the License.
        */
/**
 * Created by hangulo on 2016-01-03.
 *
 * Google analytics
 *
 * based on
 * https://github.com/googlesamples/google-services/blob/master/android/analytics/app/src/main/java/com/google/samples/quickstart/analytics/AnalyticsApplication.java
 * https://github.com/udacity/Analytics_and_Tag_Manager/blob/master/DinnerApp_part2_start/app/src/main/java/com/example/android/dinnerapp/MyApplication.java
 *
 */

package com.hangulo.powercontact;


import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;


/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*
*    AnalyticsApplication.java
*    -------------
*    for google analytics
*
*
*/
/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class AnalyticsApplication extends Application {
    private Tracker mTracker;

    // Get the tracker associated with this app
    public void setupTracker() {

        // Initialize an Analytics tracker using a Google Analytics property ID.
        // Does the Tracker already exist?
        // If not, create it

        if (mTracker == null) {
            GoogleAnalytics ga = GoogleAnalytics.getInstance(this);

            // Get the config data for the tracker
            mTracker = ga.newTracker(R.xml.track_app);

            // Enable tracking of activities
            ga.enableAutoActivityReports(this);

            // https://developers.google.com/android/reference/com/google/android/gms/analytics/GoogleAnalytics
            mTracker.enableExceptionReporting(true);
            mTracker.enableAdvertisingIdCollection(true);
        }
    }

    public void startTracking() {

        // Initialize an Analytics tracker using a Google Analytics property ID.
        // Does the Tracker already exist?
        // If not, create it

        if (mTracker == null) {
            setupTracker();
        }
    }

    public Tracker getTracker() {
        // Make sure the tracker exists
        startTracking();

        // Then return the tracker
        return mTracker;
    }
}