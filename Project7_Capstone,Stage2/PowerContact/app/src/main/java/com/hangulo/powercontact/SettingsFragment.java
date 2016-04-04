package com.hangulo.powercontact;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.hangulo.powercontact.util.UnitLocale;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*
*    SettingsFragment.java
*    -------------
*
*
*/

public class SettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener{


    private final String LOG_TAG = SettingsFragment.class.getSimpleName();
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private SharedPreferences prefs;

    public interface SettingsFragmentCallback {
        void setChanged(); // 바뀌었음...을 위에 알림
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_all);

        // 여기서부터 값이 들어오면  onPreferenceChange 가 동작한다.
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //settings.setDistanceUnits(Integer.parseInt(prefs.getString(getString(R.string.pref_key_default_distance_units), String.valueOf(Constants.DISTANCE_UNITS_AUTO))));

        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_all, false); // write default value 디폴트 값으로 쓰기

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_default_marker_type)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_default_distance_units)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_default_range_distance)));

        setupRangeDistance();
        // Summary 보여주기
    }

    // units에 따라서.. range distance가 변한다.
    void setupRangeDistance() {
        setupRangeDistance(-1);
    }

    void setupRangeDistance (int unit) {

        if(prefs == null) return;

        int distance_unit;

        if (unit == -1) { // prefs에서 읽어온다

            distance_unit = Integer.parseInt(prefs.getString
                    (getActivity().getString(R.string.pref_key_default_distance_units),
                            String.valueOf(Constants.DISTANCE_UNITS_AUTO)));
        }
        else
            distance_unit = unit; // 넘어온 값 그대로 씀

        // 기본값은 미터로 설정
        int id_range_distance_titles= R.array.pref_range_distance_titles_meter;
        int id_range_distance_values = R.array.pref_range_distance_values_meter;
        String default_value = getString(R.string.pref_default_value_range_distance_meter);

        if (distance_unit != Constants.DISTANCE_UNITS_METER) {    // mile 인지 체크
            if ((distance_unit == Constants.DISTANCE_UNITS_AUTO && (UnitLocale.getDefault() == UnitLocale.Imperial))
                    || distance_unit == Constants.DISTANCE_UNITS_MILE) {

                // change to mile
                id_range_distance_titles = R.array.pref_range_distance_titles_mile;
                id_range_distance_values = R.array.pref_range_distance_values_mile;
                default_value =getString(R.string.pref_default_value_range_distance_mile);
            }
        }
            // change to mile or meter


        // 적용
        ListPreference lpRangeDistance = (ListPreference)findPreference(getActivity().getString(R.string.pref_key_default_range_distance));


        lpRangeDistance.setEntries(getResources().getStringArray(id_range_distance_titles));
        lpRangeDistance.setEntryValues(getResources().getStringArray(id_range_distance_values));
        lpRangeDistance.setDefaultValue(default_value);
        lpRangeDistance.setValue(default_value); // 바뀌었으면 디폴트 값으로 바꾼뒤에...

       // 서머리를 바꾼다.
        // change summary
        int prefIndex = lpRangeDistance.findIndexOfValue(default_value);

        lpRangeDistance.setSummary(lpRangeDistance.getEntries()[prefIndex]);
    }



    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        // ok, say that "it changed"  바뀌었다고 전해라~~~
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));

    }



    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context)
    {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static boolean isSimplePreferences(Context context)
    {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    // 값이 바뀌었을때.... 설정화면 자체의 변화를 내보이기
    // when the settings changed
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        ((SettingsFragmentCallback)getActivity()).setChanged(); // ok. there is some changed vaule


        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);

                // is this unit change?
             // 만약..이게 단위가 바뀐것인지 체크
                if (listPreference.getKey().equals(getActivity().getString(R.string.pref_key_default_distance_units))) {
                    // Yes 그렇다!
                    Log.v(LOG_TAG, "onPreferenceChange: distance_units");
                    setupRangeDistance(Integer.valueOf((listPreference.getEntryValues()[prefIndex]).toString() ));
                }
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }



}
