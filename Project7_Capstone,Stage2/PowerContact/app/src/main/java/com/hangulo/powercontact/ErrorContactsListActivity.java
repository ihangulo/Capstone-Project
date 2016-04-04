package com.hangulo.powercontact;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*
*    ErrorContactsListActivity.java
*    -------------
*      Fix problem
*     list of erorr sync
*
*
*/


public class ErrorContactsListActivity extends AppCompatActivity  {

    private final String LOG_TAG = ErrorContactsListActivity.class.getSimpleName();
    private final String TAG_LIST_FRAGMENT = "list_fragment_tag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar toolbar;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_contacts_list);

        // Analytics tracking start
        ((AnalyticsApplication) getApplication()).startTracking();
        // use toolbar
        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbar.setTitle(getString(R.string.title_error_contacts_list)); // 제목 설정
        setSupportActionBar(toolbar);

        // 앞으로가는 화살표 버튼
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_error_contacts_list, new ErrorContactsListFragment(), TAG_LIST_FRAGMENT)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                //this.finish();
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == Constants.RESULT_ERROR_CONTACTSLIST) {

            // find fragment
            ErrorContactsListFragment fragment = (ErrorContactsListFragment) getSupportFragmentManager().findFragmentByTag(TAG_LIST_FRAGMENT);
            if (fragment!=null) {

                fragment.onAddressChanged();

            }
        }
    }

}

