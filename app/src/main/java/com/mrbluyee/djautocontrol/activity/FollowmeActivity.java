package com.mrbluyee.djautocontrol.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.CoordinateConverter;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.followme.FollowMeMission;
import dji.common.mission.followme.FollowMeEvent;
import dji.common.mission.followme.FollowMeExecutionData;
import dji.common.mission.followme.FollowMeHeading;
import dji.common.mission.followme.FollowMeMissionEvent;
import dji.common.mission.followme.FollowMeMissionExecuteState;
import dji.common.mission.followme.FollowMeMissionState;
import dji.common.model.LocationCoordinate2D;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.common.error.DJIError;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.followme.FollowMeMissionOperator;
import dji.sdk.mission.followme.FollowMeMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;
import dji.thirdparty.okhttp3.OkHttpClient;
import dji.thirdparty.okhttp3.Request;
import dji.thirdparty.okhttp3.Response;
import dji.thirdparty.rx.Observable;
import dji.thirdparty.rx.Subscription;
import dji.thirdparty.rx.functions.Action1;
import dji.thirdparty.rx.schedulers.Schedulers;


import com.mrbluyee.djautocontrol.R;
import com.mrbluyee.djautocontrol.application.PhoneLocationApplication;
import com.mrbluyee.djautocontrol.application.DJSDKApplication;
import com.mrbluyee.djautocontrol.application.WebRequestApplication;
import com.mrbluyee.djautocontrol.utils.AmapToGpsUtil;
import com.mrbluyee.djautocontrol.utils.ChargeStationInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.mrbluyee.djautocontrol.utils.AmapToGpsUtil.gps_converter;


public class FollowmeActivity extends FragmentActivity implements View.OnClickListener, OnMapClickListener {
    protected static final String TAG = FollowmeActivity.class.getName();

    private MapView mapView;
    private AMap aMap;

    private Button locate, searh_site, gosite;

    private double droneLocationLat = 181, droneLocationLng = 181;
    private final Map<Integer, Marker> mMarkers = new ConcurrentHashMap<Integer, Marker>();
    private Marker droneMarker = null;

    private float altitude = 10.0f;
    private FlightController mFlightController;
    private FollowMeMission mission;
    private FollowMeHeading mHeading = FollowMeHeading.TOWARD_FOLLOW_POSITION;
    private FollowMeMissionOperator instance;
    private LocationCoordinate2D movingObjectLocation;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private Subscription timmerSubcription;
    private Observable<Long> timer =Observable.timer(100, TimeUnit.MILLISECONDS).observeOn(Schedulers.computation()).repeat();
    public MyHandler myHandler;

    public SparseArray<ChargeStationInfo> stationInfos = new SparseArray<ChargeStationInfo>();
    private WebRequestApplication webrequest = new WebRequestApplication();

    @Override
    protected void onResume(){
        super.onResume();
        initFlightController();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(mReceiver);
        removeListener();
        super.onDestroy();
    }

    /**
     * @Description : RETURN Button RESPONSE FUNCTION
     */
    public void onReturn(View view){
        Log.d(TAG, "onReturn");
        this.finish();
    }

    private void setResultToToast(final String string){
        FollowmeActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FollowmeActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initUI() {

        locate = (Button) findViewById(R.id.locate);
        searh_site = (Button) findViewById(R.id.followme_search);
        gosite = (Button) findViewById(R.id.followme_gosite);

        locate.setOnClickListener(this);
        searh_site.setOnClickListener(this);
        gosite.setOnClickListener(this);
    }

    private void initMapView() {
        float zoomlevel = (float) 18.0;
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setOnMapClickListener(this);// add the listener for click for amap object
        }
        PhoneLocationApplication.initLocation(this);
        LatLng phone_location = gps_converter(new LatLng(PhoneLocationApplication.latitude, PhoneLocationApplication.longitude));
        aMap.addMarker(new MarkerOptions().position(phone_location).title("phone marker"));
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(phone_location , zoomlevel);
        aMap.moveCamera(cu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_followme);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJSDKApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        mapView = (MapView) findViewById(R.id.followme_map);
        mapView.onCreate(savedInstanceState);
        initMapView();
        initUI();
        addListener();
        initFlightController();
        myHandler = new MyHandler();
    }

