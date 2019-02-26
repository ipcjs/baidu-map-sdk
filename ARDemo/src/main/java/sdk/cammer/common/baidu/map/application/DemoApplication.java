package sdk.cammer.common.baidu.map.application;

import android.app.Application;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import map.baidu.ar.init.ArSdkManager;
import map.baidu.ar.init.MKGeneralListener;
import map.baidu.ar.utils.ArBDLocation;
import sdk.cammer.common.baidu.map.utils.LocSdkClient;

/**
 * Ar sdk application
 * 此demo BaiduLBS_Android.jar包集成了"百度地图基础定位sdk、百度地图检索sdk、LBS Ar sdk"
 */
public class DemoApplication extends Application {

    private static DemoApplication mInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        // ArSDK模块初始化
        ArSdkManager.getInstance().initApplication(this, new MyGeneralListener());

        // 若用百度定位sdk,需要在此初始化定位SDK
        LocSdkClient.getInstance(this).getLocationStart();

        // 若用探索功能需要再这集成检索模块 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        // 检索模块 自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        // 包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    public static DemoApplication getInstance() {
        return mInstance;
    }

    static class MyGeneralListener implements MKGeneralListener {
        // 1、事件监听，用来处理通常的网络错误，授权验证错误等
        @Override
        public void onGetPermissionState(int iError) {
            // 2、非零值表示key验证未通过
            if (iError != 0) {
                // 授权Key错误：
                Toast.makeText(DemoApplication.getInstance().getApplicationContext(),
                        "arsdk 验证异常，请在AndoridManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: " + iError, Toast
                                .LENGTH_LONG).show();
            } else {
                Toast.makeText(DemoApplication.getInstance().getApplicationContext(), "key认证成功", Toast.LENGTH_LONG)
                        .show();
            }
        }

        // 回调给ArSDK获取坐标（demo调用百度定位sdk）
        @Override
        public ArBDLocation onGetBDLocation() {
            // 3、用于传递给ArSdk经纬度信息
            // a、首先通过百度地图定位SDK获取经纬度信息
            // b、包装经纬度信息到ArSdk的ArBDLocation类中 return即可
            BDLocation location =
                    LocSdkClient.getInstance(ArSdkManager.getInstance().getAppContext()).getLocationStart()
                            .getLastKnownLocation();
            if (location == null) {
                return null;
            }
            ArBDLocation arBDLocation = new ArBDLocation();
            // 设置经纬度信息
            arBDLocation.setLongitude(location.getLongitude());
            arBDLocation.setLatitude(location.getLatitude());
            return arBDLocation;
        }
    }

}