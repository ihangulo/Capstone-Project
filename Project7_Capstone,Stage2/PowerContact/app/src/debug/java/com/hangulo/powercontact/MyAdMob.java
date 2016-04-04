package com.hangulo.powercontact;

import android.app.Activity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung
*   ================================================
*
*   date : Apr. 4th 2016
*   Created on : 2016-01-03
*
*     Kwanghyun JUNG
*     ihangulo@gmail.com
*
*    Android Devlelopment Nanodegree
*    Udacity
*
*    MyAdMob.java
*    -------------
*    Admob utility
**
*/
public class MyAdMob implements MyAdMobBanner {


    private AdView mBannerAdView;

    public MyAdMob(Activity activity) {
        mBannerAdView= (AdView) activity.findViewById(R.id.adView);
    }


    // --- Google Admob -----
    // Banner Ad
    @Override
    public void showBannerAd() {

        AdRequest adRequest = new AdRequest.Builder()
                .build();
        if (mBannerAdView!=null)
            mBannerAdView.loadAd(adRequest);
    }



}