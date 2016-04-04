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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;


import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.QuickContactBadge;
import android.widget.Spinner;
import android.widget.TextView;


import com.google.android.gms.maps.model.LatLng;
import com.hangulo.powercontact.model.PowerContactAddress;
import com.hangulo.powercontact.model.PowerContactSettings;
import com.hangulo.powercontact.util.CircleTransform;
import com.hangulo.powercontact.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/*
*   ================================================
*        Android Devlelopment Nanodegree
*        Project 8: Capstone, Stage 2 - Build
*        PowerContact by Kwanghyun Jung (ihangulo@gmail.com)
*   ================================================
*
*   date : Apr. 4th 2016
*
*    ContactsListFragment.java
*    -------------
*      List of Contacts (listview)
*
*
*/

/*
Based on
// http://developer.android.com/training/contacts-provider/retrieve-names.html
// http://developer.android.com/intl/ko/training/contacts-provider/display-contact-badge.html
 */


public class ContactsListFragment extends ListFragment implements
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<ArrayList<PowerContactAddress>> {

    final String LOG_TAG = ContactsListFragment.class.getSimpleName();

    private static final String SEARCH_KEYWORD_KEY = "SEARCH_KEYWORD";
    //private static final String DISTANCE_KEY = "DISTANCE";
    private static final String POSITION_KEY = "POSITION";
    private static final String SPINNER_KEY = "SPINNER";

    private boolean mTwoPane;

    private ContactsAdapter mAdapter; // The main query adapter
    private ArrayList<PowerContactAddress> mContactList;
    private View mRootView;
    public  android.support.v7.widget.SearchView mSearchView;
    private String mSearchKeyword =null; // searchString
//    private TextView textViewZipperIcon; // 검색결과 정보를 알려주는 라인
    //private Space mSpaceTopBlank; // 윗쪽 투명영역

    //private float mDistance; // 거리...
    private int mPosition=ListView.INVALID_POSITION ;
    public Spinner mSpinnerDistance;
    ImageView mBtnHomeUp; // 서치뷰에서 홈으로 가는 버튼(내리는 버튼)


    // callbacks --------------------------------
    ContactsListCallback mCallback; // callback
    public interface ContactsListCallback {

        void makeMapClusterItemClick(PowerContactAddress item);  // mapview의 지도 마커를클릭한 효과 (listfragment에서 부름).
        ArrayList<PowerContactAddress> getContactAddress();
        int getRealDistanceUnits(); // distance unit을 구한다.. 현재의... (1=meter 2=mile) 이때.. 0은 auto이므로 현재의 locale에서 구하는 것이 맞다.
        PowerContactSettings getPowerContactSettings(); // 현재 셋팅을 가져온다.
        void restartMainLoader(double distance); // 메인 로더를 리스타트한다.
        void setDistance(double distance); // main settings의 distance를 셋팅한다.
        void launchSearchFragment(boolean onoff) ; // search view를 한 번 열어보자..
        boolean isExpandedListViewFragment(); // 현재 프래그먼트가 커졌는지 여부

    }


    public ContactsListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwoPane = getResources().getBoolean(R.bool.two_pane);

        if(mContactList == null)
            mContactList = new ArrayList<>(0); // prevent null exception error

        // Create the main contacts adapter
        mAdapter = new ContactsAdapter(getActivity(), R.layout.contact_list_item, mContactList ); // 빈것
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the list fragment layout
        mRootView = inflater.inflate(R.layout.fragment_contact_list, container, false);
        mSearchView = (android.support.v7.widget.SearchView) mRootView.findViewById(R.id.widget_searchview);
        setupSearchView();


        // select distance spinner
        mSpinnerDistance = (Spinner) mRootView.findViewById(R.id.spinner_distance); // 반경 선택
        mBtnHomeUp = (ImageView)mRootView.findViewById (R.id.btn_home_up); // 서치뷰에서 홈으로 가는 버튼(내리는 버튼)
        mBtnHomeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.launchSearchFragment(false); // set serchview small 서치뷰를 작게 만든다.

                mSearchView.clearFocus();// clear focus of searchview 포커스도 뺏는다

            }
        });


        Log.v(LOG_TAG, "setDistanceSpinnerAdapter():");

        setDistanceSpinnerAdapter(); // 어댑터 관련 모두 셋팅
        Log.v(LOG_TAG, "setDistanceSpinnerAdapter() after initLoader:");
        getActivity().getSupportLoaderManager().initLoader(Constants.CONTACTLIST_LOADER, null, this).forceLoad();

        if (savedInstanceState != null) {

            Log.v(LOG_TAG, "restore position:" + savedInstanceState.getInt(SPINNER_KEY, 0));

            mSpinnerDistance.setOnItemSelectedListener(null);
            mSpinnerDistance.setSelection(savedInstanceState.getInt(SPINNER_KEY, 0)); // restore spinner

            mSearchKeyword= savedInstanceState.getString(SEARCH_KEYWORD_KEY);
            mPosition = savedInstanceState.getInt(POSITION_KEY,ListView.INVALID_POSITION );



        }
        return mRootView;
    }

    // get distance from MainActivity
    double getDistance() {
        return (mCallback.getPowerContactSettings()).getDistance();
    }

    void setupSearchView () {

        // Retrieves the system search manager service
        final SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        // Retrieves the SearchView from the search menu item

        if (mSearchView == null) return;

        // Assign searchable info to SearchView

        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

//      mSearchView.setSubmitButtonEnabled(true); // enable subit button
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);
        mSearchView.setFocusable(false);
        mSearchView.clearFocus();


        // text focus 리스너...
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!mTwoPane && !mCallback.isExpandedListViewFragment())
                        mCallback.launchSearchFragment(true); // 포커스가 오면 확장한다. 이미 확장상태면 안한다.
                } else {
                    if (!mTwoPane & mCallback.isExpandedListViewFragment()) {
                        mCallback.launchSearchFragment(false); // 이젠 내려라
                    }
                }
            }
        });

        // Set listeners for SearchView
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                mSearchView.clearFocus();

                if (!mTwoPane & mCallback.isExpandedListViewFragment() ) {
                    mCallback.launchSearchFragment(false); // 이젠 내려라
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Called when the action bar search text has changed.  Updates
                // the search filter, and restarts the loader to do a new query
                // using the new search string.
                //    isSearchMode=true;

                // if first String starts with "@@Search:" then wait until input complete
                // 만약 첫자가 @@Search:로 시작하면... 다 입력될때까지 기다린다.
                // @@Search:198.20123232,-111.20232323 와 같이 들어온다.
                //
                String newFilter = !TextUtils.isEmpty(newText) ? newText : null;

                // Don't do anything if the filter is empty
                if (mSearchKeyword == null && newFilter == null) {
                    return true;
                }
                // Don't do anything if the new filter is the same as the current filter
                if (mSearchKeyword != null && mSearchKeyword.equals(newFilter)) {
                    return true;
                }

                // Updates current filter to new filter
//                mPreviousSearchKeyword = mSearchKeyword; // 일단 저장해본다...아..아니다. 이건 서치가 시작되기 이전에 현재 유효한 값을가지고 있어야 한다.

                if (mSearchKeyword!=null && mSearchKeyword.startsWith("@@"))  // 만약 @@로 시작하면, @@로 끝나야지만..유효하다.
                    if(!mSearchKeyword.endsWith("@@"))
                        return true;

                // 이제 필터를 넘긴다. @@로 시작하는 것은 특수처리를 해준다. 다른 것은 그냥 String search를 해준다.
                mSearchKeyword = newFilter; // 필터를 줘서...이걸로 체크한다.
                getActivity().getSupportLoaderManager().restartLoader(Constants.CONTACTLIST_LOADER, null, ContactsListFragment.this);

                return true;
            }
        });

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up ListView, assign adapter and set some listeners. The adapter was previously
        // created in onCreate().
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    }


    // distance spinner
    void setDistanceSpinnerAdapter() {
        // title must be changed by locale / 로케일에 따라서 타이틀이 달라져야 한다.
        final int  num_array_pref_range_distance_titles;
        final int  num_array_pref_range_distance_values;

        Log.v(LOG_TAG, "setDistanceSpinnerAdapter distance");

        // set mile or meter by locale 로케일에 따라서 마일과 미터법을 설정한다.
        if (mCallback.getPowerContactSettings().getRealDistanceUnits() == Constants.DISTANCE_UNITS_METER) {
            num_array_pref_range_distance_titles = R.array.pref_range_distance_titles_meter;
            num_array_pref_range_distance_values = R.array.pref_range_distance_values_meter;
        }
        else {
            num_array_pref_range_distance_titles = R.array.pref_range_distance_titles_mile;
            num_array_pref_range_distance_values = R.array.pref_range_distance_values_mile;
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                num_array_pref_range_distance_titles, android.R.layout.simple_spinner_item); // R.layout.spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerDistance.setAdapter(adapter);
        // 리스너 셋팅
        mSpinnerDistance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                double distance = setSpinnerDistanceChanged(position, num_array_pref_range_distance_values); // change distance                 // 거리도바꾸고.. 메인로더를 리스타트한다..


                Log.v(LOG_TAG, "mSpinnerDistance/listener : loader restart distace:" + distance + "time" + System.currentTimeMillis());


                mCallback.restartMainLoader(distance); // restart main loader  // 이걸 부르려면.. 사용자가 직접 바꾼 경우만 해당된다. 화면 회전 등으로 바뀐 경우에는 바꾸면 안된다.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        String[] strArray = getResources().getStringArray(num_array_pref_range_distance_values);

        Double distance = getDistance(); // 현재의 디스턴스를 구한다.


        Log.v(LOG_TAG, "setDistanceSpinnerAdapter distance setSection" + distance);
        int i;
        for ( i=0; i< strArray.length; i++)
            if( Double.parseDouble(strArray[i]) == distance) {
                mSpinnerDistance.setSelection(i); // 2016.3.31 바꾸어본다.
               // setSpinnerDistanceChanged(i, num_array_pref_range_distance_values); // change distance
                break;
            }

        adapter.notifyDataSetChanged(); // http://stackoverflow.com/questions/9443370/how-to-update-an-spinner-dynamically-in-android-correctly



    }



    // Change spinner and return distance
    double setSpinnerDistanceChanged(int position, int num_array_pref_range_distance_values) {
        double distance;
        try {
            distance = Double.parseDouble(getResources().getStringArray(num_array_pref_range_distance_values)[position]); // 바뀐 값 설정
            Log.v(LOG_TAG, "setSpinnerDistanceChanged mCallback.setDistance(distance);"+distance);
            mCallback.setDistance(distance);

            return distance;
        } catch (NumberFormatException ex) {

            // if error, then set to default distance
            distance=  (mCallback.getPowerContactSettings().getRealDistanceUnits() == Constants.DISTANCE_UNITS_METER) ?
                        Constants.DEFAULT_DISTANCE_METER : Constants.DEFAULT_DISTANCE_MILE  ;
            mCallback.setDistance(distance);

            return distance;

        }

    }

    void setSpinnerDistanceChanged(int position) {
        int num_array_pref_range_distance_values;
        if (mCallback.getPowerContactSettings().getRealDistanceUnits() == Constants.DISTANCE_UNITS_METER) {
            num_array_pref_range_distance_values = R.array.pref_range_distance_values_meter;
        }
        else { // if (mPowerContactSettings.getRealDistanceUnits() == Constants.DISTANCE_UNITS_MILE)
            num_array_pref_range_distance_values = R.array.pref_range_distance_values_mile;
        }

        setSpinnerDistanceChanged(position, num_array_pref_range_distance_values);

        // 셋팅 값으로 설정한다.
        mSpinnerDistance.setSelection(Arrays.asList(getResources().getStringArray(num_array_pref_range_distance_values))
                .indexOf((String.valueOf(mCallback.getPowerContactSettings().getDistance())))); // set default distance

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // http://stackoverflow.com/questions/32083053/android-fragment-onattach-deprecated
        if (context instanceof Activity) {
            try {
                mCallback = (ContactsListCallback) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().toString()
                        + " must implement OnHeadlineSelectedListener");
            }
        }
    }



    void restartLoader() {
        getActivity().getSupportLoaderManager().restartLoader(Constants.CONTACTLIST_LOADER, null, this);
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        PowerContactAddress item =  mContactList.get(position); // 클릭한 것의 객체를 얻고
        mPosition = position; // 현재 선택된 포지션
        mCallback.makeMapClusterItemClick(item); // 넘겨준다.
    }




    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save mSearchkeyword

        outState.putString(SEARCH_KEYWORD_KEY, mSearchKeyword);
        //outState.putFloat(DISTANCE_KEY, mDistance);
        outState.putInt(POSITION_KEY , mPosition); // 이건..? 일단 얻고 업데이트
        outState.putInt(SPINNER_KEY, mSpinnerDistance.getSelectedItemPosition()); // spinner save --> 할필요 있나?

        //Log.v("SPINNER","SAVE POSTION:"+mSpinnerDistance.getSelectedItemPosition() );
        super.onSaveInstanceState(outState);

    }

    // 지도뷰에서 선택할 경우 이곳 리스트도 변화시켜준다
    void makeSelectItem (PowerContactAddress item) {
        for (int i =0 ; i <mContactList.size() ; i++) {
            if(item.equals(mContactList.get(i))){ // ok found!
                //getListView().setSelection(i);
                //getListView().smoothScrollToPosition(i);
                getListView().setItemChecked(i,true);
                getListView().setSelection(i);
                mPosition = i; // set current position
                break;
            }
        }
    }

    // when touch same position from MapView --> using search mode with @@ parameter
    // 지도뷰에서 같은 좌표가 있는 클러스터를 클릭했을때, 바로 서치모드로 들어간다.
    // 2016.2.2.1
    void searchItemByCoord(LatLng data) {
        String query = "@@"+data.latitude+","+data.longitude+"@@";
        mCallback.launchSearchFragment(true); // 리스트뷰를 확장한다. 이미 확장상태면 안한다.
        mSearchView.setQuery(query, false);
    }

    // 필요에 의해서 만듦
    // 지도쪽에 넘겨줄때 쓴다.
    public ArrayList<PowerContactAddress> getFilteredData() {
        if (mAdapter==null) return null;
        return mAdapter.getFilteredData();
    }

    // contact adapter -------------------------------
    private class ContactsAdapter extends ArrayAdapter<PowerContactAddress>  implements Filterable {
//            implements SectionIndexer {
        private TextAppearanceSpan highlightTextSpan; // Stores the highlight text appearance style
        private Context mContext;
        private ArrayList<PowerContactAddress>originalData = null;
        private ArrayList<PowerContactAddress>filteredData = null;
        private ItemFilter mFilter = new ItemFilter();


        public ContactsAdapter(Context context, int resource, ArrayList<PowerContactAddress> objects) {
            super(context,resource,objects );


            mContext = context;

         //   final String alphabet = context.getString(R.string.alphabet);
//            mAlphabetIndexer = new AlphabetIndexer(null, ContactsQuery.SORT_KEY, alphabet);
            highlightTextSpan = new TextAppearanceSpan(getActivity(), R.style.searchTextHiglight);
            // search때문에
            this.originalData = objects;
            this.filteredData = null ;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            PowerContactAddress contactAddressItem = getItem(position);

            LayoutInflater mInflater = (LayoutInflater) mContext
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) { // make new one

                convertView = mInflater.inflate(R.layout.contact_list_item, parent, false);

                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.address = (TextView) convertView.findViewById(R.id.address);
                holder.distance=(TextView) convertView.findViewById(R.id.distance);
                holder.photo_thumbnail = (QuickContactBadge) convertView.findViewById(R.id.photo_thumbnail);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final String displayName = contactAddressItem.getName();
            final String displayAddr = contactAddressItem.getAddr();
            String photo = contactAddressItem.getPhoto();
            Uri photoUri;
            if (photo == null || photo.length()==0) {
                photoUri=null;
            } else
                photoUri = Uri.parse(photo);

            int startIndex = indexOfSearchQuery(mSearchKeyword, displayName); // search for name

            // 이름과 색깔
            if (startIndex != -1) { // if found -- hilghlight name

                // Wraps the display name in the SpannableString
                final SpannableString highlightedName = new SpannableString(displayName);
                // Sets the span to start at the starting point of the match and end at "length"
                // characters beyond the starting point
                highlightedName.setSpan(highlightTextSpan, startIndex,
                        startIndex + mSearchKeyword.length(), 0);
                // Binds the SpannableString to the display name View object
                holder.name.setText(highlightedName);
            }
            else
                holder.name.setText(displayName);


            // now address
            startIndex = indexOfSearchQuery(mSearchKeyword, displayAddr); // search for name

            if (startIndex != -1) { // if found -- hilghlight name

                // Wraps the display name in the SpannableString
                final SpannableString highlightedAddr = new SpannableString(displayAddr);

                // Sets the span to start at the starting point of the match and end at "length"
                // characters beyond the starting point
                highlightedAddr.setSpan(highlightTextSpan, startIndex,
                        startIndex + mSearchKeyword.length(), 0);

                // Binds the SpannableString to the display name View object
                holder.address.setText(highlightedAddr); // highlited text
            }
            else
                holder.address.setText(displayAddr); // plain text


            // show distance 거리를 표시한다.
            holder.distance.setText(Utils.getDistanceValueStringWithUnits(
                    contactAddressItem.getDistance(),
                    mCallback.getRealDistanceUnits()));

            // photo
            final Uri contactUri = ContactsContract.Contacts.getLookupUri(
                    contactAddressItem.getContact_id(),
                    contactAddressItem.getLookup_key());

            // Binds the contact's lookup Uri to the QuickContactBadge
            holder.photo_thumbnail.assignContactUri(contactUri);
            //holder.photo_thumbnail.setMode(ContactsContract.QuickContact.MODE_MEDIUM);

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
            return convertView;
        }

        private class ViewHolder {
            TextView name;
            TextView address;
            TextView distance;
            QuickContactBadge photo_thumbnail;

        }


        // get from ContactList (android studio example)
        // ContactsListFragment.java
        private int indexOfSearchQuery(String searchTerm,String displayName) {
            if (!TextUtils.isEmpty(searchTerm)) {
                return displayName.toLowerCase(Locale.getDefault()).indexOf(
                        searchTerm.toLowerCase(Locale.getDefault()));
            }
            return -1;
        }

        // filtered counter

        public int getCount() {

            if (filteredData!=null ) // not yet filtered
                return filteredData.size();
            else
                return originalData.size();
        }

        public PowerContactAddress getItem(int position) {

            if (filteredData !=null)
                return filteredData.get(position);
            else
                return originalData.get(position);
        }

        // 필요에 의해서 만듦
        // 지도쪽에 넘겨줄때 쓴다.
        public ArrayList<PowerContactAddress> getFilteredData() {
            return filteredData;
        }


        // 필터 적용에 대해서 도움받음음
        //ttp://stackoverflow.com/questions/24769257/custom-listview-adapter-with-filter-android

        @Override
        public Filter getFilter() {
            return mFilter;
        }

        // 실제 서치를 담당하는 필터 부분
        // http://stackoverflow.com/questions/2519317/how-to-write-a-custom-filter-for-listview-with-arrayadapter
        // mlock에 대해서는 여기를 찾아보지만, 일단은 패스
        private class ItemFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                boolean coordMode = false; // 좌표로 찾는 것인지, 이름으로 찾는 것인지.
                LatLng filterLatLng = null;
                FilterResults results = new FilterResults();


                // No prefix is sent to filter by so we're going to send back the original array

                // back으로 지워나갈때는 오리지널 데이터가 모두 손상되는데, 어떻게 된 것일까?
                if (constraint == null || constraint.length() == 0) {
                    results.values = originalData;
                    results.count = originalData.size();

                    return results;
                }

                String filterString = constraint.toString().toLowerCase(); // search string

                if(filterString.startsWith("@@") )
                    if( !filterString.endsWith("@@")) {
                        // 이건 아직 다 입력되지 않은 관계로.. 앗. 돌려주면 안된다.
                        results.values = new ArrayList<PowerContactAddress>();
                        results.count = 0;
                        return results;
                    } else {
                        // 완성된 경우임. @@ 좌표, 좌표 @@
                        coordMode=true;

                        String temp = filterString.substring(2); // remove start @@
                        temp = temp.substring(0,temp.length()-2); // remove end @@
                        String coord[] = temp.split(","); // comma를 중심으로 둘로 나눈다.
                        if(coord.length!=2) { // 에러면... 아무것도 하지마.
                            results.values = originalData;
                            results.count = originalData.size();

                            return results;
                        }

                        try {
                            filterLatLng= new LatLng(Double.valueOf(coord[0]), Double.valueOf(coord[1]));

                        } catch( NumberFormatException e) {
                            Log.e(LOG_TAG, "invalid latlng search mode");
                        }
                    }


                final List<PowerContactAddress> originalList = originalData;

                int count = originalList.size();
                final List<PowerContactAddress> filteredList = new ArrayList<>();

                if(coordMode) { // coordinate mode
                    for (int i = 0; i < count; i++) {
                        LatLng filterableLatLng = originalList.get(i).getLatLng();
                        if (filterableLatLng.equals(filterLatLng)) { // 좌표가 같으면 더한다.
                            filteredList.add(originalList.get(i));
                        }
                    }
                } else { // keyword mode
                    String filterableStringName;
                    String filterableStringAddr;

                    for (int i = 0; i < count; i++) {
                        filterableStringName = originalList.get(i).getName(); // 이름만 비교한다.
                        filterableStringAddr = originalList.get(i).getAddr(); // 주소도 비교한다.

                        if (filterableStringName.toLowerCase().contains(filterString)) { // 이름이 같거나
                            filteredList.add(originalList.get(i));

                        } else if (filterableStringAddr.toLowerCase().contains(filterString)) { // 주소가 같거나..
                            filteredList.add(originalList.get(i));
                        }
                    }
                }


                results.values = filteredList;
                results.count = filteredList.size();

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredData = (ArrayList<PowerContactAddress>) results.values;

                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }


    public void setContactsList(ArrayList<PowerContactAddress> powerContactArrayList , String searchKeyword ) {

        if (powerContactArrayList == null) {
            if (mAdapter != null) {
                mAdapter.clear();  // 검색 정보줄 업데이트
            }
            return;
        }
        Log.v(LOG_TAG, "LOADER:List/onLoadFinished/setContactlist:"+ powerContactArrayList.size());

        if (mAdapter !=null) {
            mAdapter.clear();
            // setListAdapter(mAdapter);

            mAdapter.addAll(powerContactArrayList);
            // 이때 키워드도 같이 받아야 하지 않을까??????????????????????????????????
            if (searchKeyword == null || searchKeyword.trim().length()==0) { // null 이면 모두 다 보여주고... // ""이면 하나도 보여주지 않아야 한다.
                // 검색 정보줄 업데이트... 키워드 없음
                mSearchKeyword=""; // 이건 살펴봐야 한다. hangulo
                mAdapter.getFilter().filter(null);
            }
            else {
                mSearchKeyword = searchKeyword.trim();
                mAdapter.getFilter().filter(searchKeyword);
            }

            mAdapter.notifyDataSetChanged();

            Log.v(LOG_TAG, "LOADER:List/setContactsList/mAdapter:" + mAdapter.getCount());

        }
    }


    @Override
    public Loader<ArrayList<PowerContactAddress>> onCreateLoader(int id, Bundle args) {
        // 이 로더는 그냥 상위 액티비티 것을 가지고 오면 된다.
        return new ContactsListLoader( getActivity() ,  mCallback.getContactAddress() );
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<PowerContactAddress>> loader, ArrayList<PowerContactAddress> data) {
        // 데이터가 들어왔으면...

        Log.v(LOG_TAG, "onLoadFinished keyword:"+mSearchKeyword);
        mContactList = data;
        setContactsList(data, mSearchKeyword );


        // 여기서 해본다. (초기화)

        if(mPosition !=  ListView.INVALID_POSITION ) {
            getListView().smoothScrollToPosition(mPosition); // restore listview
            getListView().setSelection(mPosition);
            getListView().setItemChecked(mPosition,true);

//            mPosition = position; // 현재 선택된 포지션
            // 위의 맵도 선택하게 한다. --> 문제는 mContactList가 아직 만들어지지 않았다.
            if(mContactList!=null && mContactList.size()>mPosition) {
                Log.v(LOG_TAG, "now select map position = "+mPosition);
                PowerContactAddress item = mContactList.get(mPosition); // 클릭한 것의 객체를 얻고
                mCallback.makeMapClusterItemClick(item); // 넘겨준다.
            }

        }

    }
    @Override
    public void onLoaderReset(Loader<ArrayList<PowerContactAddress>> loader) {
        if (mAdapter!=null) {
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
        }

        Log.v(LOG_TAG, "ContactList/ LOADER:List/onLoaderReset  ");
    }

    // for mainactivity
    public void setLoaderReset() {

        if (mAdapter!=null) {
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
        }

    }


    // impements AsyncTaskLoader
    // http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html
    public static class ContactsListLoader extends AsyncTaskLoader<ArrayList<PowerContactAddress>> {

        private ArrayList<PowerContactAddress> mData;

        public ContactsListLoader(Context context, ArrayList<PowerContactAddress> data) {
            // Loaders may be used across multiple Activitys (assuming they aren't
            // bound to the LoaderManager), so NEVER hold a reference to the context
            // directly. Doing so will cause you to leak an entire Activity's context.
            // The superclass constructor will store a reference to the Application
            // Context instead, and can be retrieved with a call to getContext().
            super(context);

            mData=data;

        }
        @Override
        public ArrayList<PowerContactAddress> loadInBackground() {
            return mData ;  // --> 이거 다 인터페이스로 만들자.
        }

        // (2) Deliver the results to the registered listener **/
        @Override
        public void deliverResult(ArrayList<PowerContactAddress> data) {
            if (isReset()) {
                // The Loader has been reset; ignore the result and invalidate the data.
                releaseResources(data);
                return;
            }

            // Hold a reference to the old data so it doesn't get garbage collected.
            // We must protect it until the new data has been delivered.
            List<PowerContactAddress> oldData = mData;
            mData = data;

            if (isStarted()) {
                // If the Loader is in a started state, deliver the results to the
                // client. The superclass method does this for us.
                super.deliverResult(data);
            }

            // Invalidate the old data as we don't need it any more.
            if (oldData != null && oldData != data) {
                releaseResources(oldData);
            }
        }


        //(3) Implement the Loader’s state-dependent behavior

        @Override
        protected void onStartLoading() {
            if (mData != null) {
                // Deliver any previously loaded data immediately.
                deliverResult(mData);
            }

//            // Begin monitoring the underlying data source.
//            if (mObserver == null) {
//                mObserver = new SampleObserver();
//                // TODO: register the observer
//            }

            if (takeContentChanged() || mData == null) {
                // When the observer detects a change, it should call onContentChanged()
                // on the Loader, which will cause the next call to takeContentChanged()
                // to return true. If this is ever the case (or if the current data is
                // null), we force a new load.
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            // The Loader is in a stopped state, so we should attempt to cancel the
            // current load (if there is one).
            cancelLoad();

            // Note that we leave the observer as is. Loaders in a stopped state
            // should still monitor the data source for changes so that the Loader
            // will know to force a new load if it is ever started again.
        }

        @Override
        protected void onReset() {
            // Ensure the loader has been stopped.
            onStopLoading();

            // At this point we can release the resources associated with 'mData'.
            if (mData != null) {
                releaseResources(mData);
                mData = null;
            }

//            // The Loader is being reset, so we should stop monitoring for changes.
//            if (mObserver != null) {
//                // TODO: unregister the observer
//                mObserver = null;
//            }
        }

        @Override
        public void onCanceled(ArrayList<PowerContactAddress> data) {
            // Attempt to cancel the current asynchronous load.
            super.onCanceled(data);

            // The load has been canceled, so we should release the resources
            // associated with 'data'.
            releaseResources(data);
        }

        private void releaseResources(List<PowerContactAddress> data) {
            // For a simple List, there is nothing to do. For something like a Cursor, we
            // would close it in this method. All resources associated with the Loader
            // should be released here.
        }

    }
}
/*
http://kindlybugs.com/281
// 입력중인글자가 ㄱ,ㄴ,ㄷ,...ㅏㅑㅇ.. 일경우는 검색하지 않음
    function checkLastWord(text) {
        var last = text.substring(text.length - 1);
        var check = ["ㄱ", "ㄴ", "ㄷ", "ㄹ", "ㅁ", "ㅂ", "ㅅ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ",
                        "ㄲ", "ㄸ", "ㅃ", "ㅆ", "ㅉ", "ㅏ", "ㅑ", "ㅓ", "ㅕ", "ㅗ", "ㅛ", "ㅜ", "ㅠ", "ㅡ", "ㅣ", "ㅐ", "ㅒ", "ㅔ", "ㅖ"];
        for (var i = check.length - 1; i >= 0; i--) {
            if (last == check[i])
                return false;
        }
        return true;
    }


//            // 사진이 있다면...
//            if(photoUri != null) {
//                // make rounded image
//
//                try {
//                    // 비트맵을 만들고 클래스를 생성시켜서 바로 할당한다. 그런데 크기는?
//                    Bitmap bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
//                    RoundedBitmapDrawable circularBitmapDrawable =
//                            RoundedBitmapDrawableFactory.create(getContext().getResources(), bm);
//                    circularBitmapDrawable.setCircular(true);
//                    holder.photo_thumbnail.setImageDrawable(circularBitmapDrawable); // 여기에 해당 사람의 정보를 업데이트해야하는데...
//
//
//                } catch (IOException e) {
//
//                }
//            }
//            else
//                holder.photo_thumbnail.setImageBitmap(null);

 */

