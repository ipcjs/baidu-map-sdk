package sdk.cammer.common.baidu.map.mapcam;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import map.baidu.ar.ArPageListener;
import map.baidu.ar.camera.SimpleSensor;
import map.baidu.ar.camera.explore.BaseArCamGLView;
import map.baidu.ar.init.ArBuildingResponse;
import map.baidu.ar.model.ArInfo;
import map.baidu.ar.utils.TypeUtils;

/**
 * Ar识楼 Activity
 */
public class BuildingArActivity extends FragmentActivity implements ArPageListener {

    private RelativeLayout camRl;
    private BaseArCamGLView mCamGLView;
    public static ArBuildingResponse arBuildingResponse;
    private RelativeLayout mArPoiItemRl;
    private SimpleSensor mSensor;
    private TextView mMessageTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_explore_ar);
        arBuildingResponse = MainActivity.arBuildingResponse;
        mArPoiItemRl = (RelativeLayout) findViewById(R.id.ar_poi_item_rl);
        mArPoiItemRl.setVisibility(View.VISIBLE);
        initView();
    }

    private void initView() {
        mMessageTv = (TextView) findViewById(R.id.ar_page_message);
        camRl = (RelativeLayout) findViewById(R.id.cam_rl);
        mCamGLView = (BaseArCamGLView) LayoutInflater.from(this).inflate(R.layout.layout_explore_cam_view, null);
        mCamGLView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom == 0 || oldBottom != 0 || mCamGLView == null) {
                    return;
                }
                RelativeLayout.LayoutParams params = TypeUtils.safeCast(
                        mCamGLView.getLayoutParams(), RelativeLayout.LayoutParams.class);
                if (params == null) {
                    return;
                }
                params.height = bottom - top;
                mCamGLView.requestLayout();
            }
        });
        camRl.addView(mCamGLView);
        initSensor();
        // 保持屏幕不锁屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
        //        finishCamInternal();

    }

    private void initSensor() {
        if (mSensor == null) {
            mSensor = new SimpleSensor(this, new HoldPositionListenerImp());
        }
        mSensor.startSensor();
    }

    /**
     * 传感器位置监听类
     */
    private class HoldPositionListenerImp implements SimpleSensor.OnHoldPositionListener {
        @Override
        public void onOrientationWithRemap(float[] remapValue) {
            if (mCamGLView != null && mArPoiItemRl != null && arBuildingResponse != null) {
                if (arBuildingResponse.getBuildings() == null) {
                    mArPoiItemRl.setVisibility(View.GONE);
                    //                    mMessageTv.setText("附近没有可识别的楼宇");
                } else {
                    mCamGLView.setBaseArSensorState(remapValue, getLayoutInflater(), mMessageTv,
                            mArPoiItemRl, BuildingArActivity.this, arBuildingResponse.getBuildings(),
                            BuildingArActivity.this);
                    mArPoiItemRl.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void finishCamInternal() {
        if (mCamGLView != null) {
            mCamGLView.stopCam();
            camRl.removeAllViews();
            mCamGLView = null;

        }

        if (mMessageTv != null) {
            mMessageTv.setVisibility(View.GONE);
        }
        if (mArPoiItemRl != null) {
            mArPoiItemRl.removeAllViews();
        }
        if (mSensor != null) {
            mSensor.stopSensor();
        }
        // 恢复屏幕自动锁屏
        BuildingArActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public LayoutInflater getLayoutInflater() {

        return LayoutInflater.from(BuildingArActivity.this).cloneInContext(BuildingArActivity.this);
    }

    @Override
    public void selectItem(Object iMapPoiItem) {
        if (iMapPoiItem instanceof ArInfo) {
            Toast.makeText(this, "点击楼块: " + ((ArInfo) iMapPoiItem).getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void noPoiInScreen(boolean isNoPoiInScreen) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finishCamInternal();
    }
}
