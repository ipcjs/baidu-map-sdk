package com.baidu.location.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.baidulocationdemo.R;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.Calendar;
import java.util.Locale;

/**
 * 定位防作弊
 */
public class LocPreventCheatActivity extends Activity {

    private TextView tvResult;
    private Button btnStartLoc;
    private Button btnStopLoc;
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loc_prevent_cheat);

        tvResult = findViewById(R.id.tv_result);
        btnStartLoc = findViewById(R.id.btn_start_loc);
        btnStopLoc = findViewById(R.id.btn_stop_loc);

        try {
            mLocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLocationClient != null) {
            mLocationClient.registerLocationListener(new MyLocationListener());
        }

        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(1000);
        option.setOpenGps(true); // 允许开启gps定位
        option.setEnableSimulateGps(false);
        option.setCoorType("bd09ll");
        if (mLocationClient != null) {
            mLocationClient.setLocOption(option);
        }

        btnStartLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocationClient != null) {
                    mLocationClient.start();
                }
            }
        });

        btnStopLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocationClient != null) {
                    mLocationClient.stop();
                }
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {

            if (null == location) {
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvResult.setText(getResultString(location));
                }
            });
        }
    }

    private String getResultString(BDLocation location) {
        if (location != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("定位时间: ");
            sb.append(location.getTime());
            sb.append("\n回调时间: ");
            sb.append(getTimeStr());
            sb.append("\n定位结果类型 : ");
            sb.append(location.getLocType());
            sb.append("\n纬度 : ");
            sb.append(location.getLatitude());
            sb.append("\n经度 : ");
            sb.append(location.getLongitude());
            sb.append("\n精度 : ");
            sb.append(location.getRadius());

            sb.append("\n坐标系 : ");
            sb.append(location.getCoorType());


            sb.append("\n防作弊策略识别码 : ");
            sb.append(location.getMockGpsStrategy());
            sb.append("\n作弊概率 : ");
            sb.append(location.getMockGpsProbability());

            BDLocation realLoc = location.getReallLocation();
            if (location.getMockGpsStrategy() > 0 && null != realLoc) {

                sb.append("\n虚假位置和真实位置之间的距离 : ");
                sb.append(location.getDisToRealLocation());
                sb.append("\n真实定位结果类型 : ");
                sb.append(realLoc.getLocType());
                if (realLoc.getLocType() == BDLocation.TypeNetWorkLocation) {
                    sb.append("\n网络定位结果类型 : ");
                    sb.append(realLoc.getNetworkLocationType());
                    sb.append("\n真实定位精度 : ");
                    sb.append(realLoc.getRadius());
                }
                sb.append("\n真实纬度 : ");
                sb.append(realLoc.getLatitude());
                sb.append("\n真实经度 : ");
                sb.append(realLoc.getLongitude());

                sb.append("\n真实位置坐标系 : ");
                sb.append(realLoc.getCoorType());
            }

            return sb.toString();
        }
        return "";
    }


    private String getTimeStr() {
        int d, y, m, h, mi, s;
        Calendar cal = Calendar.getInstance();
        d = cal.get(Calendar.DATE);
        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH) + 1;
        h = cal.get(Calendar.HOUR_OF_DAY);
        mi = cal.get(Calendar.MINUTE);
        s = cal.get(Calendar.SECOND);
        return String.format(Locale.CHINA,"%d-%d-%d %d:%d:%d", y, m, d, h, mi, s);
    }
}
