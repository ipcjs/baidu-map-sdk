package com.baidu.location.demo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.baidulocationdemo.R;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 仿真定位
 */
public class MockLocationActivity extends Activity {

    private TextView tvResult;
    private Button btnSwitchMock;
    private Button btnSwitchLoc;
    private Button btnAddMockData;
    private Button btnRemoveMockData;
    private EditText etLat;
    private EditText etLng;
    private EditText etAlt;
    private LinearLayout llLatLng;

    private Timer timer;
    private String providerName;
    private LocationManager mLocationManager;
    private List<Location> mockData = new ArrayList<>();
    private LocationClient mLocationClient;
    private int index = 0;
    private boolean isStart = false; // 判断定位是否已经启动
    private boolean isMock = false; // 判断仿真定位是否已经启动

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_location);

        initView();

        providerName = LocationManager.GPS_PROVIDER;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            mLocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLocationClient != null) {
            mLocationClient.registerLocationListener(new MyLocationListener());
        }

        initBtnListener();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (null != mLocationClient) {
            mLocationClient.stop();
            mLocationClient = null;
        }

        try {
            if (null != mLocationManager) {
                mLocationManager.clearTestProviderEnabled(providerName);
                mLocationManager.removeTestProvider(providerName);
                mLocationManager = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != mockData) {
            mockData.clear();
            mockData = null;
        }

    }


    private void initView() {
        btnSwitchMock = findViewById(R.id.btn_switch_mock);
        btnSwitchLoc = findViewById(R.id.btn_switch_loc);
        btnAddMockData = findViewById(R.id.btn_add);
        btnRemoveMockData = findViewById(R.id.btn_remove);
        tvResult = findViewById(R.id.tv_result);
        etLat = findViewById(R.id.et_lat);
        etLng = findViewById(R.id.et_lng);
        etAlt = findViewById(R.id.et_alt);
        llLatLng = findViewById(R.id.ll_add_mock_data);
    }

    private void initBtnListener() {

        btnSwitchLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStart) {
                    btnSwitchLoc.setText("开始定位");
                    llLatLng.setVisibility(View.VISIBLE);
                    tvResult.setVisibility(View.GONE);
                    isStart = false;
                    if (mLocationClient != null) {
                        mLocationClient.stop();
                    }
                } else {
                    if (mockData != null && mockData.isEmpty()) {
                        showToast("请先添加模拟位置数据");
                        return;
                    }
                    btnSwitchLoc.setText("停止定位");
                    llLatLng.setVisibility(View.GONE);
                    tvResult.setVisibility(View.VISIBLE);
                    isStart = true;
                    setLocationOption();
                    if (mLocationClient != null) {
                        mLocationClient.start();
                    }
                }
            }
        });

        btnAddMockData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStart) {
                    showToast("请先停止定位,再添加模拟位置数据");
                    return;
                }
                if (!TextUtils.isEmpty(etLat.getText().toString())
                        && !TextUtils.isEmpty(etLng.getText().toString())
                        && !TextUtils.isEmpty(etAlt.getText().toString())) {
                    Location location = new Location("gps");
                    double lat = Double.parseDouble(etLat.getText().toString());
                    double lng = Double.parseDouble(etLng.getText().toString());
                    double alt = Double.parseDouble(etAlt.getText().toString());
                    location.setLatitude(lat);
                    location.setLongitude(lng);
                    location.setAltitude(alt);
                    if (mockData != null) {
                        mockData.add(location);
                        showToast("添加成功");
                        etLat.setText("");
                        etLng.setText("");
                        etAlt.setText("");
                    }
                } else {
                    showToast("请先输入模拟位置的经纬度和高度");
                }
            }
        });

        btnRemoveMockData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mockData && !mockData.isEmpty()) {
                    mockData.clear();
                    showToast("删除成功");
                } else {
                    showToast("您还未添加模拟位置数据");
                }
            }
        });

        btnSwitchMock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStart) {
                    if (null != mockData && !mockData.isEmpty()) {
                        showToast("请先启动定位，再开始仿真位置");
                    } else {
                        showToast("请先添加模拟位置数据，启动定位，再开始仿真位置");
                    }
                    return;
                }
                if (hasAddTestProvider()) {
                    if (isMock) {
                        isMock = false;
                        btnSwitchMock.setText("开始仿真");
                        stopUpdateMockLocation();
                    } else {
                        isMock = true;
                        btnSwitchMock.setText("停止仿真");
                        startUpdateMockLocation();
                    }
                } else {
                    startDevelopmentActivity();
                }
            }
        });
    }


    private String getResultString(BDLocation location) {
        if (location != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("实时位置结果: ");
            stringBuilder.append("\n定位结果类型: ");
            stringBuilder.append(location.getLocType());
            stringBuilder.append("\n纬度: ");
            stringBuilder.append(location.getLatitude());
            stringBuilder.append("\n经度: ");
            stringBuilder.append(location.getLongitude());
            stringBuilder.append("\n高度: ");
            stringBuilder.append(location.getAltitude());
            stringBuilder.append("\n速度: ");
            stringBuilder.append(location.getSpeed());
            stringBuilder.append("\n地址: ");
            stringBuilder.append(location.getAddrStr());
            stringBuilder.append("\n位置描述: ");
            stringBuilder.append(location.getLocationDescribe());
            stringBuilder.append("\n定位时间: ");
            stringBuilder.append(location.getTime());
            return stringBuilder.toString();
        }
        return "";
    }

    /**
     * 启动仿真定位
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void startUpdateMockLocation() {

        if (timer != null) {
            stopUpdateMockLocation();
        }
        timer = new Timer("UpdateMockLocation", false);
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                try {
                    if (mockData != null && index < mockData.size()) {
                        Location temp = mockData.get(index);
                        if (index < mockData.size() - 1) {
                            index++;
                        }
                        temp.setTime(System.currentTimeMillis());
                        temp.setAccuracy(10.0f);
                        temp.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                        pushLocation(temp);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 1000, 1000);

    }

    /**
     * 停止仿真定位
     */
    private void stopUpdateMockLocation() {
        if (timer == null) {
            return;
        }
        timer.cancel();
        timer.purge();
        timer = null;
        if (null != mLocationClient) {
            mLocationClient.stop();
            mLocationClient.start();
        }
    }

    private void pushLocation(Location mockloc) {
        mLocationManager.setTestProviderLocation(providerName, mockloc);
    }

    /**
     * 打开开发者模式界面,允许本app进行模拟位置
     */
    private void startDevelopmentActivity() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            try {
                ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings");
                Intent intent = new Intent();
                intent.setComponent(componentName);
                intent.setAction("android.intent.action.View");
                startActivity(intent);
            } catch (Exception e1) {
                try {
                    Intent intent = new Intent("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS");//部分小米手机采用这种方式跳转
                    startActivity(intent);
                } catch (Exception e2) {

                }
            }
        }
    }

    private void showToast(String showText) {
        Toast toast = Toast.makeText(getApplicationContext(), showText, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private boolean hasAddTestProvider() {
        boolean hasAddTestProvider = false;
        boolean canMockPosition = (Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0)
                || Build.VERSION.SDK_INT > 22;
        if (canMockPosition && hasAddTestProvider == false) {
            try {
                LocationProvider provider = mLocationManager.getProvider(providerName);
                if (provider != null) {
                    if (Build.VERSION.SDK_INT <= 28) {
                        mLocationManager.addTestProvider(
                                provider.getName()
                                , provider.requiresNetwork()
                                , provider.requiresSatellite()
                                , provider.requiresCell()
                                , provider.hasMonetaryCost()
                                , provider.supportsAltitude()
                                , provider.supportsSpeed()
                                , provider.supportsBearing()
                                , provider.getPowerRequirement()
                                , provider.getAccuracy());
                    }
                } else {
                    mLocationManager.addTestProvider(providerName, true, true, false, false, true, true, true
                            , Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
                }

                mLocationManager.setTestProviderEnabled(providerName, true);
                mLocationManager.setTestProviderStatus(providerName,
                        LocationProvider.AVAILABLE, null, System.currentTimeMillis());

                // 模拟位置可用
                hasAddTestProvider = true;
                canMockPosition = true;
            } catch (SecurityException e) {
                canMockPosition = false;
            }
        }

        return canMockPosition && hasAddTestProvider;

    }

    // 设置定位参数
    private void setLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setLocationNotify(true); // 可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        option.setScanSpan(2000);
        option.setOnceLocation(false);
        option.setNeedNewVersionRgc(true);
        option.setEnableSimulateGps(true);
        option.setIsNeedAddress(true);
        option.setIsNeedAltitude(true);
        option.setIsNeedLocationDescribe(true);
        if (null != mLocationClient) {
            mLocationClient.setLocOption(option);
        }
    }

    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvResult.setText(getResultString(bdLocation));
                }
            });
        }
    }
}
