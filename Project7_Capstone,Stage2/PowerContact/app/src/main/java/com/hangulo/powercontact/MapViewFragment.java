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
package com.hangulo.powercontact;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.hangulo.powercontact.model.PowerContactAddress;
import com.hangulo.powercontact.util.CircleTransform;
import com.hangulo.powercontact.util.PermissionUtils;
import com.squareup.picasso.Picasso;


import java.io.IOException;
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
*    MapViewFragment.java
*    -------------
*    Map view
*
*/

/*

http://developer.android.com/reference/android/location/Geocoder.html
http://wptrafficanalyzer.in/blog/locating-user-input-address-in-google-maps-android-api-v2-with-geocoding-api/

https://github.com/udacity/google-play-services/tree/master/LocationLessons_Final/LocationLesson2_1


* Help site 참고하면 좋은 사이트
http://developer.android.com/training/location/display-address.html  (Displaying a Location Address)
https://github.com/googlesamples/android-play-location


// https://github.com/googlemaps/android-samples/blob/master/ApiDemos/app/src/main/java/com/example/mapdemo/MyLocationDemoActivity.java


 */
public class MapViewFragment extends Fragment implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMyLocationButtonClickListener,

        ClusterManager.OnClusterClickListener<PowerContactAddress>,
        ClusterManager.OnClusterInfoWindowClickListener<PowerContactAddress>,
        ClusterManager.OnClusterItemClickListener<PowerContactAddress>,
        ClusterManager.OnClusterItemInfoWindowClickListener<PowerContactAddress>  {

    private final String LOG_TAG = MapViewFragment.class.getSimpleName();
    final static int DEFAULT_ZOOM_SIZE = 17; // default zoom level
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    final static String CAMERA_POSITION_KEY="CAMERA_POSITION";

    final static String NEED_SHOW_INFO_WINDOW_KEY="NEED_SHOW_INFO_WINDOW_KEY";
    final static String NEED_TO_CLICK_ITEM_KEY="NEED_TO_CLICK_ITEM_KEY";
    final static String NEED_TO_REFRESH_DATA_KEY = "NEED_TO_REFRESH_DATA_KEY";

    GoogleMap mMap;
    boolean mTwoPane;

    int mMarkerType = 1; //
    // 0: default marker 1:name 2: photo+name 3: photo  0:디폴트 마커 1:이름만 2:사진+이름 (사진없으면이름만) 3:사진만 (사진없으면 그냥 얼굴만?)

    private boolean mPermissionDenied = false;
    private ClusterManager<PowerContactAddress> mClusterManager;
    IconGenerator mIconFactory;

    View mPhotoProfile; // 프로필 사진을 포함한 내 프로파일 뷰

    View rootView;

    LatLng mCurrentLatLng ; //= new LatLng(37.4789134,126.8773348) ; // 가산동 현재 빌딩
    float mZoomLevel=17.0f; // defalut zoom level
    static CameraPosition savedCameraPosition; // 저장하는 카메라 포지션

    PowerContactClusterRenderer mClusterRenderer;

    PowerContactAddress mLastClickedItem=null; // save last marker
    boolean needRefreshData = false; // 데이터를 업데이트 해야 함 (onMapReady에서)
    boolean needShowInfoWindowMarker=false; //  마커를 눌러야 하는데 아직 맵이 레이디 되지 않으면.. (로테이션시 나타남)
    PowerContactAddress mNeedToclickItem; // 바로 눌러야 할 아이템

    // callbacks --------------------------------
    MapViewCallback mCallback; // callback

    public interface MapViewCallback {
        void selectListMarkerItem(PowerContactAddress item); // 마커를 클릭하면 리스트뷰를 선택하듯이 보여준다...?(scroll & change color)
        void searchListMarkerItemByCoord(LatLng data); // 동일한 주소가 있는 것이므로 바로 서치모드로 들어가서 보여준다.
        LatLng getCurrentLatLng() ;   // 현재 위치를 돌려준다.
        void onMyLocationButtonClick(); // 현재 위치가 변했음을.. 알린다.
        void requestLocationPermissions(int requestCode); // 퍼미션을 얻는다 (>23)
        ArrayList<PowerContactAddress> getContactAddress();
        int getMarkerType(); // 마커 타입을 얻는다.
    }
    // callbacks --------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mTwoPane = getResources().getBoolean(R.bool.two_pane); // two_pane mode check
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        if (rootView == null)
            return null;

        if(savedInstanceState!=null) {
            savedCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION_KEY);
            needShowInfoWindowMarker = savedInstanceState.getBoolean(NEED_SHOW_INFO_WINDOW_KEY, false);
            mNeedToclickItem = savedInstanceState.getParcelable(NEED_TO_CLICK_ITEM_KEY);
            needRefreshData = savedInstanceState.getBoolean(NEED_TO_REFRESH_DATA_KEY, false);
        }

        Log.v(LOG_TAG, "Mapview created");

        return rootView;
    }


    // 외부에서 클러스터 매니저를 셋팅 (클릭시)
    public void setClusterManager(ArrayList<PowerContactAddress> powerContactArrayList, int markerType) {

        Log.v(LOG_TAG, "setClusterManager..  ");

        if(mClusterManager == null) {
            needRefreshData =true; // --> call when onMapReady()
            return; // error or reset
        }

        Log.v(LOG_TAG, "setClusterManager.. mClusterManager not null");
        mMarkerType = markerType;

        mClusterManager.clearItems(); // delete previous data

        if (mMap != null)
            mMap.clear();

        mClusterManager.addItems(powerContactArrayList);
        mClusterManager.cluster(); // force to be clustering

        needRefreshData =false; // reset
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpMapIfNeeded();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // 현재 지도의 위치, zoom level을 저장한다. (나중에 열릴때 바꾼다.)
        if(mMap!=null) {
            savedCameraPosition = mMap.getCameraPosition();

            outState.putParcelable(CAMERA_POSITION_KEY, savedCameraPosition);
            // 클릭되어 show되고 있는 포인트를 저장한다.

        }


        outState.putBoolean(NEED_SHOW_INFO_WINDOW_KEY, needShowInfoWindowMarker);
        outState.putParcelable(NEED_TO_CLICK_ITEM_KEY, mNeedToclickItem);
        outState.putBoolean(NEED_TO_REFRESH_DATA_KEY, needRefreshData);

        //float zoom= cameraPosition.zoom;
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // http://stackoverflow.com/questions/32083053/android-fragment-onattach-deprecated
        if (context instanceof Activity) {
            try {
                mCallback = (MapViewCallback) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().toString()
                        + " must implement OnHeadlineSelectedListener");
            }
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            //mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

            // http://codentrick.com/retrieve-supportmapfragment-when-show-google-map-on-android-fragment/
            SupportMapFragment mapFrag =  (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFrag!=null) {
               // mMap = mapFrag.getMap();
                mapFrag.getMapAsync(this);
            }
        }
    }


    private void setUpClusterMap (){
        // 사진으로 바꾼 클러스터링

        Log.v(LOG_TAG, "setupclustermap()... ");
        if (mMap == null ) return; // error

        mClusterManager = new ClusterManager<>(getActivity(), mMap);
        //이걸 셋팅안하면 기본아이콘으로 나온다.
        mClusterRenderer = new PowerContactClusterRenderer();
        mClusterManager.setRenderer(mClusterRenderer);

        //mMap.setOnCameraChangeListener(mClusterManager); // 원래 루틴... 2016.2.21 테스트
        // http://stackoverflow.com/questions/28560816/how-to-add-2-listeners-to-map
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mZoomLevel = cameraPosition.zoom;
                mClusterManager.onCameraChange(cameraPosition);
            }
        });


        mMap.setOnMarkerClickListener(mClusterManager);

        // Setting an info window adapter allows us to change the both the contents and look of the
        // info window.
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        mMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        mIconFactory = new IconGenerator(getActivity());

        if (savedCameraPosition!=null) {// if there is saved position 저장된 것이 있다면...
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(savedCameraPosition));
            mClusterManager.onCameraChange(savedCameraPosition); // move 움직인다.. 2016.3.25
        }

    }

    // 클러스터 뭉치는 개수를 조정하려면 http://stackoverflow.com/questions/23363188/does-the-google-maps-android-api-utility-cluster-manager-have-a-minimum-number-o

    // Show From Memory
    // Fetch From Database
    // --- 이미지 클러스터
    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class PowerContactClusterRenderer extends DefaultClusterRenderer<PowerContactAddress> {
        private final IconGenerator mIconGenerator = new IconGenerator(getActivity());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity());

        public PowerContactClusterRenderer() {
            super(getActivity(), mMap, mClusterManager);

            mPhotoProfile = getActivity().getLayoutInflater().inflate(R.layout.marker_photo_profile, null); // 내가만든 뷰

            if (mPhotoProfile == null || mClusterIconGenerator == null)
                return;

            mClusterIconGenerator.setContentView(mPhotoProfile); // 원래는 multiProfile
            mClusterIconGenerator.setBackground(null);
        }


        @Override
        protected void onBeforeClusterItemRendered(PowerContactAddress person, MarkerOptions markerOptions) {

            Bitmap icon;

            switch( mMarkerType ) {

                case Constants.MARKER_TYPE_DEFAULT : // default marker
                    // https://developers.google.com/android/reference/com/google/android/gms/maps/model/BitmapDescriptorFactory
                    //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)); can change color
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker())
                            .title(person.getName())
                            .snippet(person.getAddr());
                    break;

                case Constants.MARKER_TYPE_NAME_ONLY : // name only mode
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(mIconFactory.makeIcon(person.getName())))
                            .anchor(mIconFactory.getAnchorU(), mIconFactory.getAnchorV())
                            .title(person.getName())
                            .snippet(person.getAddr());
                    break;

                case Constants.MARKER_TYPE_NAME_WITH_PHOTO  : {//  photo + name

                    ImageView mClusterImageView = (ImageView) mPhotoProfile.findViewById(R.id.image_custom_marker); // 내가 만든뷰의 사진
                    Uri photoUri;

                    if (person.getPhoto() != null && person.getPhoto().length() > 0) { // if there is photo
                        photoUri = Uri.parse(person.getPhoto());

                        Picasso.with(getActivity())
                                .load(photoUri)
                                .transform(new CircleTransform())
                                .error(R.drawable.ic_person_40dp)
                                .into(mClusterImageView);

                    } else {
                        Picasso.with(getActivity())
                                .load(R.drawable.ic_person_40dp)
                                .transform(new CircleTransform())
                                .into(mClusterImageView);
                    }

                    String title = person.getName(); // 마커의 타이틀은 이렇게 마커 속에 들어가 있다.
                    TextView titleUi = ((TextView) mPhotoProfile.findViewById(R.id.text_custom_marker));
                    if (titleUi != null)
                        if (title != null) {
                            titleUi.setText(title);
                        } else {
                            titleUi.setText("");
                        }

                    final Drawable TRANSPARENT_DRAWABLE = new ColorDrawable(Color.TRANSPARENT); // 투명하게
                    mIconGenerator.setBackground(TRANSPARENT_DRAWABLE);

                    icon = mClusterIconGenerator.makeIcon();

                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
                            .title(person.getName())
                            .anchor(0.5f, 0.5f)
                            .anchor(mIconFactory.getAnchorU(), mIconFactory.getAnchorV())
                            .snippet(person.getAddr());

                }
                    break;

                case Constants.MARKER_TYPE_PHOTO_ONLY : {// 사진만...
                    RoundedBitmapDrawable circularBitmapDrawable;
                    Bitmap bitmap;
                    Uri photoUri;
                    try {
                        if (person.getPhoto() != null && person.getPhoto().length() > 0) {
                            photoUri = Uri.parse(person.getPhoto());
                            icon = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
                        } else {
                            icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_person_40dp); // default
                        }

                        // circiled drawable 동그란 드로어블~!
                        circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getContext().getResources(), icon);
                        circularBitmapDrawable.setCircular(true);
                        circularBitmapDrawable.setAntiAlias(true);

                        // convert to bitmap
                        // http://stackoverflow.com/questions/18053156/set-image-from-drawable-as-marker-in-google-map-version-2
                        Canvas canvas = new Canvas();
                        bitmap = Bitmap.createBitmap(circularBitmapDrawable.getIntrinsicWidth(), circularBitmapDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        canvas.setBitmap(bitmap);
                        circularBitmapDrawable.setBounds(0, 0, circularBitmapDrawable.getIntrinsicWidth(), circularBitmapDrawable.getIntrinsicHeight());
                        circularBitmapDrawable.draw(canvas);


                    } catch (IOException e) {
                        e.printStackTrace(); // errror
                        break;
                    }

                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                            .title(person.getName())
                            .anchor(0.5f, 0.5f)
                            .snippet(person.getAddr());
                }
                    break;

                case Constants.MARKER_TYPE_SMALL_CIRCLE :
                    // tiny marker 아주 작은 마커 (drawable)
                    markerOptions.
                            icon(BitmapDescriptorFactory.fromResource(R.drawable.point_circle))
                            .title(person.getName())
                            .snippet(person.getAddr());
                    break;
            }
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<PowerContactAddress> cluster, MarkerOptions markerOptions) {
             IconGenerator iconFactory = new IconGenerator(getActivity());
            // 만약, 모두 같은 데이터만 있는 클러스터면 모양을 달리한다.
            if ( cluster.getSize()>1 && isLocationAllSame(cluster) ) {
                // IconGeneratorDemoActivity 에서 가져온 것

                iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
                iconFactory.setTextAppearance(R.style.ClusterText);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(mIconFactory.makeIcon(String.valueOf(cluster.getSize()))))
                        .anchor(mIconFactory.getAnchorU(), mIconFactory.getAnchorV());


            } else { // 일반 클러스터는 그냥 한다.

                super.onBeforeClusterRendered(cluster, markerOptions);
            }

        }
        // when clustring? --> more than 10 point same / but below zoom level 15.. then don't clustering

        // 언제 클러스터링 하는가?
        // 10개 이상 겹쳤을 경우, but zoom Level이 일정 수준 이상인 경우에는 모두 푼다.
        // 일정수준은.. 15부터로 일단 설정한다.
        // // 줌레벨
        // https://developers.google.com/maps/documentation/android-api/views
        //            1: World
        //            5: Landmass/continent
        //            10: City
        //            15: Streets
        //            20: Buildings
        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {

            // 1.모두 똑같은 클러스터면 --> 무조건 클러스터링
            // 2. 10개 이상 클러스터링 --> 무조건 클러스터링
            // 3. 현재 줌레벨이 16이하면.. --> 클러스터링
            if(cluster.getSize()>1)
                if (isLocationAllSame(cluster) ||  ( (cluster.getSize() > 9 && mZoomLevel < 17)) )
                    return true;


            return false;

        }
    } //---end of rendere


    @Override
    public boolean onClusterClick(Cluster<PowerContactAddress> cluster) {

        if (        isLocationAllSame(cluster)   )  { // same location data?
            //Toast.makeText(getActivity(), "same data", Toast.LENGTH_SHORT).show();
            mCallback.searchListMarkerItemByCoord(cluster.getPosition()); // 현재위치를 실어서 보낸다.

        } else { // if not same location data

            // http://stackoverflow.com/questions/25395357/android-how-to-uncluster-on-single-tap-on-a-cluster-marker-maps-v2
            // when click cluster then zoom
            // 클러스터 클릭시 확대하는 루틴
            // ZoomLevel
            // https://developers.google.com/maps/documentation/android-api/views
            // 1: World   5: Landmass/continent   10: City     15: Streets       20: Buildings

            if (mMap.getCameraPosition().zoom < 20 )
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    cluster.getPosition(), (float) Math.floor(mMap.getCameraPosition().zoom + 1)), 300, null);
        }
        return true;
    }


    @Override
    public void onClusterInfoWindowClick(Cluster<PowerContactAddress> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

  // 아이템 클릭

    @Override
    public boolean onClusterItemClick(PowerContactAddress item) {
        // 아래 리스트의 위치를 바꾼다.
        // change listview position
        mCallback.selectListMarkerItem(item); // scroll listview upto this item
        mLastClickedItem = item; // save한다(미래를 위해서)



//        // 컬러를 바꾼다.
//        // This causes the marker at Adelaide to change color and alpha.
//        final Random mRandom = new Random();
//        Marker marker = mClusterRenderer.getMarker(item); // 이렇게 마커를 구할 수 있다.....!!!!!
//        marker.setIcon(BitmapDescriptorFactory.defaultMarker(mRandom.nextFloat() * 360));


        return false;
    }


    // 아래 리스트에서 아이템 클릭한 효과내기
    public  boolean onClusterItemClickFromListView(final PowerContactAddress item) {

        Log.v(LOG_TAG, "onClusterItemClickFromListView(): will click");

        // 아직 맵이 준비되지 않았나?
        if(mMap == null) { // 나중을 위해서 셋팅한다

            Log.v(LOG_TAG, "onClusterItemClickFromListView(): mMap=null / wait....");

            needShowInfoWindowMarker=true; //
            mNeedToclickItem = item;
            return false;
        }

        makeClusterItemClickFromOthers(item, -1);

        return true;
    }
    // 아래 리스트에서 아이템 클릭한 효과내기 (재귀적 용법)

    float cameraZoomLevel;

    public void makeClusterItemClickFromOthers(final PowerContactAddress item, float zoomLevel) {
        if(zoomLevel == -1) {
            cameraZoomLevel = mMap.getCameraPosition().zoom; // get current zoom level

        } else
            cameraZoomLevel = zoomLevel;

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                item.getPosition(), cameraZoomLevel), 100, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                Log.v(LOG_TAG, "onClusterItemClickFromListView()::  onFinish");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 다시 마커에서 찾아본다.
                        Log.v(LOG_TAG, "start");
                        Marker myMarker = searchMarkersFromArrayList(item);
                        if (myMarker != null) {
                            // 이건 ui thread임.
                            myMarker.showInfoWindow(); // 일단 여기까지.
                            Log.v(LOG_TAG, "onClusterItemClickFromListView()::  ok. showinfowindow()" + cameraZoomLevel);
                        } else {  // 못찾았으면...
                            if (cameraZoomLevel < mMap.getMaxZoomLevel()) {
                                makeClusterItemClickFromOthers(item, mMap.getMaxZoomLevel()); // recursive
                            }
                        }
                        Log.v(LOG_TAG, "onClusterItemClickFromListView():: end " + cameraZoomLevel + "/");

                    }
                }, 500);
            }

            @Override
            public void onCancel() {

            }
        });
    }



    // 클러스터의 데이터가 모두 같은 좌표인지 체크하는 루틴
    public boolean isLocationAllSame (Cluster <PowerContactAddress> cluster) {
        PowerContactAddress prevItem=null;
        for (PowerContactAddress item : cluster.getItems()) {
            if(prevItem==null)
                prevItem = item;
            else {
                //firstName = firstName + "," +item.getName();
                if (!prevItem.getLatLng().equals( item.getLatLng() )) {
                    return false;
                }
            }
        }
        return true;
    }


    // item의 마커를 찾아서 리턴한다.
    // search marker on ArrayList and return it
    public Marker searchMarkersFromArrayList(PowerContactAddress Addressitem) {

        if (mClusterManager == null) {
            Log.v(LOG_TAG,"mClusterManager=null");
            return null;
        }

        java.util.Collection<Marker> userCollection = mClusterManager.getMarkerCollection().getMarkers();
        //MarkerManager.Collection markerCollection =  mClusterManager.getMarkerCollection();

        if(userCollection==null)
            return null;

        for(Marker marker: userCollection) {
            if(marker.getPosition().latitude == Addressitem.getPosition().latitude  // 위도
                    &&  marker.getPosition().longitude== Addressitem.getPosition().longitude // 경도
                    &&  marker.getTitle().equals(Addressitem.getName()) // 이름 // 같은 위도,경도에 여러 사람이 들어갈 수 있다. (아파트)
                    &&  marker.getSnippet().equals(Addressitem.getAddr()) )   // 주소
                return marker;
        }

        return null; // not found
    }


    // 특정 마커를 가지고 address item을 찾아낸다. 1:1 대응이라고 믿는다. (이름/주소가 같은 데이터는 없다고 가정한다)
    // 또한, 이미 화면에 그러져 있어야 한다.
    // find PowerContactAddress item from marer.
    // suppose that there is no contact item have same name & same address.. --> will imporve next version
    // also this marker is drawed on screen already.
    public PowerContactAddress searchAddressItemFromMarker(Marker marker) {

        // 단, 이때는 인포윈도우를
        // 여기서 마커를 얻기 위한 기본 작업을 한다.
        //java.util.Collection<Marker> userCollection = mClusterManager.getMarkerCollection().getMarkers();
        //MarkerManager.Collection markerCollection =  mClusterManager.getMarkerCollection();

        for(PowerContactAddress addressItem: mCallback.getContactAddress()) {
            if(marker.getPosition().latitude == addressItem.getPosition().latitude  // 위도
                    &&  marker.getPosition().longitude== addressItem.getPosition().longitude // 경도
                    &&  marker.getTitle().equals(addressItem.getName()) // 이름 // 같은 위도,경도에 여러 사람이 들어갈 수 있다. (아파트)
                    &&  marker.getSnippet().equals(addressItem.getAddr()) )   // 주소
                return addressItem;
        }

        return null; // not found
    }




    // 인포윈도우 자체 customize는 다음 참조 https://developers.google.com/maps/documentation/android-api/infowindows
    // 아이템 인포 윈도우 클릭
    // 어떻게 marker에 다른 데이터를 넣어서 이미지 URL을 전달할 것인가..
    // http://androidfreakers.blogspot.kr/2013/08/display-custom-info-window-with.html
    @Override
    public void onClusterItemInfoWindowClick(PowerContactAddress item) {

        Uri lookupUri = ContactsContract.Contacts.getLookupUri(item.getContact_id(),
                item.getLookup_key()); // 간편화면 (lookup) // http://developer.android.com/reference/android/provider/ContactsContract.Contacts.html#getLookupUri(long, java.lang.String)

        if (lookupUri!=null)
            ContactsContract.QuickContact.showQuickContact(getActivity(), mPhotoProfile, lookupUri,
                ContactsContract.QuickContact.MODE_LARGE, null);

        if(MainActivity.mPowerContactSettings.isDemoMode())
           Toast.makeText(getActivity(),R.string.msg_demo_mode_clusteritem_clicked, Toast.LENGTH_SHORT).show();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);

        enableMyLocation(); // activate current location button

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mCallback.getCurrentLatLng(); // if getLocation then callback moveMapLocation()
        Log.v(LOG_TAG, "onMapReady... setupclustermap()");
        setUpClusterMap();

        if(needRefreshData && mCallback != null) {
            setClusterManager(mCallback.getContactAddress(), mCallback.getMarkerType());
        }

        // 위의 클러스터링이 끝난 후에 해야 하는데... 그렇다면 조금 더 기다리든지..?
        if( needShowInfoWindowMarker) { // 마커를 눌렀어야 했는데.. 못누른 경우, 혹은 앱을 나갔다가 들어온 경우
            if(mNeedToclickItem!=null) {
                makeClusterItemClickFromOthers(mNeedToclickItem, -1);
                needShowInfoWindowMarker=false;
                mNeedToclickItem=null;
            }
        }
    }

    /**
     * https://github.com/googlemaps/android-samples/blob/master/ApiDemos/app/src/main/java/com/example/mapdemo/MyLocationDemoActivity.java
     * Enables the My Location layer if the fine location permission has been granted.
     */
    public void enableMyLocation() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mMap.setMyLocationEnabled(true);
        }
        else {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true); // 여기서 안되면.. 나중에 grant될때 될거다.
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {

        mCallback.onMyLocationButtonClick(); // if getLocation then callback moveMapLocation()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getActivity().getSupportFragmentManager(), "dialog");
    }


    // 외부에서 (main에서) 맵의 위치를 정한다.
    public void moveMapLocation (LatLng location) {
        if (location == null)
            return;

        mCurrentLatLng = location;
     //   mCurrentLatLng = location;

        if (mMap != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM_SIZE)); // 준비되면 움직여라!
    }


    // 인포윈도우 customize
    // Infowindow customize

    // googlemap-android-samples API의 MarkerDemoActivity에서 가져옴
    // get this from  googlemap-android-samples API :MarkerDemoActivity
    //  Demonstrates customizing the info window and/or its contents.
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mContents;

        CustomInfoWindowAdapter() {
            mContents = getActivity().getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null; // default

    // if want to custom window, then like this
    // before this -->      mWindow = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
    //            render(marker, mWindow);
    //            return mWindow;


        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;

            // if default info contants, then return null
        }

        private void render(Marker marker, View view) {
            int badge;

            // 먼저 marker로부터 contactList 아이템을 얻는다.
            PowerContactAddress contactItem = searchAddressItemFromMarker(marker);

            ImageView imgBadge = (ImageView) view.findViewById(R.id.badge);
            badge = R.drawable.ic_person_40dp; // 이건 그냥하는 거인데, 실제로는... 그림을 얻어야 하는데...
            if (contactItem != null && contactItem.getPhoto() != null && contactItem.getPhoto().length() > 0) { // 디폴트값

                Uri photoUri = Uri.parse(contactItem.getPhoto());

                // http://stackoverflow.com/questions/26112150/android-create-circular-image-with-picasso
                Picasso.with(getActivity())
                        .load(photoUri)
                        .transform(new CircleTransform())
                        .error(badge)
                        .into(imgBadge);
            } else {
                Picasso.with(getActivity())
                        .load(badge)
                        .transform(new CircleTransform())
                        .placeholder(badge)
                        .error(badge)
                        .into(imgBadge);

            }



            // 실제로는 여기서 Marker를 가지고 다시 item을 얻어내야 한다.
            // 그렇다면, 이 상황에서 어떻게 다시 접근이 가능할까?

            String title = marker.getTitle(); // 마커의 타이틀은 이렇게 마커 속에 들어가 있다.
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else
                titleUi.setText("");


            String snippet = marker.getSnippet(); // 스니펫도 마찬가지로 들어가 있다.
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));

            if (snippet==null)
                snippet="";
                    snippetUi.setText(snippet);

        }
    }
}

