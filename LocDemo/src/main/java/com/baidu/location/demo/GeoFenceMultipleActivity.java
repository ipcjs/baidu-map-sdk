
package com.baidu.location.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.baidulocationdemo.R;
import com.baidu.geofence.GeoFence;
import com.baidu.geofence.GeoFenceClient;
import com.baidu.geofence.GeoFenceListener;
import com.baidu.geofence.model.DPoint;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 地理围栏
 */
public class GeoFenceMultipleActivity extends CheckPermissionsActivity
        implements
        OnClickListener,
        GeoFenceListener,
        BaiduMap.OnMapClickListener,
        OnCheckedChangeListener,
        RadioGroup.OnCheckedChangeListener {

    private View lyOption;

    private TextView tvGuide;
    private TextView tvResult;

    private RadioGroup rgFenceType;

    private EditText etCustomId;
    private EditText etRadius;
    private EditText etPoiType;
    private EditText etKeyword;
    private EditText etCity;
    private EditText etFenceSize;

    private EditText etInNum;
    private EditText etOutNum;
    private EditText etStayNum;

    private CheckBox cbAlertIn;
    private CheckBox cbAlertOut;
    private CheckBox cbAldertStated;

    private Button btAddFence;
    private Button btOption;

    /**
     * 用于显示当前的位置
     * 示例中是为了显示当前的位置，在实际使用中，单独的地理围栏可以不使用定位接口
     */
    private LocationClient mlocationClient;
    private MapView mMapView;
    private BaiduMap mBdMap;
    // 中心点坐标
    private LatLng centerLatLng = null;
    // 多边形围栏的边界点
    private List<LatLng> polygonPoints = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    // 当前的坐标点集合，主要用于进行地图的可视区域的缩放
    private LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
    // 中心点marker
    private Marker centerMarker;
    private MarkerOptions markerOption = null;
    // 地理围栏客户端
    private GeoFenceClient fenceClient = null;
    // 要创建的围栏半径
    private float fenceRadius = 0.0F;
    // 触发地理围栏的行为，默认为进入提醒
    private int activatesAction = GeoFenceClient.GEOFENCE_IN;
    // 地理围栏的广播action
    private static final String GEOFENCE_BROADCAST_ACTION = "com.example.geofence";

    // 记录已经添加成功的围栏
    private HashMap<String, GeoFence> fenceMap = new HashMap<>();

    private boolean flag1 = true;
    private boolean flag2 = false;
    private boolean flag3 = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence_new);
        setTitle("地理围栏");
        // 初始化地理围栏
        fenceClient = new GeoFenceClient(getApplicationContext());

        rgFenceType = findViewById(R.id.rg_fenceType);
        lyOption = findViewById(R.id.ly_option);
        btAddFence = findViewById(R.id.bt_addFence);
        btOption = findViewById(R.id.bt_option);
        tvGuide = findViewById(R.id.tv_guide);
        tvResult = findViewById(R.id.tv_result);
        tvResult.setVisibility(View.GONE);
        etCustomId = findViewById(R.id.et_customId);
        etCity = findViewById(R.id.et_city);
        etRadius = findViewById(R.id.et_radius);
        etPoiType = findViewById(R.id.et_poitype);
        etKeyword = findViewById(R.id.et_keyword);
        etFenceSize = findViewById(R.id.et_fenceSize);

        etInNum = findViewById(R.id.in_num);
        etOutNum = findViewById(R.id.out_num);
        etStayNum = findViewById(R.id.stay_num);

        cbAlertIn = findViewById(R.id.cb_alertIn);
        cbAlertOut = findViewById(R.id.cb_alertOut);
        cbAldertStated = findViewById(R.id.cb_alertStated);


        mMapView = findViewById(R.id.map);
