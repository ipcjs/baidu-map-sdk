package com.baidu.location.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.baidulocationdemo.R;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;
import com.baidu.location.service.LocationService;

/**
 * 签到场景示例demo
 */
public class SignInSceneActivity extends Activity implements View.OnClickListener {

    private TextView mSceneDescribeTV;
    private TextView mResultTV;
    private Button mStartLoctionBtn;
    private LocationService mLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_location);
        mSceneDescribeTV = (TextView) findViewById(R.id.scene_desc);
        mStartLoctionBtn = (Button) findViewById(R.id.start_loc_btn);
        mResultTV = (TextView) findViewById(R.id.result_tv);
        mSceneDescribeTV.setText("签到场景 只进行一次定位返回最接近真实位置的定位结果（定位速度可能会延迟1-3s）");
        mStartLoctionBtn.setOnClickListener(this);

        // 初始化 LocationClient
        mLocationService = new LocationService(this);
        // 注册监听
        mLocationService.registerListener(mListener);
        LocationClientOption option = mLocationService.getOption();
        // 签到场景 只进行一次定位返回最接近真实位置的定位结果（定位速度可能会延迟1-3s）
        option.setLocationPurpose(LocationClientOption.BDLocationPurpose.SignIn);
        // 设置定位参数
        mLocationService.setLocationOption(option);
    }

    @Override
    public void onClick(View v) {
        mResultTV.setText("正在获取位置...");
        if (null != mLocationService) {
            if (mLocationService.isStart()){
                mLocationService.requestLocation();
                return;
            }
            //签到只需调用startLocation即可
            mLocationService.start();
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
                    location.getLocType() != BDLocation.TypeCriteriaException){
                mResultTV.setText("签到成功，签到经纬度：(" + location.getLatitude() + "," + location.getLongitude()+ ")");
            }else {
                mResultTV.setText("签到定位失败，错误码：" + location.getLocType() );
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationService != null) {
            mLocationService.unregisterListener(mListener);
            mLocationService.stop();
        }
    }
}
