package personal.wl.mymapapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private BaiduMap baiduMap;
    public LocationClient mLocationClient = null;
    private BitmapDescriptor mMarker;
    private MyLocationListener myListener = new MyLocationListener();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private boolean isFirstLoc = true;
    private String TAG = "BAIDU";
    private final int REQUEST_CODE = 1;
    private Context context;

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        setContentView(R.layout.activity_main);
//        mapView = findViewById(R.id.baidumapview);
//        baiduMap = mapView.getMap();
        initView();
        initLoc();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
        }

        this.context=MainActivity.this;
    }

    private void initView() {
        //地图显示控件 同时需要处理它的生命周期
        mapView = (MapView) findViewById(R.id.baidumapview);
        //获取BadiuMap实例 地图控制器类
        baiduMap = mapView.getMap();
        mLocationClient = new LocationClient(this);
        //注册定位回调
        mLocationClient.registerLocationListener(myListener);

        //覆盖物 用于显示当前位置
        mMarker = BitmapDescriptorFactory.fromResource(R.drawable.ic_add_location_black_24dp);
    }


    private void initLoc() {

        //开启定位图层
        baiduMap.setMyLocationEnabled(true);
        //定位相关参数设置
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        //共有三种坐标可选
        //1. gcj02：国测局坐标；
        //2. bd09：百度墨卡托坐标；
        //3. bd09ll：百度经纬度坐标；

        int span = 1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要


        //加载设置
        mLocationClient.setLocOption(option);
        mLocationClient.start();

    }


    /**
     * 实现定位回调
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {

            Log.d(TAG, "SUCCESS: ");
            //定位成功后回调该方法
            //BDLocation封装了定位相关的各种信息

            //构造定位数据
            MyLocationData data = new MyLocationData.Builder()
                    .accuracy(location.getRadius())//定位精度
                    .latitude(location.getLatitude())//纬度
                    .longitude(location.getLongitude())//经度
                    .direction(100)//方向 可利用手机方向传感器获取 此处为方便写死
                    .build();
            //设置定位数据
            baiduMap.setMyLocationData(data);

            //配置定位图层显示方式
            //有两个不同的构造方法重载 分别为三个参数和五个参数的
            //这里主要讲一下常用的三个参数的构造方法
            //三个参数：LocationMode(定位模式：罗盘，跟随),enableDirection（是否允许显示方向信息）
            // ,customMarker（自定义图标）
            MyLocationConfiguration configuration = new MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.NORMAL, true, mMarker);

            baiduMap.setMyLocationConfiguration(configuration);

            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            //第一次定位需要更新下地图显示状态
            if (isFirstLoc) {
                isFirstLoc = false;
                MapStatus.Builder builder = new MapStatus.Builder()
                        .target(ll)//地图缩放中心点
                        .zoom(18f);//缩放倍数 百度地图支持缩放21级 部分特殊图层为20级
                //改变地图状态
                baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            Log.d(TAG, "SUCCESS: ");
            Log.i(TAG, "城市代码--->"+
                    location.getCityCode() + "\t经度--->" +
                    location.getLatitude() + "\t纬度-->" +
                    location.getLongitude() + "\t使用的定位类型-->" +
                    location.getCoorType() + "\t国家-->" +
                    location.getCountry() + "\t城市-->" +
                    location.getCity() + "\t地址-->" +
                    location.getAddrStr());
            Toast.makeText(context,location.getLocationDescribe(),Toast.LENGTH_LONG).show();
        }

    }
}