    class MyHandler extends Handler {
        public MyHandler() {
        }

        public MyHandler(Looper L) {
            super(L);
        }
        // 子类必须重写此方法，接受数据
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.d("MyHandler","handleMessage");
            super.handleMessage(msg);
            // 此处可以更新UI
            Bundle b = msg.getData();
            webrequest.chargeStationInfoHandler(b,stationInfos);
            for(int i = 0;i<stationInfos.size();i++){
                ChargeStationInfo stationInfo = stationInfos.valueAt(i);
                markchargesite(stationInfo.getStationPos(), "" + stationInfos.keyAt(i));
            }
        }
    }

    //Add Listener for WaypointMissionOperator
    private void addListener() {
        if (getFollowMeMissionOperator() != null) {
            getFollowMeMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener() {
        if (getFollowMeMissionOperator() != null) {
            getFollowMeMissionOperator().removeListener(eventNotificationListener);
        }
    }

    private FollowMeMissionOperatorListener eventNotificationListener = new FollowMeMissionOperatorListener(){
        @Override
        public void onExecutionStart() {

        }

        @Override
        public void onExecutionFinish(@Nullable DJIError djiError) {
            setResultToToast("Execution finished: " + (djiError == null ? "Success!" : djiError.getDescription()));
        }

        @Override
        public void onExecutionUpdate(@NonNull FollowMeMissionEvent followMeMissionEvent) {

        }
    };

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange()
    {
        initFlightController();
        loginAccount();
    }

    private void loginAccount(){

        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.e(TAG, "Login Success");
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        setResultToToast("Login Error:"
                                + error.getDescription());
                    }
                });
    }

    private void initFlightController() {

        BaseProduct product = DJSDKApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {

            mFlightController.setStateCallback(
                    new FlightControllerState.Callback() {
                        @Override
                        public void onUpdate(FlightControllerState
                                                     djiFlightControllerCurrentState) {
                            droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                            droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                            updateDroneLocation();
                        }
                    });

        }
    }

    public FollowMeMissionOperator getFollowMeMissionOperator() {
        if (instance == null) {
            instance = DJISDKManager.getInstance().getMissionControl().getFollowMeMissionOperator();
        }
        return instance;
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    // Update the drone location based on states from MCU.
    private void updateDroneLocation(){

        LatLng pos = gps_converter(new LatLng(droneLocationLat, droneLocationLng));
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    droneMarker = aMap.addMarker(markerOptions);
                }
            }
        });
    }

    private void markchargesite(LatLng point,String title){
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.charge_site));
        markerOptions.title(title);
        Marker marker = aMap.addMarker(markerOptions);
        mMarkers.put(mMarkers.size(), marker);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate:{
                updateDroneLocation();
                cameraUpdate(); // Locate the drone's place
                break;
            }
            case R.id.followme_search:{
                webrequest.Get_chargesite_gps_info(myHandler);
                break;
            }
            case R.id.followme_gosite:{

                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onMapClick(LatLng point){

    }

    private void cameraUpdate(){
        LatLng pos = gps_converter(new LatLng(droneLocationLat, droneLocationLng));
        float zoomlevel = (float) 18.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        aMap.moveCamera(cu);
    }

    private void followMeStart(){
        if (getFollowMeMissionOperator().getCurrentState().toString().equals(FollowMeMissionState.READY_TO_EXECUTE.toString())){
            //ToDo: You need init or get the location of your moving object which will be followed by the aircraft.

            getFollowMeMissionOperator().startMission(FollowMeMission.getInstance().initUserData(movingObjectLocation.getLatitude() , movingObjectLocation.getLongitude(), altitude), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    setResultToToast("Mission Start: " + (djiError == null ? "Successfully" : djiError.getDescription()));
                }});

            if (!isRunning.get()) {
                isRunning.set(true);
                timmerSubcription = timer.subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        getFollowMeMissionOperator().updateFollowingTarget(new LocationCoordinate2D(movingObjectLocation.getLatitude(), movingObjectLocation.getLongitude()),
                                new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError error) {
                                        isRunning.set(false);
                                    }
                                });
                    }
                });
            }
        } else{
            Toast.makeText(getApplicationContext(), getFollowMeMissionOperator().getCurrentState().toString(), Toast.LENGTH_SHORT).show();
        }
    }

}