//		mMapView.onCreate(savedInstanceState);
        markerOption = new MarkerOptions().draggable(true);
        init();
        try {
            mlocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 使用gps定位
        option.setCoorType(GeoFenceClient.BD09LL); // 设置坐标类型
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);

        // 设置locationClientOption
        if (mlocationClient != null) {
            mlocationClient.setLocOption(option);
        }

        // 注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        if (mlocationClient != null){
            mlocationClient.registerLocationListener(myLocationListener);
            mlocationClient.start();
        }
    }

    void init() {
        if (mBdMap == null) {
            mBdMap = mMapView.getMap();
            mBdMap.setMyLocationEnabled(true);
            mBdMap.getUiSettings().setRotateGesturesEnabled(false);
            mBdMap.setOnMapClickListener(this);
        }

        rgFenceType.setVisibility(View.VISIBLE);
        btOption.setVisibility(View.VISIBLE);
        btOption.setText("隐藏设置");
        resetView();
        resetViewRound();

        findViewById(R.id.bt_removeFence).setOnClickListener(this);
        findViewById(R.id.bt_resumeFence).setOnClickListener(this);
        findViewById(R.id.bt_pauseFence).setOnClickListener(this);
        rgFenceType.setOnCheckedChangeListener(this);
        btAddFence.setOnClickListener(this);
        btAddFence.setOnClickListener(this);
        btOption.setOnClickListener(this);
        cbAlertIn.setOnCheckedChangeListener(this);
        cbAlertOut.setOnCheckedChangeListener(this);
        cbAldertStated.setOnCheckedChangeListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(mGeoFenceReceiver, filter);
        /**
         * 创建pendingIntent
         */
        fenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        fenceClient.isHighAccuracyLoc(true); // 在即将触发侦听行为时允许开启高精度定位模式(开启gps定位，gps定位结果优先)
        fenceClient.setGeoFenceListener(this);
        /**
         * 设置地理围栏的触发行为,默认为进入
         */
        fenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        try {
            unregisterReceiver(mGeoFenceReceiver);
        } catch (Throwable e) {
        }

        if (null != fenceClient) {
            fenceClient.removeGeoFence();
        }
        if (null != mlocationClient) {
            mlocationClient.stop();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_addFence:
                setRadioGroupAble(false);
                addFence();
                break;
            case R.id.bt_removeFence:
                fenceList.clear();
                fenceClient.removeGeoFence();
                if (mBdMap != null) {
                    mBdMap.clear(); // 批量清除地图图层上所有标记
                }
                fenceMap.clear();
                centerMarker = null;
                markerList.clear();
                polygonPoints.clear();
                tvResult.setText("");
                break;
            case R.id.bt_resumeFence:
                if (fenceClient.getAllGeoFence().isEmpty()) {
                    Toast.makeText(this, "当前没有成功创建的围栏，请先创建围栏，再点击恢复监听", Toast.LENGTH_LONG).show();
                } else {
                    fenceClient.resumeGeoFence();
                }
                break;
            case R.id.bt_pauseFence:
                fenceClient.pauseGeoFence();
                break;
            case R.id.bt_option:
                if (btOption.getText().toString()
                        .equals("显示设置")) {
                    lyOption.setVisibility(View.VISIBLE);
                    btOption.setText("隐藏设置");
                } else {
                    lyOption.setVisibility(View.GONE);
                    btOption.setText("显示设置");
                }
                break;
            default:
                break;
        }
    }

    private void drawFence(GeoFence fence) {
        switch (fence.getType()) {
            case GeoFence.TYPE_ROUND:
                drawCircle(fence, false);
                break;
            case GeoFence.TYPE_BDMAPPOI:
                drawCircle(fence, true);
                break;
            case GeoFence.TYPE_POLYGON:
//			case GeoFence.TYPE_DISTRICT :
                drawPolygon(fence);
                break;
            default:
                break;
        }

        // 设置所有maker显示在当前可视区域地图中
        LatLngBounds bounds = boundsBuilder.build();
        removeMarkers();
    }

    private void drawCircle(GeoFence fence, boolean isPoi) {
        LatLng center;
        int radius;
        if (isPoi) {
            BDLocation bdLocation = new BDLocation();
            bdLocation.setLatitude(fence.getCenter().getLatitude());
            bdLocation.setLongitude(fence.getCenter().getLongitude());
            BDLocation tempLocation = LocationClient
                    .getBDLocationInCoorType(bdLocation, BDLocation.BDLOCATION_GCJ02_TO_BD09LL);
            center = new LatLng(tempLocation.getLatitude(),
                    tempLocation.getLongitude());
        } else {
            center = centerLatLng;
        }
        radius = (int) fence.getRadius();
        // 绘制一个圆形
        if (center == null) {
            return;
        }
        mBdMap.addOverlay(new CircleOptions().center(center)
                .radius(radius)
                .fillColor(0xAA0000FF) // 填充颜色
                .stroke(new Stroke(5, 0xAA00ff00)));
        boundsBuilder.include(center);
        if (!isPoi) {
            centerLatLng = null;
        }
    }

    private void drawPolygon(GeoFence fence) {
        final List<DPoint> pointList = fence.getPoints();
        if (null == pointList || pointList.isEmpty()) {
            return;
        }
        List<LatLng> lst = new ArrayList<>();

        for (DPoint point : pointList) {
            lst.add(new LatLng(point.getLatitude(), point.getLongitude()));
            boundsBuilder.include(
                    new LatLng(point.getLatitude(), point.getLongitude()));
        }
        mBdMap.addOverlay(new PolygonOptions()
                .points(polygonPoints)
                .fillColor(0xAAFFFF00) // 填充颜色
                .stroke(new Stroke(5, 0xAA00FF00)));

        if (polygonPoints != null && polygonPoints.size() > 0) {
            polygonPoints.clear();
        }

    }

    Object lock = new Object();

    void drawFence2Map() {
        new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        if (null == fenceList || fenceList.isEmpty()) {
                            return;
                        }
                        for (GeoFence fence : fenceList) {
                            if (fenceMap.containsKey(fence.getFenceId())) {
                                continue;
                            }
                            drawFence(fence);
                            fenceMap.put(fence.getFenceId(), fence);
                        }
                    }
                } catch (Throwable e) {

                }
            }
        }.start();
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    StringBuffer sb = new StringBuffer();
                    sb.append("添加围栏成功");
                    String customId = (String) msg.obj;
                    if (!TextUtils.isEmpty(customId)) {
                        sb.append("customId: ").append(customId);
                    }
                    Toast.makeText(getApplicationContext(), sb.toString(),
                            Toast.LENGTH_SHORT).show();
                    drawFence2Map();
                    break;
                case 1:
                    int errorCode = msg.arg1;
                    Toast.makeText(getApplicationContext(),
                            "添加围栏失败,errorcode = " + errorCode, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    String statusStr = (String) msg.obj;
                    tvResult.setVisibility(View.VISIBLE);
                    tvResult.append(statusStr + "\n");
                    break;
                default:
                    break;
            }
            setRadioGroupAble(true);
        }
    };

    List<GeoFence> fenceList = new ArrayList<>();

    @Override
    public void onGeoFenceCreateFinished(final List<GeoFence> geoFenceList,
                                         int errorCode, String customId) {
        Message msg = Message.obtain();
        if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {
            fenceList.addAll(geoFenceList);
            msg.obj = customId;
            msg.what = 0;
        } else {
            msg.arg1 = errorCode;
            msg.what = 1;
        }
        handler.sendMessage(msg);
    }

    @Override
    public void onMapPoiClick(MapPoi mapPoi) {

    }

    @Override
    public void onMapClick(LatLng point) {
        markerOption.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.icon_markb));
        switch (rgFenceType.getCheckedRadioButtonId()) {
            case R.id.rb_roundFence:
            case R.id.rb_nearbyFence:
                centerLatLng = point;
                addCenterMarker(centerLatLng);
                tvGuide.setBackgroundColor(Color.parseColor("#33333333"));
                tvGuide.setText("选中的坐标：" + centerLatLng.longitude + ","
                        + centerLatLng.latitude);
                break;
            case R.id.rb_polygonFence:
                if (null == polygonPoints) {
                    polygonPoints = new ArrayList<LatLng>();
                }
                polygonPoints.add(point);
                addPolygonMarker(point);
                tvGuide.setBackgroundColor(Color.parseColor("#33333333"));
                tvGuide.setText("已选择" + polygonPoints.size() + "个点");
                if (polygonPoints.size() >= 3) {
                    btAddFence.setEnabled(true);
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_alertIn:
                flag1 = isChecked;
                break;
            case R.id.cb_alertOut:
                flag2 = isChecked;
                break;
            case R.id.cb_alertStated:
                flag3 = isChecked;
                break;
            default:
                break;
        }

        if (flag1) {
            activatesAction = GeoFenceClient.GEOFENCE_IN;
            etInNum.setEnabled(true);
            etOutNum.setEnabled(false);
            etStayNum.setEnabled(false);
        } else {
            etInNum.setEnabled(false);
        }
        if (flag2) {
            activatesAction = GeoFenceClient.GEOFENCE_OUT;
            etInNum.setEnabled(false);
            etOutNum.setEnabled(true);
            etStayNum.setEnabled(false);
        } else {
            etOutNum.setEnabled(false);
        }
        if (flag3) {
            activatesAction = GeoFenceClient.GEOFENCE_STAYED;
            etInNum.setEnabled(false);
            etOutNum.setEnabled(false);
            etStayNum.setEnabled(true);
        } else {
            etStayNum.setEnabled(false);
        }
        if (flag1 && flag2) {
            activatesAction = GeoFenceClient.GEOFENCE_IN_OUT;
            etInNum.setEnabled(true);
            etOutNum.setEnabled(true);
            etStayNum.setEnabled(false);
        }
        if (flag1 && flag3) {
            activatesAction = GeoFenceClient.GEOFENCE_IN_STAYED;
            etInNum.setEnabled(true);
            etOutNum.setEnabled(false);
            etStayNum.setEnabled(true);
        }
        if (flag2 && flag3) {
            activatesAction = GeoFenceClient.GEOFENCE_OUT_STAYED;
            etInNum.setEnabled(false);
            etOutNum.setEnabled(true);
            etStayNum.setEnabled(true);
        }
        if (flag1 && flag2 && flag3) {
            activatesAction = GeoFenceClient.GEOFENCE_IN_OUT_STAYED;
            etInNum.setEnabled(true);
            etOutNum.setEnabled(true);
            etStayNum.setEnabled(true);
        }


        if (null != fenceClient) {
            fenceClient.setActivateAction(activatesAction);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        removeMarkers();
        resetView();
        centerLatLng = null;
        btAddFence.setEnabled(true);
        switch (checkedId) {
            case R.id.rb_roundFence:
                resetViewRound();
                break;
            case R.id.rb_polygonFence:
                resetViewPolygon();
                break;
            case R.id.rb_keywordFence:
                resetViewKeyword();
                break;
            case R.id.rb_nearbyFence:
                resetViewNearby();
                break;
            case R.id.rb_districeFence:
                resetViewDistrict();
                break;

            default:
                break;
        }
    }

    /**
     * 接收触发围栏后的广播,当添加围栏成功之后，会立即对所有围栏状态进行一次侦测，只会发送一次围栏和位置之间的初始状态；
     * 当触发围栏之后也会收到广播,对于同一触发行为只会发送一次广播不会重复发送，除非位置和围栏的关系再次发生了改变。
     */
    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收广播
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                Bundle bundle = intent.getExtras();
                String customId = bundle
                        .getString(GeoFence.BUNDLE_KEY_CUSTOMID);
                String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
                GeoFence geoFence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                int locType = bundle.getInt(GeoFence.BUNDLE_KEY_LOCERRORCODE);
                StringBuffer sb = new StringBuffer();
                switch (status) {
                    case GeoFence.INIT_STATUS_IN:
                        sb.append("围栏初始状态:在围栏内");
                        break;
                    case GeoFence.INIT_STATUS_OUT:
                        sb.append("围栏初始状态:在围栏外");
                        break;
                    case GeoFence.STATUS_LOCFAIL:
                        sb.append("定位失败,无法判定目标当前位置和围栏之间的状态");
                        break;
                    case GeoFence.STATUS_IN:
                        sb.append("进入围栏 ");
                        break;
                    case GeoFence.STATUS_OUT:
                        sb.append("离开围栏 ");
                        break;
                    case GeoFence.STATUS_STAYED:
                        sb.append("在围栏内停留超过10分钟 ");
                        break;
                    default:
                        break;
                }
                if (status != GeoFence.STATUS_LOCFAIL) {
                    if (!TextUtils.isEmpty(customId)) {
                        sb.append(" customId: " + customId);
                    }
                    sb.append(" fenceId: " + fenceId);
                }
                String str = sb.toString();
                Message msg = Message.obtain();
                msg.obj = str;
                msg.what = 2;
                handler.sendMessage(msg);
            }
        }
    };

    private void addCenterMarker(LatLng latlng) {
        if (null == centerMarker) {
            centerMarker = (Marker) mBdMap.addOverlay(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_markb)));
        }
        centerMarker.setPosition(latlng);
        centerMarker.setVisible(true);
        markerList.add(centerMarker);
    }

    // 添加多边形的边界点marker
    private void addPolygonMarker(LatLng latlng) {
        markerOption.position(latlng);
        Marker marker = (Marker) mBdMap.addOverlay(markerOption);
        markerList.add(marker);
    }

    private void removeMarkers() {
        if (null != centerMarker) {
            centerMarker.remove();
            centerMarker = null;
        }
        if (null != markerList && markerList.size() > 0) {
            for (Marker marker : markerList) {
                marker.remove();
            }
            markerList.clear();
        }
    }

    private void resetView() {
        etCustomId.setVisibility(View.VISIBLE);
        etCity.setVisibility(View.GONE);
        etFenceSize.setVisibility(View.GONE);
        etKeyword.setVisibility(View.GONE);
        etPoiType.setVisibility(View.GONE);
        etRadius.setVisibility(View.GONE);
        tvGuide.setVisibility(View.GONE);
    }

    private void resetViewRound() {
        etRadius.setVisibility(View.VISIBLE);
        etRadius.setHint("围栏半径");
        tvGuide.setBackgroundColor(Color.parseColor("#ff0000"));
        tvGuide.setText("请点击地图选择围栏的中心点");
        tvGuide.setVisibility(View.VISIBLE);
    }

    private void resetViewPolygon() {
        tvGuide.setBackgroundColor(Color.parseColor("#ff0000"));
        tvGuide.setText("请点击地图选择围栏的边界点,至少3个点");
        tvGuide.setVisibility(View.VISIBLE);
        tvGuide.setVisibility(View.VISIBLE);
        polygonPoints = new ArrayList<LatLng>();
        btAddFence.setEnabled(false);
    }

    private void resetViewKeyword() {
        etKeyword.setVisibility(View.VISIBLE);
        etPoiType.setVisibility(View.VISIBLE);
        etCity.setVisibility(View.VISIBLE);
        etFenceSize.setVisibility(View.VISIBLE);
    }

    private void resetViewNearby() {
        tvGuide.setText("请点击地图选择中心点");
        etRadius.setHint("周边半径");
        tvGuide.setVisibility(View.VISIBLE);
        etKeyword.setVisibility(View.VISIBLE);
        etRadius.setVisibility(View.VISIBLE);
        etPoiType.setVisibility(View.VISIBLE);
        etFenceSize.setVisibility(View.VISIBLE);
    }

    private void resetViewDistrict() {
        etKeyword.setVisibility(View.VISIBLE);
    }

    private void setRadioGroupAble(boolean isEnable) {
        for (int i = 0; i < rgFenceType.getChildCount(); i++) {
            rgFenceType.getChildAt(i).setEnabled(isEnable);
        }
    }

    /**
     * 添加围栏
     */
    private void addFence() {
        switch (rgFenceType.getCheckedRadioButtonId()) {
            case R.id.rb_roundFence:
                addRoundFence();
                break;
            case R.id.rb_polygonFence:
                addPolygonFence();
                break;
            case R.id.rb_keywordFence:
                addKeywordFence();
                break;
            case R.id.rb_nearbyFence:
                addNearbyFence();
                break;
            case R.id.rb_districeFence:
                addDistrictFence();
                break;
            default:
                break;
        }
    }

    /**
     * 添加圆形围栏
     */
    private void addRoundFence() {
        String customId = etCustomId.getText().toString();
        String radiusStr = etRadius.getText().toString();
        if (null == centerLatLng || TextUtils.isEmpty(radiusStr)) {
            Toast.makeText(getApplicationContext(), "参数不全", Toast.LENGTH_SHORT)
                    .show();
            setRadioGroupAble(true);
            return;
        }
        DPoint centerPoint = new DPoint(centerLatLng.latitude,
                centerLatLng.longitude);
        fenceRadius = Float.parseFloat(radiusStr);
        setTriggerNum();
        fenceClient.addGeoFence(centerPoint, GeoFenceClient.BD09LL, fenceRadius, customId);
    }

    private int[] setTriggerNum () {
        int[] num = new int[3];
        for (int i = 0; i < 3; i++) {
            num[i] = Integer.MAX_VALUE;
        }
        String in = etInNum.getText().toString();
        if (!TextUtils.isEmpty(in)) {
            num[0] = Integer.parseInt(in);
        }
        String out = etOutNum.getText().toString();
        if (!TextUtils.isEmpty(out)) {
            num[1] = Integer.parseInt(out);
        }
        String stay = etStayNum.getText().toString();
        if (!TextUtils.isEmpty(stay)) {
            num[2] = Integer.parseInt(stay);
        }
        fenceClient.setTriggerCount(num[0], num[1], num[2]);
        etInNum.setText("");
        etOutNum.setText("");
        etStayNum.setText("");
        return num;
    }

    /**
     * 添加多边形围栏
     */
    private void addPolygonFence() {
        String customId = etCustomId.getText().toString();
        if (null == polygonPoints || polygonPoints.size() < 3) {
            Toast.makeText(getApplicationContext(), "参数不全", Toast.LENGTH_SHORT)
                    .show();
            setRadioGroupAble(true);
            btAddFence.setEnabled(true);
            return;
        }
        ArrayList<DPoint> pointList = new ArrayList<DPoint>();
        for (LatLng latLng : polygonPoints) {
            pointList.add(new DPoint(latLng.latitude, latLng.longitude));
        }
        setTriggerNum();
        fenceClient.addGeoFence(pointList, GeoFenceClient.BD09LL, customId);
    }

    /**
     * 添加关键字围栏
     */
    private void addKeywordFence() {
        String customId = etCustomId.getText().toString();
        String keyword = etKeyword.getText().toString();
        String city = etCity.getText().toString();
        String poiType = etPoiType.getText().toString();
        String sizeStr = etFenceSize.getText().toString();
        int size = 10;
        if (!TextUtils.isEmpty(sizeStr)) {
            try {
                size = Integer.parseInt(sizeStr);
            } catch (Throwable e) {
            }
        }

        if (TextUtils.isEmpty(keyword) && TextUtils.isEmpty(poiType)) {
            Toast.makeText(getApplicationContext(), "参数不全", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        setTriggerNum();
        fenceClient.addGeoFence(keyword, poiType, city, size, customId);
    }

    /**
     * 添加周边围栏
     */
    private void addNearbyFence() {
        String customId = etCustomId.getText().toString();
        String searchRadiusStr = etRadius.getText().toString();
        String keyword = etKeyword.getText().toString();
        String poiType = etPoiType.getText().toString();
        String sizeStr = etFenceSize.getText().toString();
        int size = 10;
        if (!TextUtils.isEmpty(sizeStr)) {
            try {
                size = Integer.parseInt(sizeStr);
            } catch (Throwable e) {
            }
        }

        if (null == centerLatLng) {
            Toast.makeText(getApplicationContext(), "参数不全", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        DPoint centerPoint = new DPoint(centerLatLng.latitude,
                centerLatLng.longitude);
        float aroundRadius = Float.parseFloat(searchRadiusStr);
        setTriggerNum();
        fenceClient.addGeoFence(keyword, poiType, centerPoint, GeoFenceClient.BD09LL, aroundRadius,
                size, customId);
    }

    /**
     * 添加行政区划围栏
     */
    private void addDistrictFence() {
        String keyword = etKeyword.getText().toString();
        String customId = etCustomId.getText().toString();
        if (TextUtils.isEmpty(keyword)) {
            Toast.makeText(getApplicationContext(), "参数不全", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        setTriggerNum();
        fenceClient.addGeoFence(keyword, customId);
    }


    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBdMap.setMyLocationData(locData);
        }
    }
}

