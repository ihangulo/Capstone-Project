package com.hangulo.powercontact;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdSize;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*
*    SettingsActivity.java
*    -------------
*
*
*/

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.SettingsFragmentCallback {

    private final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private boolean isSettingChanged; // when settings changed this variable set true;
    static MyAdMob myAdmob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar toolbar;                              // Declaring the Toolbar Object

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Analytics tracking start
        ((AnalyticsApplication) getApplication()).startTracking();
        // use toolbar
        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);

        toolbar.setTitle(getString(R.string.title_activity_settings)); // 제목 설정

        setSupportActionBar(toolbar);
        // 앞으로가는 화살표 버튼
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_settings, new SettingsFragment())
                .commit();

        // admob
        myAdmob= new MyAdMob(this);
        myAdmob.showBannerAd();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // google analytics

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
      //  getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id)
        {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                processResult();
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        processResult();
        super.onBackPressed();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        Intent parentIntent = super.getParentActivityIntent();
        if (parentIntent != null)
            parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return parentIntent;
    }


    // before end, it needs some processing
    void processResult() {

        if (isSettingChanged) {
            setResult(RESULT_OK); // ok, it has some change value
        } else
            setResult(RESULT_CANCELED); // nope, there is no change
    }


    // Settings callback from fragment
    @Override
    public void setChanged () {
        isSettingChanged=true; // ok, some settings changed, when it backto MainActivity, it need some processing
    }

}
