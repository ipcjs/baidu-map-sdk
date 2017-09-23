package com.baidu.lbsapi.panodemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 *
 */
public class PanoDemoActivity extends Activity {

    public static final int PID = 0;// PID方式
    public static final int GEO = 1;// 经纬度方式
    public static final int MERCATOR = 2;// 墨卡托方式
    public static final int UID_STREET = 3;// UID方式展示外景
    public static final int UID_INTERIOR = 4;// UID方式展示内景
    public static final int UID_STREET_CUSTOMALBUM = 5;// UID方式展示外景(使用自定义相册)
    public static final int MARKER = 6;// 标注
    public static final int OTHER = 7;// 其他测试
    public static final int COORDINATE_CONVERTER = 8;// 坐标转换测试
    public static final int WGS84 = 9;// 大地坐标系方式
    public static final int GCJ02 = 10;// 国测局加密方式

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.panodemo);

        ListView mListView = (ListView) findViewById(R.id.panodemo_list);
        // 添加ListItem，设置事件响应
        mListView.setAdapter(new DemoListAdapter());
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
                onListItemClick(index);
            }
        });
    }

    private static final DemoInfo[] demos = {
            new DemoInfo(PID, R.string.demo_title_panorama, R.string.demo_desc_pid, PanoDemoMain.class),
            new DemoInfo(WGS84, R.string.demo_title_panorama, R.string.demo_desc_wgs84, PanoDemoMain.class),
            new DemoInfo(GCJ02, R.string.demo_title_panorama, R.string.demo_desc_gcj02, PanoDemoMain.class),
            new DemoInfo(GEO, R.string.demo_title_panorama, R.string.demo_desc_geo, PanoDemoMain.class),
            new DemoInfo(MERCATOR, R.string.demo_title_panorama, R.string.demo_desc_mercator, PanoDemoMain.class),
            new DemoInfo(UID_STREET, R.string.demo_title_panorama, R.string.demo_desc_uid_street, PanoDemoMain.class),
            new DemoInfo(UID_INTERIOR, R.string.demo_title_panorama, R.string.demo_desc_uid_interior, PanoDemoMain.class),
            new DemoInfo(UID_STREET_CUSTOMALBUM, R.string.demo_title_panorama, R.string.demo_desc_uid_street_customalbum, PanoDemoMain.class),
            new DemoInfo(MARKER, R.string.demo_title_panorama, R.string.demo_desc_marker, PanoDemoMain.class),
            new DemoInfo(OTHER, R.string.demo_title_panorama, R.string.demo_desc_other, PanoDemoMain.class),
            new DemoInfo(COORDINATE_CONVERTER, R.string.demo_title_coordinate, R.string.demo_desc_coordinate_converter,
                    PanoDemoCoordinate.class)};

    private void onListItemClick(int index) {
        Intent intent = new Intent(PanoDemoActivity.this, demos[index].demoClass);
        intent.putExtra("type", demos[index].type);
        this.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 建议在APP整体退出之前调用MapApi的destroy()函数，不要在每个activity的OnDestroy中调用，
        // 避免MapApi重复创建初始化，提高效率
        PanoDemoApplication app = (PanoDemoApplication) this.getApplication();
        if (app.mBMapManager != null) {
            // app.mBMapManager.destroy();
            app.mBMapManager = null;
        }
        super.onDestroy();
        System.exit(0);
    }

    private class DemoListAdapter extends BaseAdapter {
        public DemoListAdapter() {
            super();
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            convertView = View.inflate(PanoDemoActivity.this, R.layout.panodemo_list_item, null);
            TextView title = (TextView) convertView.findViewById(R.id.item_title);
            TextView desc = (TextView) convertView.findViewById(R.id.item_desc);

            title.setText(demos[index].title);
            desc.setText(demos[index].desc);
            return convertView;
        }

        @Override
        public int getCount() {
            return demos.length;
        }

        @Override
        public Object getItem(int index) {
            return demos[index];
        }

        @Override
        public long getItemId(int id) {
            return id;
        }
    }

    private static class DemoInfo {
        public int type;
        public int title;
        public int desc;
        public Class<? extends Activity> demoClass;

        public DemoInfo(int type, int title, int desc, Class<? extends Activity> demoClass) {
            this.type = type;
            this.title = title;
            this.desc = desc;
            this.demoClass = demoClass;
        }
    }
}