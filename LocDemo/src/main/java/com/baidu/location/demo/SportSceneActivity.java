package com.baidu.location.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.baidulocationdemo.R;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;
import com.baidu.location.service.LocationService;

/**
 * 出行场景示例demo
 */
public class SportSceneActivity extends Activity implements View.OnClickListener {

    private TextView mSceneDescribeTV;
    private TextView mResultTV;
    private Button mStartLoctionBtn;
    private LocationService mLocationService;
    private boolean isStartLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_location);
        mSceneDescribeTV = (TextView) findViewById(R.id.scene_desc);
        mStartLoctionBtn = (Button) findViewById(R.id.start_loc_btn);
        mResultTV = (TextView) findViewById(R.id.result_tv);
        mStartLoctionBtn.setOnClickListener(this);
        mSceneDescribeTV.setText("出行场景 高精度连续定位，适用于用户内外切换的场景，卫星定位和网络定位相互切换，" +
                "卫星定位成功之后网络定位不再返回，卫星信号断开之后一段时间才会返回网络结果");
        mStartLoctionBtn.setText("开始定位");
        // 初始化 LocationClient
        mLocationService = new LocationService(this);
        // 注册监听
        mLocationService.registerListener(mListener);
        LocationClientOption option = mLocationService.getOption();
        /* 出行场景 高精度连续定位，适用于有户内外切换的场景，卫星定位和网络定位相互切换，卫星定位成功之后网络定位不再返回，
        卫星信号断开之后一段时间才会返回网络结果*/
        option.setLocationPurpose(LocationClientOption.BDLocationPurpose.Sport);
        // 设置定位参数
        mLocationService.setLocationOption(option);
    }

    @Override
    public void onClick(View v) {
        if (isStartLocation) {
            mResultTV.setText("正在获取位置...");
            if (null != mLocationService) {
                // 启动定位
                mLocationService.start();
            }
            mStartLoctionBtn.setText("停止定位");
            isStartLocation = false;
        } else {
            if (null != mLocationService) {
                // 关闭定位
                mLocationService.stop();
            }
            mResultTV.setText("停止定位");
            mStartLoctionBtn.setText("开始定位");
            isStartLocation = true;
        }
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        /**
         * 定位请求回调函数
         *
         * @param location 定位结果
         */
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError &&
                    location.getLocType() != BDLocation.TypeOffLineLocationFail &&
                    location.getLocType() != BDLocation.TypeCriteriaException) {
                StringBuffer sb = new StringBuffer(256);
                sb.append("定位成功");
                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\nNetworkLocationType:");
                sb.append(getNetworkLocationType(location.getNetworkLocationType()));
                sb.append("\nlocType : ");// 定位类型
                sb.append(location.getLocType());
                sb.append("\nlocType description : ");// *****对应的定位类型说明*****
                sb.append(location.getLocTypeDescription());
                sb.append("\nlatitude : ");// 纬度
                sb.append(location.getLatitude());
                sb.append("\nlongtitude : ");// 经度
                sb.append(location.getLongitude());
                sb.append("\nradius : ");// 半径
                sb.append(location.getRadius());
                sb.append("\nCountryCode : ");// 国家码
                sb.append(location.getCountryCode());
                sb.append("\nProvince : ");// 获取省份
                sb.append(location.getProvince());
                sb.append("\nCountry : ");// 国家名称
                sb.append(location.getCountry());
                sb.append("\ncitycode : ");// 城市编码
                sb.append(location.getCityCode());
                sb.append("\ncity : ");// 城市
                sb.append(location.getCity());
                sb.append("\nDistrict : ");// 区
                sb.append(location.getDistrict());
                sb.append("\nTown : ");// 获取镇信息
                sb.append(location.getTown());
                sb.append("\nStreet : ");// 街道
                sb.append(location.getStreet());
                sb.append("\naddr : ");// 地址信息
                sb.append(location.getAddrStr());
                mResultTV.setText(sb.toString());
            }
        }

        /**
         * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
         * @param locType 当前定位类型
         * @param diagnosticType 诊断类型（1~9）
         * @param diagnosticMessage 具体的诊断信息释义
         */
        @Override
        public void onLocDiagnosticMessage(int locType, int diagnosticType,
                                           String diagnosticMessage) {
            super.onLocDiagnosticMessage(locType, diagnosticType, diagnosticMessage);
            StringBuffer sb = new StringBuffer(256);
            sb.append("locType:" + locType);
            sb.append("\n" + "诊断结果: ");
            if (locType == BDLocation.TypeNetWorkLocation) {
                if (diagnosticType == 1) {
                    sb.append("网络定位成功，没有开启GPS，建议打开GPS会更好" + "\n");
                    sb.append(diagnosticMessage);
                } else if (diagnosticType == 2) {
                    sb.append("网络定位成功，没有开启Wi-Fi，建议打开Wi-Fi会更好" + "\n");
                    sb.append(diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeOffLineLocationFail) {
                if (diagnosticType == 3) {
                    sb.append("定位失败，请您检查您的网络状态" + "\n");
                    sb.append(diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeCriteriaException) {
                if (diagnosticType == 4) {
                    sb.append("定位失败，无法获取任何有效定位依据" + "\n");
                    sb.append(diagnosticMessage);
                } else if (diagnosticType == 5) {
                    sb.append("定位失败，无法获取有效定位依据，请检查运营商网络或者Wi-Fi网络是否正常开启，尝试重新请求定位" + "\n");
                    sb.append(diagnosticMessage);
                } else if (diagnosticType == 6) {
                    sb.append("定位失败，无法获取有效定位依据，请尝试插入一张sim卡或打开Wi-Fi重试" + "\n");
                    sb.append(diagnosticMessage);
                } else if (diagnosticType == 7) {
                    sb.append("定位失败，飞行模式下无法获取有效定位依据，请关闭飞行模式重试" + "\n");
                    sb.append(diagnosticMessage);
                } else if (diagnosticType == 9) {
                    sb.append("定位失败，无法获取任何有效定位依据" + "\n");
                    sb.append(diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeServerError) {
                if (diagnosticType == 8) {
                    sb.append("定位失败，请确认您定位的开关打开状态，是否赋予APP定位权限" + "\n");
                    sb.append(diagnosticMessage);
                }
            }
            mResultTV.setText(sb.toString());
        }
    };

    /**
     * 在网络定位结果的情况下，获取网络定位结果是通过基站定位得到的还是通过wifi定位得到的还是GPS得结果
     *
     * @param networkLocationType location.getNetworkLocationType()
     * @return 定位结果类型
     */
    private String getNetworkLocationType(String networkLocationType){
        String str = "";
        switch (networkLocationType){
            case "wf":
                str = "wifi定位结果";
                break;
            case "cl":
                str = "基站定位结果";
                break;
            case "ll":
                str = "GPS定位结果";
                break;
            case "":
                str = "没有获取到定位结果采用的类型";
                break;
        }
        return str;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationService != null) {
            mLocationService.unregisterListener(mListener);
            mLocationService.stop();
        }
    }
}
