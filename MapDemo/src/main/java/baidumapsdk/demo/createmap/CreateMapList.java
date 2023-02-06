package baidumapsdk.demo.createmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import baidumapsdk.demo.R;
import baidumapsdk.demo.util.DemoInfo;
import baidumapsdk.demo.util.DemoListAdapter;

public class CreateMapList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menulist);
        ListView demoList = (ListView) findViewById(R.id.mapList);
        // 添加ListItem，设置事件响应
        demoList.setAdapter(new DemoListAdapter(CreateMapList.this,DEMOS));
        demoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
                onListItemClick(index);
            }
        });
    }

    void onListItemClick(int index) {
        Intent intent;
        intent = new Intent(CreateMapList.this, DEMOS[index].demoClass);
        this.startActivity(intent);
    }

    private static final DemoInfo[] DEMOS = {
            new DemoInfo(R.string.demo_title_map_type, R.string.demo_desc_map_type, MapTypeDemo.class),
            new DemoInfo(R.string.demo_title_custommap, R.string.demo_desc_custommap, CustomMapDemo.class),
            new DemoInfo(R.string.demo_title_map_fragment, R.string.demo_desc_map_fragment, MapFragmentDemo.class),
            new DemoInfo(R.string.demo_title_texturemapview, R.string.demo_desc_texturemapview,TextureMapViewDemo.class),
            new DemoInfo(R.string.demo_title_indoor, R.string.demo_desc_indoor, IndoorMapDemo.class),
            new DemoInfo(R.string.demo_title_multimapview, R.string.demo_desc_multimapview,MultiMapViewDemo.class),
            new DemoInfo(R.string.demo_title_offline, R.string.demo_desc_offline,OfflineDemo.class),
            new DemoInfo(R.string.demo_title_map_type_frag, R.string.demo_desc_map_type_frag,MapTypeFragmentDemo.class)
    };
}

