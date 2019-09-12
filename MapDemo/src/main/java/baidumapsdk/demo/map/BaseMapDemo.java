package baidumapsdk.demo.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.MapStatusUpdateFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 演示MapView的基本用法
 * 同时示例个性化地图的基本使用
 * 并且作为个性化编辑器样式预览的个性化地图展示
 */
public class BaseMapDemo extends Activity {

    @SuppressWarnings("unused")
    private static final String LTAG = BaseMapDemo.class.getSimpleName();

    private MapView mMapView;

    FrameLayout layout;

    private static final int OPEN_ID = 0;
    private static final int CLOSE_ID = 1;

    //用于设置个性化地图的样式文件
    private static final String CUSTOM_FILE_NAME = "custom_map_config.json";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 默认值为0，即使用本地加载方式
        int loadCustomStyleFileMode = 0;

        Intent intent = getIntent();
        if (intent.hasExtra("loadCustomStyleFileMode")) {
            // 当用intent参数时，设置中心点为指定点
            Bundle bundle = intent.getExtras();
            loadCustomStyleFileMode = bundle.getInt("loadCustomStyleFileMode");
        }

        /**
         * 本地加载个性化地图样式
         * MapView (TextureMapView)的
         * {@link MapView.setCustomMapStylePath(String customMapStylePath)}
         * 方法一定要在MapView(TextureMapView)创建之前调用。
         * 如果是setContentView方法通过布局加载MapView(TextureMapView), 那么一定要放置在
         * MapView.setCustomMapStylePath方法之后执行，否则个性化地图不会显示
         */
        // loadCustomStyleFileMode == 0 说明是本地样式文件加载；loadCustomStyleFileMode == 1编辑器样式预览
        if (0 == loadCustomStyleFileMode) {
            setMapCustomFile(this, CUSTOM_FILE_NAME);
        }

        /**
         * 个性化地图样式文件加载方式，当前只提供两种方式：
         * 0 —— 本地加载；
         * 1 —— 编辑器样式预览
         * 默认值为0，即使用本地加载方式时，该方法可以不调用。如果使用编辑器样式预览，需要调用该方法
         *
         * 该方法一定要在MapView(TextureMapView)创建之前调用。
         * 如果是setContentView方法通过布局加载MapView(TextureMapView), 那么一定要放置在
         * MapView.setCustomMapStylePath方法之后执行，否则个性化地图不会显示。
         */
        MapView.setLoadCustomMapStyleFileMode(loadCustomStyleFileMode);

        mMapView = new MapView(this, new BaiduMapOptions());
        initView(this);
        setContentView(layout);

        MapStatus.Builder builder = new MapStatus.Builder();
        LatLng center = new LatLng(39.998152, 116.276973); // 颐和园
        float zoom = 14.5f; // 地图缩放级别
        builder.target(center).zoom(zoom);
        mMapView.getMap().setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        MapView.setMapCustomEnable(true);
    }

    // 初始化View
    private void initView(Context context) {
        layout = new FrameLayout(this);
        layout.addView(mMapView);

        RadioGroup group = new RadioGroup(context);
        group.setBackgroundColor(Color.GRAY);
        // 个性化开关水平排列
        group.setOrientation(LinearLayout.HORIZONTAL);
        group.setGravity(Gravity.CENTER_HORIZONTAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);

        final RadioButton openBtn = new RadioButton(context);
        openBtn.setText("开启个性化地图");
        openBtn.setId(OPEN_ID);
        openBtn.setTextColor(Color.WHITE);
        group.addView(openBtn, params);

        final RadioButton closeBtn = new RadioButton(context);
        closeBtn.setText("关闭个性化地图");
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setId(CLOSE_ID);
        group.addView(closeBtn, params);
        // 默认打开个性化地图
        openBtn.setChecked(true);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == OPEN_ID) {
                    MapView.setMapCustomEnable(true);
                } else if (checkedId == CLOSE_ID) {
                    MapView.setMapCustomEnable(false);
                }
            }
        });

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.addView(group, layoutParams);
    }

    // 设置个性化地图config文件路径
    private void setMapCustomFile(Context context, String PATH) {
        FileOutputStream out = null;
        InputStream inputStream = null;
        String moduleName = null;

        try {
            inputStream = context.getAssets().open("customConfigdir/" + PATH);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);

            moduleName = context.getFilesDir().getAbsolutePath();
            File f = new File(moduleName + "/" + PATH);
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();

            out = new FileOutputStream(f);
            out.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        MapView.setCustomMapStylePath(moduleName + "/" + PATH);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // activity 暂停时同时暂停地图控件
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activity 恢复时同时恢复地图控件
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 销毁地图前线关闭个性化地图，否则会出现资源无法释放
        MapView.setMapCustomEnable(false);
        // activity 销毁时同时销毁地图控件
        mMapView.onDestroy();
    }

}
