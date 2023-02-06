package baidumapsdk.demo.geometry;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.baidu.mapapi.map.HeatMapAnimation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import baidumapsdk.demo.R;

public class HeatMap3DDemo extends AppCompatActivity {
    BaiduMap mBaiduMap;
    MapView mMapView;
    Button mAddHeatMapBtn;
    Button mRemoveBtn;
    Button mStartBtn;
    Button mStopBtn;
    HeatMap mHeatmap;
    MapStatusUpdate mMapStatusUpdate;
    private int mIndexCallBack;
    private int mProgress = 0;
    private SeekBar frameSeekBar;
    private ProgressBar progressBar;
    private Handler handler;
    private TextView textView;
    private TextView textViewBottom;
    private boolean isDestroy =  false;
    private int mDataSize = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3d_heatmap);
        mMapView = (MapView) findViewById(R.id.mapview);
        mBaiduMap = mMapView.getMap();
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.overlook(-25.0f);

        mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(builder.build());

        mBaiduMap.setMapStatus(mMapStatusUpdate);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(5));
        mAddHeatMapBtn = (Button) findViewById(R.id.add);
        mRemoveBtn = (Button) findViewById(R.id.remove);
        mStartBtn  = (Button)findViewById(R.id.start);
        mStopBtn = (Button)findViewById(R.id.stop);
        textView = (TextView) findViewById(R.id.tv_persent);
        textViewBottom = (TextView) findViewById(R.id.hm_persent);
        frameSeekBar = (SeekBar) findViewById(R.id.alphaBar);
        progressBar = (ProgressBar)findViewById(R.id.bnav_rg_left_progress);

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                int index = bundle.getInt("frameIndex");


                if (mProgress != (index * (progressBar.getMax())) / (mDataSize)) {
                    mProgress = (index * (progressBar.getMax())) / (mDataSize);
                    progressBar.setProgress(mProgress);
                    textView.setText("第 " + index + " 帧");
                }
            }
        };

        frameSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                seekBar.setProgress(progress);
                int index = ((int) (seekBar.getProgress()) * (mDataSize -1) / seekBar.getMax());
                final SeekBar mSeekbar = seekBar;
                if (!isDestroy) {
                    textViewBottom.setText("第 " + index +" 帧");
                    mBaiduMap.setHeatMapFrameAnimationIndex(index);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



    }

    public void addHeatMap(View view) {
        if (view.getId() == R.id.add) {
            if (mHeatmap != null) {
                mHeatmap.removeHeatMap();
            }
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (!isDestroy) {
                        switch (msg.what) {
                            case 0:
                                mBaiduMap.addHeatMap(mHeatmap);
                                break;
                        }

                    }
                }
            };
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    List<List<LatLng>> datas = getLocations();
                    List<LatLng> data = getLocation();
                    HeatMapAnimation init = new HeatMapAnimation(true, 100, HeatMapAnimation.AnimationType.Linear);
                    HeatMapAnimation frame = new HeatMapAnimation(true,2000, HeatMapAnimation.AnimationType.Linear);
                    mHeatmap = new HeatMap.Builder().datas(datas).initAnimation(init).frameAnimation(frame).maxIntensity(3.1f).opacity(0.8).build();
                    mDataSize = datas.size();
                    handler.sendEmptyMessage(0);
                }
            }.start();
        }
    }

    public void removeHeatMap(View view) {
        if (view.getId() == R.id.remove && !isDestroy && mHeatmap != null) {
            mHeatmap.removeHeatMap();
            mProgress = 0;
            progressBar.setProgress(mProgress);
            frameSeekBar.setProgress(mProgress);
            mBaiduMap.setHeatMapFrameAnimationIndex(0);
            textView.setText("第 0 帧");
            textViewBottom.setText("第 0 帧");
        }
    }

    public void startFrameAnimation(View view) {
        if (view.getId() == R.id.start && !isDestroy) {
            mBaiduMap.startHeatMapFrameAnimation();
            //回调动态热力图帧数，子进程通过handler更新UI信息
            mBaiduMap.setOnHeatMapDrawFrameCallBack(new BaiduMap.OnHeatMapDrawFrameCallBack() {
                @Override
                public void frameIndex(int indexCallBack) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("frameIndex", indexCallBack);
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            });
        }
    }

    public void stopFrameAnimation(View view) {
        if (view.getId() == R.id.stop && !isDestroy) {
            mBaiduMap.stopHeatMapFrameAnimation();
        }
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
        mBaiduMap.clear();
        // activity 销毁时同时销毁地图控件
        mMapView.onDestroy();
    }

    private List<List<LatLng>> getLocations() {
        List<List<LatLng>> datas= new ArrayList<>();
        InputStream inputStream = getResources().openRawResource(R.raw.locations);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array;
        int frameCount = 16;
        try {
            array = new JSONArray(json);
            for (int j = 0; j < frameCount; j++) {
                List<LatLng> list = new ArrayList<LatLng>();
                int oneFrameNum = (int)(array.length() / frameCount);
                for (int i = j * oneFrameNum; i < (j * oneFrameNum + oneFrameNum) && j < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    double lat = object.getDouble("lat");
                    double lng = object.getDouble("lng");
                    list.add(new LatLng(lat, lng));
                }
                datas.add(list);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return datas;
    }

    private List<LatLng> getLocation() {
        List<LatLng> list = new ArrayList<LatLng>();
        InputStream inputStream = getResources().openRawResource(R.raw.locations);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array;
        try {
            array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                double lat = object.getDouble("lat");
                double lng = object.getDouble("lng");
                list.add(new LatLng(lat, lng));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }
}
