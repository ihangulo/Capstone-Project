package com.hangulo.powercontact.util;

import java.util.Locale;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*   Created on : 2015-12-28
*
*    UnitLocale.java
*    -------------
*    Utility class for change unit (mile <-> kilo)
*
*/


/**
  *
 * Check country of using "MILE"
 * only for 'autometic' on settings

 * 마일을 쓰고 있는 곳을 자동으로 걸러낸다.
 * 셋팅에서 automatic일 경우에만 사용한다.
 *
 * http://stackoverflow.com/questions/4898237/using-locale-settings-to-detect-wheter-to-use-imperial-units
 *
 * if (UnitLocale.getDefault() == UnitLocale.Imperial) convertToimperial();
 */
public class UnitLocale {
    public static UnitLocale Imperial = new UnitLocale();
    public static UnitLocale Metric = new UnitLocale();

    public static UnitLocale getDefault() {
        return getFrom(Locale.getDefault());
    }
    public static UnitLocale getFrom(Locale locale) {
        String countryCode = locale.getCountry();
        // 오직 세나라만 빼고는 다 사용하는 미터법
        // only 3 countries use Imperial
        if ("US".equals(countryCode)) return Imperial; // USA
        if ("LR".equals(countryCode)) return Imperial; // liberia
        if ("MM".equals(countryCode)) return Imperial; // burma
        return Metric;
    }
}