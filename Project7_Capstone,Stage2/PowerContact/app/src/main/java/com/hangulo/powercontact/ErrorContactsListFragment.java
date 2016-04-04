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

package com.hangulo.powercontact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hangulo.powercontact.data.PowerContactContract;
import com.hangulo.powercontact.model.PowerContactAddress;
import com.hangulo.powercontact.service.FetchPowerAddressIntentService;
import com.hangulo.powercontact.util.CircleTransform;
import com.squareup.picasso.Picasso;

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
*    ErrorContactsListFragment.java
*    -------------
*      Fix problem
*     list of erorr sync
*
*
*/

// http://developer.android.com/training/contacts-provider/retrieve-names.html

public class ErrorContactsListFragment extends ListFragment implements
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    final String LOG_TAG = ErrorContactsListFragment.class.getSimpleName();


    private static final int ERROR_CONTACT_LOADER = 0;
    private static final String LAST_DATA_ID_KEY = "LAST_DATA_ID_KEY";
    private static final String LAST_CONTACT_ID_KEY = "LAST_CONTACT_ID";
    private static final String LAST_LOOKUP_KEY = "LAST_LOOKUP_KEY";

    private boolean mTwoPane;

    private ContactsCursorAdapter mAdapter; // The main query adapter
    private ArrayList<PowerContactAddress> mErrorContactList;
    private View mRootView;
    private String mSearchKeyword =null; // searchString


    private int mPosition=ListView.INVALID_POSITION ;
    public Spinner mSpinnerDistance;
    ImageView mBtnHomeUp; // 서치뷰에서 홈으로 가는 버튼(내리는 버튼)

    long mLastContactId=-1;
    long mLastDataId=-1;
    String mLastLookupKey;



    public ErrorContactsListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwoPane = getResources().getBoolean(R.bool.two_pane);

        if(mErrorContactList == null)
            mErrorContactList = new ArrayList<>(0); // prevent null exception error

        mAdapter = new ContactsCursorAdapter(getActivity(), null, 0 ); // 빈것
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the list fragment layout
        mRootView = inflater.inflate(R.layout.fragment_error_contact_list, container, false);
        getActivity().getSupportLoaderManager().initLoader(ERROR_CONTACT_LOADER, null, this);

        if (savedInstanceState != null) {


        }
        return mRootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up ListView, assign adapter and set some listeners. The adapter was previously
        // created in onCreate().
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        if (savedInstanceState != null) {

            mLastDataId = savedInstanceState.getLong(LAST_DATA_ID_KEY,-1);
            mLastContactId = savedInstanceState.getLong(LAST_CONTACT_ID_KEY,-1);
            mLastLookupKey = savedInstanceState.getString(LAST_LOOKUP_KEY, "");
        }

    }



    @Override
    public void onStart() {
        if(mPosition !=  ListView.INVALID_POSITION ) {
            getListView().smoothScrollToPosition(mPosition); // restore listview
            getListView().setSelection(mPosition);
        }
        super.onStart();
    }


    // 클릭시 --> Quick view를 보여준다.
    // http://developer.android.com/intl/ko/training/contacts-provider/modify-data.html
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        PowerContactAddress item =  mErrorContactList.get(position); // 클릭한 것의 객체를 얻고 --> 얻지 못한다.
        mPosition = position; // 현재 선택된 포지션

        mLastDataId = item.getData_id();
        mLastContactId=item.getContact_id();
        mLastLookupKey=item.getLookup_key();
        Uri mSelectedContactUri =ContactsContract.Contacts.getLookupUri(mLastContactId, mLastLookupKey);
        // Creates a new Intent to edit a contact
        Intent editIntent = new Intent(Intent.ACTION_EDIT);
    /*
     * Sets the contact URI to edit, and the data type that the
     * Intent must match
     */
        editIntent.setDataAndType(mSelectedContactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        // Sets the special extended data for navigation
        editIntent.putExtra("finishActivityOnSaveCompleted", true);
        // Sends the Intent
        getActivity().startActivityForResult(editIntent, Constants.RESULT_ERROR_CONTACTSLIST);

    }


    // 위에서 부른 수정창이 열리고 닫히면.. 여기로 온다.
    public void onAddressChanged() {

      //  Toast.makeText(getActivity(), "GET CODE", Toast.LENGTH_LONG).show();

        // ok, then check if this address is effective & can saved
        // 그렇다면.. 여기서 서비스를 불러야 하는데, 서비스에 한 데이터만 부르는 기능이 있던가?
        startIntentService();

    }

    boolean onFetching = false;
    private BroadcastReceiver messageReceiver;

    protected void startIntentService() {


        if (onFetching) return; // it cannot running twice same time.

        // register message receiver
        if (messageReceiver == null) {
            messageReceiver = new MessageReciever();
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageReceiver, new IntentFilter(Constants.MESSAGE_EVENT)); //register receiver


        Intent intent = new Intent(getActivity(), FetchPowerAddressIntentService.class);
        intent.putExtra(Constants.DATA_ID_KEY, String.valueOf(mLastDataId));
        intent.putExtra(Constants.CONTACT_ID_KEY, String.valueOf(mLastContactId));
        intent.putExtra(Constants.LOOKUP_KEY, mLastLookupKey);
        getActivity().startService(intent);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save mSearchkeyword

        outState.putLong(LAST_DATA_ID_KEY, mLastDataId);
        outState.putLong(LAST_CONTACT_ID_KEY, mLastContactId);
        outState.putString(LAST_LOOKUP_KEY,mLastLookupKey);
        super.onSaveInstanceState(outState);

    }

    // contact adapter -------------------------------
    private class ContactsCursorAdapter extends CursorAdapter {

        public ContactsCursorAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, 0);
        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.error_contact_list_item, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {


            ViewHolder holder = (ViewHolder) view.getTag();


            long contact_id = cursor.getLong(INDEX_COL_CONTACT_ID);
            long data_id = cursor.getLong(INDEX_COL_DATA_ID);
            String lookup_key = cursor.getString(INDEX_COL_LOOKUP_KEY);
            String displayName = cursor.getString(INDEX_COL_NAME);
            String displayAddr = cursor.getString(INDEX_COL_ADDR);
            int type = cursor.getInt(INDEX_COL_TYPE);
            String label = cursor.getString(INDEX_COL_LABEL);
            double lat = cursor.getDouble(INDEX_COL_LAT);
            double lng = cursor.getDouble(INDEX_COL_LNG);
            String photo = cursor.getString(INDEX_COL_PHOTO); // photo
            int data_type = cursor.getInt(INDEX_COL_DATA_TYPE); // demo(0), normal(1), error(-1)

            // 화면에 표시할 메시지
            String strStatusMsg = (data_type == PowerContactContract.PowerContactEntry.POWERCONTACT_MODE_NORMAL) ? getString(R.string.title_status_ok) : getString(R.string.title_status_error);

            Uri photoUri;
            if (photo == null || photo.length() == 0) {
                photo = "";
                photoUri = null;
            } else
                photoUri = Uri.parse(photo);

            holder.address.setText(displayAddr); // plain text
            holder.name.setText(displayName);
            holder.status_msg.setText(strStatusMsg); // 컬러도 바꾸자

            // photo
            final Uri contactUri = ContactsContract.Contacts.getLookupUri(
                    contact_id,
                    lookup_key);

            // Binds the contact's lookup Uri to the QuickContactBadge
            holder.photo_thumbnail.assignContactUri(contactUri);

            if (photoUri != null) { // defalut

                // http://stackoverflow.com/questions/26112150/android-create-circular-image-with-picasso
                Picasso.with(getActivity())
                        .load(photoUri)
                        .transform(new CircleTransform())
                        .into(holder.photo_thumbnail);
            } else {
                Picasso.with(getActivity())
                        .load(R.drawable.ic_person_40dp)
                        .transform(new CircleTransform())
                        .into(holder.photo_thumbnail);
            }

        }

        private class ViewHolder {
            TextView name;
            TextView address;
            TextView status_msg;
            //ImageView photo_thumbnail;// 원래 잘 돌아가던  루틴이미지 뷰
            QuickContactBadge photo_thumbnail;

            public ViewHolder(View view) {
                name = (TextView) view.findViewById(R.id.name);
                address = (TextView) view.findViewById(R.id.address);
                status_msg=(TextView) view.findViewById(R.id.my_status); // 정상 유무
                photo_thumbnail = (QuickContactBadge) view.findViewById(R.id.photo_thumbnail);

            }

        }


    }



    // Loader callback
    // Query index
    static final int INDEX_COL_COLUMN_ID=0;
    static final int INDEX_COL_CONTACT_ID = 1;
    static final int INDEX_COL_DATA_ID = 2;
    static final int INDEX_COL_LOOKUP_KEY = 3;
    static final int INDEX_COL_NAME = 4;
    static final int INDEX_COL_ADDR = 5;
    static final int INDEX_COL_TYPE = 6;
    static final int INDEX_COL_LABEL = 7;
    static final int INDEX_COL_PHOTO = 8;
    static final int INDEX_COL_LAT = 9;
    static final int INDEX_COL_LNG = 10;
    static final int INDEX_COL_DATA_TYPE=11;
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                PowerContactContract.PowerContactEntry.COLUMN_ID, // cursor adapter에서는 필수다.
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
                PowerContactContract.PowerContactEntry.COLUMN_CONTACT_DATA_TYPE,
        };

        Log.v(LOG_TAG, "LOADER:MainActivity/onCreateLoader : ");

        return new CursorLoader(getActivity(),
                PowerContactContract.PowerContactEntry.buildLocationByMode(PowerContactContract.PowerContactEntry.POWERCONTACT_MODE_ERROR), // only error status
                projection, // projection // only works
                null, // cols for "where" clause // ignored 어차피 무시됨
                null, // values for "where" clause // ignored 어차피 무시됨
                PowerContactContract.PowerContactEntry.COLUMN_NAME + " ASC");  // sort order --> by name);
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Log.v(LOG_TAG, "LOADER:MainActivity/onLoaderFinished : cursor num " + cursor.getCount());

      //  if (cursor==null) return;

        mErrorContactList.clear(); // first clear!

        if (data.moveToFirst()) {
            do {
                long contact_id = data.getLong(INDEX_COL_CONTACT_ID);
                long data_id = data.getLong(INDEX_COL_DATA_ID);
                String lookup_key = data.getString(INDEX_COL_LOOKUP_KEY);
                String name = data.getString(INDEX_COL_NAME);
                String addr = data.getString(INDEX_COL_ADDR);
                int type = data.getInt(INDEX_COL_TYPE);
                String label = data.getString(INDEX_COL_LABEL);
                double lat = data.getDouble(INDEX_COL_LAT);
                double lng = data.getDouble(INDEX_COL_LNG);
                //double dist = cursor.getDouble(INDEX_COL_DISTANCE); // distance
                String photo = data.getString(INDEX_COL_PHOTO) ; // photo
                mErrorContactList.add(new PowerContactAddress(contact_id, data_id, lookup_key, name, addr, type, label, photo, lat, lng, 0));

            } while (data.moveToNext());
        }

        mAdapter.swapCursor(data);
//
    }



    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mAdapter.swapCursor(null);
    }


    // Broadcast receiver ====================================================================
    // get message from
    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int result = intent.getIntExtra(Constants.RESULT_KEY, Constants.FAILURE_RESULT);
            String msgKey = intent.getStringExtra(Constants.MESSAGE_KEY);
            if (msgKey != null)
                Toast.makeText(getActivity(), intent.getStringExtra(Constants.MESSAGE_KEY), Toast.LENGTH_LONG).show();

            // 만약 실패면.. retry를 넣어서... 보여주면 어떨까?
            // 만약에 여기에 progress integer를 통해서 얼마나 남았는지 체크도 가능하다. pass1과 pass2에서....

            switch (result) {
                // if message contains "service is ended" then delete it's result
                case Constants.SUCCESS_RESULT:
                case Constants.FAILURE_RESULT:

                    Log.v(LOG_TAG, "Loader restarted");
                    getActivity().getSupportLoaderManager().restartLoader(ERROR_CONTACT_LOADER, null, ErrorContactsListFragment.this);

                    LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageReceiver);
                    messageReceiver = null;
                    break;

                case Constants.START_LOADING:
                    break;
            }
        }
    }

}