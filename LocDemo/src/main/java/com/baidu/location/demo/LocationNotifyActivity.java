package com.baidu.location.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.baidulocationdemo.R;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;

/**
 * 此demo演示了是否到达位置提醒范围内
 */
public class LocationNotifyActivity extends Activity implements BaiduMap.OnMapClickListener {

    private MapView mMapView;
    private EditText mRadius;
    private Button startNotify;
    private BaiduMap mBaiduMap;
    private TextView mGuide;
    private Marker mk;
    private LocationClient mLocationClient;
    private NotifyLister mNotifyLister;
    private double mlatitude = 0.0d;
    private double mlongitude = 0.0d;
    boolean isFirstLoc = true; // 是否首次定位
    private NotiftLocationListener listener;
    private BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence);

        mMapView = (MapView) findViewById(R.id.bmap);
        mRadius = (EditText) findViewById(R.id.radius);
        startNotify = (Button) findViewById(R.id.start_notify);
        mGuide = (TextView) findViewById(R.id.guide);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapClickListener(this);
        // 初始化定位服务
        try {
            mLocationClient = new LocationClient(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("baidu_location", "onCreate: LocationNotifyActivity mLocationClient = " + mLocationClient);
        listener = new NotiftLocationListener();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mNotifyLister = new NotifyLister();
        if (mLocationClient != null) {
            // 注册 BDAbstractLocationListener 定位监听函数
            mLocationClient.registerLocationListener(listener);
            mLocationClient.start();
        }
    }

    /**
     * 地图点击事件获取中心点
     *
     * @param latLng 经纬度
     */
    @Override
    public void onMapClick(LatLng latLng) {
        if (null != mk) {
            mk.remove();
        }
        mlatitude = latLng.latitude;
        mlongitude = latLng.longitude;
        mGuide.setText("纬度:" + mlatitude + "经度:" + mlongitude);
        mGuide.setTextColor(Color.WHITE);
        MarkerOptions ooA = new MarkerOptions().position(latLng).icon(bd).zIndex(9).draggable(true);
        mk = (Marker) mBaiduMap.addOverlay(ooA);
    }

    @Override
    public void onMapPoiClick(MapPoi mapPoi) {

    }

    /**
     *  在地图中添加提醒范围
     *
     * @param v
     */
    public void addCircleNotifyOnClick(View v) {
        mBaiduMap.clear();
        if (mlatitude == 0.0d || mlongitude == 0.0d) {
            Toast.makeText(LocationNotifyActivity.this, "请点击地图添加中心点坐标", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(mlatitude, mlongitude);
            int radius = Integer.parseInt(mRadius.getText().toString());
            // 添加圆
            addCircle(latLng, radius);
        }
    }

    /**
     * 开启/关闭 位置提醒
     *
     * @param v
     */
    public void starNotifyOnClick(View v) {
        if (startNotify.getText().toString().equals("开启提醒")) {
            if (mlatitude == 0.0d && mlongitude == 0.0d) {
                Toast.makeText(LocationNotifyActivity.this, "点击地图添加提醒点", Toast.LENGTH_SHORT).show();
            } else {
                if (mLocationClient != null) {
                    mLocationClient.registerNotify(mNotifyLister);
                    mLocationClient.start();
                }
                startNotify.setText("关闭提醒");
            }
        } else {
            if (mNotifyLister != null) {
                // 取消注册的位置提醒监听
                if (mLocationClient != null) {
                    mLocationClient.removeNotifyEvent(mNotifyLister);
                }
                startNotify.setText("开启提醒");
            }
        }
    }

    private Handler notifyHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                Log.e("fence", "handleMessage: ++");
                int radius = Integer.parseInt(mRadius.getText().toString());
                // 设置位置提醒的点的相关参数，
                if (mLocationClient != null) {
                    mNotifyLister.SetNotifyLocation(mlatitude, mlongitude, radius, mLocationClient.getLocOption().getCoorType());//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
                }

            } catch (NumberFormatException e) {
                e.printStackTrace(System.err);
            }
        }

    };

    /**
     * 定位请求回调接口
     */
    public class NotiftLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.e("fence", "onReceiveLocation");
            //Receive Location
            double longtitude = location.getLongitude();
            double latitude = location.getLatitude();
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(latitude, longtitude);
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            notifyHandler.sendEmptyMessage(0);
        }

    }

    /**
     * 位置提醒功能，可供地理围栏需求比较小的开发者使用
     */
    public class NotifyLister extends BDNotifyListener {
        /**
         * 位置提醒回调函数
         * @param mlocation 位置坐标
         * @param distance 当前位置跟设定提醒点的距离
         */
        public void onNotify(BDLocation mlocation, float distance) {
            Toast.makeText(LocationNotifyActivity.this, "已到达提醒点范围", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
        bd.recycle();
        if (mLocationClient != null) {
            // 取消注册的位置提醒监听
            mLocationClient.removeNotifyEvent(mNotifyLister);
            // 停止定位
            mLocationClient.stop();
        }
        // 释放地图资源
        mBaiduMap.clear();
        mMapView.onDestroy();
    }

    /**
     * 在地图中添加提醒范围
     *
     * @param latLng 中心点经纬度信息
     * @param radius 提醒半径
     */
    public void addCircle(LatLng latLng, int radius) {
        // 绘制圆
        OverlayOptions ooCircle = new CircleOptions().fillColor(0x000000FF)
                .center(latLng).stroke(new Stroke(5, 0xAA000000))
                .radius(radius);
        mBaiduMap.addOverlay(ooCircle);
    }
}
