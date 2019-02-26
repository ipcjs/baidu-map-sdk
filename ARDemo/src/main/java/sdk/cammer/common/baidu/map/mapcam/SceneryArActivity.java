package sdk.cammer.common.baidu.map.mapcam;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;
import map.baidu.ar.ArPageListener;
import map.baidu.ar.camera.SimpleSensor;
import map.baidu.ar.camera.sceneryimpl.SceneryCamGLView;
import map.baidu.ar.model.ArInfoScenery;
import map.baidu.ar.model.ArPoiScenery;
import map.baidu.ar.utils.TypeUtils;

/**
 * Ar景区 Activity
 */
public class SceneryArActivity extends FragmentActivity implements ArPageListener {

    RelativeLayout camRl;
    SceneryCamGLView mCamGLView;
    SimpleSensor mSensor;
    private ArInfoScenery mInfo;
    private RelativeLayout mArPoiItemRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_scenery_ar);
        mInfo = MainActivity.arInfoScenery;
        mArPoiItemRl = (RelativeLayout) findViewById(R.id.ar_poi_item_rl);
        mArPoiItemRl.setVisibility(View.VISIBLE);
        initView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initView() {
        camRl = (RelativeLayout) findViewById(R.id.cam_rl);
        mCamGLView = (SceneryCamGLView) LayoutInflater.from(this).inflate(R.layout.layout_scenery_cam_view, null);
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

    public LayoutInflater getLayoutInflater() {

        return LayoutInflater.from(SceneryArActivity.this).cloneInContext(SceneryArActivity.this);
    }

    private void initSensor() {
        if (mSensor == null) {
            mSensor = new SimpleSensor(this, new HoldPositionListenerImp());
        }
        mSensor.startSensor();
    }

    /**
     * 实现SimpleSensor.OnHoldPositionListener传感器监听
     *
     */
    private class HoldPositionListenerImp implements SimpleSensor.OnHoldPositionListener {
        @Override
        public void onOrientationWithRemap(float[] remapValue) {
            if (mCamGLView != null && mInfo != null) {
                if (mInfo.getIsInAoi() && mInfo.getSon() != null && mInfo.getSon()
                        .size() > 0) {
                    // 在景区则传入子点集合
                    mCamGLView.setScenerySensorState(remapValue, getLayoutInflater(),
                            mArPoiItemRl, SceneryArActivity.this, mInfo.getSon(), SceneryArActivity.this);
                    mInfo.getFather().setShowInAr(false);
                } else {
                    // 不在景区则传入父点
                    ArrayList<ArPoiScenery> father = new ArrayList<>();
                    mInfo.getFather().setShowInAr(true);
                    father.add(mInfo.getFather());
                    for (int i = 0; i < mInfo.getSon().size(); i++) {
                        mInfo.getSon().get(i).setShowInAr(false);
                    }
                    mCamGLView.setScenerySensorState(remapValue, getLayoutInflater(),
                            mArPoiItemRl, SceneryArActivity.this, father, SceneryArActivity.this);
                }
            }
        }

    }


    @Override
    public void noPoiInScreen(boolean isNoPoiInScreen) {

    }

    @Override
    public void selectItem(Object iMapPoiItem) {
        if (iMapPoiItem instanceof ArPoiScenery) {
            Toast.makeText(this, "点击景区: " + ((ArPoiScenery) iMapPoiItem).getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finishCamInternal();
    }

    private void finishCamInternal() {
        if (mCamGLView != null) {
            mCamGLView.stopCam();
            camRl.removeAllViews();
            mCamGLView = null;

        }
        if (mArPoiItemRl != null) {
            mArPoiItemRl.removeAllViews();
        }
        if (mSensor != null) {
            mSensor.stopSensor();
        }
        // 恢复屏幕自动锁屏
        SceneryArActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


}
